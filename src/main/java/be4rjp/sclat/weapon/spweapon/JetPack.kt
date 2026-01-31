package be4rjp.sclat.weapon.spweapon

import be4rjp.blockstudio.BlockStudio
import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.SuperJumpMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
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
import org.bukkit.util.Consumer
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object JetPack {
    @JvmStatic
    fun JetPackRunnable(player: Player) {
        val api = BlockStudio.getBlockStudioAPI()
        val objectData = api.getObjectData("jetpack")
        val bsObject =
            api.createObjectFromObjectData(
                notDuplicateNumber.toString(),
                player.getLocation(),
                objectData,
                40.0,
                false,
            )
        bsObject.startTaskAsync(40)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var ol: Location = getPlayerData(player)!!.playerGroundLocation
                var i: Int = 0
                var id: Int = 0
                var btl: Location = player.getLocation()
                var `as`: ArmorStand =
                    player
                        .getWorld()
                        .spawn<ArmorStand>(
                            player.getLocation(),
                            ArmorStand::class.java,
                            Consumer { armorStand: ArmorStand ->
                                armorStand.setSmall(true)
                                armorStand.setGravity(false)
                                armorStand.setVisible(false)
                                armorStand.setBasePlate(false)
                                armorStand.setMarker(true)
                            },
                        )
                var leader: ArmorStand =
                    player
                        .getWorld()
                        .spawn<ArmorStand>(
                            player.getLocation(),
                            ArmorStand::class.java,
                            Consumer { armorStand: ArmorStand ->
                                armorStand.setSmall(true)
                                armorStand.setVisible(false)
                                armorStand.setBasePlate(false)
                            },
                        )
                var list: MutableList<ArmorStand?> = ArrayList<ArmorStand?>()

                var vehicleVector: Vector = Vector(0, 0, 0)

                override fun run() {
                    p.setSprinting(true)

                    var onBlock = false
                    var yh = 1
                    var y = p.getLocation().getBlockY()
                    while (y >= 1 && y >= p.getLocation().getBlockY() - 7) {
                        val bl =
                            Location(
                                p.getLocation().getWorld(),
                                p.getLocation().getX(),
                                y.toDouble(),
                                p.getLocation().getZ(),
                            )
                        if (bl.getBlock().getType() != Material.AIR && bl.getBlock().getType() != Material.WATER) {
                            onBlock = true
                            break
                        }
                        yh++
                        y--
                    }

                    p.setAllowFlight(true)
                    p.setFlying(true)

                    var vec = Vector(0, 0, 0)
                    if (i % 2 == 0) vec = getPlayerData(p)!!.vehicleVector.clone().multiply(0.8)
                    val pvec = p.getEyeLocation().getDirection()
                    val w_WASDVector = (Vector(pvec.getX(), 0.0, pvec.getZ())).multiply(vec.getX())
                    val d_WASDVector = (Vector(pvec.getZ(), 0.0, pvec.getX() * -1)).multiply(vec.getZ())
                    val xzVector = w_WASDVector.add(d_WASDVector)
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
                    leader.setVelocity(vehicleVector)

                    // as.teleport(as.getLocation().add(vehicleVector));
                    if (!`as`.getPassengers().contains(p)) {
                        `as`.addPassenger(p)
                    }

                    // p.setWalkSpeed(0.1F);
                    // p.setFlySpeed(0.02F);
                    val leaderEyeLoc = leader.getEyeLocation()
                    val leaderLoc = leader.getLocation().add(0.0, -0.3, 0.0)
                    leaderEyeLoc.setYaw(p.getEyeLocation().getYaw())
                    // as.teleport(leaderLoc);
                    (`as` as CraftArmorStand).getHandle().setPositionRotation(
                        leaderLoc.getX(),
                        leaderLoc.getY(),
                        leaderLoc.getZ(),
                        p.getLocation().getYaw(),
                        0f,
                    )

                    // move object
                    val pv = p.getEyeLocation().getDirection()
                    val direction = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                    val locPlus = direction.clone().multiply(-0.2)
                    bsObject.setBaseLocation(leaderLoc.clone().add(locPlus.getX(), 0.5, locPlus.getZ()))
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

                    PaintMgr.PaintHightestBlock(loc2, player, false, true)
                    PaintMgr.PaintHightestBlock(loc3, player, false, true)

                    if (i != 0) {
                        // effect
                        // org.bukkit.block.data.BlockData bd =
                        // DataMgr.getPlayerData(player).getTeam().getTeamColor().wool.createBlockData();
                        val position = loc2.clone().add(0.0, -0.2, 0.0)
                        val position2 = loc3.clone().add(0.0, -0.2, 0.0)
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(o_player)!!.settings.ShowEffect_SPWeapon()) continue
                            if (o_player.getWorld() === position.getWorld()) {
                                if (o_player
                                        .getLocation()
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
                                            ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!),
                                        )
                                        // o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 0, 0, -2, 0,
                                        // 10, bd);
                                    }
                                }
                                if (o_player
                                        .getLocation()
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
                                            ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!),
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
                        getPlayerData(p)!!.setIsUsingSP(true)
                        SPWeaponMgr.setSPCoolTimeAnimation(player, 175)
                        getPlayerData(p)!!.setIsUsingJetPack(true)

                        p.getInventory().clear()

                        val item = ItemStack(Material.QUARTZ)
                        val meta = item.getItemMeta()
                        meta!!.setDisplayName("右クリックで弾を発射")
                        item.setItemMeta(meta)
                        for (count in 0..8) {
                            player.getInventory().setItem(count, item)
                        }
                        player.updateInventory()
                        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 176, 1))
                        SuperArmor.setArmor(player, 1.0, 175, false)

                        val nmsWorld = (p.getWorld() as CraftWorld).getHandle()
                        val `as` = EntityArmorStand(nmsWorld, ol.getX(), ol.getY(), ol.getZ())
                        `as`.setPosition(ol.getX(), ol.getY(), ol.getZ())
                        `as`.setInvisible(true)
                        `as`.setNoGravity(true)
                        `as`.setBasePlate(false)
                        `as`.setCustomName(
                            CraftChatMessage.fromStringOrNull(
                                getPlayerData(p)!!.team.teamColor!!.colorCode + "↓↓↓  くコ:彡  ↓↓↓",
                            ),
                        )
                        `as`.setCustomNameVisible(true)
                        `as`.setSmall(true)
                        id = `as`.getBukkitEntity().getEntityId()
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (p.getWorld() === target.getWorld()) {
                                (target as CraftPlayer)
                                    .getHandle()
                                    .playerConnection
                                    .sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
                            }
                        }
                    }

                    val atl = p.getLocation()
                    // p.sendMessage(String.valueOf(sv.getX() + ", " + sv.getY() + ", " +
                    // sv.getZ()));
                    btl = p.getLocation()

                    if (i == 170 || p.getGameMode() == GameMode.SPECTATOR || !getPlayerData(p)!!.isInMatch() ||
                        getPlayerData(
                            p,
                        )!!.getIsDead()
                    ) {
                        if (`as`.getPassengers().contains(p)) `as`.removePassenger(p)
                        `as`.remove()
                        leader.remove()
                        (p as CraftPlayer).getHandle().stopRiding()

                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (p.getWorld() === target.getWorld()) {
                                (target as CraftPlayer)
                                    .getHandle()
                                    .playerConnection
                                    .sendPacket(PacketPlayOutEntityDestroy(id))
                            }
                        }
                        p.getInventory().clear()
                        if (p.getWorld() === ol.getWorld() && p.getGameMode() != GameMode.SPECTATOR) {
                            if (p.getLocation().distanceSquared(ol) > 9 /* 3^2 */) {
                                SuperJumpMgr.SuperJumpRunnable(p, ol)
                                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
                            } else {
                                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2f, 1.3f)
                                val v = Vector(0, 1, 0)
                                p.setVelocity(v)
                                p.getInventory().clear()
                                WeaponClassMgr.setWeaponClass(p)
                            }
                        }
                        getPlayerData(p)!!.setIsUsingJetPack(false)
                        getPlayerData(p)!!.setIsUsingSP(false)
                        bsObject.remove()
                        p.setFlySpeed(0.1f)
                        getPlayerData(player)!!.setCanUseSubWeapon(true)
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
    fun ShootJetPack(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            player.getWorld().playSound(
                                player.getLocation(),
                                Sound.ENTITY_PLAYER_ATTACK_STRONG,
                                1.5f,
                                1.2f,
                            )
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.2f, 2f)
                            p_vec = p.getEyeLocation().getDirection()
                            val bom = ItemStack(getPlayerData(p)!!.team.teamColor!!.wool!!).clone()
                            val bom_m = bom.getItemMeta()
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.setItemMeta(bom_m)
                            val dl = p.getEyeLocation().add(p_vec!!.clone().multiply(1.5))
                            drop = p.getWorld().dropItem(dl, bom)
                            drop!!.setVelocity(p_vec!!.multiply(1.5))
                            drop!!.setGravity(false)
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.getWorld().spawnEntity(dl, EntityType.SNOWBALL) as Snowball
                            ball!!.setShooter(p)
                            ball!!.setGravity(false)
                            ball!!.setVelocity(Vector(0, 0, 0))
                            ball!!.setCustomName("JetPack")
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                val connection = (o_player as CraftPlayer).getHandle().playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                            }
                            p_vec = p.getEyeLocation().getDirection()
                        }

                        if (!drop!!.isOnGround() &&
                            !(
                                drop!!.getVelocity().getX() == 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() != 0.0
                                ) &&
                            !(
                                drop!!.getVelocity().getX() != 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() == 0.0
                                )
                        ) {
                            ball!!.setVelocity(drop!!.getVelocity())
                        }

                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.settings.ShowEffect_SPWeapon()) {
                                continue
                            }
                            if (target.getWorld() === ball!!.getWorld()) {
                                if (target
                                        .getLocation()
                                        .distanceSquared(ball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team.teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        ball!!.getLocation(),
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
                            if (target.getWorld() === drop!!.getLocation().getWorld()) {
                                if (target
                                        .getLocation()
                                        .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val dustOptions =
                                        Particle.DustOptions(
                                            getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                            1f,
                                        )
                                    target.spawnParticle<Particle.DustOptions?>(
                                        Particle.REDSTONE,
                                        drop!!.getLocation(),
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

                        if (getSnowballIsHit(ball) || drop!!.isOnGround()) {
                            // 半径

                            val maxDist = 4.0

                            // 爆発音
                            player.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(drop!!.getLocation(), 3.0, 25, player)

                            // 塗る
                            var i = 0
                            while (i <= maxDist) {
                                val p_locs: MutableList<Location> = getSphere(drop!!.getLocation(), i.toDouble(), 20)
                                for (loc in p_locs) {
                                    PaintMgr.Paint(loc, p, false)
                                }
                                i++
                            }

                            // 攻撃判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== p.getWorld()) continue
                                if (target.getLocation().distanceSquared(drop!!.getLocation()) <= 12.25 /* 3.5^2 */) {
                                    val damage = (3.5 - target.getLocation().distance(drop!!.getLocation())) * 10
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
                                if (`as` is ArmorStand &&
                                    `as`.getLocation().distanceSquared(drop!!.getLocation()) <= 12.25 // 3.5^2
                                ) {
                                    val damage = (3.5 - `as`.getLocation().distance(drop!!.getLocation())) * 10
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                }
                            }
                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ちょっと上の方に移動
                    /*
                     * //ボムの視認用エフェクト for (Player o_player :
                     * Main.getPlugin().getServer().getOnlinePlayers()) {
                     * if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_SPWeapon()){
                     * if(o_player.getWorld() == drop.getLocation().getWorld()) { if
                     * (o_player.getLocation().distanceSquared(drop.getLocation()) <
                     * Main.PARTICLE_RENDER_DISTANCE_SQUARED) { Particle.DustOptions dustOptions =
                     * new Particle.DustOptions(DataMgr.getPlayerData(p).getTeam().getTeamColor().
                     * getBukkitColor(), 1); o_player.spawnParticle(Particle.REDSTONE,
                     * drop.getLocation(), 1, 0, 0, 0, 50, dustOptions); } } } }
                     *
                     */
                        c++
                        x = drop!!.getLocation().getX()
                        z = drop!!.getLocation().getZ()

                        if (c > 20) {
                            drop!!.remove()
                            ball!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        drop!!.remove()
                        cancel()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            task.runTaskTimer(plugin, 0, 1)
        }

        val cooltime: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    getPlayerData(player)!!.setCanUseSubWeapon(true)
                }
            }
        cooltime.runTaskLater(plugin, 20)
    }
}
