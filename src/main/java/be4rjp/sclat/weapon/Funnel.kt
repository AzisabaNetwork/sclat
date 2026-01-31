package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
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
    var HashPlayer: HashMap<ArmorStand?, Player?> = HashMap<ArmorStand?, Player?>()
    var HashArmorstand: HashMap<ArmorStand?, ArmorStand?> = HashMap<ArmorStand?, ArmorStand?>()
    var FunnelMaxHP: Int = 10
    var FunnelMaxHP2: Int = 3
    var FunnelSpeed: Double = 1.0

    fun FunnelShot(
        player: Player,
        funnel: ArmorStand,
        taegetloc: Location,
    ) {
        val damage = 3.0
        val funloc = funnel.getEyeLocation()
        if (player.getGameMode() == GameMode.SPECTATOR) return
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        val rayTrace =
            RayTrace(
                funloc.toVector(),
                Vector(
                    taegetloc.getX() - funloc.getX(),
                    taegetloc.getY() - funloc.getY(),
                    taegetloc.getZ() - funloc.getZ(),
                ).normalize(),
            )
        val positions = rayTrace.traverse(4.0, 0.2)

        loop@ for (vector in positions) {
            val position = vector.toLocation(player.getLocation().getWorld()!!)
            val block = player.getLocation().getWorld()!!.getBlockAt(position)

            if (block.getType() != Material.AIR) {
                break
            }
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) {
                    if (target.getWorld() === position.getWorld()) {
                        if (target.getLocation().distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            target.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                        }
                    }
                }
            }

            val maxDistSquad = 4.0 // 2*2
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (!getPlayerData(target)!!.isInMatch()) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.getGameMode() == GameMode.ADVENTURE
                ) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(target as Entity), 4.0, 0.05)) {
                            giveDamage(player, target, damage, "killed")
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var p: Player = target

                                    override fun run() {
                                        target.setNoDamageTicks(0)
                                    }
                                }
                            task.runTaskLater(plugin, 1)
                            break@loop
                        }
                    }
                }
            }

            for (`as` in player.getWorld().getEntities()) {
                if (`as` is ArmorStand) {
                    if (`as`.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(`as` as Entity), 4.0, 0.05)) {
                            if (`as`.getCustomName() != null) {
                                if (`as`.getCustomName() == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.getCustomName() == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.getCustomName()!!)) {
                                        if (`as`.getCustomName() != "21" &&
                                            `as`.getCustomName() != "100"
                                        ) {
                                            if (`as`.isVisible()) {
                                                player.playSound(
                                                    player.getLocation(),
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
    fun FunnelFloat(player: Player) {
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

                var list: MutableList<ArmorStand> = ArrayList<ArmorStand>()
                var list1: MutableList<ArmorStand> = ArrayList<ArmorStand>()
                var list2: MutableList<ArmorStand> = ArrayList<ArmorStand>()
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
                        val locp = p.getLocation()
                        var pv = p.getEyeLocation().getDirection().normalize()
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
                                        HashPlayer.remove(list.get(0))
                                        HashArmorstand.remove(list.get(0))
                                        data!!.subArmorlist(list.get(0))
                                        for (`as` in list) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list.clear()
                                    as3 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 2.5, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as1 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 2.8, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as2 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 2.8, 0.0),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as1.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0)))
                                    as2.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0)))
                                    list.add(as3)
                                    list.add(as1)
                                    list.add(as2)
                                    data!!.setArmorlist(as3)
                                    GlowingAPI.setGlowing(as3, player, false)
                                    for (`as` in list) {
                                        `as`.setSmall(true)
                                        `as`.setBasePlate(false)
                                        `as`.setVisible(false)
                                        `as`.setGravity(false)
                                        `as`.setCustomName("Kasa")
                                        setKasaDataWithARmorStand(`as`, kdata)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                                        (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list.get(2).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list.get(1).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list.get(0).getEntityId(),
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
                                        HashPlayer.remove(list1.get(0))
                                        HashArmorstand.remove(list1.get(0))
                                        data!!.subArmorlist(list1.get(0))
                                        for (`as` in list1) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list1.clear()
                                    as13 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as11 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as12 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as11!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0)))
                                    as12!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0)))
                                    list1.add(as13!!)
                                    list1.add(as11!!)
                                    list1.add(as12!!)
                                    data!!.setArmorlist(as13)
                                    GlowingAPI.setGlowing(as13!!, player, false)
                                    for (`as` in list1) {
                                        `as`.setSmall(true)
                                        `as`.setBasePlate(false)
                                        `as`.setVisible(false)
                                        `as`.setGravity(false)
                                        `as`.setCustomName("Kasa")
                                        setKasaDataWithARmorStand(`as`, kdata1)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                                        (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1.get(2).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1.get(1).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list1.get(0).getEntityId(),
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
                                        HashPlayer.remove(list2.get(0))
                                        HashArmorstand.remove(list2.get(0))
                                        data!!.subArmorlist(list2.get(0))
                                        for (`as` in list2) {
                                            `as`.remove()
                                        }
                                    } catch (e: Exception) {
                                    }
                                    list2.clear()
                                    as23 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as21 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as22 =
                                        p.getWorld().spawnEntity(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                            EntityType.ARMOR_STAND,
                                        ) as ArmorStand
                                    as21!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0)))
                                    as22!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0)))
                                    list2.add(as23!!)
                                    list2.add(as21!!)
                                    list2.add(as22!!)
                                    data!!.setArmorlist(as23)
                                    GlowingAPI.setGlowing(as23!!, player, false)
                                    for (`as` in list2) {
                                        `as`.setSmall(true)
                                        `as`.setBasePlate(false)
                                        `as`.setVisible(false)
                                        `as`.setGravity(false)
                                        `as`.setCustomName("Kasa")
                                        setKasaDataWithARmorStand(`as`, kdata2)
                                    }
                                    val team = data!!.team
                                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                                        (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2.get(2).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2.get(1).getEntityId(),
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
                                        o_player.getHandle().playerConnection.sendPacket(
                                            PacketPlayOutEntityEquipment(
                                                list2.get(0).getEntityId(),
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
                                p.getWorld().spawnEntity(
                                    locp.clone().add(0.0, 2.5, 0.0),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            pv = as3!!.getEyeLocation().getDirection().normalize()
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
                                p.getWorld().spawnEntity(
                                    locp.clone().add(0.0, 2.8, 0.0).add(vec1.clone().multiply(0.3)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as2 =
                                p.getWorld().spawnEntity(
                                    locp.clone().add(0.0, 2.8, 0.0).add(vec2.clone().multiply(0.3)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list.add(as3!!)
                            list.add(as1!!)
                            list.add(as2!!)
                            as13 =
                                p.getWorld().spawnEntity(
                                    locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as11 =
                                p.getWorld().spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec1.clone().multiply(0.3))
                                        .add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as12 =
                                p.getWorld().spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec2.clone().multiply(0.3))
                                        .add(l1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list1.add(as13!!)
                            list1.add(as11!!)
                            list1.add(as12!!)
                            as23 =
                                p.getWorld().spawnEntity(
                                    locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as21 =
                                p.getWorld().spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec1.clone().multiply(0.3))
                                        .add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            as22 =
                                p.getWorld().spawnEntity(
                                    locp
                                        .clone()
                                        .add(0.0, 1.3, 0.0)
                                        .add(vec2.clone().multiply(0.3))
                                        .add(r1.clone().multiply(1.5)),
                                    EntityType.ARMOR_STAND,
                                ) as ArmorStand
                            list2.add(as23!!)
                            list2.add(as21!!)
                            list2.add(as22!!)

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
                                aslist.get(1)!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0)))
                                aslist.get(2)!!.setHeadPose(EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0)))
                                for (`as` in aslist) {
                                    `as`!!.setSmall(true)
                                    `as`.setBasePlate(false)
                                    `as`.setVisible(false)
                                    `as`.setGravity(false)
                                    `as`.setCustomName("Kasa")
                                }
                            }
                            val team = data!!.team
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                for (aslist in list5) {
                                    (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist.get(1)!!.getEntityId(),
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
                                    o_player.getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist.get(2)!!.getEntityId(),
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
                                    o_player.getHandle().playerConnection.sendPacket(
                                        PacketPlayOutEntityEquipment(
                                            aslist.get(0)!!.getEntityId(),
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
                            if (p.getGameMode() == GameMode.SPECTATOR) {
                                if (kdata.damage <= FunnelMaxHP) {
                                    kdata.damage = 1024.0
                                }
                                if (kdata1.damage <= FunnelMaxHP) {
                                    kdata1.damage = 1024.0
                                }
                                if (kdata2.damage <= FunnelMaxHP) {
                                    kdata2.damage = 1024.0
                                }
                            }
                            if (kdata.damage > FunnelMaxHP && kdata.damage < 9999) {
                                val kasaStand = kdata.armorStandList!!.get(0)
                                data!!.subArmorlist(kasaStand)
                                if (HashPlayer.containsKey(kasaStand)) {
                                    if (HashPlayer.get(kasaStand)!!.getGameMode() != GameMode.SPECTATOR) {
                                        kdataReset += 60
                                    }
                                    HashPlayer.remove(kasaStand)
                                } else if (HashArmorstand.containsKey(kasaStand)) {
                                    kdataReset += 60
                                    HashArmorstand.remove(kasaStand)
                                } else {
                                    list6.remove(kasaStand)
                                    if (kdata.damage == 1024.0) {
                                        listremove.runTaskLater(plugin, 110)
                                    } else {
                                        listremove.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata.damage = 10000.0
                                for (`as` in kdata.armorStandList!!) {
                                    `as`!!.remove()
                                }
                            }
                            if (kdata1.damage > FunnelMaxHP && kdata1.damage < 9999) {
                                val kasaStand1 = kdata1.armorStandList!!.get(0)
                                data!!.subArmorlist(kasaStand1)
                                if (HashPlayer.containsKey(kasaStand1)) {
                                    if (HashPlayer.get(kasaStand1)!!.getGameMode() != GameMode.SPECTATOR) {
                                        kdataReset1 += 60
                                    }
                                    HashPlayer.remove(kasaStand1)
                                } else if (HashArmorstand.containsKey(kasaStand1)) {
                                    kdataReset1 += 60
                                    HashArmorstand.remove(kasaStand1)
                                } else {
                                    list6.remove(kasaStand1)
                                    if (kdata1.damage == 1024.0) {
                                        listremove1.runTaskLater(plugin, 110)
                                    } else {
                                        listremove1.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata1.damage = 10000.0
                                for (`as` in kdata1.armorStandList!!) {
                                    `as`!!.remove()
                                }
                            }
                            if (kdata2.damage > FunnelMaxHP && kdata2.damage < 9999) {
                                val kasaStand2 = kdata2.armorStandList!!.get(0)
                                data!!.subArmorlist(kasaStand2)
                                if (HashPlayer.containsKey(kasaStand2)) {
                                    if (HashPlayer.get(kasaStand2)!!.getGameMode() != GameMode.SPECTATOR) {
                                        kdataReset2 += 60
                                    }
                                    HashPlayer.remove(kasaStand2)
                                } else if (HashArmorstand.containsKey(kasaStand2)) {
                                    kdataReset2 += 60
                                    HashArmorstand.remove(kasaStand2)
                                } else {
                                    list6.remove(kasaStand2)
                                    if (kdata2.damage == 1024.0) {
                                        listremove2.runTaskLater(plugin, 110)
                                    } else {
                                        listremove2.runTaskLater(plugin, 160)
                                    }
                                }
                                kdata2.damage = 10000.0
                                for (`as` in kdata2.armorStandList!!) {
                                    `as`!!.remove()
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
                                        .getEyeLocation()
                                        .getDirection()
                                        .normalize()
                                        .getX(),
                                    0.0,
                                    p
                                        .getEyeLocation()
                                        .getDirection()
                                        .normalize()
                                        .getZ(),
                                )
                            var io = 0
                            for (aslist in list5) {
                                val aslistget0: ArmorStand = aslist.get(0)!!
                                if (io == 0) {
                                    if (!HashPlayer.containsKey(aslistget0) &&
                                        !HashArmorstand.containsKey(aslistget0)
                                    ) {
                                        aslistget0.teleport(locp.clone().add(0.0, 2.5, 0.0))
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashPlayer
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(pv.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 0) {
                                            if (!getPlayerData(HashPlayer.get(aslistget0))!!.getIsUsingSP()) {
                                                FunnelShot(
                                                    p,
                                                    aslistget0,
                                                    HashPlayer.get(aslistget0)!!.getEyeLocation(),
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                HashPlayer
                                                    .get(aslistget0)!!
                                                    .getGameMode() == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        HashPlayer.get(aslistget0),
                                                    )!!.isInMatch() || !HashPlayer.get(aslistget0)!!.isOnline()
                                            ) &&
                                            kdata.damage < FunnelMaxHP
                                        ) {
                                            kdata.damage = (FunnelMaxHP + 1).toDouble()
                                            kdataReset = i + 3
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashArmorstand
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(pv.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 0) {
                                            FunnelShot(
                                                p,
                                                aslistget0,
                                                HashArmorstand.get(aslistget0)!!.getEyeLocation(),
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!HashArmorstand.get(aslistget0)!!.isVisible()) {
                                            kdataReset = i + 3
                                            kdata.damage = (FunnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    if (i % 20 == 0) {
                                        val team = data!!.team
                                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                                            (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(2)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(1)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(0)!!.getEntityId(),
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
                                    if (!HashPlayer.containsKey(aslistget0) &&
                                        !HashArmorstand.containsKey(aslistget0)
                                    ) {
                                        aslistget0.teleport(
                                            locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)),
                                        )
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashPlayer
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(l1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 16) {
                                            if (!getPlayerData(HashPlayer.get(aslistget0))!!.getIsUsingSP()) {
                                                FunnelShot(
                                                    p,
                                                    aslistget0,
                                                    HashPlayer.get(aslistget0)!!.getEyeLocation(),
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                HashPlayer
                                                    .get(aslistget0)!!
                                                    .getGameMode() == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        HashPlayer.get(aslistget0),
                                                    )!!.isInMatch() || !HashPlayer.get(aslistget0)!!.isOnline()
                                            ) &&
                                            kdata1.damage < FunnelMaxHP
                                        ) {
                                            kdataReset1 = i + 3
                                            kdata1.damage = (FunnelMaxHP + 1).toDouble()
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashArmorstand
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(l1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 16) {
                                            FunnelShot(
                                                p,
                                                aslistget0,
                                                HashArmorstand.get(aslist.get(0))!!.getEyeLocation(),
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!HashArmorstand.get(aslistget0)!!.isVisible()) {
                                            kdataReset1 = i + 3
                                            kdata1.damage = (FunnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    if (i % 20 == 0) {
                                        val team = data!!.team
                                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                                            (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(2)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(1)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(0)!!.getEntityId(),
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
                                    if (!HashPlayer.containsKey(aslistget0) &&
                                        !HashArmorstand.containsKey(aslist.get(0))
                                    ) {
                                        aslistget0.teleport(
                                            locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)),
                                        )
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashPlayer
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(r1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 32) {
                                            if (!getPlayerData(HashPlayer.get(aslistget0))!!.getIsUsingSP()) {
                                                FunnelShot(
                                                    p,
                                                    aslistget0,
                                                    HashPlayer.get(aslistget0)!!.getEyeLocation(),
                                                )
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if ((
                                                HashPlayer
                                                    .get(aslistget0)!!
                                                    .getGameMode() == GameMode.SPECTATOR ||
                                                    !getPlayerData(
                                                        HashPlayer.get(aslistget0),
                                                    )!!.isInMatch() || !HashPlayer.get(aslistget0)!!.isOnline()
                                            ) &&
                                            kdata2.damage < FunnelMaxHP
                                        ) {
                                            kdataReset2 = i + 3
                                            kdata2.damage = (FunnelMaxHP + 1).toDouble()
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        val las = aslistget0.getLocation()
                                        val lpl =
                                            HashArmorstand
                                                .get(aslistget0)!!
                                                .getLocation()
                                                .add(r1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0)))
                                        pv =
                                            Vector(
                                                lpl.getX() - las.getX(),
                                                lpl.getY() - las.getY(),
                                                lpl.getZ() - las.getZ(),
                                            )
                                        if (i % 48 == 32) {
                                            FunnelShot(
                                                p,
                                                aslistget0,
                                                HashArmorstand.get(aslistget0)!!.getEyeLocation(),
                                            )
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true)
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed))
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false)
                                            }
                                            aslistget0.teleport(lpl)
                                        }
                                        if (!HashArmorstand.get(aslistget0)!!.isVisible()) {
                                            kdataReset2 = i + 3
                                            kdata2.damage = (FunnelMaxHP + 1).toDouble()
                                        }
                                    }
                                    // 残数表記
                                    if (i % 20 == 0) {
                                        if (p.getGameMode() != GameMode.SPECTATOR) {
                                            val funnelamo = Funnelamount(player)
                                            val nuget: ItemStack?
                                            if (funnelamo > 0) {
                                                nuget = ItemStack(Material.GOLD_NUGGET, funnelamo)
                                            } else {
                                                nuget = ItemStack(Material.AIR)
                                            }
                                            player.getInventory().setItem(8, nuget)
                                        }
                                        // 残数表記了
                                        val team = data!!.team
                                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                                            (o_player as CraftPlayer).getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(2)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(1)!!.getEntityId(),
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
                                            o_player.getHandle().playerConnection.sendPacket(
                                                PacketPlayOutEntityEquipment(
                                                    aslist.get(0)!!.getEntityId(),
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
                                    aslist
                                        .get(0)!!
                                        .getEyeLocation()
                                        .getDirection()
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
                                val floc2 = aslist.get(0)!!.getLocation().clone()
                                aslist.get(1)!!.teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec1.clone().multiply(0.3)))
                                aslist.get(2)!!.teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec2.clone().multiply(0.3)))
                            }
                        }
                        if (check && p.isSneaking() && p.getGameMode() != GameMode.SPECTATOR) {
                            check = false
                            taskcheck.runTaskLater(plugin, 18)
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
                            val rayTrace =
                                RayTrace(
                                    player.getEyeLocation().toVector(),
                                    player.getEyeLocation().getDirection(),
                                )
                            val positions = rayTrace.traverse(55.0, 0.3)

                            loop@ for (it in positions.indices) {
                                val position = positions.get(it).toLocation(player.getLocation().getWorld()!!)
                                val block = player.getLocation().getWorld()!!.getBlockAt(position)

                                if (block.getType() != Material.AIR) {
                                    break
                                }
                                if (getPlayerData(player)!!.settings.ShowEffect_MainWeaponInk()) {
                                    if (it < 10) {
                                        if (player.getWorld() === position.getWorld()) {
                                            if (player
                                                    .getLocation()
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val bd =
                                                    getPlayerData(player)!!
                                                        .team
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
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (!getPlayerData(target)!!.isInMatch()) continue
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.getGameMode() == GameMode.ADVENTURE
                                    ) {
                                        if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                                            // if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                                            player.getWorld().playSound(
                                                player.getLocation(),
                                                Sound.BLOCK_NOTE_BLOCK_BIT,
                                                1.0f,
                                                5f,
                                            )
                                            if (!list6.isEmpty()) {
                                                if (list6.get(list6.size - 1) == as3 && FunAmoP(target)) {
                                                    player.getWorld().playSound(
                                                        target.getLocation(),
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    HashPlayer.put(as3, target)
                                                    GlowingAPI.setGlowing(as3!!, player, true)
                                                    GlowingAPI.setGlowing(as3!!, target, true)
                                                    if (kdata.damage < FunnelMaxHP2) {
                                                        kdata.damage = FunnelMaxHP2.toDouble()
                                                    }
                                                    as3!!.setGravity(true)
                                                    kdataReset = i + 210
                                                    // listremove.runTaskLater(Main.getPlugin(), 140);
                                                    list6.removeAt(list6.size - 1)
                                                } else if (list6.get(list6.size - 1) == as13 && FunAmoP(target)) {
                                                    player.getWorld().playSound(
                                                        target.getLocation(),
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    HashPlayer.put(as13, target)
                                                    GlowingAPI.setGlowing(as13!!, player, true)
                                                    GlowingAPI.setGlowing(as13!!, target, true)
                                                    if (kdata1.damage < FunnelMaxHP2) {
                                                        kdata1.damage = FunnelMaxHP2.toDouble()
                                                    }
                                                    as13!!.setGravity(true)
                                                    kdataReset1 = i + 210
                                                    // listremove1.runTaskLater(Main.getPlugin(), 140);
                                                    list6.removeAt(list6.size - 1)
                                                } else if (list6.get(list6.size - 1) == as23 && FunAmoP(target)) {
                                                    player.getWorld().playSound(
                                                        target.getLocation(),
                                                        Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                        1.0f,
                                                        2f,
                                                    )
                                                    HashPlayer.put(as23, target)
                                                    GlowingAPI.setGlowing(as23!!, player, true)
                                                    GlowingAPI.setGlowing(as23!!, target, true)
                                                    if (kdata2.damage < FunnelMaxHP2) {
                                                        kdata2.damage = FunnelMaxHP2.toDouble()
                                                    }
                                                    as23!!.setGravity(true)
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
                                                                // player.playSound(player.getLocation(),
                                                                // Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                                                player.getWorld().playSound(
                                                                    player.getLocation(),
                                                                    Sound.BLOCK_NOTE_BLOCK_BIT,
                                                                    1.0f,
                                                                    5f,
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (!list6.isEmpty()) {
                                                        if (list6.get(list6.size - 1) == as3 &&
                                                            FunAmoA(`as`)
                                                        ) {
                                                            player.getWorld().playSound(
                                                                `as`.getLocation(),
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            HashArmorstand.put(as3, `as`)
                                                            GlowingAPI.setGlowing(as3!!, player, true)
                                                            if (kdata.damage < FunnelMaxHP2) {
                                                                kdata.damage = FunnelMaxHP2.toDouble()
                                                            }
                                                            as3!!.setGravity(true)
                                                            kdataReset = i + 210
                                                            // listremove.runTaskLater(Main.getPlugin(), 140);
                                                            list6.removeAt(list6.size - 1)
                                                        } else if (list6.get(list6.size - 1) == as13 &&
                                                            FunAmoA(`as`)
                                                        ) {
                                                            player.getWorld().playSound(
                                                                `as`.getLocation(),
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            HashArmorstand.put(as13, `as`)
                                                            GlowingAPI.setGlowing(as13!!, player, true)
                                                            if (kdata1.damage < FunnelMaxHP2) {
                                                                kdata1.damage = FunnelMaxHP2.toDouble()
                                                            }
                                                            as13!!.setGravity(true)
                                                            kdataReset1 = i + 210
                                                            // listremove1.runTaskLater(Main.getPlugin(), 140);
                                                            list6.removeAt(list6.size - 1)
                                                        } else if (list6.get(list6.size - 1) == as23 &&
                                                            FunAmoA(`as`)
                                                        ) {
                                                            player.getWorld().playSound(
                                                                `as`.getLocation(),
                                                                Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO,
                                                                1.0f,
                                                                2f,
                                                            )
                                                            HashArmorstand.put(as23, `as`)
                                                            GlowingAPI.setGlowing(as23!!, player, true)
                                                            if (kdata2.damage < FunnelMaxHP2) {
                                                                kdata2.damage = FunnelMaxHP2.toDouble()
                                                            }
                                                            as23!!.setGravity(true)
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
                        if (!p.isOnline() || !data!!.isInMatch()) {
                            if (getPlayerData(p)!!.isInMatch()) {
                                as1!!
                                    .getWorld()
                                    .playSound(as1!!.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                            }

                            for (aslist in list5) {
                                for (`as` in aslist) {
                                    `as`!!.remove()
                                }
                            }
                            las!!.remove()
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

    fun FunnelPursuit(
        player: Player,
        target: ArmorStand,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (HashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (HashArmorstand.get(getPlayerData(player)!!.getArmorlist(ai)) == target) {
                        rate = rate + 1.5
                    }
                }
            } catch (e: Exception) {
                rate = rate - 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.getLocation()
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai).getEyeLocation()
                            val vec =
                                Vector(
                                    loct.getX() - locd!!.getX(),
                                    loct.getY() - locd!!.getY() + 1.5,
                                    loct.getZ() - locd!!.getZ(),
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions.get(i).toLocation(p.getLocation().getWorld()!!)
                                if (player.getWorld() === position.getWorld()) {
                                    if (player
                                            .getLocation()
                                            .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                data!!.team.teamColor!!.bukkitColor!!,
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
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (target == p ||
                                        getPlayerData(target)!!.settings.ShowEffect_ChargerLine()
                                    ) {
                                        if (target.getWorld() === p.getWorld()) {
                                            if (target
                                                    .getLocation()
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data!!.team.teamColor!!.bukkitColor!!,
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

    fun FunnelPursuitPlayer(
        player: Player,
        target: Player,
    ): Double {
        var rate = 0.0
        for (ai in 0..2) {
            try {
                if (HashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    if (HashPlayer.get(getPlayerData(player)!!.getArmorlist(ai)) == target) {
                        rate = rate + 1.5
                    }
                }
            } catch (e: Exception) {
                rate = rate - 0.7
            }
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var data: PlayerData? = getPlayerData(player)
                var loct: Location = target.getLocation()
                var locd: Location? = null

                override fun run() {
                    for (ai in 0..2) {
                        try {
                            locd = data!!.getArmorlist(ai).getEyeLocation()
                            val vec =
                                Vector(
                                    loct.getX() - locd!!.getX(),
                                    loct.getY() - locd!!.getY() + 1.5,
                                    loct.getZ() - locd!!.getZ(),
                                )
                            val rayTrace = RayTrace(locd!!.toVector(), vec)
                            val positions = rayTrace.traverse(vec.length().toInt().toDouble(), 0.4)
                            var veclength = vec.length() / 2
                            if (veclength > 12) {
                                veclength = 12.0
                            }
                            var i = 0
                            while (i < veclength) {
                                val position = positions.get(i).toLocation(p.getLocation().getWorld()!!)
                                if (player.getWorld() === position.getWorld()) {
                                    if (player
                                            .getLocation()
                                            .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                data!!.team.teamColor!!.bukkitColor!!,
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
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (target == p ||
                                        getPlayerData(target)!!.settings.ShowEffect_ChargerLine()
                                    ) {
                                        if (target.getWorld() === p.getWorld()) {
                                            if (target
                                                    .getLocation()
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data!!.team.teamColor!!.bukkitColor!!,
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

    fun Funnelamount(player: Player?): Int {
        var rate = 3
        for (ai in 0..2) {
            try {
                if (HashPlayer.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate = rate - 1
                }
                if (HashArmorstand.containsKey(getPlayerData(player)!!.getArmorlist(ai))) {
                    rate = rate - 1
                }
            } catch (e: Exception) {
                rate = rate - 1
            }
        }
        return rate
    }

    private fun FunAmoP(player: Player?): Boolean {
        var count = 0
        for (entry in HashPlayer.entries) {
            if (entry.value === player) {
                count++
            }
        }
        return count < 3
    }

    private fun FunAmoA(stand: ArmorStand?): Boolean {
        var count = 0
        for (entry in HashArmorstand.entries) {
            if (entry.value === stand) {
                count++
            }
        }
        return count < 3
    }
}
