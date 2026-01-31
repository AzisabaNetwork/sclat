package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import be4rjp.sclat.data.DataMgr.setKasaDataWithPlayer
import be4rjp.sclat.data.KasaData
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

object Funnel {
    var hashPlayer: HashMap<ArmorStand?, Player?> = HashMap()
    var hashArmorstand: HashMap<ArmorStand?, ArmorStand?> = HashMap()
    var funnelMaxHP: Int = 10
    var funnelMaxHP2: Int = 3
    var funnelSpeed: Double = 1.0

    fun funnelShot(
        player: Player,
        funnel: ArmorStand,
        taegetloc: Location,
    ) {
        val damage = 3.0
        val funloc = funnel.eyeLocation
        if (player.gameMode == GameMode.SPECTATOR) return
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        val rayTrace =
            RayTrace(
                funloc.toVector(),
                Vector(
                    taegetloc.x - funloc.x,
                    taegetloc.y - funloc.y,
                    taegetloc.z - funloc.z,
                ).normalize(),
            )
        val positions = rayTrace.traverse(4.0, 0.2)

        loop@ for (vector in positions) {
            val position = vector.toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                break
            }
            for (target in plugin.server.onlinePlayers) {
                if (getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) {
                    if (target.world === position.world) {
                        if (target.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            target.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 4.0 // 2*2
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(target as Entity), 4.0, 0.05)) {
                            giveDamage(player, target, damage, "killed")
                            player.playSound(player.location, Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var p: Player = target

                                    override fun run() {
                                        target.noDamageTicks = 0
                                    }
                                }
                            task.runTaskLater(plugin, 1)
                            break@loop
                        }
                    }
                }
            }

