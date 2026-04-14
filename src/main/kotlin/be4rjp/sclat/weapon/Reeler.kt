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
    fun reelerShootRunnable(player: Player) {
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
                            reelerShoot(p)
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
    fun reelerRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.location

                // int sl = 0;
                // スライドの仕様改変
                var slRecharge1: Boolean = true
                var killcount: Int = getPlayerData(p)!!.killCount
                var grRecharge: Int = 100

                // スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
                var check: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    p.location

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    val location = p.location

                    val vec = p.eyeLocation.direction

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();
                    if (grRecharge <= 100) {
                        grRecharge++
                    }
                    if (killcount < data.killCount) {
                        grRecharge = 100
                        killcount = data.killCount
                    }
                    if (data.isSneaking &&
                        grRecharge >= 100 &&
                        slRecharge1 &&
                        !data.isSliding &&
                        (
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
                    ) {
                        val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize().multiply(3)
                        val ev = jvec.clone().normalize().multiply(-2)
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
                                if (getPlayerData(o_player)!!.settings!!.showEffectBombEx()) {
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
                        p.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                        val task1: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    data.isSliding = true
                                }
                            }
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                var i: Int = 1

                                override fun run() {
                                    if (i == 3) {
                                        data.canShoot = true
                                        cancel()
                                    }
                                    i++
                                }
                            }
                        // リーラ―起動部分
                        if (Sclat.conf!!
                                .config!!
                                .getString("WorkMode") != "Trial"
                        ) {
                            val dest = grap(player)
                            if (dest !== player) {
                                grapple(player, dest)
                                grRecharge = 0
                                data.canShoot = false
                                task1.runTaskLater(plugin, 9)
                                task.runTaskTimer(plugin, 0, 1)
                            }
                        } else {
                            var destarm: ArmorStand?
                            destarm = graptest(player)
                            if (destarm != null) {
                                grappletest(player, destarm)
                                grRecharge = 0
                                data.canShoot = false
                                task1.runTaskLater(plugin, 9)
                                task.runTaskTimer(plugin, 0, 1)
                            }
                        }
                        data.isSneaking = false
                        // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                        slRecharge1 = false
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
                                    slRecharge1 = true
                                    // check = true;
                                }
                            }
                        task2.runTaskLater(plugin, 10)
                    }

                    // }else{
                    // p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                    // player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                    // }
                    if (data.isSliding) {
                        if (p.isOnGround) {
                            data.isUsingManeuver = false
                            data.isSliding = false
                        } else {
                            data.isUsingManeuver = true
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
                var beforeploc: Location = p.location
                var i: Int = 1

                override fun run() {
                    if (i == 7) {
                        if (!getPlayerData(p)!!.isInMatch ||
                            !p.isOnline ||
                            !getPlayerData(target)!!.isInMatch ||
                            !target.isOnline
                        ) {
                            cancel()
                            return
                        }
                        beforeploc = p.location
                    }
                    if (i == 8) {
                        if (!getPlayerData(p)!!.isInMatch ||
                            !p.isOnline ||
                            !getPlayerData(target)!!.isInMatch ||
                            !target.isOnline
                        ) {
                            cancel()
                            return
                        }
                        val tl = target.location
                        val pl = p.location
                        // Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                        val shot =
                            Vector(
                                tl.x - pl.x,
                                (tl.y - pl.y) * (0.93),
                                tl.z - pl.z,
                            )
                        shot.multiply(0.2)
                        if (pl.x - beforeploc.x != 0.0 || pl.z - beforeploc.z != 0.0) {
                            shot.add(
                                Vector(pl.x - beforeploc.x, 0.0, pl.z - beforeploc.z)
                                    .normalize()
                                    .multiply(shot.length() * 0.4),
                            )
                        }
                        // shot.add(new Vector(eye.getX(),0,eye.getZ()).multiply(shot.length()*0.2));
                        if (p.gameMode == GameMode.ADVENTURE && target.gameMode == GameMode.ADVENTURE) {
                            if (p.isOnGround) {
                                shot.multiply(0.68)
                                shot.add(Vector(0.0, 0.6 - shot.getY(), 0.0))
                            } else {
                                shot.multiply(0.5)
                                shot.add(Vector(0.0, 0.5, 0.0))
                            }
                            p.velocity = shot
                            val pdata = getPlayerData(p)
                            pdata!!.isSliding = true
                            pdata.isUsingManeuver = true
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
                var beforeploc: Location = p.location
                var i: Int = 1

                override fun run() {
                    if (i == 7) {
                        if (!getPlayerData(p)!!.isInMatch || !p.isOnline) {
                            cancel()
                            return
                        }
                        beforeploc = p.location
                    }
                    if (i == 8) {
                        if (!getPlayerData(p)!!.isInMatch || !p.isOnline) {
                            cancel()
                            return
                        }
                        val tl = target.location
                        val pl = p.location
                        // Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                        val shot =
                            Vector(
                                tl.x - pl.x,
                                (tl.y - pl.y) * (0.93),
                                tl.z - pl.z,
                            )
                        shot.multiply(0.2)
                        if (pl.x - beforeploc.x != 0.0 || pl.z - beforeploc.z != 0.0) {
                            shot.add(
                                Vector(pl.x - beforeploc.x, 0.0, pl.z - beforeploc.z)
                                    .normalize()
                                    .multiply(shot.length() * 0.4),
                            )
                        }
                        // shot.add(new Vector(eye.getX(), 0, eye.getZ()).multiply(shot.length() *
                        // 0.2));
                        if (p.gameMode == GameMode.ADVENTURE) {
                            if (p.isOnGround) {
                                shot.multiply(0.68)
                                shot.add(Vector(0.0, 0.6 - shot.getY(), 0.0))
                            } else {
                                shot.multiply(0.5)
                                shot.add(Vector(0.0, 0.5, 0.0))
                            }
                            p.velocity = shot
                            getPlayerData(p)!!.isSliding = true
                            getPlayerData(p)!!.isUsingManeuver = true
                        }
                    }
                    i++
                }
            }
        graptask.runTaskTimer(plugin, 0, 1)
    }

    fun grap(player: Player): Player {
        var dest = player
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions = rayTrace.traverse(20.0, 0.2)

        loop@ for (it in positions.indices) {
            val position = positions[it].toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                break
            }
            if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
                if (it < 10) {
                    if (player.world === position.world) {
                        if (player.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 6.0 // 2*2
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquad) {
                        // if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                        dest = target
                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 10, 1))
                        player.playSound(player.location, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2.0f, 2f)
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
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions = rayTrace.traverse(20.0, 0.2)
        loop2@ for (it in positions.indices) {
            val position = positions[it].toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                break
            }
            if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
                if (it < 10) {
                    if (player.world === position.world) {
                        if (player.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 6.0 // 2*2
            if (Sclat.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
                for (`as` in player.world.entities) {
                    if (`as` is ArmorStand) {
                        if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                            // if(rayTrace.intersects(new BoundingBox((Entity)as), (int)(30), 0.2)){
                            if (`as`.customName != null) {
                                if (`as`.customName == "SplashShield") {
                                    // SplashShieldData ssdata =
                                    // DataMgr.getSplashShieldDataFromArmorStand((ArmorStand)as);
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(player).getTeam()){
                                    // break loop;
                                    // }
                                } else if (`as`.customName == "Kasa") {
                                    // KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand)as);
                                    // if(DataMgr.getPlayerData(ssdata.player).getTeam() !=
                                    // DataMgr.getPlayerData(player).getTeam()){
                                    // break loop;
                                    // }
                                } else {
                                    if (SclatUtil.isNumber(`as`.customName!!)) {
                                        if (`as`.customName != "21" &&
                                            `as`.customName != "100"
                                        ) {
                                            if (`as`.isVisible) {
                                                dest = `as`
                                                player.playSound(
                                                    player.location,
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

    fun reelerShoot(player: Player) {
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
        player.exp -=
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
                            if (`as`.location.distanceSquared(position) <= 4) { // 2*2
                                isLockOnPlayer = true
                                break@check
                            }
                        }
                    }
                }
            }
        }

        PaintMgr.paintHightestBlock(player.location, player, true, true)

        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.slideNeedINK)
        val random = data.weaponClass?.mainWeapon!!.chargeRatio
        val distick = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.maxCharge
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.velocity = vec
        ball.shooter = player
        // スライド時かどうかをSnowballListenerに渡すためのnameの改変
        val originName = notDuplicateNumber.toString()
        val name = originName + "#slided"
        // String name = String.valueOf(Main.getNotDuplicateNumber());//ここで改変終わり
        DataMgr.mws.add(name)
        DataMgr.tsl.add(name)
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

                // Vector fallvec = new Vector(inkball.getVelocity().getX(),
                // inkball.getVelocity().getY() ,
                // inkball.getVelocity().getZ()).multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed()/17);
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(0.01)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

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
                            if (getPlayerData(o_player)!!.settings!!.showEffectMainWeaponInk()) {
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
                    if ((Random().nextInt(7)) == 0) PaintMgr.paintHightestBlock(inkball!!.location, p, false, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
