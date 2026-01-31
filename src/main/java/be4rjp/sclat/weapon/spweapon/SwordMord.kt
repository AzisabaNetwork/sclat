package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
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
import org.bukkit.util.Consumer
import org.bukkit.util.Vector
import java.util.Random

object SwordMord {
    @JvmStatic
    fun setSwordMord(player: Player) {
        getPlayerData(player)!!.setIsUsingSP(true)
        getPlayerData(player)!!.setIsUsingSS(true)
        SPWeaponMgr.setSPCoolTimeAnimation(player, 160)

        val it: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    player.getInventory().clear()
                    player.updateInventory()

                    val item = ItemStack(Material.WHEAT)
                    val meta = item.getItemMeta()
                    meta!!.setDisplayName("右クリックで斬撃、シフトで防御")
                    item.setItemMeta(meta)
                    for (count in 0..8) {
                        player.getInventory().setItem(count, item)
                        if (count % 2 != 0) player.getInventory().setItem(count, ItemStack(Material.AIR))
                    }
                    player.updateInventory()
                    player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 161, 1))
                    // player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 161, 0));
                    // SwordPaintRunnable(p);
                    SwordGurdRunnable(p)
                }
            }
        it.runTaskLater(plugin, 2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    if (getPlayerData(p)!!.isInMatch()) {
                        getPlayerData(p)!!.setIsUsingSP(false)
                        getPlayerData(p)!!.setIsUsingSS(false)
                        player.getInventory().clear()
                        WeaponClassMgr.setWeaponClass(p)
                    }
                }
            }
        task.runTaskLater(plugin, 160)
    }

    @JvmStatic
    fun AttackSword(player: Player) {
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            if (!player.isSneaking()) {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f, 1.5f)
                val vec =
                    player
                        .getLocation()
                        .add(
                            player
                                .getEyeLocation()
                                .getDirection()
                                .normalize()
                                .multiply(2.4),
                        )
                for (target in plugin.getServer().getOnlinePlayers()) {
                    if (getPlayerData(target)!!.settings.ShowEffect_Bomb()) {
                        if (target.getWorld() ===
                            player.getWorld()
                        ) {
                            if (target
                                    .getLocation()
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
                for (i in 0..maxDist - 1) {
                    val p_locs = getSphere(vec, i.toDouble(), 20)
                    for (loc in p_locs) {
                        PaintMgr.Paint(loc, player, false)
                        PaintMgr.PaintHightestBlock(loc, player, false, false)
                    }
                }

                for (target in plugin.getServer().getOnlinePlayers()) {
                    if (!getPlayerData(target)!!.isInMatch()) continue
                    if (target.getLocation().distance(vec) <= maxDist) {
                        val damage = 15.1
                        if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                            target.getGameMode() == GameMode.ADVENTURE
                        ) {
                            giveDamage(player, target, damage, "spWeapon")

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var p: Player = target

                                    override fun run() {
                                        target.setNoDamageTicks(0)
                                    }
                                }
                            task.runTaskLater(plugin, 1)
                        }
                    }
                }

                for (`as` in player.getWorld().getEntities()) {
                    if (`as` is ArmorStand) {
                        if (`as`.getLocation().distanceSquared(vec) <= (maxDist + 1) * (maxDist + 1)) {
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
                        getPlayerData(p)!!.setCanUseSubWeapon(true)
                    }
                }
            task2.runTaskLater(plugin, 7)
        }
    }

    fun SwordPaintRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    try {
                        val data = getPlayerData(p)
                        if (!data!!.isInMatch() || !p.isOnline() || !getPlayerData(player)!!.getIsUsingSP()) cancel()

                        if (p.hasPotionEffect(PotionEffectType.LUCK) && p.getGameMode() != GameMode.SPECTATOR && (
                                p
                                    .getInventory()
                                    .getItemInMainHand()
                                    .getType() != Material.AIR
                                )
                        ) {
                            val locvec = p.getEyeLocation().getDirection()
                            val eloc = p.getEyeLocation()
                            val vec = Vector(locvec.getX(), 0.0, locvec.getZ()).normalize()
                            val front = eloc.add(vec.getX() * 0.5, -0.9, vec.getZ() * 0.5)
                            PaintMgr.PaintHightestBlock(front, p, false, true)
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun SwordGurdRunnable(player: Player) {
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
                var eloc: Location = p.getEyeLocation()
                var pv: Vector = p.getEyeLocation().getDirection().normalize()
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
                            if (!data!!.isInMatch() || !p.isOnline() || !getPlayerData(player)!!.getIsUsingSP()) {
                                as1!!.remove()
                                as2!!.remove()
                                as3!!.remove()
                                as4!!.remove()
                                cancel()
                            }
                            // 防具立て召喚
                            if (p.hasPotionEffect(PotionEffectType.LUCK) && p.getGameMode() != GameMode.SPECTATOR && p.isSneaking()) {
                                eloc = p.getEyeLocation()
                                pv = eloc.getDirection().normalize()
                                vec3 = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                                vec1 = Vector(vec3.getZ() * -1, 0.0, vec3.getX())
                                vec2 = Vector(vec3.getZ(), 0.0, vec3.getX() * -1)
                                l1 = eloc.clone().add(vec1.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                                r1 = eloc.clone().add(vec2.clone().multiply(0.4)).add(vec3.clone().multiply(0.7))
                                m1 = eloc.clone().add(vec3.clone().multiply(0.8))
                                if (!gurd) {
                                    kdata = KasaData(player)
                                    setKasaDataWithPlayer(player, kdata)
                                    val list: MutableList<ArmorStand> = ArrayList<ArmorStand>()
                                    as1 =
                                        player.getWorld().spawn<ArmorStand>(
                                            m1.clone().add(0.0, -1.8, 0.0),
                                            ArmorStand::class.java,
                                            Consumer { armorStand: ArmorStand ->
                                                armorStand.setGravity(false)
                                                armorStand.setVisible(false)
                                            },
                                        )
                                    as2 =
                                        player.getWorld().spawn<ArmorStand>(
                                            m1.clone().add(0.0, -0.8, 0.0),
                                            ArmorStand::class.java,
                                            Consumer { armorStand: ArmorStand ->
                                                armorStand.setGravity(false)
                                                armorStand.setVisible(false)
                                            },
                                        )
                                    as3 =
                                        player.getWorld().spawn<ArmorStand>(
                                            r1.clone().add(0.0, -1.2, 0.0),
                                            ArmorStand::class.java,
                                            Consumer { armorStand: ArmorStand ->
                                                armorStand.setGravity(false)
                                                armorStand.setVisible(false)
                                            },
                                        )
                                    as4 =
                                        player.getWorld().spawn<ArmorStand>(
                                            l1.clone().add(0.0, -1.2, 0.0),
                                            ArmorStand::class.java,
                                            Consumer { armorStand: ArmorStand ->
                                                armorStand.setGravity(false)
                                                armorStand.setVisible(false)
                                            },
                                        )
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
                                        `as`.setCustomName("Kasa")
                                        setKasaDataWithARmorStand(`as`, kdata)
                                    }
                                } else {
                                    as1!!.teleport(m1.clone().add(0.0, -1.8, 0.0))
                                    as2!!.teleport(m1.clone().add(0.0, -0.8, 0.0))
                                    as3!!.teleport(r1.clone().add(0.0, -1.2, 0.0))
                                    as4!!.teleport(l1.clone().add(0.0, -1.2, 0.0))
                                }
                                if (kdata.damage > 0.1) {
                                    val rayTrace = RayTrace(as1!!.getLocation().toVector(), Vector(0, 1, 0))
                                    for (target in plugin.getServer().getOnlinePlayers()) {
                                        if (!getPlayerData(target)!!.isInMatch()) continue
                                        if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                            target.getGameMode() == GameMode.ADVENTURE
                                        ) {
                                            if (rayTrace.intersects(BoundingBox(target as Entity), 5.0, 0.05)) {
                                                giveDamage(player, target, 6.0, "spWeapon")

                                                // AntiNoDamageTime
                                                val taskdamage: BukkitRunnable =
                                                    object : BukkitRunnable() {
                                                        var p: Player = target

                                                        override fun run() {
                                                            target.setNoDamageTicks(0)
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
                        if (p.getGameMode() != GameMode.SPECTATOR && p.isSneaking() && gurd && kdata.damage != 0.0) {
                            ShootCounter(player)
                            kdata.damage = (0).toDouble()
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun ShootCounter(player: Player) {
        val QuadroShootSpeed = 1.0
        if (player.getGameMode() == GameMode.SPECTATOR) return
        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true)

        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec = player.getLocation().getDirection().multiply(QuadroShootSpeed)
        val random = 0.1
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.setVelocity(vec)
        ball.setShooter(player)
        val originName = notDuplicateNumber.toString()
        val name = originName + "#QuadroArmsShotgunCounterShot"
        DataMgr.mws.add(name) //
        ball.setCustomName(name)
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val SpinnerTask: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = 4

                // Vector fallvec;
                var inkball: Snowball? = ball
                var addedFallVec: Boolean = false
                var p: Player = player
                var speedvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(5.0)
                var fallvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(QuadroShootSpeed / 35)

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
                    if (i < tick && !addedFallVec && i >= 1) {
                        inkball!!.setVelocity(speedvec)
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
        SpinnerTask.runTaskTimer(plugin, 0, 1)
    }
}
