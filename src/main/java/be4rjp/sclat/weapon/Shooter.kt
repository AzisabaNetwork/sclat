package be4rjp.sclat.weapon

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object Shooter {
    @JvmStatic
    fun ShooterRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var sl: Int = 0
                var check: Boolean = true
                var maxRandomCount: Int = 0

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch() || !p.isOnline() || data.stoprun) {
                        cancel()
                        return
                    }

                    if (!data.getIsUsingManeuver() && data.getCanShoot()) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch()
                        ) {
                            Shoot(
                                p,
                                false,
                                false,
                                maxRandomCount >= data.getWeaponClass().mainWeapon!!.maxRandomCount,
                            )
                            data.tick = (
                                data.tick +
                                    getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootTick
                                )
                            if (data.getWeaponClass().mainWeapon!!.maxRandom != 0.0 &&
                                maxRandomCount <= data.getWeaponClass().mainWeapon!!.maxRandomCount * 2
                            ) {
                                maxRandomCount++
                            }
                        } else {
                            if (data.getWeaponClass().mainWeapon!!.maxRandom != 0.0 && maxRandomCount >= 0) maxRandomCount -= 2
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .getWeaponClass()
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    @JvmStatic
    fun ManeuverShootRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var sl: Int = 0
                var check: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }

                    if (data.getIsUsingManeuver()) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch()
                        ) {
                            Shoot(p, true, false, false)
                            data.tick = (
                                data.tick +
                                    getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootTick
                                )
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .getWeaponClass()
                .mainWeapon!!
                .slidingShootTick
                .toLong(),
        )
    }

    @JvmStatic
    fun ManeuverRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.getLocation()
                var before: Location = player.getLocation()
                var before_2: Location = player.getLocation()

                // int sl = 0;
                // スライドの仕様改変
                var sl_recharge_1: Boolean = true
                var sl_recharge_2: Boolean = true

                // スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
                var check: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    val ploc = p.getLocation()

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }

                    val location = p.getLocation()

                    var x = location.getX() - before.getX()
                    var z = location.getZ() - before.getZ()
                    var vec = p.getEyeLocation().getDirection()
                    if (x != 0.0 || z != 0.0) {
                        vec = Vector(x, 0.0, z)
                    } else {
                        x = location.getX() - before_2.getX()
                        z = location.getZ() - before_2.getZ()
                        if (x != 0.0 || z != 0.0) {
                            vec = Vector(x, 0.0, z)
                        }
                    }
                    before_2 = before.clone()
                    before = location.clone()

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                    // マニューバー系
                    if (data.getWeaponClass().mainWeapon!!.isManeuver) {
                        // if(p.getExp() >= ink) {
                        if (data.getIsSneaking() && sl_recharge_2 && !data.getIsSliding() && (
                                p
                                    .getInventory()
                                    .getItemInMainHand()
                                    .getType()
                                    ==
                                    data
                                        .getWeaponClass()
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .getType()
                                )
                        ) { // slをsl_recharge_2に変更することで優先順位が低い方のスライドが残っている時のみ使えるようにしました
                            val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize().multiply(3)
                            val ev = jvec.clone().normalize().multiply(-2)
                            check = true

                            // p.setExp(p.getExp() - ink);

                            // エフェクト
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            val random = 1.0
                            for (i in 0..34) {
                                val randomVector =
                                    Vector(
                                        Math.random() * random - random / 2,
                                        Math.random() * random - random / 2,
                                        Math.random() * random - random / 2,
                                    )
                                val erv = ev.clone().add(randomVector)
                                for (o_player in plugin.getServer().getOnlinePlayers()) {
                                    if (getPlayerData(o_player)!!.settings.ShowEffect_BombEx()) {
                                        if (o_player.getWorld() === location.getWorld()) {
                                            if (o_player
                                                    .getLocation()
                                                    .distanceSquared(location) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                o_player.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    location.clone().add(0.0, 0.7, 0.0).add(
                                                        randomVector.getX(),
                                                        randomVector.getY(),
                                                        randomVector.getZ(),
                                                    ),
                                                    0,
                                                    erv.getX(),
                                                    erv.getY(),
                                                    erv.getZ(),
                                                    1.0,
                                                    bd,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (getPlayerData(player)!!.armor > 9999) {
                                getPlayerData(player)!!.armor = 0.0
                            }
                            p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                            p.setVelocity(jvec.clone().setY(if (p.isOnGround()) 0.0 else -0.4))
                            data.setIsSneaking(false)
                            data.setIsSliding(true)
                            data.setCanShoot(false)
                            // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                            if (!sl_recharge_1) {
                                sl_recharge_2 = false
                            } else {
                                sl_recharge_1 = false
                            }
                            // sl++;
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.setVelocity(Vector(0, 0, 0))
                                            data.setIsUsingManeuver(true)
                                            data.setCanShoot(true)
                                        }

                                        if (i == 10) {
                                            data.setIsUsingManeuver(false)
                                            loc = p.getLocation()
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            task.runTaskTimer(plugin, 0, 1)

                            val task1: BukkitRunnable =
                                object : BukkitRunnable() {
                                    override fun run() {
                                        data.setIsSliding(false)
                                    }
                                }
                            task1.runTaskLater(plugin, 10)
                            // BukkitRunnable task2 = new BukkitRunnable() {
                            // @Override
                            // public void run() {
                            // sl = 0;
                            // check = true;
                            // }
                            // };
                            // BukkitRunnable task2 = new BukkitRunnable() {//二つのtaskの追加でそれぞれのスライドを管理しています
                            // @Override
                            // public void run() {
                            // sl_recharge_1 = true;
                            // //check = true;
                            // }
                            // };
                            // BukkitRunnable task3 = new BukkitRunnable() {
                            // @Override
                            // public void run() {
                            // sl_recharge_2 = true;
                            // //check = true;
                            // }
                            // };
                            // スライド仕様変更の改変
                            // if( sl_recharge_2 == true){task2.runTaskLater(Main.getPlugin(), 64);}
                            // else{task3.runTaskLater(Main.getPlugin(), 64);}
                            // booleam型の変数で二つのスライドをそれぞれ表現している、優先順位が低い方がTrueのときは高い方が使われた後のため高い方のリチャージをする（優先順位が高い方は2秒、低い方は2.2秒）
                            // check = false;
                        }
                        // }else{
                        // p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                        // player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                        // }
                    }

                    if (!data.getIsSliding()) {
                        if (loc.getX() == ploc.getX() && loc.getZ() == ploc.getZ()) {
                            data.setIsUsingManeuver(true)
                        } else {
                            if (check) {
                                val task4: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            sl_recharge_1 = true
                                            // check = true;
                                        }
                                    }

                                val task5: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            sl_recharge_1 = true
                                            sl_recharge_2 = true
                                            // check = true;
                                        }
                                    }
                                if (sl_recharge_2) {
                                    task4.runTaskLater(plugin, 64)
                                } else {
                                    task5.runTaskLater(plugin, 64)
                                }
                                check = false
                            }
                            data.setIsUsingManeuver(false)
                        }
                    }

                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun Shoot(
        player: Player,
        slided: Boolean,
        sound: Boolean,
        maxRandom: Boolean,
    ) {
        var maxRandom = maxRandom
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.getExp() <=
            (
                data!!.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.setExp(
            player.getExp() -
                (
                    data.getWeaponClass().mainWeapon!!.needInk
                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                    ).toFloat(),
        )
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions =
            rayTrace.traverse(
                data.getWeaponClass().mainWeapon!!.shootSpeed
                    * data.getWeaponClass().mainWeapon!!.distanceTick,
                0.7,
            )
        var isLockOnPlayer = false
        if (data.getWeaponClass().mainWeapon!!.maxRandom == 0.0) {
            check@ for (vector in positions) {
                val position = vector.toLocation(player.getLocation().getWorld()!!)
                for (target in plugin.getServer().getOnlinePlayers()) {
                    if (player !== target && player.getWorld() === target.getWorld()) {
                        if (target.getLocation().distance(position) < 2) {
                            isLockOnPlayer = true
                            break@check
                        }
                    }
                }

                for (`as` in player.getWorld().getEntities()) {
                    if (`as` is ArmorStand) {
                        if (`as`.getCustomName() != null) {
                            if (`as`.getLocation().distanceSquared(position) <= 4 /* 2*2 */) {
                                isLockOnPlayer = true
                                break@check
                            }
                        }
                    }
                }
            }
        } else {
            if (!player.isOnGround()) maxRandom = true
        }

        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true)

        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .getLocation()
                .getDirection()
                .multiply(getPlayerData(player)!!.getWeaponClass().mainWeapon!!.shootSpeed)
        var random = data.getWeaponClass().mainWeapon!!.random
        if (maxRandom) random = data.getWeaponClass().mainWeapon!!.maxRandom
        if (isLockOnPlayer) random /= 2.0
        if (slided) random /= 10.0
        val distick = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.distanceTick
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.setVelocity(vec)
        ball.setShooter(player)
        // スライド時かどうかをSnowballListenerに渡すためのnameの改変
        val originName = notDuplicateNumber.toString()
        val buf = StringBuilder()
        buf.append(originName)
        if (slided) {
            buf.append("#slided")
        }
        val name = buf.toString()
        // String name = String.valueOf(Main.getNotDuplicateNumber());//ここで改変終わり
        DataMgr.mws.add(name)
        if (sound || slided) DataMgr.tsl.add(name)
        ball.setCustomName(name)
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick

                // Vector fallvec;
                var origvec: Vector = vec
                var inkball: Snowball? = ball
                var addedFallVec: Boolean = false
                var p: Player = player
                var fallvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team.teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_MainWeaponInk()) {
                                if (o_player.getWorld() ===
                                    inkball!!.getWorld()
                                ) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball!!.getLocation(),
                                            0,
                                            0.0,
                                            -1.0,
                                            0.0,
                                            1.0,
                                            bd,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (i >= tick && !addedFallVec) {
                        inkball!!.setVelocity(fallvec)
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.setVelocity(
                            inkball!!.getVelocity().add(Vector(0.0, -0.1, 0.0)),
                        )
                    }
                    // if(i != tick)
                    if ((Random().nextInt(7)) == 0) PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, false, true)
                    if (inkball!!.isDead()) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
