package be4rjp.sclat.weapon

import be4rjp.dadadachecker.ClickType
import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil
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
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

object Reeler {
    @JvmStatic
    fun ReelerShootRunnable(player: Player) {
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
                            ReelerShoot(p)
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
    fun ReelerRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.getLocation()

                // int sl = 0;
                // スライドの仕様改変
                var sl_recharge_1: Boolean = true
                var killcount: Int = getPlayerData(p)!!.getKillCount()
                var gr_recharge: Int = 100

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

                    val vec = p.getEyeLocation().getDirection()

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();
                    if (gr_recharge <= 100) {
                        gr_recharge++
                    }
                    if (killcount < data.getKillCount()) {
                        gr_recharge = 100
                        killcount = data.getKillCount()
                    }
                    if (data.getIsSneaking() && gr_recharge >= 100 && sl_recharge_1 && !data.getIsSliding() && (
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
                    ) {
                        val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize().multiply(3)
                        val ev = jvec.clone().normalize().multiply(-2)
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
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                        val task1: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    data.setIsSliding(true)
                                }
                            }
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                var i: Int = 1

                                override fun run() {
                                    if (i == 3) {
                                        data.setCanShoot(true)
                                        cancel()
                                    }
                                    i++
                                }
                            }
                        // リーラ―起動部分
                        if (Sclat.Companion.conf!!
                                .config!!
                                .getString("WorkMode") != "Trial"
                        ) {
                            val dest = grap(player)
                            if (dest !== player) {
                                grapple(player, dest)
                                gr_recharge = 0
                                data.setCanShoot(false)
                                task1.runTaskLater(plugin, 9)
                                task.runTaskTimer(plugin, 0, 1)
                            }
                        } else {
                            var destarm: ArmorStand? = null
                            destarm = graptest(player)
                            if (destarm != null) {
                                grappletest(player, destarm)
                                gr_recharge = 0
                                data.setCanShoot(false)
                                task1.runTaskLater(plugin, 9)
                                task.runTaskTimer(plugin, 0, 1)
                            }
                        }
                        data.setIsSneaking(false)
                        // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                        sl_recharge_1 = false
                        // sl++;
                        // BukkitRunnable task2 = new BukkitRunnable() {
                        // @Override
                        // public void run() {
                        // sl = 0;
                        // check = true;
                        // }
                        // };
                        val task2: BukkitRunnable =
                            object : BukkitRunnable() {
                                // 二つのtaskの追加でそれぞれのスライドを管理しています
                                override fun run() {
                                    sl_recharge_1 = true
                                    // check = true;
                                }
                            }
                        task2.runTaskLater(plugin, 10)
                    }

                    // }else{
                    // p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                    // player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                    // }
                    if (data.getIsSliding()) {
                        if (p.isOnGround()) {
                            data.setIsUsingManeuver(false)
                            data.setIsSliding(false)
                        } else {
                            data.setIsUsingManeuver(true)
                        }
                    }

                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    fun grapple(
        p: Player,
        target: Player,
    ) {
        val graptask: BukkitRunnable =
            object : BukkitRunnable() {
                var beforeploc: Location = p.getLocation()
                var i: Int = 1

                override fun run() {
                    if (i == 7) {
                        if (!getPlayerData(p)!!.isInMatch() || !p.isOnline() || !getPlayerData(target)!!.isInMatch() ||
                            !target.isOnline()
                        ) {
                            cancel()
                            return
                        }
                        beforeploc = p.getLocation()
                    }
                    if (i == 8) {
                        if (!getPlayerData(p)!!.isInMatch() || !p.isOnline() || !getPlayerData(target)!!.isInMatch() ||
                            !target.isOnline()
                        ) {
                            cancel()
                            return
                        }
                        val tl = target.getLocation()
                        val pl = p.getLocation()
                        // Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                        val shot =
                            Vector(
                                tl.getX() - pl.getX(),
                                (tl.getY() - pl.getY()) * (0.93),
                                tl.getZ() - pl.getZ(),
                            )
                        shot.multiply(0.2)
                        if (pl.getX() - beforeploc.getX() != 0.0 || pl.getZ() - beforeploc.getZ() != 0.0) {
                            shot.add(
                                Vector(pl.getX() - beforeploc.getX(), 0.0, pl.getZ() - beforeploc.getZ())
                                    .normalize()
                                    .multiply(shot.length() * 0.4),
                            )
                        }
                        // shot.add(new Vector(eye.getX(),0,eye.getZ()).multiply(shot.length()*0.2));
                        if (p.getGameMode() == GameMode.ADVENTURE && target.getGameMode() == GameMode.ADVENTURE) {
                            if (p.isOnGround()) {
                                shot.multiply(0.68)
                                shot.add(Vector(0.0, 0.6 - shot.getY(), 0.0))
                            } else {
                                shot.multiply(0.5)
                                shot.add(Vector(0.0, 0.5, 0.0))
                            }
                            p.setVelocity(shot)
                            val pdata = getPlayerData(p)
                            pdata!!.setIsSliding(true)
                            pdata.setIsUsingManeuver(true)
                            if (pdata.armor > 9999) {
                                pdata.armor = 0.0
                            }
                        }
                        cancel()
                    }
                    i++
                }
            }
        // graptask.runTaskLater(Main.getPlugin(), 8);
        graptask.runTaskTimer(plugin, 0, 1)
    }

    fun grappletest(
        p: Player,
        target: ArmorStand,
    ) {
        val graptask: BukkitRunnable =
            object : BukkitRunnable() {
                var beforeploc: Location = p.getLocation()
                var i: Int = 1

                override fun run() {
                    if (i == 7) {
                        if (!getPlayerData(p)!!.isInMatch() || !p.isOnline()) {
                            cancel()
                            return
                        }
                        beforeploc = p.getLocation()
                    }
                    if (i == 8) {
                        if (!getPlayerData(p)!!.isInMatch() || !p.isOnline()) {
                            cancel()
                            return
                        }
                        val tl = target.getLocation()
                        val pl = p.getLocation()
                        // Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                        val shot =
                            Vector(
                                tl.getX() - pl.getX(),
                                (tl.getY() - pl.getY()) * (0.93),
                                tl.getZ() - pl.getZ(),
                            )
                        shot.multiply(0.2)
                        if (pl.getX() - beforeploc.getX() != 0.0 || pl.getZ() - beforeploc.getZ() != 0.0) {
                            shot.add(
                                Vector(pl.getX() - beforeploc.getX(), 0.0, pl.getZ() - beforeploc.getZ())
                                    .normalize()
                                    .multiply(shot.length() * 0.4),
                            )
                        }
                        // shot.add(new Vector(eye.getX(), 0, eye.getZ()).multiply(shot.length() *
                        // 0.2));
                        if (p.getGameMode() == GameMode.ADVENTURE) {
                            if (p.isOnGround()) {
                                shot.multiply(0.68)
                                shot.add(Vector(0.0, 0.6 - shot.getY(), 0.0))
                            } else {
                                shot.multiply(0.5)
                                shot.add(Vector(0.0, 0.5, 0.0))
                            }
                            p.setVelocity(shot)
                            getPlayerData(p)!!.setIsSliding(true)
                            getPlayerData(p)!!.setIsUsingManeuver(true)
                        }
                    }
                    i++
                }
            }
        graptask.runTaskTimer(plugin, 0, 1)
    }

    fun grap(player: Player): Player {
        var dest = player
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace.traverse(20.0, 0.2)

        loop@ for (it in positions.indices) {
            val position = positions.get(it).toLocation(player.getLocation().getWorld()!!)
            val block = player.getLocation().getWorld()!!.getBlockAt(position)

            if (block.getType() != Material.AIR) {
                break
            }
            if (getPlayerData(player)!!.settings.ShowEffect_MainWeaponInk()) {
                if (it < 10) {
                    if (player.getWorld() === position.getWorld()) {
                        if (player.getLocation().distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 6.0 // 2*2
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (!getPlayerData(target)!!.isInMatch()) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.getGameMode() == GameMode.ADVENTURE
                ) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        // if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                        dest = target
                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 10, 1))
                        player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2.0f, 2f)
                        break@loop
                        // }
                    }
                }
            }
        }
        return dest
    }

    fun graptest(player: Player): ArmorStand? {
        var dest: ArmorStand? = null
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace.traverse(20.0, 0.2)
        loop2@ for (it in positions.indices) {
            val position = positions.get(it).toLocation(player.getLocation().getWorld()!!)
            val block = player.getLocation().getWorld()!!.getBlockAt(position)

            if (block.getType() != Material.AIR) {
                break
            }
            if (getPlayerData(player)!!.settings.ShowEffect_MainWeaponInk()) {
                if (it < 10) {
                    if (player.getWorld() === position.getWorld()) {
                        if (player.getLocation().distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 6.0 // 2*2
            if (Sclat.Companion.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
                for (`as` in player.getWorld().getEntities()) {
                    if (`as` is ArmorStand) {
                        if (`as`.getLocation().distanceSquared(position) <= maxDistSquad) {
                            // if(rayTrace.intersects(new BoundingBox((Entity)as), (int)(30), 0.2)){
                            if (`as`.getCustomName() != null) {
                                if (`as`.getCustomName() == "SplashShield") {
                                    // SplashShieldData ssdata =
                                    // DataMgr.getSplashShieldDataFromArmorStand((ArmorStand)as);
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(player).getTeam()){
                                    // break loop;
                                    // }
                                } else if (`as`.getCustomName() == "Kasa") {
                                    // KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand)as);
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(player).getTeam()){
                                    // break loop;
                                    // }
                                } else {
                                    if (SclatUtil.isNumber(`as`.getCustomName()!!)) {
                                        if (`as`.getCustomName() != "21" &&
                                            `as`.getCustomName() != "100"
                                        ) {
                                            if (`as`.isVisible()) {
                                                dest = `as`
                                                player.playSound(
                                                    player.getLocation(),
                                                    Sound.BLOCK_IRON_TRAPDOOR_CLOSE,
                                                    2.0f,
                                                    2f,
                                                )
                                                // player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER,
                                                // 1.2F, 1.3F);
                                            }
                                        }
                                    }
                                }
                                break@loop2
                            }
                        }
                        // ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                        // }
                    }
                }
            }
        }
        return dest
    }

    fun ReelerShoot(player: Player) {
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
                .multiply(getPlayerData(player)!!.getWeaponClass().mainWeapon!!.slideNeedINK)
        val random = data.getWeaponClass().mainWeapon!!.chargeRatio
        val distick = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.maxCharge
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.setVelocity(vec)
        ball.setShooter(player)
        // スライド時かどうかをSnowballListenerに渡すためのnameの改変
        val originName = notDuplicateNumber.toString()
        val name = originName + "#slided"
        // String name = String.valueOf(Main.getNotDuplicateNumber());//ここで改変終わり
        DataMgr.mws.add(name)
        DataMgr.tsl.add(name)
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

                // Vector fallvec = new Vector(inkball.getVelocity().getX(),
                // inkball.getVelocity().getY() ,
                // inkball.getVelocity().getZ()).multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed()/17);
                var fallvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(0.01)

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
