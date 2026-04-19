package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import be4rjp.sclat.data.DataMgr.setKasaDataWithPlayer
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.data.KasaData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.shape.Sphere.getSphere
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

object SwordMord {
    @JvmStatic
    fun setSwordMord(player: Player) {
        getPlayerData(player)!!.isUsingSP = true
        getPlayerData(player)!!.isUsingSS = true
        SPWeaponMgr.setSPCoolTimeAnimation(player, 160)

        val it: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    player.inventory.clear()
                    player.updateInventory()

                    val item = ItemStack(Material.WHEAT)
                    val meta = item.itemMeta
                    meta!!.setDisplayName("右クリックで斬撃、シフトで防御")
                    item.itemMeta = meta
                    for (count in 0..8) {
                        player.inventory.setItem(count, item)
                        if (count % 2 != 0) player.inventory.setItem(count, ItemStack(Material.AIR))
                    }
                    player.updateInventory()
                    player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 161, 1))
                    // player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 161, 0));
                    // SwordPaintRunnable(p);
                    swordGurdRunnable(p)
                }
            }
        it.runTaskLater(plugin, 2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    if (getPlayerData(p)!!.isInMatch) {
                        getPlayerData(p)!!.isUsingSP = false
                        getPlayerData(p)!!.isUsingSS = false
                        player.inventory.clear()
                        WeaponClassMgr.setWeaponClass(p)
                    }
                }
            }
        task.runTaskLater(plugin, 160)
    }

    @JvmStatic
    fun attackSword(player: Player) {
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            if (!player.isSneaking) {
                player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f, 1.5f)
                val vec =
                    player
                        .location
                        .add(
                            player
                                .eyeLocation
                                .direction
                                .normalize()
                                .multiply(2.4),
                        )
                for (target in plugin.server.onlinePlayers) {
                    if (getPlayerData(target)!!.settings!!.showEffectBomb()) {
                        if (target.world ===
                            player.world
                        ) {
                            if (target
                                    .location
                                    .distance(vec) < Sclat.particleRenderDistance
                            ) {
                                if (target == player) {
                                    target.spawnParticle(Particle.SWEEP_ATTACK, vec.add(0.0, 1.5, 0.0), 0, 10.0, 7.0, 10.0)
                                } else {
                                    target.spawnParticle(Particle.SWEEP_ATTACK, vec, 0, 8.0, 5.0, 8.0)
                                }
                            }
                        }
                    }
                }
                val maxDist = 3
                for (i in 0..<maxDist) {
                    val pLocs = getSphere(vec, i.toDouble(), 20)
                    for (loc in pLocs) {
                        PaintMgr.paint(loc, player, false)
                        PaintMgr.paintHightestBlock(loc, player, false, false)
                    }
                }

                for (target in plugin.server.onlinePlayers) {
                    if (!getPlayerData(target)!!.isInMatch) continue
                    if (target.location.distance(vec) <= maxDist) {
                        val damage = 15.1
                        if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                            target.gameMode == GameMode.ADVENTURE
                        ) {
                            giveDamage(player, target, damage, "spWeapon")

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var p: Player = target

                                    override fun run() {
                                        target.noDamageTicks = 0
                                    }
                                }
                            task.runTaskLater(plugin, 1)
                        }
                    }
                }

                for (`as` in player.world.entities) {
                    if (`as` is ArmorStand) {
                        if (`as`.location.distanceSquared(vec) <= (maxDist + 1) * (maxDist + 1)) {
                            val damage = 15.1
                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                        }
                    }
                }
            }

            val task2: BukkitRunnable =
                object : BukkitRunnable() {
                    var p: Player = player

                    override fun run() {
                        getPlayerData(p)!!.canUseSubWeapon = true
                    }
                }
            task2.runTaskLater(plugin, 7)
        }
    }

    fun swordPaintRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    try {
                        val data = getPlayerData(p)
                        if (!data!!.isInMatch || !p.isOnline || !getPlayerData(player)!!.isUsingSP) cancel()

                        if (p.hasPotionEffect(PotionEffectType.LUCK) &&
                            p.gameMode != GameMode.SPECTATOR &&
                            (
                                p
                                    .inventory
                                    .itemInMainHand
                                    .type != Material.AIR
                            )
                        ) {
                            val locvec = p.eyeLocation.direction
                            val eloc = p.eyeLocation
                            val vec = Vector(locvec.getX(), 0.0, locvec.getZ()).normalize()
                            val front = eloc.add(vec.getX() * 0.5, -0.9, vec.getZ() * 0.5)
                            PaintMgr.paintHightestBlock(front, p, false, true)
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun swordGurdRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var kdata: KasaData = KasaData(player)
                var as1: ArmorStand? = null
                var as2: ArmorStand? = null
                var as3: ArmorStand? = null
                var as4: ArmorStand? = null
                var c: Int = 0
                var gurd: Boolean = false
                var p: Player = player
                var eloc: Location = p.eyeLocation
                var pv: Vector = p.eyeLocation.direction.normalize()
                var vec3: Vector = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                var vec1: Vector = Vector(vec3.getZ() * -1, 0.0, vec3.getX())
                var vec2: Vector = Vector(vec3.getZ(), 0.0, vec3.getX() * -1)
                var l1: Location = eloc.clone().add(vec1.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                var r1: Location = eloc.clone().add(vec2.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                var m1: Location = eloc.clone().add(vec3.clone().multiply(0.8))

                override fun run() {
                    try {
                        c++
                        if (c % 2 == 0) {
                            val data = getPlayerData(p)
                            if (!data!!.isInMatch || !p.isOnline || !getPlayerData(player)!!.isUsingSP) {
                                as1!!.remove()
                                as2!!.remove()
                                as3!!.remove()
                                as4!!.remove()
                                cancel()
                            }
                            // 防具立て召喚
                            if (p.hasPotionEffect(PotionEffectType.LUCK) && p.gameMode != GameMode.SPECTATOR && p.isSneaking) {
                                eloc = p.eyeLocation
                                pv = eloc.direction.normalize()
                                vec3 = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                                vec1 = Vector(vec3.getZ() * -1, 0.0, vec3.getX())
                                vec2 = Vector(vec3.getZ(), 0.0, vec3.getX() * -1)
                                l1 = eloc.clone().add(vec1.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                                r1 = eloc.clone().add(vec2.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                                m1 = eloc.clone().add(vec3.clone().multiply(0.8))
                                if (!gurd) {
                                    kdata = KasaData(player)
                                    setKasaDataWithPlayer(player, kdata)
                                    val list: MutableList<ArmorStand> = ArrayList()
                                    as1 =
                                        player.world.spawn(
                                            m1.clone().add(0.0, -1.8, 0.0),
                                            ArmorStand::class.java,
                                        ) { armorStand: ArmorStand ->
                                            armorStand.setGravity(false)
                                            armorStand.isVisible = false
                                        }
                                    as2 =
                                        player.world.spawn(
                                            m1.clone().add(0.0, -0.8, 0.0),
                                            ArmorStand::class.java,
                                        ) { armorStand: ArmorStand ->
                                            armorStand.setGravity(false)
                                            armorStand.isVisible = false
                                        }
                                    as3 =
                                        player.world.spawn(
                                            r1.clone().add(0.0, -1.2, 0.0),
                                            ArmorStand::class.java,
                                        ) { armorStand: ArmorStand ->
                                            armorStand.setGravity(false)
                                            armorStand.isVisible = false
                                        }
                                    as4 =
                                        player.world.spawn(
                                            l1.clone().add(0.0, -1.2, 0.0),
                                            ArmorStand::class.java,
                                        ) { armorStand: ArmorStand ->
                                            armorStand.setGravity(false)
                                            armorStand.isVisible = false
                                        }
                                    gurd = true
                                    list.add(as1!!)
                                    list.add(as2!!)
                                    list.add(as3!!)
                                    list.add(as4!!)
                                    val aslist: MutableList<ArmorStand> = list.toMutableList()
                                    kdata.armorStandList = aslist
                                    for (`as` in list) {
                                        // as.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
                                        `as`.setBasePlate(false)
                                        // as.setVisible(false);
                                        // as.setGravity(false);
                                        `as`.customName = "Kasa"
                                        setKasaDataWithARmorStand(`as`, kdata)
                                    }
                                } else {
                                    as1!!.teleport(m1.clone().add(0.0, -1.8, 0.0))
                                    as2!!.teleport(m1.clone().add(0.0, -0.8, 0.0))
                                    as3!!.teleport(r1.clone().add(0.0, -1.2, 0.0))
                                    as4!!.teleport(l1.clone().add(0.0, -1.2, 0.0))
                                }
                                if (kdata.damage > 0.1) {
                                    val rayTrace = RayTrace(as1!!.location.toVector(), Vector(0, 1, 0))
                                    for (target in plugin.server.onlinePlayers) {
                                        if (!getPlayerData(target)!!.isInMatch) continue
                                        if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                            target.gameMode == GameMode.ADVENTURE
                                        ) {
                                            if (rayTrace.intersects(BoundingBox(target as Entity), 5.0, 0.05)) {
                                                giveDamage(player, target, 6.0, "spWeapon")

                                                // AntiNoDamageTime
                                                val taskdamage: BukkitRunnable =
                                                    object : BukkitRunnable() {
                                                        var p: Player = target

                                                        override fun run() {
                                                            target.noDamageTicks = 0
                                                        }
                                                    }
                                                taskdamage.runTaskLater(plugin, 1)
                                            }
                                        }
                                    }
                                }
                            } else if (gurd) {
                                as1!!.remove()
                                as2!!.remove()
                                as3!!.remove()
                                as4!!.remove()
                                gurd = false
                            }
                        }
                        if (p.gameMode != GameMode.SPECTATOR && p.isSneaking && gurd && kdata.damage != 0.0) {
                            shootCounter(player)
                            kdata.damage = (0).toDouble()
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun shootCounter(player: Player) {
        val quadroShootSpeed = 1.0
        if (player.gameMode == GameMode.SPECTATOR) return
        PaintMgr.paintHightestBlock(player.location, player, true, true)

        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec = player.location.direction.multiply(quadroShootSpeed)
        val random = 0.1
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.velocity = vec
        ball.shooter = player
        val originName = notDuplicateNumber.toString()
        val name = "$originName#QuadroArmsShotgunCounterShot"
        DataMgr.mws.add(name) //
        ball.customName = name
        mainSnowballNameMap[name] = ball
        setSnowballHitCount(name, 0)
        val spinnerTask: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = 4

                // Vector fallvec;
                var inkball: Snowball? = ball
                var addedFallVec: Boolean = false
                var p: Player = player
                var speedvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(5.0)
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(quadroShootSpeed / 35)

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
                    if (i < tick && !addedFallVec && i >= 1) {
                        inkball!!.velocity = speedvec
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
        spinnerTask.runTaskTimer(plugin, 0, 1)
    }
}
