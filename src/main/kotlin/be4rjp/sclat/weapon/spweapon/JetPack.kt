package be4rjp.sclat.weapon.spweapon

import be4rjp.blockstudio.BlockStudio
import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.SuperJumpMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import net.azisaba.sclat.core.shape.Sphere.getSphere
import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object JetPack {
    @JvmStatic
    fun jetPackRunnable(player: Player) {
        val api = BlockStudio.getBlockStudioAPI()
        val objectData = api.getObjectData("jetpack")
        val bsObject =
            api.createObjectFromObjectData(
                notDuplicateNumber.toString(),
                player.location,
                objectData,
                40.0,
                false,
            )
        bsObject.startTaskAsync(40)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var ol: Location = getPlayerData(player)!!.playerGroundLocation!!
                var i: Int = 0
                var id: Int = 0
                var btl: Location = player.location
                var `as`: ArmorStand =
                    player
                        .world
                        .spawn(
                            player.location,
                            ArmorStand::class.java,
                        ) { armorStand: ArmorStand ->
                            armorStand.isSmall = true
                            armorStand.setGravity(false)
                            armorStand.isVisible = false
                            armorStand.setBasePlate(false)
                            armorStand.isMarker = true
                        }
                var leader: ArmorStand =
                    player
                        .world
                        .spawn(
                            player.location,
                            ArmorStand::class.java,
                        ) { armorStand: ArmorStand ->
                            armorStand.isSmall = true
                            armorStand.isVisible = false
                            armorStand.setBasePlate(false)
                        }
                var list: MutableList<ArmorStand?> = ArrayList()

                var vehicleVector: Vector = Vector(0, 0, 0)

                override fun run() {
                    p.isSprinting = true

                    var onBlock = false
                    var yh = 1
                    var y = p.location.blockY
                    while (y >= 1 && y >= p.location.blockY - 7) {
                        val bl =
                            Location(
                                p.location.world,
                                p.location.x,
                                y.toDouble(),
                                p.location.z,
                            )
                        if (bl.block.type != Material.AIR && bl.block.type != Material.WATER) {
                            onBlock = true
                            break
                        }
                        yh++
                        y--
                    }

                    p.allowFlight = true
                    p.isFlying = true

                    var vec = Vector(0, 0, 0)
                    if (i % 2 == 0) vec = getPlayerData(p)!!.vehicleVector!!.clone().multiply(0.8)
                    val pvec = p.eyeLocation.direction
                    val wWasdvector = (Vector(pvec.getX(), 0.0, pvec.getZ())).multiply(vec.getX())
                    val dWasdvector = (Vector(pvec.getZ(), 0.0, pvec.getX() * -1)).multiply(vec.getZ())
                    val xzVector = wWasdvector.add(dWasdvector)
                    val moveVector =
                        Vector(
                            xzVector.getX(),
                            if (onBlock) (if (i >= 30) vec.getY() + 0.1 else 0.7) else -0.5,
                            xzVector.getZ(),
                        ).multiply(0.3)
                    if ((vehicleVector.clone().add(moveVector)).lengthSquared() <= 0.19) {
                        vehicleVector.add(moveVector)
                        vehicleVector = vehicleVector.multiply(0.9)
                    }
                    leader.velocity = vehicleVector

                    // as.teleport(as.getLocation().add(vehicleVector));
                    if (!`as`.passengers.contains(p)) {
                        `as`.addPassenger(p)
                    }

                    // p.setWalkSpeed(0.1F);
                    // p.setFlySpeed(0.02F);
                    val leaderEyeLoc = leader.eyeLocation
                    val leaderLoc = leader.location.add(0.0, -0.3, 0.0)
                    leaderEyeLoc.yaw = p.eyeLocation.yaw
                    // as.teleport(leaderLoc);
                    (`as` as CraftArmorStand).handle.setPositionRotation(
                        leaderLoc.x,
                        leaderLoc.y,
                        leaderLoc.z,
                        p.location.yaw,
                        0f,
                    )

                    // move object
                    val pv = p.eyeLocation.direction
                    val direction = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                    val locPlus = direction.clone().multiply(-0.2)
                    bsObject.baseLocation = leaderLoc.clone().add(locPlus.getX(), 0.5, locPlus.getZ())
                    bsObject.setDirection(direction)
                    bsObject.move()

                    val vec1 = Vector(pv.getX(), 0.0, pv.getZ()).normalize().multiply(-0.2)
                    val pl = leaderEyeLoc.clone()
                    // Location loc1 = pl.add(vec1.getX() + sv.getX(), sv.getY() * 0.8, vec1.getZ()
                    // + sv.getZ());
                    val loc1 = pl.add(vec1.getX(), 0.0, vec1.getZ())

                    val vec2 = Vector(vec1.getZ() * -1, 0.0, vec1.getX()).normalize().multiply(0.6)
                    val vec3 = Vector(vec1.getZ(), 0.0, vec1.getX() * -1).normalize().multiply(0.6)
                    val loc2 = loc1.clone().add(vec2.getX(), 0.0, vec2.getZ())
                    val loc3 = loc1.clone().add(vec3.getX(), 0.0, vec3.getZ())

                    PaintMgr.paintHightestBlock(loc2, player, false, true)
                    PaintMgr.paintHightestBlock(loc3, player, false, true)

                    if (i != 0) {
                        // effect
                        // org.bukkit.block.data.BlockData bd =
                        // DataMgr.getPlayerData(player).getTeam().getTeamColor().wool.createBlockData();
                        val position = loc2.clone().add(0.0, -0.2, 0.0)
                        val position2 = loc3.clone().add(0.0, -0.2, 0.0)
                        for (o_player in plugin.server.onlinePlayers) {
                            if (!getPlayerData(o_player)!!.settings!!.showEffectSPWeapon()) continue
                            if (o_player.world === position.world) {
                                if (o_player
                                        .location
                                        .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                ) {
                                    for (i in 0..10) {
                                        val random = 0.015
                                        o_player.spawnParticle<ItemStack?>(
                                            Particle.ITEM_CRACK,
                                            position,
                                            0,
                                            Math.random() * random - random / 2,
                                            -0.13,
                                            Math.random() * random - random / 2,
                                            10.0,
                                            ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!),
                                        )
                                        // o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 0, 0, -2, 0,
                                        // 10, bd);
                                    }
                                }
                                if (o_player
                                        .location
                                        .distanceSquared(position2) < Sclat.particleRenderDistanceSquared
                                ) {
                                    for (i in 0..10) {
                                        val random = 0.015
                                        o_player.spawnParticle<ItemStack?>(
                                            Particle.ITEM_CRACK,
                                            position,
                                            0,
                                            Math.random() * random - random / 2,
                                            -0.13,
                                            Math.random() * random - random / 2,
                                            10.0,
                                            ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!),
                                        )
                                    }
                                }
                            }
                        }

                    /*
                     * for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                     * if(!DataMgr.getPlayerData(o_player).getSettings().ShowEffect_SPWeapon())
                     * continue; if(o_player.getWorld() == position.getWorld()){
                     * if(o_player.getLocation().distanceSquared(position) <
                     * Main.PARTICLE_RENDER_DISTANCE_SQUARED){ for(int i = 0; i <= 10; i++) { double
                     * random = 0.015; o_player.spawnParticle(Particle.ITEM_CRACK, position, 0,
                     * Math.random() * random - random/2, -0.13, Math.random() * random - random/2,
                     * 10, new
                     * ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().wool)); } }
                     * } }
                     *
                     */
                    }

                    if (i == 0) {
                        getPlayerData(p)!!.isUsingSP = true
                        SPWeaponMgr.setSPCoolTimeAnimation(player, 175)
                        getPlayerData(p)!!.isUsingJetPack = true

                        p.inventory.clear()

                        val item = ItemStack(Material.QUARTZ)
                        val meta = item.itemMeta
                        meta!!.setDisplayName("右クリックで弾を発射")
                        item.itemMeta = meta
                        for (count in 0..8) {
                            player.inventory.setItem(count, item)
                        }
                        player.updateInventory()
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 176, 1))
                        SuperArmor.setArmor(player, 1.0, 175, false)

                        val nmsWorld = (p.world as CraftWorld).handle
                        val `as` = EntityArmorStand(nmsWorld, ol.x, ol.y, ol.z)
                        `as`.setPosition(ol.x, ol.y, ol.z)
                        `as`.isInvisible = true
                        `as`.isNoGravity = true
                        `as`.setBasePlate(false)
                        `as`.customName =
                            CraftChatMessage.fromStringOrNull(
                                getPlayerData(p)!!.team!!.teamColor!!.colorCode + "↓↓↓  くコ:彡  ↓↓↓",
                            )
                        `as`.customNameVisible = true
                        `as`.isSmall = true
                        id = `as`.bukkitEntity.entityId
                        for (target in plugin.server.onlinePlayers) {
                            if (p.world === target.world) {
                                (target as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
                            }
                        }
                    }

                    p.location
                    // p.sendMessage(String.valueOf(sv.getX() + ", " + sv.getY() + ", " +
                    // sv.getZ()));
                    btl = p.location

                    if (i == 170 ||
                        p.gameMode == GameMode.SPECTATOR ||
                        !getPlayerData(p)!!.isInMatch ||
                        getPlayerData(
                            p,
                        )!!.isDead
                    ) {
                        if (`as`.passengers.contains(p)) `as`.removePassenger(p)
                        `as`.remove()
                        leader.remove()
                        (p as CraftPlayer).handle.stopRiding()

                        for (target in plugin.server.onlinePlayers) {
                            if (p.world === target.world) {
                                (target as CraftPlayer)
                                    .handle
                                    .playerConnection
                                    .sendPacket(PacketPlayOutEntityDestroy(id))
                            }
                        }
                        p.inventory.clear()
                        if (p.world === ol.world && p.gameMode != GameMode.SPECTATOR) {
                            if (p.location.distanceSquared(ol) > 9) { // 3^2
                                SuperJumpMgr.superJumpRunnable(p, ol)
                                p.world.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
                            } else {
                                p.world.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
                                val v = Vector(0, 1, 0)
                                p.velocity = v
                                p.inventory.clear()
                                WeaponClassMgr.setWeaponClass(p)
                            }
                        }
                        getPlayerData(p)!!.isUsingJetPack = false
                        getPlayerData(p)!!.isUsingSP = false
                        bsObject.remove()
                        p.flySpeed = 0.1f
                        getPlayerData(player)!!.canUseSubWeapon = true
                        // WeaponClassMgr.setWeaponClass(p);
                        cancel()
                        return
                    }

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    @JvmStatic
    fun shootJetPack(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var pVec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var blockCheck: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            player.world.playSound(
                                player.location,
                                Sound.ENTITY_PLAYER_ATTACK_STRONG,
                                1.5f,
                                1.2f,
                            )
                            p.world.playSound(p.location, Sound.ENTITY_WITHER_SHOOT, 0.2f, 2f)
                            pVec = p.eyeLocation.direction
                            val bom = ItemStack(getPlayerData(p)!!.team!!.teamColor!!.wool!!).clone()
                            val bomM = bom.itemMeta
                            bomM!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.itemMeta = bomM
                            val dl = p.eyeLocation.add(pVec!!.clone().multiply(1.5))
                            drop = p.world.dropItem(dl, bom)
                            drop!!.velocity = pVec!!.multiply(1.5)
                            drop!!.setGravity(false)
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.world.spawnEntity(dl, EntityType.SNOWBALL) as Snowball
                            ball!!.shooter = p
                            ball!!.setGravity(false)
                            ball!!.velocity = Vector(0, 0, 0)
                            ball!!.customName = "JetPack"
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.server.onlinePlayers) {
                                val connection = (o_player as CraftPlayer).handle.playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.entityId))
                            }
                            pVec = p.eyeLocation.direction
                        }

                        if (!drop!!.isOnGround &&
                            !(
                                drop!!.velocity.getX() == 0.0 &&
                                    drop!!
                                        .velocity
                                        .getZ() != 0.0
                            ) &&
                            !(
                                drop!!.velocity.getX() != 0.0 &&
                                    drop!!
                                        .velocity
                                        .getZ() == 0.0
                            )
                        ) {
                            ball!!.velocity = drop!!.velocity
                        }

                        for (target in plugin.server.onlinePlayers) {
                            if (!getPlayerData(target)!!.settings!!.showEffectSPWeapon()) {
                                continue
                            }
                            if (target.world === ball!!.world) {
                                if (target
                                        .location
                                        .distanceSquared(ball!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team
                                            ?.let { it.teamColor!! }!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        ball!!.location,
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0,
                                        bd,
                                    )
                                }
                            }

                            // ボムの視認用エフェクト
                            if (target.world === drop!!.location.world) {
                                if (target
                                        .location
                                        .distanceSquared(drop!!.location) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            getPlayerData(p)!!.team?.let { it.teamColor!! }!!.bukkitColor!!,
                                            1f,
                                        )
                                    target.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        drop!!.location,
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        50.0,
                                        dustOptions,
                                    )
                                }
                            }
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround) {
                            // 半径

                            val maxDist = 4.0

                            // 爆発音
                            player.world.playSound(drop!!.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.location, 3.0, 25, player)

                            // 塗る
                            var i = 0
                            while (i <= maxDist) {
                                val pLocs: MutableList<Location> = getSphere(drop!!.location, i.toDouble(), 20)
                                for (loc in pLocs) {
                                    PaintMgr.paint(loc, p, false)
                                }
                                i++
                            }

                            // 攻撃判定の処理
                            for (target in plugin.server.onlinePlayers) {
                                if (!getPlayerData(target)!!.isInMatch || target.world !== p.world) continue
                                if (target.location.distanceSquared(drop!!.location) <= 12.25) { // 3.5^2
                                    val damage = (3.5 - target.location.distance(drop!!.location)) * 10
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
                                if (`as` is ArmorStand &&
                                    `as`.location.distanceSquared(drop!!.location) <= 12.25 // 3.5^2
                                ) {
                                    val damage = (3.5 - `as`.location.distance(drop!!.location)) * 10
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ちょっと上の方に移動
//                    /*
//                     * //ボムの視認用エフェクト for (Player o_player :
//                     * Main.getPlugin().getServer().getOnlinePlayers()) {
//                     * if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_SPWeapon()){
//                     * if(o_player.getWorld() == drop.getLocation().getWorld()) { if
//                     * (o_player.getLocation().distanceSquared(drop.getLocation()) <
//                     * Main.PARTICLE_RENDER_DISTANCE_SQUARED) { Particle.DustOptions dustOptions =
//                     * new Particle.DustOptions(DataMgr.getPlayerData(p).getTeam().getTeamColor().
//                     * getBukkitColor(), 1); o_player.spawnParticle(Particle.REDSTONE,
//                     * drop.getLocation(), 1, 0, 0, 0, 50, dustOptions); } } } }
//                     *
//                     */
                        c++
                        x = drop!!.location.x
                        z = drop!!.location.z

                        if (c > 20) {
                            drop!!.remove()
                            ball!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
                        sclatLogger.warn(e.message)
                    }
                }
            }
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            task.runTaskTimer(plugin, 0, 1)
        }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.canUseSubWeapon = true
                }
            }
        cooltime.runTaskLater(plugin, 20)
    }
}