            for (`as` in player.world.entities) {
                if (`as` is ArmorStand) {
                    if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(`as` as Entity), 4.0, 0.05)) {
                            if (`as`.customName != null) {
                                if (`as`.customName == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .world
                                            .playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.customName == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .world
                                            .playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.customName!!)) {
                                        if (`as`.customName != "21" &&
                                            `as`.customName != "100"
                                        ) {
                                            if (`as`.isVisible) {
                                                player.playSound(
                                                    player.location,
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                            }
                                        }
                                    }
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    break@loop
                                }
                            }
                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun funnelFloat(player: Player) {
        val kdata = KasaData(player)
        val kdata1 = KasaData(player)
        val kdata2 = KasaData(player)
        setKasaDataWithPlayer(player, kdata)
        setKasaDataWithPlayer(player, kdata1)
        setKasaDataWithPlayer(player, kdata2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                var data: PlayerData? = getPlayerData(p)
                var i: Int = 0

                var list: MutableList<ArmorStand> = ArrayList()
                var list1: MutableList<ArmorStand> = ArrayList()
                var list2: MutableList<ArmorStand> = ArrayList()
                var list5: MutableList<MutableList<ArmorStand>> = mutableListOf()
                var list6: MutableList<ArmorStand> = mutableListOf()

                lateinit var as1: ArmorStand
                lateinit var as2: ArmorStand
                lateinit var as3: ArmorStand

                lateinit var as11: ArmorStand
                lateinit var as12: ArmorStand
                lateinit var as13: ArmorStand

                lateinit var as21: ArmorStand
                lateinit var as22: ArmorStand
                lateinit var as23: ArmorStand
                lateinit var las: ArmorStand
                var check: Boolean = false
                var kdataReset: Int = -1
                var kdataReset1: Int = -1
                var kdataReset2: Int = -1

                override fun run() {
                    try {
                        // Location loc = p.getLocation().add(0, -1.7, 0);
                        val locp = p.location
                        var pv = p.eyeLocation.direction.normalize()
                        val vec = Vector(pv.getX(), 0.0, pv.getZ()).normalize()
                        var vec1: Vector?
                        var vec2: Vector?
                        val l1 = Vector(vec.clone().getZ() * -1, 0.0, vec.clone().getX())
                        val r1 = Vector(vec.clone().getZ(), 0.0, vec.clone().getX() * -1)
                        val taskcheck: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    check = true
                                }
                            }
                        val listremove: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    try {
                                        hashPlayer.remove(list[0])
                                        hashArmorstand.remove(list[0])
                                        data!!.subArmorlist(list[0])
                                        for (`as` in list) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list.clear()
                                    as3 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 2.5, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as1 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 2.8, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as2 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 2.8, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as1.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
                                    as2.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))
                                    list.add(as3)
                                    list.add(as1)
                                    list.add(as2)
                                    data!!.setArmorlist(as3)
                                    GlowingAPI.setGlowing(as3, player, false)
                                    for (`as` in list) {
                                        `as`.isSmall = true
                                        `as`.setBasePlate(false)
                                        `as`.isVisible = false
                                        `as`.setGravity(false)
                                        `as`.customName = "Kasa"
                                        setKasaDataWithARmorStand(`as`, kdata)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.server.onlinePlayers) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list[2].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list[1].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list[0].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(ItemStack(team.teamColor!!.wool!!)),
                                            ),
                                        )
                                    }
                                    list6.add(as3)
                                    kdata.damage = 0.0
                                    kdata.armorStandList = list
                                    cancel()
                                }
                            }
                        val listremove1: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    try {
                                        hashPlayer.remove(list1[0])
                                        hashArmorstand.remove(list1[0])
                                        data!!.subArmorlist(list1[0])
                                        for (`as` in list1) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list1.clear()
                                    as13 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as11 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as12 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as11.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
                                    as12.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))
                                    list1.add(as13)
                                    list1.add(as11)
                                    list1.add(as12)
                                    data!!.setArmorlist(as13)
                                    GlowingAPI.setGlowing(as13, player, false)
                                    for (`as` in list1) {
                                        `as`.isSmall = true
                                        `as`.setBasePlate(false)
                                        `as`.isVisible = false
                                        `as`.setGravity(false)
                                        `as`.customName = "Kasa"
                                        setKasaDataWithARmorStand(`as`, kdata1)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.server.onlinePlayers) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1[2].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team?.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1[1].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1[0].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(ItemStack(team.teamColor!!.wool!!)),
                                            ),
                                        )
                                    }
                                    list6.add(as13)
                                    kdata1.damage = 0.0
                                    kdata1.armorStandList = list1
                                    cancel()
                                }
                            }
                        val listremove2: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    try {
                                        hashPlayer.remove(list2[0])
                                        hashArmorstand.remove(list2[0])
                                        data!!.subArmorlist(list2[0])
                                        for (`as` in list2) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list2.clear()
                                    as23 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as21 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as22 =
                                        p.world.spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as21.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
                                    as22.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))
                                    list2.add(as23)
                                    list2.add(as21)
                                    list2.add(as22)
                                    data!!.setArmorlist(as23)
                                    GlowingAPI.setGlowing(as23, player, false)
                                    for (`as` in list2) {
                                        `as`.isSmall = true
                                        `as`.setBasePlate(false)
                                        `as`.isVisible = false
                                        `as`.setGravity(false)
                                        `as`.customName = "Kasa"
                                        setKasaDataWithARmorStand(`as`, kdata2)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.server.onlinePlayers) {
                                        (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2[2].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team!!.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2[1].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(
                                                    ItemStack(
                                                        Material.getMaterial(
                                                            team.teamColor!!.glass.toString() + "_PANE",
                                                        )!!,
                                                    ),
                                                ),
                                            ),
                                        )
                                        o_player.handle.playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2[0].entityId,
                                                EnumItemSlot.HEAD,
                                                CraftItemStack.asNMSCopy(ItemStack(team.teamColor!!.wool!!)),
                                            ),
                                        )
                                    }
                                    list6.add(as23)
                                    kdata2.damage = 0.0
                                    kdata2.armorStandList = list2
                                    cancel()
                                }
                            }
                        if (i == 0) {
                            as3 =
                                p.world.spawnEntity(
                                    locp.clone().add(0.0, 2.5, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            pv = as3.eyeLocation.direction.normalize()
                            vec1 =
                                Vector(
                                    pv.clone().getX() * 0.707 - pv.clone().getZ() * 0.707,
                                    0.0,
                                    pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                ).normalize()
                            vec2 =
                                Vector(
                                    pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                    0.0,
                                    -pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                ).normalize()
                            as1 =
                                p.world.spawnEntity(
                                    locp.clone().add(0.0, 2.8, 0.0).add(vec1.clone().multiply(0.3)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as2 =
                                p.world.spawnEntity(
                                    locp.clone().add(0.0, 2.8, 0.0).add(vec2.clone().multiply(0.3)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list.add(as3)
                            list.add(as1)
                            list.add(as2)
                            as13 =
                                p.world.spawnEntity(
                                    locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as11 =
                                p.world.spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec1.clone().multiply(0.3))
                                        .add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as12 =
                                p.world.spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec2.clone().multiply(0.3))
                                        .add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list1.add(as13)
                            list1.add(as11)
                            list1.add(as12)
                            as23 =
                                p.world.spawnEntity(
                                    locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as21 =
                                p.world.spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec1.clone().multiply(0.3))
                                        .add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as22 =
                                p.world.spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec2.clone().multiply(0.3))
                                        .add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list2.add(as23)
                            list2.add(as21)
                            list2.add(as22)

                            list5.add(list)
                            list5.add(list1)
                            list5.add(list2)
                            data!!.setArmorlist(as3)
                            data!!.setArmorlist(as13)
                            data!!.setArmorlist(as23)
                            list6.add(as3)
                            list6.add(as13)
                            list6.add(as23)
                            kdata.armorStandList = list
                            kdata1.armorStandList = list1
                            kdata2.armorStandList = list2
                            kdata.damage = 0.0
                            kdata1.damage = 0.0
                            kdata2.damage = 0.0
                            for (`as` in list) {
                                setKasaDataWithARmorStand(`as`, kdata)
                            }
                            for (`as` in list1) {
                                setKasaDataWithARmorStand(`as`, kdata1)
                            }
                            for (`as` in list2) {
                                setKasaDataWithARmorStand(`as`, kdata2)
                            }
                            for (aslist in list5) {
                                aslist[1].headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
                                aslist[2].headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))
                                for (`as` in aslist) {
                                    `as`.isSmall = true
                                    `as`.setBasePlate(false)
                                    `as`.isVisible = false
                                    `as`.setGravity(false)
                                    `as`.customName = "Kasa"
                                }
                            }
                            val team = data!!.team
                            for (o_player in plugin.server.onlinePlayers) {
                                for (aslist in list5) {
                                    (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist[1].entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(
                                                ItemStack(
                                                    Material.getMaterial(
                                                        team!!.teamColor!!.glass.toString() + "_PANE",
                                                    )!!,
                                                ),
                                            ),
                                        ),
                                    )
                                    o_player.handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist[2].entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(
                                                ItemStack(
                                                    Material.getMaterial(
                                                        team.teamColor!!.glass.toString() + "_PANE",
                                                    )!!,
                                                ),
                                            ),
                                        ),
                                    )
                                    o_player.handle.playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist[0].entityId,
                                            EnumItemSlot.HEAD,
                                            CraftItemStack.asNMSCopy(ItemStack(team.teamColor!!.wool!!)),
                                        ),
                                    )
                                }
                            }
                            taskcheck.runTaskLater(plugin, 20)
                        }
                        if (i >= 0) {
                            // ファンネル破壊時の復活処理
                            if (p.gameMode == GameMode.SPECTATOR) {
                                if (kdata.damage <= funnelMaxHP) {
                                    kdata.damage = 1024.0
                                }
                                if (kdata1.damage <= funnelMaxHP) {
                                    kdata1.damage = 1024.0
                                }
                                if (kdata2.damage <= funnelMaxHP) {
                                    kdata2.damage = 1024.0
                                }
                            }
                            if (kdata.damage > funnelMaxHP && kdata.damage < 9999) {
                                val kasaStand = kdata.armorStandList[0]
                                data!!.subArmorlist(kasaStand)
                                if (hashPlayer.containsKey(kasaStand)) {
                                    if (hashPlayer.get(kasaStand)!!.gameMode != GameMode.SPECTATOR) {
                                        kdataReset += 60
                                    }
                                    hashPlayer.remove(kasaStand)
                                } else if (hashArmorstand.containsKey(kasaStand)) {
                                    kdataReset += 60
                                    hashArmorstand.remove(kasaStand)
                                } else {
                                    list6.remove(kasaStand)
                                    if (kdata.damage == 1024.0) {
                                        listremove.runTaskLater(plugin, 110)
                                    } else {
                                        listremove.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata.damage = 10000.0
                                for (`as` in kdata.armorStandList) {
                                    `as`.remove()
                                }
                            }
                            if (kdata1.damage > funnelMaxHP && kdata1.damage < 9999) {
                                val kasaStand1 = kdata1.armorStandList[0]
                                data!!.subArmorlist(kasaStand1)
                                if (hashPlayer.containsKey(kasaStand1)) {
                                    if (hashPlayer.get(kasaStand1)!!.gameMode != GameMode.SPECTATOR) {
                                        kdataReset1 += 60
                                    }
                                    hashPlayer.remove(kasaStand1)
                                } else if (hashArmorstand.containsKey(kasaStand1)) {
                                    kdataReset1 += 60
                                    hashArmorstand.remove(kasaStand1)
                                } else {
                                    list6.remove(kasaStand1)
                                    if (kdata1.damage == 1024.0) {
                                        listremove1.runTaskLater(plugin, 110)
                                    } else {
                                        listremove1.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata1.damage = 10000.0
                                for (`as` in kdata1.armorStandList) {
                                    `as`.remove()
                                }
                            }
                            if (kdata2.damage > funnelMaxHP && kdata2.damage < 9999) {
                                val kasaStand2 = kdata2.armorStandList[0]
                                data!!.subArmorlist(kasaStand2)
                                if (hashPlayer.containsKey(kasaStand2)) {
                                    if (hashPlayer.get(kasaStand2)!!.gameMode != GameMode.SPECTATOR) {
                                        kdataReset2 += 60
                                    }
                                    hashPlayer.remove(kasaStand2)
                                } else if (hashArmorstand.containsKey(kasaStand2)) {
                                    kdataReset2 += 60
                                    hashArmorstand.remove(kasaStand2)
                                } else {
                                    list6.remove(kasaStand2)
                                    if (kdata2.damage == 1024.0) {
                                        listremove2.runTaskLater(plugin, 110)
                                    } else {
                                        listremove2.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata2.damage = 10000.0
                                for (`as` in kdata2.armorStandList) {
                                    `as`.remove()
                                }
                            }
                            if (i == kdataReset) {
                                listremove.runTaskLater(plugin, 1)
                                kdataReset = -1
                            }
                            if (i == kdataReset1) {
                                listremove1.runTaskLater(plugin, 1)
                                kdataReset1 = -1
                            }
                            if (i == kdataReset2) {
                                listremove2.runTaskLater(plugin, 1)
                                kdataReset2 = -1
                            }
                            // ファンネル破壊時の復活処理了
                            pv =
                                Vector(
                                    p
                                        .eyeLocation
                                        .direction
                                        .normalize()
                                        .getX(),
                                    0.0,
                                    p
                                        .eyeLocation
                                        .direction
                                        .normalize()
                                        .getZ(),
                                )
                            var io = 0
                            for (aslist in list5) {
                                val aslistget0: ArmorStand = aslist[0]
                                if (io == 0) {
                                    if (!hashPlayer.containsKey(aslistget0) &&
                                        !hashArmorstand.containsKey(aslistget0)
                                    ) {
                                        aslistget0.teleport(locp.clone().add(0.0, 2.5, 0.0))
                                    } else if (hashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashPlayer
                                                .get(aslistget0)!!
                                                .location
                                                .add(pv.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 0) {
                                            if (!getPlayerData(hashPlayer.get(aslistget0))!!.isUsingSP) {
                                                funnelShot(
                                                    p,
                                                    aslistget0,
                                                    hashPlayer.get(aslistget0)!!.eyeLocation,
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                hashPlayer
                                                    .get(aslistget0)!!
                                                    .gameMode == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        hashPlayer.get(aslistget0),
                                                    )!!.isInMatch ||
                                                    !hashPlayer.get(aslistget0)!!.isOnline
                                            ) &&
                                            kdata.damage < funnelMaxHP
                                        ) {
                                            kdata.damage = (funnelMaxHP + 1).toDouble()
                                            kdataReset = i + 3
                                        }
                                    } else if (hashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashArmorstand
                                                .get(aslistget0)!!
                                                .location
                                                .add(pv.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 0) {
                                            funnelShot(
                                                p,
                                                aslistget0,
                                                hashArmorstand.get(aslistget0)!!.eyeLocation,
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!hashArmorstand.get(aslistget0)!!.isVisible) {
                                            kdataReset = i + 3
                                            kdata.damage = (funnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    if (i % 20 == 0) {
                                        val team = data!!.team
                                        for (o_player in plugin.server.onlinePlayers) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[2].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[1].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[0].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(team.teamColor!!.wool!!),
                                                    ),
                                                ),
                                            )
                                        }
                                    }
                                }
                                if (io == 1) {
                                    if (!hashPlayer.containsKey(aslistget0) &&
                                        !hashArmorstand.containsKey(aslistget0)
                                    ) {
                                        aslistget0.teleport(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                        )
                                    } else if (hashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashPlayer
                                                .get(aslistget0)!!
                                                .location
                                                .add(l1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 16) {
                                            if (!getPlayerData(hashPlayer.get(aslistget0))!!.isUsingSP) {
                                                funnelShot(
                                                    p,
                                                    aslistget0,
                                                    hashPlayer.get(aslistget0)!!.eyeLocation,
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                hashPlayer
                                                    .get(aslistget0)!!
                                                    .gameMode == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        hashPlayer.get(aslistget0),
                                                    )!!.isInMatch ||
                                                    !hashPlayer.get(aslistget0)!!.isOnline
                                            ) &&
                                            kdata1.damage < funnelMaxHP
                                        ) {
                                            kdataReset1 = i + 3
                                            kdata1.damage = (funnelMaxHP + 1).toDouble()
                                        }
                                    } else if (hashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashArmorstand
                                                .get(aslistget0)!!
                                                .location
                                                .add(l1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 16) {
                                            funnelShot(
                                                p,
                                                aslistget0,
                                                hashArmorstand.get(aslist[0])!!.eyeLocation,
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!hashArmorstand.get(aslistget0)!!.isVisible) {
                                            kdataReset1 = i + 3
                                            kdata1.damage = (funnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    if (i % 20 == 0) {
                                        val team = data!!.team
                                        for (o_player in plugin.server.onlinePlayers) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[2].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[1].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[0].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(team.teamColor!!.wool!!),
                                                    ),
                                                ),
                                            )
                                        }
                                    }
                                }
                                if (io == 2) {
                                    if (!hashPlayer.containsKey(aslistget0) &&
                                        !hashArmorstand.containsKey(aslist[0])
                                    ) {
                                        aslistget0.teleport(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                        )
                                    } else if (hashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashPlayer
                                                .get(aslistget0)!!
                                                .location
                                                .add(r1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 32) {
                                            if (!getPlayerData(hashPlayer.get(aslistget0))!!.isUsingSP) {
                                                funnelShot(
                                                    p,
                                                    aslistget0,
                                                    hashPlayer.get(aslistget0)!!.eyeLocation,
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                hashPlayer
                                                    .get(aslistget0)!!
                                                    .gameMode == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        hashPlayer.get(aslistget0),
                                                    )!!.isInMatch ||
                                                    !hashPlayer.get(aslistget0)!!.isOnline
                                            ) &&
                                            kdata2.damage < funnelMaxHP
                                        ) {
                                            kdataReset2 = i + 3
                                            kdata2.damage = (funnelMaxHP + 1).toDouble()
                                        }
                                    } else if (hashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.location
                                        val lpl =
                                            hashArmorstand
                                                .get(aslistget0)!!
                                                .location
                                                .add(r1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.x - las.x,
                                                lpl.y - las.y,
                                                lpl.z - las.z,
                                            )
                                        if (i % 48 == 32) {
                                            funnelShot(
                                                p,
                                                aslistget0,
                                                hashArmorstand.get(aslistget0)!!.eyeLocation,
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.velocity = pv.normalize().multiply(funnelSpeed)
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!hashArmorstand.get(aslistget0)!!.isVisible) {
                                            kdataReset2 = i + 3
                                            kdata2.damage = (funnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    // 残数表記
                                    if (i % 20 == 0) {
                                        if (p.gameMode != GameMode.SPECTATOR) {
                                            val funnelamo = funnelamount(player)
                                            val nuget: ItemStack?
                                            nuget =
                                                if (funnelamo > 0) {
                                                    ItemStack(Material.GOLD_NUGGET, funnelamo)
                                                } else {
                                                    ItemStack(Material.AIR)
                                                }
                                            player.inventory.setItem(8, nuget)
                                        }
                                        // 残数表記了
                                        val team = data!!.team
                                        for (o_player in plugin.server.onlinePlayers) {
                                            (o_player as CraftPlayer).handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[2].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team!!.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[1].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(
                                                            Material.getMaterial(
                                                                team.teamColor!!.glass.toString() + "_PANE",
                                                            )!!,
                                                        ),
                                                    ),
                                                ),
                                            )
                                            o_player.handle.playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist[0].entityId,
                                                    EnumItemSlot.HEAD,
                                                    CraftItemStack.asNMSCopy(
                                                        ItemStack(team.teamColor!!.wool!!),
                                                    ),
                                                ),
                                            )
                                        }
                                    }
                                }
                                io++
                            }
                            for (aslist in list5) {
                                pv =
                                    aslist[0]
                                        .eyeLocation
                                        .direction
                                        .normalize()
                                vec1 =
                                    Vector(
                                        pv.clone().getX() * 0.707 - pv.clone().getZ() * 0.707,
                                        0.0,
                                        pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                    ).normalize()
                                vec2 =
                                    Vector(
                                        pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                        0.0,
                                        -pv.clone().getX() * 0.707 + pv.clone().getZ() * 0.707,
                                    ).normalize()
                                val floc2 = aslist[0].location.clone()
                                aslist[1].teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec1.clone().multiply(0.3)))
                                aslist[2].teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec2.clone().multiply(0.3)))
                            }
                        }
                        if (check && p.isSneaking && p.gameMode != GameMode.SPECTATOR) {
                            check = false
                            taskcheck.runTaskLater(plugin, 18)
                            player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
                            val rayTrace =
                                RayTrace(
                                    player.eyeLocation.toVector(),
                                    player.eyeLocation.direction,
                                )
                            val positions = rayTrace.traverse(55.0, 0.3)

                            loop@ for (it in positions.indices) {
                                val position = positions[it].toLocation(player.location.world!!)
                                val block = player.location.world!!.getBlockAt(position)

                                if (block.type != Material.AIR) {
                                    break
                                }
                                if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
                                    if (it < 10) {
                                        if (player.world === position.world) {
                                            if (player
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val bd =
                                                    getPlayerData(player)!!
                                                        .team!!
                                                        .teamColor!!
                                                        .wool!!
                                                        .createBlockData()
                                                player.spawnParticle<BlockData?>(
                                                    Particle.BLOCK_DUST,
                                                    position,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    1.0,
                                                    bd,
                                                )
                                            }
                                        }
                                    }
                                }

                                val maxDistSquad = 20.0 // 2*2
                                for (target in plugin.server.onlinePlayers) {
                                    if (!getPlayerData(target)!!.isInMatch) continue
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.gameMode == GameMode.ADVENTURE
                                    ) {
                                        if (target.location.distanceSquared(position) <= maxDistSquad) {
                                            // if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                                            player.world.playSound(
                                                player.location,
                                                Sound.BLOCK_NOTE_BLOCK_BIT,
                                                1.0f,
                                                5f,
                                            )
                                            if (!list6.isEmpty()) {
                                                if (list6[list6.size - 1] == as3 && funAmoP(target)) {
                                                    player.world.playSound(
                                                        target.location,
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    hashPlayer[as3] = target
                                                    GlowingAPI.setGlowing(as3, player, true)
                                                    GlowingAPI.setGlowing(as3, target, true)
                                                    if (kdata.damage < funnelMaxHP2) {
                                                        kdata.damage = funnelMaxHP2.toDouble()
                                                    }
                                                    as3.setGravity(true)
                                                    kdataReset = i + 210
                                                    // listremove.runTaskLater(Main.getPlugin(), 140);
                                                    list6.removeAt(list6.size - 1)
                                                } else if (list6[list6.size - 1] == as13 && funAmoP(target)) {
                                                    player.world.playSound(
                                                        target.location,
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    hashPlayer[as13] = target
                                                    GlowingAPI.setGlowing(as13, player, true)
                                                    GlowingAPI.setGlowing(as13, target, true)
                                                    if (kdata1.damage < funnelMaxHP2) {
                                                        kdata1.damage = funnelMaxHP2.toDouble()
                                                    }
                                                    as13.setGravity(true)
                                                    kdataReset1 = i + 210
                                                    // listremove1.runTaskLater(Main.getPlugin(), 140);
                                                    list6.removeAt(list6.size - 1)
                                                } else if (list6[list6.size - 1] == as23 && funAmoP(target)) {
                                                    player.world.playSound(
                                                        target.location,
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    hashPlayer[as23] = target
                                                    GlowingAPI.setGlowing(as23, player, true)
                                                    GlowingAPI.setGlowing(as23, target, true)
                                                    if (kdata2.damage < funnelMaxHP2) {
                                                        kdata2.damage = funnelMaxHP2.toDouble()
                                                    }
                                                    as23.setGravity(true)
                                                    kdataReset2 = i + 210
                                                    // listremove2.runTaskLater(Main.getPlugin(), 140);
                                                    list6.removeAt(list6.size - 1)
                                                }
                                            }
                                            break@loop
                                            // }
                                        }
                                    }
                                }

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
                                                                // player.playSound(player.getLocation(),
                                                                // Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                                                player.world.playSound(
                                                                    player.location,
                                                                    Sound.BLOCK_NOTE_BLOCK_BIT,
                                                                    1.0f,
                                                                    5f,
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (!list6.isEmpty()) {
                                                        if (list6[list6.size - 1] == as3 &&
                                                            funAmoA(`as`)
                                                        ) {
                                                            player.world.playSound(
                                                                `as`.location,
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            hashArmorstand[as3] = `as`
                                                            GlowingAPI.setGlowing(as3, player, true)
                                                            if (kdata.damage < funnelMaxHP2) {
                                                                kdata.damage = funnelMaxHP2.toDouble()
                                                            }
                                                            as3.setGravity(true)
                                                            kdataReset = i + 210
                                                            // listremove.runTaskLater(Main.getPlugin(), 140);
                                                            list6.removeAt(list6.size - 1)
                                                        } else if (list6[list6.size - 1] == as13 &&
                                                            funAmoA(`as`)
                                                        ) {
                                                            player.world.playSound(
                                                                `as`.location,
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            hashArmorstand[as13] = `as`
                                                            GlowingAPI.setGlowing(as13, player, true)
                                                            if (kdata1.damage < funnelMaxHP2) {
                                                                kdata1.damage = funnelMaxHP2.toDouble()
                                                            }
                                                            as13.setGravity(true)
                                                            kdataReset1 = i + 210
                                                            // listremove1.runTaskLater(Main.getPlugin(), 140);
                                                            list6.removeAt(list6.size - 1)
                                                        } else if (list6[list6.size - 1] == as23 &&
                                                            funAmoA(`as`)
                                                        ) {
                                                            player.world.playSound(
                                                                `as`.location,
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            hashArmorstand[as23] = `as`
                                                            GlowingAPI.setGlowing(as23, player, true)
                                                            if (kdata2.damage < funnelMaxHP2) {
                                                                kdata2.damage = funnelMaxHP2.toDouble()
                                                            }
                                                            as23.setGravity(true)
                                                            kdataReset2 = i + 210
                                                            // listremove2.runTaskLater(Main.getPlugin(), 140);
                                                            list6.removeAt(list6.size - 1)
                                                        }
                                                    }
                                                    break@loop
                                                }
                                            }
                                            // ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                            // }
                                        }
                                    }
                                }
                            }
                        }
                        if (!p.isOnline || !data!!.isInMatch) {
                            if (getPlayerData(p)!!.isInMatch) {
                                as1
                                    .world
                                    .playSound(as1.location, Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                            }

                            for (aslist in list5) {
                                for (`as` in aslist) {
                                    `as`.remove()
                                }
                            }
                            las.remove()
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun funnelPursuit(
        player: Player,
        target: ArmorStand,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (hashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (hashArmorstand.get(getPlayerData(player)!!.getArmorlist(ai)) == target) {
                        rate += 1.5
                    }
                }
            } catch (e: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.location
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd!!.x,
                                    loct.y - locd!!.y + 1.5,
                                    loct.z - locd!!.z,
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions[i].toLocation(p.location.world!!)
                                if (player.world === position.world) {
                                    if (player
                                            .location
                                            .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                data!!.team!!.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            position,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            3.0,
                                            dustOptions,
                                        )
                                    }
                                }
                                for (target in plugin.server.onlinePlayers) {
                                    if (target == p ||
                                        getPlayerData(target)!!.settings!!.showEffectChargerLine()
                                    ) {
                                        if (target.world === p.world) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data!!.team!!.teamColor!!.bukkitColor!!,
                                                        1f,
                                                    )
                                                target.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    position,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    3.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                                i++
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        task.runTaskLater(plugin, 1)
        return rate
    }

    fun funnelPursuitPlayer(
        player: Player,
        target: Player,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (hashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (hashPlayer.get(getPlayerData(player)!!.getArmorlist(ai)) == target) {
                        rate += 1.5
                    }
                }
            } catch (e: Exception) {
                rate -= 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.location
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai)!!.eyeLocation
                            val vec =
                                Vector(
                                    loct.x - locd!!.x,
                                    loct.y - locd!!.y + 1.5,
                                    loct.z - locd!!.z,
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length().toInt().toDouble(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions[i].toLocation(p.location.world!!)
                                if (player.world === position.world) {
                                    if (player
                                            .location
                                            .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                data!!.team!!.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            position,
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            3.0,
                                            dustOptions,
                                        )
                                    }
                                }
                                for (target in plugin.server.onlinePlayers) {
                                    if (target == p ||
                                        getPlayerData(target)!!.settings!!.showEffectChargerLine()
                                    ) {
                                        if (target.world === p.world) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data!!.team!!.teamColor!!.bukkitColor!!,
                                                        1f,
                                                    )
                                                target.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    position,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    3.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                                i++
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        task.runTaskLater(plugin, 1)
        return rate
    }

    fun funnelamount(player: Player?): Int {
        var rate = 3
        for (ai in 0..2) {
            try {
                if (hashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate -= 1
                }
                if (hashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate -= 1
                }
            } catch (e: Exception) {
                rate -= 1
            }
        }
        return rate
    }

    private fun funAmoP(player: Player?): Boolean {
        var count = 0
        for (entry in hashPlayer.entries) {
            if (entry.value === player) {
                count++
            }
        }
        return count < 3
    }

    private fun funAmoA(stand: ArmorStand?): Boolean {
        var count = 0
        for (entry in hashArmorstand.entries) {
            if (entry.value === stand) {
                count++
            }
        }
        return count < 3
    }
}
