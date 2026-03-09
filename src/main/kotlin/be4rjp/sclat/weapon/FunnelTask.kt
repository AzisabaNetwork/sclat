package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import be4rjp.sclat.data.DataMgr.setKasaDataWithPlayer
import be4rjp.sclat.data.KasaData
import be4rjp.sclat.plugin
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

/**
 * Manages the lifecycle and per-tick behavior of the three funnel (kasa) groups for a player.
 * Handles spawning, movement, targeting, lock-on, and respawn scheduling.
 */
class FunnelTask(
    private val player: Player,
) : BukkitRunnable() {
    private val kdata = KasaData(player)
    private val kdata1 = KasaData(player)
    private val kdata2 = KasaData(player)
    private val data: PlayerData? = getPlayerData(player)

    private val list: MutableList<ArmorStand> = ArrayList()
    private val list1: MutableList<ArmorStand> = ArrayList()
    private val list2: MutableList<ArmorStand> = ArrayList()
    private val list5: MutableList<MutableList<ArmorStand>> = mutableListOf()
    private val list6: MutableList<ArmorStand> = mutableListOf()

    private lateinit var as3: ArmorStand
    private lateinit var as13: ArmorStand
    private lateinit var as23: ArmorStand
    private lateinit var las: ArmorStand

    private var i: Int = 0
    private var check: Boolean = false
    private var kdataReset: Int = -1
    private var kdataReset1: Int = -1
    private var kdataReset2: Int = -1

    /** Registers kasa data and starts the repeating task. */
    fun start() {
        setKasaDataWithPlayer(player, kdata)
        setKasaDataWithPlayer(player, kdata1)
        setKasaDataWithPlayer(player, kdata2)
        runTaskTimer(plugin, 0, 1)
    }

    override fun run() {
        try {
            val locp = player.location
            val pv = player.eyeLocation.direction.normalize()
            val vec = Vector(pv.x, 0.0, pv.z).normalize()
            val l1 = Vector(-vec.z, 0.0, vec.x)
            val r1 = Vector(vec.z, 0.0, -vec.x)

            val taskcheck: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        check = true
                    }
                }

            if (i == 0) {
                initializeFunnelGroups(locp, pv, l1, r1)
                taskcheck.runTaskLater(plugin, 20)
            }

            if (i >= 0) {
                handleSpectatorDestruction()
                handleDestroyedGroups(locp, l1, r1)

                var io = 0
                for (aslist in list5) {
                    val aslistget0: ArmorStand = aslist[0]
                    val kdataForGroup =
                        when (io) {
                            0 -> kdata
                            1 -> kdata1
                            else -> kdata2
                        }
                    val idleTarget =
                        when (io) {
                            0 -> locp.clone().add(0.0, 2.5, 0.0)
                            1 -> locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5))
                            else -> locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5))
                        }
                    if (!ArmorStandManager.hashPlayer.containsKey(aslistget0) &&
                        !ArmorStandManager.hashArmorstand.containsKey(aslistget0)
                    ) {
                        aslistget0.teleport(idleTarget)
                    } else {
                        moveFunnelToTarget(aslistget0, io, pv, l1, r1, kdataForGroup, i)
                    }

                    if (i % 20 == 0) {
                        val team = data!!.team
                        if (team != null) {
                            ArmorStandManager.sendEquipmentPackets(aslist, team)
                        }
                        if (io == 2 && player.gameMode != GameMode.SPECTATOR) {
                            val funnelamo = Funnel.funnelamount(player)
                            val nuget =
                                if (funnelamo > 0) {
                                    ItemStack(Material.GOLD_NUGGET, funnelamo)
                                } else {
                                    ItemStack(Material.AIR)
                                }
                            player.inventory.setItem(8, nuget)
                        }
                    }

                    io++
                }

                for (aslist in list5) {
                    val dir = aslist[0].eyeLocation.direction.normalize()
                    val vec1 =
                        Vector(
                            dir.x * 0.707 - dir.z * 0.707,
                            0.0,
                            dir.x * 0.707 + dir.z * 0.707,
                        ).normalize()
                    val vec2 =
                        Vector(
                            dir.x * 0.707 + dir.z * 0.707,
                            0.0,
                            -dir.x * 0.707 + dir.z * 0.707,
                        ).normalize()
                    val floc2 = aslist[0].location.clone()
                    aslist[1].teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec1.clone().multiply(0.3)))
                    aslist[2].teleport(floc2.clone().add(0.0, 0.3, 0.0).add(vec2.clone().multiply(0.3)))
                }
            }

            if (check && player.isSneaking && player.gameMode != GameMode.SPECTATOR) {
                check = false
                taskcheck.runTaskLater(plugin, 18)
                performLockOnScan(l1, r1)
            }

            if (!player.isOnline || !data!!.isInMatch) {
                if (getPlayerData(player)!!.isInMatch) {
                    list[1].world.playSound(list[1].location, Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f)
                }
                for (aslist in list5) {
                    for (armorStand in aslist) {
                        armorStand.remove()
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

    // ── Initialization ────────────────────────────────────────────────────────

    /**
     * Spawns all three funnel groups and configures them on the very first tick (i == 0).
     */
    private fun initializeFunnelGroups(
        locp: Location,
        pv: Vector,
        l1: Vector,
        r1: Vector,
    ) {
        val vec1 =
            Vector(
                pv.x * 0.707 - pv.z * 0.707,
                0.0,
                pv.x * 0.707 + pv.z * 0.707,
            ).normalize()
        val vec2 =
            Vector(
                pv.x * 0.707 + pv.z * 0.707,
                0.0,
                -pv.x * 0.707 + pv.z * 0.707,
            ).normalize()

        // Group 0 – above the player
        as3 = player.world.spawnEntity(locp.clone().add(0.0, 2.5, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        val as1 =
            player.world.spawnEntity(
                locp.clone().add(0.0, 2.8, 0.0).add(vec1.clone().multiply(0.3)),
                EntityType.ARMOR_STAND,
            ) as ArmorStand
        val as2 =
            player.world.spawnEntity(
                locp.clone().add(0.0, 2.8, 0.0).add(vec2.clone().multiply(0.3)),
                EntityType.ARMOR_STAND,
            ) as ArmorStand
        list.add(as3)
        list.add(as1)
        list.add(as2)

        // Group 1 – left side
        as13 = player.world.spawnEntity(locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND) as ArmorStand
        val as11 =
            player.world.spawnEntity(
                locp
                    .clone()
                    .add(0.0, 1.3, 0.0)
                    .add(vec1.clone().multiply(0.3))
                    .add(l1.clone().multiply(1.5)),
                EntityType.ARMOR_STAND,
            ) as ArmorStand
        val as12 =
            player.world.spawnEntity(
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

        // Group 2 – right side
        as23 = player.world.spawnEntity(locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND) as ArmorStand
        val as21 =
            player.world.spawnEntity(
                locp
                    .clone()
                    .add(0.0, 1.3, 0.0)
                    .add(vec1.clone().multiply(0.3))
                    .add(r1.clone().multiply(1.5)),
                EntityType.ARMOR_STAND,
            ) as ArmorStand
        val as22 =
            player.world.spawnEntity(
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
        data.setArmorlist(as13)
        data.setArmorlist(as23)
        list6.add(as3)
        list6.add(as13)
        list6.add(as23)
        kdata.armorStandList = list
        kdata1.armorStandList = list1
        kdata2.armorStandList = list2
        kdata.damage = 0.0
        kdata1.damage = 0.0
        kdata2.damage = 0.0

        for (armorStand in list) setKasaDataWithARmorStand(armorStand, kdata)
        for (armorStand in list1) setKasaDataWithARmorStand(armorStand, kdata1)
        for (armorStand in list2) setKasaDataWithARmorStand(armorStand, kdata2)

        for (aslist in list5) {
            aslist[1].headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
            aslist[2].headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))
            for (armorStand in aslist) {
                armorStand.isSmall = true
                armorStand.setBasePlate(false)
                armorStand.isVisible = false
                armorStand.setGravity(false)
                armorStand.customName = "Kasa"
            }
        }

        val team = data.team
        if (team != null) {
            for (aslist in list5) {
                ArmorStandManager.sendEquipmentPackets(aslist, team)
            }
        }
    }

    // ── Destruction / revival ─────────────────────────────────────────────────

    /** Forces all funnel groups into the destroyed state when the player is in spectator. */
    private fun handleSpectatorDestruction() {
        if (player.gameMode == GameMode.SPECTATOR) {
            if (kdata.damage <= Funnel.funnelMaxHP) kdata.damage = 1024.0
            if (kdata1.damage <= Funnel.funnelMaxHP) kdata1.damage = 1024.0
            if (kdata2.damage <= Funnel.funnelMaxHP) kdata2.damage = 1024.0
        }
    }

    /**
     * Checks each group's damage; if a group has been destroyed, removes its entities,
     * updates state, and schedules a rebuild runnable.
     */
    private fun handleDestroyedGroups(
        locp: Location,
        l1: Vector,
        r1: Vector,
    ) {
        checkGroupDestroyed(kdata, list, 0, locp, l1, r1)
        checkGroupDestroyed(kdata1, list1, 1, locp, l1, r1)
        checkGroupDestroyed(kdata2, list2, 2, locp, l1, r1)

        if (i == kdataReset) {
            scheduleGroupRebuild(0, locp, l1, r1, 1)
            kdataReset = -1
        }
        if (i == kdataReset1) {
            scheduleGroupRebuild(1, locp, l1, r1, 1)
            kdataReset1 = -1
        }
        if (i == kdataReset2) {
            scheduleGroupRebuild(2, locp, l1, r1, 1)
            kdataReset2 = -1
        }
    }

    private fun checkGroupDestroyed(
        kd: KasaData,
        groupList: MutableList<ArmorStand>,
        groupIndex: Int,
        locp: Location,
        l1: Vector,
        r1: Vector,
    ) {
        if (kd.damage <= Funnel.funnelMaxHP || kd.damage >= 9999) return
        val kasaStand = kd.armorStandList[0]
        data!!.subArmorlist(kasaStand)

        when {
            ArmorStandManager.hashPlayer.containsKey(kasaStand) -> {
                if (ArmorStandManager.hashPlayer[kasaStand]!!.gameMode != GameMode.SPECTATOR) {
                    incrementReset(groupIndex)
                }
                ArmorStandManager.hashPlayer.remove(kasaStand)
            }
            ArmorStandManager.hashArmorstand.containsKey(kasaStand) -> {
                incrementReset(groupIndex)
                ArmorStandManager.hashArmorstand.remove(kasaStand)
            }
            else -> {
                list6.remove(kasaStand)
                val delay = if (kd.damage == 1024.0) 110L else 160L
                scheduleGroupRebuild(groupIndex, locp, l1, r1, delay)
            }
        }
        kd.damage = 10000.0
        for (armorStand in kd.armorStandList) {
            armorStand.remove()
        }
    }

    private fun incrementReset(groupIndex: Int) {
        when (groupIndex) {
            0 -> kdataReset += 60
            1 -> kdataReset1 += 60
            2 -> kdataReset2 += 60
        }
    }

    /**
     * Schedules a BukkitRunnable that cleans up and respawns the specified funnel group.
     *
     * @param groupIndex 0 = above, 1 = left, 2 = right
     * @param locp       Player location captured at the tick when the rebuild is scheduled
     * @param l1         Left-perpendicular vector captured at that tick
     * @param r1         Right-perpendicular vector captured at that tick
     * @param delay      Delay in ticks before the rebuild executes
     */
    private fun scheduleGroupRebuild(
        groupIndex: Int,
        locp: Location,
        l1: Vector,
        r1: Vector,
        delay: Long,
    ) {
        val groupList =
            when (groupIndex) {
                0 -> list
                1 -> list1
                else -> list2
            }
        val kd =
            when (groupIndex) {
                0 -> kdata
                1 -> kdata1
                else -> kdata2
            }

        object : BukkitRunnable() {
            override fun run() {
                try {
                    ArmorStandManager.cleanupFunnelGroup(groupList, data!!)
                } catch (e: Exception) {
                }

                val mainLoc: Location
                val wingsLoc: Location
                when (groupIndex) {
                    0 -> {
                        mainLoc = locp.clone().add(0.0, 2.5, 0.0)
                        wingsLoc = locp.clone().add(0.0, 2.8, 0.0)
                    }
                    1 -> {
                        mainLoc = locp.clone().add(0.0, 1.0, 0.0).add(l1.clone().multiply(1.5))
                        wingsLoc = mainLoc
                    }
                    else -> {
                        mainLoc = locp.clone().add(0.0, 1.0, 0.0).add(r1.clone().multiply(1.5))
                        wingsLoc = mainLoc
                    }
                }

                val newGroup =
                    ArmorStandManager.spawnFunnelGroup(
                        player,
                        data!!,
                        kd,
                        mainLoc,
                        wingsLoc,
                        applyGlowing = true,
                    )
                groupList.addAll(newGroup)

                when (groupIndex) {
                    0 -> as3 = groupList[0]
                    1 -> as13 = groupList[0]
                    2 -> as23 = groupList[0]
                }

                val team = data.team
                if (team != null) {
                    ArmorStandManager.sendEquipmentPackets(groupList, team)
                }
                list6.add(groupList[0])
                kd.damage = 0.0
                kd.armorStandList = groupList
                cancel()
            }
        }.runTaskLater(plugin, delay)
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    /**
     * Moves the given funnel stand towards its locked-on target (player or armor stand),
     * firing a shot at the appropriate interval.
     * Updates kdataReset if the target is gone.
     */
    private fun moveFunnelToTarget(
        stand: ArmorStand,
        io: Int,
        pv: Vector,
        l1: Vector,
        r1: Vector,
        kd: KasaData,
        tick: Int,
    ) {
        val shotInterval = 48
        val shotOffset =
            when (io) {
                0 -> 0
                1 -> 16
                else -> 32
            }
        val dirOffset =
            when (io) {
                0 -> pv.clone().multiply(2).add(Vector(0.0, 1.4, 0.0))
                1 -> l1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0))
                else -> r1.clone().multiply(2).add(Vector(0.0, 1.4, 0.0))
            }

        if (ArmorStandManager.hashPlayer.containsKey(stand)) {
            val target = ArmorStandManager.hashPlayer[stand]!!
            val lpl = target.location.add(dirOffset)
            moveStandTowards(stand, lpl)
            if (tick % shotInterval == shotOffset) {
                if (!getPlayerData(target)!!.isUsingSP) {
                    Funnel.funnelShot(player, stand, target.eyeLocation)
                }
            }
            if ((
                    target.gameMode == GameMode.SPECTATOR ||
                        !getPlayerData(target)!!.isInMatch ||
                        !target.isOnline
                ) &&
                kd.damage < Funnel.funnelMaxHP
            ) {
                kd.damage = (Funnel.funnelMaxHP + 1).toDouble()
                setResetForGroup(io, tick + 3)
            }
        } else if (ArmorStandManager.hashArmorstand.containsKey(stand)) {
            val targetStand = ArmorStandManager.hashArmorstand[stand]!!
            val lpl = targetStand.location.add(dirOffset)
            moveStandTowards(stand, lpl)
            if (tick % shotInterval == shotOffset) {
                Funnel.funnelShot(player, stand, targetStand.eyeLocation)
            }
            if (!targetStand.isVisible) {
                setResetForGroup(io, tick + 3)
                kd.damage = (Funnel.funnelMaxHP + 1).toDouble()
            }
        }
    }

    /** Moves a funnel stand towards [target] using velocity or teleport depending on distance. */
    private fun moveStandTowards(
        stand: ArmorStand,
        target: Location,
    ) {
        val las = stand.location
        val moveVec = Vector(target.x - las.x, target.y - las.y, target.z - las.z)
        if (moveVec.length() > 1) {
            if (!stand.hasGravity()) stand.setGravity(true)
            stand.velocity = moveVec.normalize().multiply(Funnel.funnelSpeed)
        } else {
            if (stand.hasGravity()) stand.setGravity(false)
            stand.teleport(target)
        }
    }

    private fun setResetForGroup(
        io: Int,
        value: Int,
    ) {
        when (io) {
            0 -> kdataReset = value
            1 -> kdataReset1 = value
            2 -> kdataReset2 = value
        }
    }

    // ── Lock-on scan ──────────────────────────────────────────────────────────

    /**
     * Performs the lock-on ray trace triggered by the player sneaking.
     * Attempts to assign the next available funnel from [list6] to the first nearby target found.
     */
    private fun performLockOnScan(
        l1: Vector,
        r1: Vector,
    ) {
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions = rayTrace.traverse(55.0, 0.3)

        loop@ for (idx in positions.indices) {
            val position = positions[idx].toLocation(player.location.world!!)
            if (player.location.world!!
                    .getBlockAt(position)
                    .type != Material.AIR
            ) {
                break
            }

            if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk() && idx < 10) {
                if (player.world === position.world &&
                    player.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared
                ) {
                    val bd =
                        getPlayerData(player)!!
                            .team!!
                            .teamColor!!
                            .wool!!
                            .createBlockData()
                    player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                }
            }

            val maxDistSquared = 20.0
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquared) {
                        player.world.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 5f)
                        if (list6.isNotEmpty()) tryLockOnPlayer(target)
                        break@loop
                    }
                }
            }

            for (entity in player.world.entities) {
                if (entity !is ArmorStand) continue
                if (entity.location.distanceSquared(position) > maxDistSquared) continue
                if (entity.customName == null) continue
                if (entity.customName == "SplashShield" || entity.customName == "Kasa") continue

                if (SclatUtil.isNumber(entity.customName!!)) {
                    if (entity.customName != "21" && entity.customName != "100" && entity.isVisible) {
                        player.world.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 5f)
                    }
                }
                if (list6.isNotEmpty()) tryLockOnArmorStand(entity)
                break@loop
            }
        }
    }

    /** Attempts to lock the last available funnel in [list6] onto a player target. */
    private fun tryLockOnPlayer(target: Player) {
        if (list6.isEmpty()) return
        when (list6.last()) {
            as3 -> if (Funnel.funAmoP(target)) lockFunnelOnPlayer(as3, target, kdata, 0)
            as13 -> if (Funnel.funAmoP(target)) lockFunnelOnPlayer(as13, target, kdata1, 1)
            as23 -> if (Funnel.funAmoP(target)) lockFunnelOnPlayer(as23, target, kdata2, 2)
        }
    }

    private fun lockFunnelOnPlayer(
        stand: ArmorStand,
        target: Player,
        kd: KasaData,
        groupIndex: Int,
    ) {
        player.world.playSound(target.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2f)
        ArmorStandManager.hashPlayer[stand] = target
        GlowingAPI.setGlowing(stand, player, true)
        GlowingAPI.setGlowing(stand, target, true)
        if (kd.damage < Funnel.funnelMaxHP2) kd.damage = Funnel.funnelMaxHP2.toDouble()
        stand.setGravity(true)
        setResetForGroup(groupIndex, i + 210)
        list6.removeAt(list6.size - 1)
    }

    /** Attempts to lock the last available funnel in [list6] onto an armor stand target. */
    private fun tryLockOnArmorStand(target: ArmorStand) {
        if (list6.isEmpty()) return
        when (list6.last()) {
            as3 -> if (Funnel.funAmoA(target)) lockFunnelOnArmorStand(as3, target, kdata, 0)
            as13 -> if (Funnel.funAmoA(target)) lockFunnelOnArmorStand(as13, target, kdata1, 1)
            as23 -> if (Funnel.funAmoA(target)) lockFunnelOnArmorStand(as23, target, kdata2, 2)
        }
    }

    private fun lockFunnelOnArmorStand(
        stand: ArmorStand,
        target: ArmorStand,
        kd: KasaData,
        groupIndex: Int,
    ) {
        player.world.playSound(target.location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2f)
        ArmorStandManager.hashArmorstand[stand] = target
        GlowingAPI.setGlowing(stand, player, true)
        if (kd.damage < Funnel.funnelMaxHP2) kd.damage = Funnel.funnelMaxHP2.toDouble()
        stand.setGravity(true)
        setResetForGroup(groupIndex, i + 210)
        list6.removeAt(list6.size - 1)
    }
}
