package be4rjp.sclat.api.holo

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.RankingType
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.manager.RankMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class RankingHolograms(
    private val player: Player,
) {
    private val title: EntityArmorStand
    private val separator: EntityArmorStand
    private val you: EntityArmorStand
    private val mode: EntityArmorStand

    val clickHit1: EntityArmorStand
    private val clickHit2: EntityArmorStand
    private val clickHit3: EntityArmorStand
    private val clickHit4: EntityArmorStand

    private val rankArmorStands: MutableList<EntityArmorStand>

    @JvmField
    val armorStandList: MutableList<EntityArmorStand?>

    private val location: Location

    var rankingType: RankingType = RankingType.TOTAL

    init {
        armorStandList = ArrayList<EntityArmorStand?>()

        val worldName = Sclat.conf?.config!!.getString("RankingHolograms.WorldName")
        val w = Bukkit.getWorld(worldName!!)
        val ix = Sclat.conf?.config!!.getDouble("RankingHolograms.X")
        val iy = Sclat.conf?.config!!.getDouble("RankingHolograms.Y")
        val iz = Sclat.conf?.config!!.getDouble("RankingHolograms.Z")
        location = Location(w, ix + 0.5, iy, iz + 0.5)

        val nmsWorld = (location.world as CraftWorld).handle

        title = EntityArmorStand(nmsWorld, location.x, location.y + 2.5, location.z)
        title.isNoGravity = true
        title.setPosition(location.x, location.y + 2.5, location.z)
        title.setBasePlate(false)
        title.isInvisible = true
        title.isSmall = true
        title.customName = CraftChatMessage.fromStringOrNull("§a----------- §b§lTotal Ranking §r§a-----------")
        title.customNameVisible = true
        armorStandList.add(title)

        separator = EntityArmorStand(nmsWorld, location.x, location.y + 0.0, location.z)
        separator.isNoGravity = true
        separator.setPosition(location.x, location.y + 0.0, location.z)
        separator.setBasePlate(false)
        separator.isInvisible = true
        separator.isSmall = true
        separator.customName = CraftChatMessage.fromStringOrNull("§a-------------------------------------")
        separator.customNameVisible = true
        armorStandList.add(separator)

        you = EntityArmorStand(nmsWorld, location.x, location.y - 0.4, location.z)
        you.isNoGravity = true
        you.setPosition(location.x, location.y - 0.4, location.z)
        you.setBasePlate(false)
        you.isInvisible = true
        you.isSmall = true
        you.customName = CraftChatMessage.fromStringOrNull("--")
        you.customNameVisible = true
        armorStandList.add(you)

        mode = EntityArmorStand(nmsWorld, location.x, location.y - 0.8, location.z)
        mode.isNoGravity = true
        mode.setPosition(location.x, location.y - 0.8, location.z)
        mode.setBasePlate(false)
        mode.isInvisible = true
        mode.isSmall = true
        mode.customName = CraftChatMessage.fromStringOrNull("§a§l[Total] §7§l[Kill] [Paint]")
        mode.customNameVisible = true
        armorStandList.add(mode)

        clickHit1 = EntityArmorStand(nmsWorld, location.x + 0.3, location.y + 0.0, location.z + 0.3)
        clickHit1.isNoGravity = true
        clickHit1.setPosition(location.x + 0.3, location.y + 0.0, location.z + 0.3)
        clickHit1.setBasePlate(false)
        clickHit1.isInvisible = true
        clickHit1.customName = CraftChatMessage.fromStringOrNull("clickHit1")
        clickHit1.customNameVisible = false
        armorStandList.add(clickHit1)

        clickHit2 = EntityArmorStand(nmsWorld, location.x + 0.3, location.y + 0.0, location.z - 0.3)
        clickHit2.isNoGravity = true
        clickHit2.setPosition(location.x + 0.3, location.y + 0.0, location.z - 0.3)
        clickHit2.setBasePlate(false)
        clickHit2.isInvisible = true
        clickHit2.customName = CraftChatMessage.fromStringOrNull("clickHit2")
        clickHit2.customNameVisible = false
        armorStandList.add(clickHit2)

        clickHit3 = EntityArmorStand(nmsWorld, location.x - 0.3, location.y + 0.0, location.z - 0.3)
        clickHit3.isNoGravity = true
        clickHit3.setPosition(location.x - 0.3, location.y + 0.0, location.z - 0.3)
        clickHit3.setBasePlate(false)
        clickHit3.isInvisible = true
        clickHit3.customName = CraftChatMessage.fromStringOrNull("clickHit3")
        clickHit3.customNameVisible = false
        armorStandList.add(clickHit3)

        clickHit4 = EntityArmorStand(nmsWorld, location.x - 0.3, location.y + 0.0, location.z + 0.3)
        clickHit4.isNoGravity = true
        clickHit4.setPosition(location.x - 0.3, location.y + 0.0, location.z + 0.3)
        clickHit4.setBasePlate(false)
        clickHit4.isInvisible = true
        clickHit4.customName = CraftChatMessage.fromStringOrNull("clickHit4")
        clickHit4.customNameVisible = false
        armorStandList.add(clickHit4)

        rankArmorStands = ArrayList<EntityArmorStand>()
        for (i in 0..4) {
            val armorStand =
                EntityArmorStand(
                    nmsWorld,
                    location.x,
                    location.y + 2.0 - (0.4 * i.toDouble()),
                    location.z,
                )
            armorStand.isNoGravity = true
            armorStand.setPosition(location.x, location.y + 2.0 - (0.4 * i.toDouble()), location.z)
            armorStand.setBasePlate(false)
            armorStand.isInvisible = true
            armorStand.isSmall = true
            armorStand.customName = CraftChatMessage.fromStringOrNull("--")
            armorStand.customNameVisible = true
            rankArmorStands.add(armorStand)
            armorStandList.add(armorStand)
        }

        refreshRankingAsync()
    }

    fun switchNextRankingType() {
        when (rankingType) {
            RankingType.TOTAL -> {
                rankingType = RankingType.KILL
                title.customName = CraftChatMessage.fromStringOrNull("§a----------- §b§lKill Ranking §r§a-----------")
                separator.customName = CraftChatMessage.fromStringOrNull("§a------------------------------------")
                mode.customName = CraftChatMessage.fromStringOrNull("§7§l[Total] §a§l[Kill] §7§l[Paint]")
            }

            RankingType.KILL -> {
                rankingType = RankingType.PAINT
                title.customName = CraftChatMessage.fromStringOrNull("§a----------- §b§lPaint Ranking §r§a-----------")
                separator.customName = CraftChatMessage.fromStringOrNull("§a-------------------------------------")
                mode.customName = CraftChatMessage.fromStringOrNull("§7§l[Total] [Kill] §a§l[Paint]")
            }

            RankingType.PAINT -> {
                rankingType = RankingType.TOTAL
                title.customName = CraftChatMessage.fromStringOrNull("§a----------- §b§lTotal Ranking §r§a-----------")
                separator.customName = CraftChatMessage.fromStringOrNull("§a-------------------------------------")
                mode.customName = CraftChatMessage.fromStringOrNull("§a§l[Total] §7§l[Kill] [Paint]")
            }
        }
    }

    fun refreshRankingAsync() {
        val async: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    val list: MutableList<EntityArmorStand> = ArrayList<EntityArmorStand>()
                    list.add(clickHit1)
                    list.add(clickHit2)
                    list.add(clickHit3)
                    list.add(clickHit4)

                    var i = 0
                    for (armorStand in rankArmorStands) {
                        try {
                            list.add(armorStand)

                            if (rankingType == RankingType.TOTAL) {
                                val uuid = RankMgr.ranking.get(i)
                                val mcid = Sclat.conf?.uUIDCash!!.getString(uuid!!)

                                val rank = PlayerStatusMgr.getRank(uuid)

                                if (rank != 0) {
                                    armorStand.customName =
                                        CraftChatMessage.fromStringOrNull(
                                            (
                                                "§e" + (i + 1).toString() + "位 §f" + mcid +
                                                    "  §6Rank : §r" + rank + " [§b " + RankMgr.toABCRank(rank) + " §f]"
                                            ),
                                        )
                                } else {
                                    armorStand.customName = CraftChatMessage.fromStringOrNull("--")
                                }
                            }

                            if (rankingType == RankingType.KILL) {
                                val uuid = RankMgr.killRanking.get(i)
                                val mcid = Sclat.conf?.uUIDCash!!.getString(uuid!!)

                                val kill = PlayerStatusMgr.getKill(uuid)

                                if (kill != 0) {
                                    armorStand.customName =
                                        CraftChatMessage.fromStringOrNull(
                                            "§e" + (i + 1).toString() + "位 §f" + mcid + "  §6Kill(s) : §r" + kill,
                                        )
                                } else {
                                    armorStand.customName = CraftChatMessage.fromStringOrNull("--")
                                }
                            }

                            if (rankingType == RankingType.PAINT) {
                                val uuid = RankMgr.paintRanking.get(i)
                                val mcid = Sclat.conf?.uUIDCash!!.getString(uuid!!)

                                val paint = PlayerStatusMgr.getPaint(uuid)

                                if (paint != 0) {
                                    armorStand.customName =
                                        CraftChatMessage.fromStringOrNull(
                                            "§e" + (i + 1).toString() + "位 §f" + mcid + "  §6Paint(s) : §r" + paint,
                                        )
                                } else {
                                    armorStand.customName = CraftChatMessage.fromStringOrNull("--")
                                }
                            }
                        } catch (e: Exception) {
                        }
                        i++
                    }

                    try {
                        if (rankingType == RankingType.TOTAL) {
                            val mcid = player.name
                            var ranking = 1
                            for (uuid in RankMgr.ranking) {
                                if (uuid == player.uniqueId.toString()) break
                                ranking++
                            }

                            val rank = PlayerStatusMgr.getRank(player.uniqueId.toString())

                            you.customName =
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" + (if (rank == 0) "-" else ranking) +
                                            "位 §f" + mcid + "  §6Rank : §r" + rank + " [§b " + RankMgr.toABCRank(rank) + " §f]"
                                    ),
                                )
                        }

                        if (rankingType == RankingType.KILL) {
                            val mcid = player.name
                            var ranking = 1
                            for (uuid in RankMgr.killRanking) {
                                if (uuid == player.uniqueId.toString()) break
                                ranking++
                            }

                            val kill = PlayerStatusMgr.getKill(player.uniqueId.toString())

                            you.customName =
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" + (if (kill == 0) "-" else ranking) +
                                            "位 §f" + mcid + "  §6Kill(s) : §r" + kill
                                    ),
                                )
                        }

                        if (rankingType == RankingType.PAINT) {
                            val mcid = player.name
                            var ranking = 1
                            for (uuid in RankMgr.paintRanking) {
                                if (uuid == player.uniqueId.toString()) break
                                ranking++
                            }

                            val paint = PlayerStatusMgr.getPaint(player.uniqueId.toString())

                            you.customName =
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" +
                                            (if (paint == 0) "-" else ranking) + "位 §f" + mcid + "  §6Paint(s) : §r" + paint
                                    ),
                                )
                        }
                        list.add(you)
                    } catch (e: Exception) {
                    }

                    list.add(title)
                    list.add(separator)
                    list.add(mode)

                    if (player.isOnline && player.world === location.world) {
                        try {
                            for (armorStand in list) {
                                val destroyPacket =
                                    PacketPlayOutEntityDestroy(
                                        armorStand.bukkitEntity.entityId,
                                    )
                                (player as CraftPlayer).handle.playerConnection.sendPacket(destroyPacket)
                            }
                        } catch (e: Exception) {
                        }
                        try {
                            for (armorStand in list) {
                                val spawnPacket = PacketPlayOutSpawnEntityLiving(armorStand)
                                (player as CraftPlayer).handle.playerConnection.sendPacket(spawnPacket)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        async.runTaskAsynchronously(plugin)
    }
}
