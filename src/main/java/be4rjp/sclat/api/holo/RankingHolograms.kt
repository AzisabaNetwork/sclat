package be4rjp.sclat.api.holo

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.RankingType
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.manager.RankMgr
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

        val WorldName = Sclat.conf?.config!!.getString("RankingHolograms.WorldName")
        val w = Bukkit.getWorld(WorldName!!)
        val ix = Sclat.conf?.config!!.getDouble("RankingHolograms.X")
        val iy = Sclat.conf?.config!!.getDouble("RankingHolograms.Y")
        val iz = Sclat.conf?.config!!.getDouble("RankingHolograms.Z")
        location = Location(w, ix + 0.5, iy, iz + 0.5)

        val nmsWorld = (location.getWorld() as CraftWorld).getHandle()

        title = EntityArmorStand(nmsWorld, location.getX(), location.getY() + 2.5, location.getZ())
        title.setNoGravity(true)
        title.setPosition(location.getX(), location.getY() + 2.5, location.getZ())
        title.setBasePlate(false)
        title.setInvisible(true)
        title.setSmall(true)
        title.setCustomName(CraftChatMessage.fromStringOrNull("§a----------- §b§lTotal Ranking §r§a-----------"))
        title.setCustomNameVisible(true)
        armorStandList.add(title)

        separator = EntityArmorStand(nmsWorld, location.getX(), location.getY() + 0.0, location.getZ())
        separator.setNoGravity(true)
        separator.setPosition(location.getX(), location.getY() + 0.0, location.getZ())
        separator.setBasePlate(false)
        separator.setInvisible(true)
        separator.setSmall(true)
        separator.setCustomName(CraftChatMessage.fromStringOrNull("§a-------------------------------------"))
        separator.setCustomNameVisible(true)
        armorStandList.add(separator)

        you = EntityArmorStand(nmsWorld, location.getX(), location.getY() - 0.4, location.getZ())
        you.setNoGravity(true)
        you.setPosition(location.getX(), location.getY() - 0.4, location.getZ())
        you.setBasePlate(false)
        you.setInvisible(true)
        you.setSmall(true)
        you.setCustomName(CraftChatMessage.fromStringOrNull("--"))
        you.setCustomNameVisible(true)
        armorStandList.add(you)

        mode = EntityArmorStand(nmsWorld, location.getX(), location.getY() - 0.8, location.getZ())
        mode.setNoGravity(true)
        mode.setPosition(location.getX(), location.getY() - 0.8, location.getZ())
        mode.setBasePlate(false)
        mode.setInvisible(true)
        mode.setSmall(true)
        mode.setCustomName(CraftChatMessage.fromStringOrNull("§a§l[Total] §7§l[Kill] [Paint]"))
        mode.setCustomNameVisible(true)
        armorStandList.add(mode)

        clickHit1 = EntityArmorStand(nmsWorld, location.getX() + 0.3, location.getY() + 0.0, location.getZ() + 0.3)
        clickHit1.setNoGravity(true)
        clickHit1.setPosition(location.getX() + 0.3, location.getY() + 0.0, location.getZ() + 0.3)
        clickHit1.setBasePlate(false)
        clickHit1.setInvisible(true)
        clickHit1.setCustomName(CraftChatMessage.fromStringOrNull("clickHit1"))
        clickHit1.setCustomNameVisible(false)
        armorStandList.add(clickHit1)

        clickHit2 = EntityArmorStand(nmsWorld, location.getX() + 0.3, location.getY() + 0.0, location.getZ() - 0.3)
        clickHit2.setNoGravity(true)
        clickHit2.setPosition(location.getX() + 0.3, location.getY() + 0.0, location.getZ() - 0.3)
        clickHit2.setBasePlate(false)
        clickHit2.setInvisible(true)
        clickHit2.setCustomName(CraftChatMessage.fromStringOrNull("clickHit2"))
        clickHit2.setCustomNameVisible(false)
        armorStandList.add(clickHit2)

        clickHit3 = EntityArmorStand(nmsWorld, location.getX() - 0.3, location.getY() + 0.0, location.getZ() - 0.3)
        clickHit3.setNoGravity(true)
        clickHit3.setPosition(location.getX() - 0.3, location.getY() + 0.0, location.getZ() - 0.3)
        clickHit3.setBasePlate(false)
        clickHit3.setInvisible(true)
        clickHit3.setCustomName(CraftChatMessage.fromStringOrNull("clickHit3"))
        clickHit3.setCustomNameVisible(false)
        armorStandList.add(clickHit3)

        clickHit4 = EntityArmorStand(nmsWorld, location.getX() - 0.3, location.getY() + 0.0, location.getZ() + 0.3)
        clickHit4.setNoGravity(true)
        clickHit4.setPosition(location.getX() - 0.3, location.getY() + 0.0, location.getZ() + 0.3)
        clickHit4.setBasePlate(false)
        clickHit4.setInvisible(true)
        clickHit4.setCustomName(CraftChatMessage.fromStringOrNull("clickHit4"))
        clickHit4.setCustomNameVisible(false)
        armorStandList.add(clickHit4)

        rankArmorStands = ArrayList<EntityArmorStand>()
        for (i in 0..4) {
            val armorStand =
                EntityArmorStand(
                    nmsWorld,
                    location.getX(),
                    location.getY() + 2.0 - (0.4 * i.toDouble()),
                    location.getZ(),
                )
            armorStand.setNoGravity(true)
            armorStand.setPosition(location.getX(), location.getY() + 2.0 - (0.4 * i.toDouble()), location.getZ())
            armorStand.setBasePlate(false)
            armorStand.setInvisible(true)
            armorStand.setSmall(true)
            armorStand.setCustomName(CraftChatMessage.fromStringOrNull("--"))
            armorStand.setCustomNameVisible(true)
            rankArmorStands.add(armorStand)
            armorStandList.add(armorStand)
        }

        refreshRankingAsync()
    }

    fun switchNextRankingType() {
        when (rankingType) {
            RankingType.TOTAL -> {
                rankingType = RankingType.KILL
                title.setCustomName(
                    CraftChatMessage.fromStringOrNull("§a----------- §b§lKill Ranking §r§a-----------"),
                )
                separator.setCustomName(CraftChatMessage.fromStringOrNull("§a------------------------------------"))
                mode.setCustomName(CraftChatMessage.fromStringOrNull("§7§l[Total] §a§l[Kill] §7§l[Paint]"))
            }

            RankingType.KILL -> {
                rankingType = RankingType.PAINT
                title.setCustomName(
                    CraftChatMessage.fromStringOrNull("§a----------- §b§lPaint Ranking §r§a-----------"),
                )
                separator.setCustomName(CraftChatMessage.fromStringOrNull("§a-------------------------------------"))
                mode.setCustomName(CraftChatMessage.fromStringOrNull("§7§l[Total] [Kill] §a§l[Paint]"))
            }

            RankingType.PAINT -> {
                rankingType = RankingType.TOTAL
                title.setCustomName(
                    CraftChatMessage.fromStringOrNull("§a----------- §b§lTotal Ranking §r§a-----------"),
                )
                separator.setCustomName(CraftChatMessage.fromStringOrNull("§a-------------------------------------"))
                mode.setCustomName(CraftChatMessage.fromStringOrNull("§a§l[Total] §7§l[Kill] [Paint]"))
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
                                val mcid = Sclat.conf.uUIDCash.getString(uuid)

                                val rank = PlayerStatusMgr.getRank(uuid)

                                if (rank != 0) {
                                    armorStand.setCustomName(
                                        CraftChatMessage.fromStringOrNull(
                                            (
                                                "§e" + (i + 1).toString() + "位 §f" + mcid +
                                                    "  §6Rank : §r" + rank + " [§b " + RankMgr.toABCRank(rank) + " §f]"
                                                ),
                                        ),
                                    )
                                } else {
                                    armorStand.setCustomName(CraftChatMessage.fromStringOrNull("--"))
                                }
                            }

                            if (rankingType == RankingType.KILL) {
                                val uuid = RankMgr.killRanking.get(i)
                                val mcid = Sclat.conf.uUIDCash.getString(uuid)

                                val kill = PlayerStatusMgr.getKill(uuid)

                                if (kill != 0) {
                                    armorStand.setCustomName(
                                        CraftChatMessage.fromStringOrNull(
                                            "§e" + (i + 1).toString() + "位 §f" + mcid + "  §6Kill(s) : §r" + kill,
                                        ),
                                    )
                                } else {
                                    armorStand.setCustomName(CraftChatMessage.fromStringOrNull("--"))
                                }
                            }

                            if (rankingType == RankingType.PAINT) {
                                val uuid = RankMgr.paintRanking.get(i)
                                val mcid = Sclat.conf.uUIDCash.getString(uuid)

                                val paint = PlayerStatusMgr.getPaint(uuid)

                                if (paint != 0) {
                                    armorStand.setCustomName(
                                        CraftChatMessage.fromStringOrNull(
                                            "§e" + (i + 1).toString() + "位 §f" + mcid + "  §6Paint(s) : §r" + paint,
                                        ),
                                    )
                                } else {
                                    armorStand.setCustomName(CraftChatMessage.fromStringOrNull("--"))
                                }
                            }
                        } catch (e: Exception) {
                        }
                        i++
                    }

                    try {
                        if (rankingType == RankingType.TOTAL) {
                            val mcid = player.getName()
                            var ranking = 1
                            for (uuid in RankMgr.ranking) {
                                if (uuid == player.getUniqueId().toString()) break
                                ranking++
                            }

                            val rank = PlayerStatusMgr.getRank(player.getUniqueId().toString())

                            you.setCustomName(
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" + (if (rank == 0) "-" else ranking) +
                                            "位 §f" + mcid + "  §6Rank : §r" + rank + " [§b " + RankMgr.toABCRank(rank) + " §f]"
                                        ),
                                ),
                            )
                        }

                        if (rankingType == RankingType.KILL) {
                            val mcid = player.getName()
                            var ranking = 1
                            for (uuid in RankMgr.killRanking) {
                                if (uuid == player.getUniqueId().toString()) break
                                ranking++
                            }

                            val kill = PlayerStatusMgr.getKill(player.getUniqueId().toString())

                            you.setCustomName(
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" + (if (kill == 0) "-" else ranking) +
                                            "位 §f" + mcid + "  §6Kill(s) : §r" + kill
                                        ),
                                ),
                            )
                        }

                        if (rankingType == RankingType.PAINT) {
                            val mcid = player.getName()
                            var ranking = 1
                            for (uuid in RankMgr.paintRanking) {
                                if (uuid == player.getUniqueId().toString()) break
                                ranking++
                            }

                            val paint = PlayerStatusMgr.getPaint(player.getUniqueId().toString())

                            you.setCustomName(
                                CraftChatMessage.fromStringOrNull(
                                    (
                                        "§aYou ->> §e" +
                                            (if (paint == 0) "-" else ranking) + "位 §f" + mcid + "  §6Paint(s) : §r" + paint
                                        ),
                                ),
                            )
                        }
                        list.add(you)
                    } catch (e: Exception) {
                    }

                    list.add(title)
                    list.add(separator)
                    list.add(mode)

                    if (player.isOnline() && player.getWorld() === location.getWorld()) {
                        try {
                            for (armorStand in list) {
                                val destroyPacket =
                                    PacketPlayOutEntityDestroy(
                                        armorStand.getBukkitEntity().getEntityId(),
                                    )
                                (player as CraftPlayer).getHandle().playerConnection.sendPacket(destroyPacket)
                            }
                        } catch (e: Exception) {
                        }
                        try {
                            for (armorStand in list) {
                                val spawnPacket = PacketPlayOutSpawnEntityLiving(armorStand)
                                (player as CraftPlayer).getHandle().playerConnection.sendPacket(spawnPacket)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        async.runTaskAsynchronously(Sclat.getPlugin())
    }
}
