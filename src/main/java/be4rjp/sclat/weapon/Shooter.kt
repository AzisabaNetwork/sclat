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
    fun shooterRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var sl: Int = 0
                var check: Boolean = true
                var maxRandomCount: Int = 0

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch || !p.isOnline || data.stoprun) {
                        cancel()
                        return
                    }

                    if (!data.isUsingManeuver && data.canShoot) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch
                        ) {
                            shoot(
                                p,
                                false,
                                false,
                                maxRandomCount >= data.weaponClass?.mainWeapon!!.maxRandomCount,
                            )
                            data.tick = (
                                data.tick +
                                    getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootTick
                                )
                            if (data.weaponClass?.mainWeapon!!.maxRandom != 0.0 &&
                                maxRandomCount <= data.weaponClass?.mainWeapon!!.maxRandomCount * 2
                            ) {
                                maxRandomCount++
                            }
                        } else {
                            if (data.weaponClass?.mainWeapon!!.maxRandom != 0.0 && maxRandomCount >= 0) maxRandomCount -= 2
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    @JvmStatic
    fun maneuverShootRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var sl: Int = 0
                var check: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    if (data.isUsingManeuver) {
                        val clickType = Sclat.dadadaCheckerAPI!!.getPlayerClickType(player)
                        if ((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) &&
                            data.isInMatch
                        ) {
                            shoot(p, true, false, false)
                            data.tick = (
                                data.tick +
                                    getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootTick
                                )
                        }
                    }
                }
            }
        delay.runTaskTimer(
            plugin,
            0,
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .slidingShootTick
                .toLong(),
        )
    }

    @JvmStatic
    fun maneuverRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.location
                var before: Location = player.location
                var before2: Location = player.location

                // int sl = 0;
                // スライドの仕様改変
                var slRecharge1: Boolean = true
                var slRecharge2: Boolean = true

                // スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
                var check: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    val ploc = p.location

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    val location = p.location

                    var x = location.x - before.x
                    var z = location.z - before.z
                    var vec = p.eyeLocation.direction
                    if (x != 0.0 || z != 0.0) {
                        vec = Vector(x, 0.0, z)
                    } else {
                        x = location.x - before2.x
                        z = location.z - before2.z
                        if (x != 0.0 || z != 0.0) {
                            vec = Vector(x, 0.0, z)
                        }
                    }
                    before2 = before.clone()
                    before = location.clone()

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                    // マニューバー系
                    if (data.weaponClass?.mainWeapon!!.isManeuver) {
                        // if(p.getExp() >= ink) {
                        if (data.isSneaking && slRecharge2 && !data.isSliding && (
                                p
                                    .inventory
                                    .itemInMainHand
                                    .type
                                    ==
                                    data
                                        .weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .type
                                )
                        ) { // slをsl_recharge_2に変更することで優先順位が低い方のスライドが残っている時のみ使えるようにしました
                            val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize().multiply(3)
                            val ev = jvec.clone().normalize().multiply(-2)
                            check = true

                            // p.setExp(p.getExp() - ink);

                            // エフェクト
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
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
                                for (o_player in plugin.server.onlinePlayers) {
                                    if (getPlayerData(o_player)!!.settings!!.ShowEffect_BombEx()) {
                                        if (o_player.world === location.world) {
                                            if (o_player
                                                    .location
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
                            p.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                            p.velocity = jvec.clone().setY(if (p.isOnGround) 0.0 else -0.4)
                            data.isSneaking = false
                            data.isSliding = true
                            data.canShoot = false
                            // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                            if (!slRecharge1) {
                                slRecharge2 = false
                            } else {
                                slRecharge1 = false
                            }
                            // sl++;
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.velocity = Vector(0, 0, 0)
                                            data.isUsingManeuver = (true)
                                            data.canShoot = true
                                        }

                                        if (i == 10) {
                                            data.isUsingManeuver = (false)
                                            loc = p.location
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            task.runTaskTimer(plugin, 0, 1)

                            val task1: BukkitRunnable =
                                object : BukkitRunnable() {
                                    override fun run() {
                                        data.isSliding = false
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

                    if (!data.isSliding) {
                        if (loc.x == ploc.x && loc.z == ploc.z) {
                            data.isUsingManeuver = (true)
                        } else {
                            if (check) {
                                val task4: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            slRecharge1 = true
                                            // check = true;
                                        }
                                    }

                                val task5: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            slRecharge1 = true
                                            slRecharge2 = true
                                            // check = true;
                                        }
                                    }
                                if (slRecharge2) {
                                    task4.runTaskLater(plugin, 64)
                                } else {
                                    task5.runTaskLater(plugin, 64)
                                }
                                check = false
                            }
                            data.isUsingManeuver = (false)
                        }
                    }

                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun shoot(
        player: Player,
        slided: Boolean,
        sound: Boolean,
        maxRandom: Boolean,
    ) {
        var maxRandom = maxRandom
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass!!.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.exp = player.exp -
            (
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions =
            rayTrace.traverse(
                data.weaponClass?.mainWeapon!!.shootSpeed
                    * data.weaponClass?.mainWeapon!!.distanceTick,
                0.7,
            )
        var isLockOnPlayer = false
        if (data.weaponClass?.mainWeapon!!.maxRandom == 0.0) {
            check@ for (vector in positions) {
                val position = vector.toLocation(player.location.world!!)
                for (target in plugin.server.onlinePlayers) {
                    if (player !== target && player.world === target.world) {
                        if (target.location.distance(position) < 2) {
                            isLockOnPlayer = true
                            break@check
                        }
                    }
                }

                for (`as` in player.world.entities) {
                    if (`as` is ArmorStand) {
                        if (`as`.customName != null) {
                            if (`as`.location.distanceSquared(position) <= 4) { // 2x2
                                isLockOnPlayer = true
                                break@check
                            }
                        }
                    }
                }
            }
        } else {
            if (!player.isOnGround) maxRandom = true
        }

        PaintMgr.PaintHightestBlock(player.location, player, true, true)

        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.shootSpeed)
        var random = data.weaponClass?.mainWeapon!!.random
        if (maxRandom) random = data.weaponClass?.mainWeapon!!.maxRandom
        if (isLockOnPlayer) random /= 2.0
        if (slided) random /= 10.0
        val distick = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.distanceTick
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.velocity = vec
        ball.shooter = player
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
        ball.customName = name
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
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.ShowEffect_MainWeaponInk()) {
                                if (o_player.world ===
                                    inkball!!.world
                                ) {
                                    if (o_player
                                            .location
                                            .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball!!.location,
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
                        inkball!!.velocity = fallvec
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.velocity = inkball!!.velocity.add(Vector(0.0, -0.1, 0.0))
                    }
                    // if(i != tick)
                    if ((Random().nextInt(7)) == 0) PaintMgr.PaintHightestBlock(inkball!!.location, p, false, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
