package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.plugin
import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_14_R1.DataWatcherRegistry
import net.minecraft.server.v1_14_R1.EntityArmorStand
import net.minecraft.server.v1_14_R1.EntityPlayer
import net.minecraft.server.v1_14_R1.MinecraftServer
import net.minecraft.server.v1_14_R1.PacketPlayOutAnimation
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import net.minecraft.server.v1_14_R1.PlayerInteractManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_14_R1.CraftServer
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object PlayerStatusMgr {
    var list: MutableMap<Player?, EntityArmorStand> = HashMap<Player?, EntityArmorStand>()
    var list1: MutableMap<Player?, EntityArmorStand> = HashMap<Player?, EntityArmorStand>()
    var list2: MutableMap<Player?, EntityArmorStand> = HashMap<Player?, EntityArmorStand>()

    fun setupPlayerStatus(player: Player) {
        val playerUuid: String = player.uniqueId.toString()

        if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + playerUuid)
        ) {
            setDefaultStatus(player)
        } else if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + playerUuid + ".Money")
        ) {
            setDefaultStatus(player)
        }
    }

    fun setDefaultStatus(player: Player) {
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Money", 10000)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Lv", 0)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Rank", 0)
        val wlist: MutableList<String?> = ArrayList<String?>()
        wlist.add(
            Sclat.conf!!
                .config!!
                .getString("DefaultClass"),
        )
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".WeaponClass", wlist)
        val glist: MutableList<Int?> = ArrayList<Int?>()
        glist.add(0)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".GearList", glist)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Gear", 0)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Kill", 0)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Paint", 0)
        Sclat.conf!!.playerStatus.set(
            "Status." + player.uniqueId + ".EquiptClass",
            Sclat.conf!!
                .config!!
                .getString("DefaultClass"),
        )
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Tutorial", 0)
        // ガチャチケ用
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".Ticket", 0)
        addGear(player, 9)
        setGear(player, 9)
    }

    fun sendHologram(player: Player) {
        val w =
            Bukkit.getWorld(
                Sclat.conf!!
                    .config!!
                    .getString("Hologram.WorldName")!!,
            )
        val ix =
            Sclat.conf!!
                .config!!
                .getInt("Hologram.X")
        val iy =
            Sclat.conf!!
                .config!!
                .getInt("Hologram.Y")
        val iz =
            Sclat.conf!!
                .config!!
                .getInt("Hologram.Z")
        val iyaw =
            Sclat.conf!!
                .config!!
                .getInt("Hologram.Yaw")
        val location = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
        location.yaw = iyaw.toFloat()

        val nmsServer: MinecraftServer = (Bukkit.getServer() as CraftServer).server
        val nmsWorld = (location.world as CraftWorld).handle
        val gameProfile = GameProfile(player.uniqueId, player.name)

        val npc = EntityPlayer(nmsServer, nmsWorld, gameProfile, PlayerInteractManager(nmsWorld))
        npc.setLocation(location.x, location.y, location.z, location.yaw, 0f)
        npc.dataWatcher.set<Byte?>(DataWatcherRegistry.a.a(15), 127.toByte())

        val connection = (player as CraftPlayer).handle.playerConnection
        connection
            .sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc))
        connection.sendPacket(PacketPlayOutNamedEntitySpawn(npc))
        connection.sendPacket(
            PacketPlayOutEntityHeadRotation(
                npc,
                ((location.yaw * 256.0f) / 360.0f).toInt().toByte(),
            ),
        )
        connection.sendPacket(PacketPlayOutAnimation(npc, 0))
        connection.sendPacket(PacketPlayOutEntityMetadata(npc.id, npc.dataWatcher, true))

        val `as` = EntityArmorStand(nmsWorld, location.x, location.y + 0.8, location.z)
        `as`.setLocation(location.x, location.y + 0.8, location.z, location.yaw, 0f)
        `as`.isInvisible = true
        `as`.customNameVisible = true
        `as`.isNoGravity = true
        `as`.customName = CraftChatMessage.fromStringOrNull("§aMoney : §r" + getMoney(player) + "  §aLv : §r" + getLv(player))

        list.put(player, `as`)

        val as1 = EntityArmorStand(nmsWorld, location.x, location.y + 1.2, location.z)
        as1.setLocation(location.x, location.y + 1.2, location.z, location.yaw, 0f)
        as1.isInvisible = true
        as1.customNameVisible = true
        as1.isNoGravity = true
        as1.customName =
            CraftChatMessage.fromStringOrNull(
                "§6Rank : §r" + getRank(player) + "  [ §b" + RankMgr.toABCRank(getRank(player)) + " §r]",
            )

        list1.put(player, as1)

        val as2 = EntityArmorStand(nmsWorld, location.x, location.y + 0.4, location.z)
        as2.setLocation(location.x, location.y + 0.4, location.z, location.yaw, 0f)
        as2.isInvisible = true
        as2.customNameVisible = true
        as2.isNoGravity = true
        as2.customName =
            CraftChatMessage
                .fromStringOrNull("§aPaints : §r" + getPaint(player) + "  §aKills : §r" + getKill(player))

        list2.put(player, as2)

        connection.sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
        connection.sendPacket(PacketPlayOutSpawnEntityLiving(as1))
        connection.sendPacket(PacketPlayOutSpawnEntityLiving(as2))
    }

    fun hologramUpdateRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    if (!player.isOnline) cancel()
                    try {
                        val `as`: EntityArmorStand = list.get(player)!!
                        val connection = (player as CraftPlayer).handle.playerConnection
                        connection.sendPacket(PacketPlayOutEntityDestroy(`as`.bukkitEntity.entityId))
                        `as`.customName =
                            CraftChatMessage
                                .fromStringOrNull("§aMoney : §r" + getMoney(player) + "  §aLv : §r" + getLv(player))
                        connection.sendPacket(PacketPlayOutSpawnEntityLiving(`as`))

                        val as1: EntityArmorStand = list1.get(player)!!
                        connection.sendPacket(PacketPlayOutEntityDestroy(as1.bukkitEntity.entityId))
                        as1.customName =
                            CraftChatMessage.fromStringOrNull(
                                "§6Rank : §r" + getRank(player) + "  [ §b" + RankMgr.toABCRank(getRank(player)) + " §r]",
                            )
                        connection.sendPacket(PacketPlayOutSpawnEntityLiving(as1))

                        val as2: EntityArmorStand = list2.get(player)!!
                        connection.sendPacket(PacketPlayOutEntityDestroy(as2.bukkitEntity.entityId))
                        as2.customName =
                            CraftChatMessage
                                .fromStringOrNull("§aPaints : §r" + getPaint(player) + "  §aKills : §r" + getKill(player))
                        connection.sendPacket(PacketPlayOutSpawnEntityLiving(as2))
                    } catch (e: Exception) {
                    }
                }
            }
        task.runTaskTimer(
            plugin,
            0,
            Sclat.conf!!
                .config!!
                .getInt("HologramUpdatePeriod")
                .toLong(),
        )
    }

    @JvmStatic
    fun sendHologramUpdate(player: Player) {
        val `as`: EntityArmorStand = list.get(player)!!
        val connection = (player as CraftPlayer).handle.playerConnection
        connection.sendPacket(PacketPlayOutEntityDestroy(`as`.bukkitEntity.entityId))
        `as`.customName = CraftChatMessage.fromStringOrNull("§aMoney : §r" + getMoney(player) + "  §aLv : §r" + getLv(player))
        connection.sendPacket(PacketPlayOutSpawnEntityLiving(`as`))
    }

    @JvmStatic
    fun haveWeapon(
        player: Player,
        wname: String?,
    ): Boolean {
        val wlist =
            Sclat.conf!!
                .playerStatus
                .getStringList("Status." + player.uniqueId + ".WeaponClass")
        return wlist.contains(wname)
    }

    @JvmStatic
    fun haveGear(
        player: Player,
        g: Int,
    ): Boolean {
        val glist =
            Sclat.conf!!
                .playerStatus
                .getIntegerList("Status." + player.uniqueId + ".GearList")
        return glist.contains(g)
    }

    fun setRank(
        player: Player,
        rank: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".Rank", rank)
    }

    fun setRank(
        uuid: String?,
        rank: Int,
    ) {
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".Rank", rank)
    }

    fun setLv(
        uuid: String?,
        lv: Int,
    ) {
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".Lv", lv)
    }

    @JvmStatic
    fun setGear(
        player: Player,
        g: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".Gear", g)
    }

    fun setEquiptClass(
        player: Player,
        name: String?,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".EquiptClass", name)
    }

    fun setTutorialState(
        uuid: String?,
        g: Int,
    ) {
        Sclat.conf!!
            .playerStatus
            .set("Status." + uuid + ".Tutorial", g)
    }

    @JvmStatic
    fun addWeapon(
        player: Player,
        wname: String?,
    ) {
        val wlist =
            Sclat.conf!!
                .playerStatus
                .getStringList("Status." + player.uniqueId + ".WeaponClass")
        wlist.add(wname)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".WeaponClass", wlist)
    }

    @JvmStatic
    fun addGear(
        player: Player,
        g: Int,
    ) {
        val glist =
            Sclat.conf!!
                .playerStatus
                .getIntegerList("Status." + player.uniqueId + ".GearList")
        glist.add(g)
        Sclat.conf!!
            .playerStatus
            .set("Status." + player.uniqueId + ".GearList", glist)
    }

    @JvmStatic
    fun addMoney(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Money",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Money") + m,
        )
    }

    fun addMoney(
        uuid: String?,
        m: Int,
    ) {
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Money",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Money") + m,
        )
    }

    @JvmStatic
    fun subMoney(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Money",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Money") - m,
        )
    }

    fun addLv(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Lv",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Lv") + m,
        )
    }

    fun addRank(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        if (getRank(player) + m > 0) {
            Sclat.conf!!.playerStatus.set(
                "Status." + uuid + ".Rank",
                Sclat.conf!!
                    .playerStatus
                    .getInt("Status." + uuid + ".Rank") + m,
            )
        } else {
            Sclat.conf!!
                .playerStatus
                .set("Status." + uuid + ".Rank", 0)
        }
    }

    fun addKill(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Kill",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Kill") + m,
        )
    }

    fun addPaint(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Paint",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Paint") + m,
        )
    }

    fun addLv(
        uuid: String?,
        m: Int,
    ) {
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Lv",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Lv") + m,
        )
    }

    fun addRank(
        uuid: String?,
        m: Int,
    ) {
        if (getRank(uuid) + m > 0) {
            Sclat.conf!!.playerStatus.set(
                "Status." + uuid + ".Rank",
                Sclat.conf!!
                    .playerStatus
                    .getInt("Status." + uuid + ".Rank") + m,
            )
        } else {
            Sclat.conf!!
                .playerStatus
                .set("Status." + uuid + ".Rank", 0)
        }
    }

    fun addKill(
        uuid: String?,
        m: Int,
    ) {
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Kill",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Kill") + m,
        )
    }

    fun addPaint(
        uuid: String?,
        m: Int,
    ) {
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Paint",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Paint") + m,
        )
    }

    @JvmStatic
    fun getMoney(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Money")
    }

    @JvmStatic
    fun getLv(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Lv")
    }

    fun getLv(uuid: String?): Int =
        Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Lv")

    @JvmStatic
    fun getRank(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Rank")
    }

    fun getRank(uuid: String?): Int =
        Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Rank")

    fun getGear(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Gear")
    }

    @JvmStatic
    fun getKill(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Kill")
    }

    fun getKill(uuid: String?): Int =
        Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Kill")

    @JvmStatic
    fun getPaint(player: Player): Int {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Paint")
    }

    fun getPaint(uuid: String?): Int =
        Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Paint")

    fun getEquiptClass(player: Player): String? {
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getString("Status." + uuid + ".EquiptClass")
    }

    fun getTutorialState(uuid: String?): Int =
        Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Tutorial")

    fun addTicket(
        player: Player,
        m: Int,
    ) {
        val uuid: String = player.uniqueId.toString()
        if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + uuid + ".Ticket")
        ) {
            Sclat.conf!!
                .playerStatus
                .set("Status." + uuid + ".Ticket", 0)
        }
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Ticket",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Ticket") + m,
        )
    }

    fun addTicketUuid(
        uuid: String?,
        m: Int,
    ) {
        if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + uuid + ".Ticket")
        ) {
            Sclat.conf!!
                .playerStatus
                .set("Status." + uuid + ".Ticket", 0)
        }
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Ticket",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Ticket") + m,
        )
    }

    @JvmStatic
    fun subTicket(
        player: Player,
        m: Int,
    ) {
        if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + player.uniqueId + ".Ticket")
        ) {
            Sclat.conf!!
                .playerStatus
                .set("Status." + player.uniqueId + ".Ticket", 0)
        }
        val uuid: String = player.uniqueId.toString()
        Sclat.conf!!.playerStatus.set(
            "Status." + uuid + ".Ticket",
            Sclat.conf!!
                .playerStatus
                .getInt("Status." + uuid + ".Ticket") - m,
        )
    }

    @JvmStatic
    fun getTicket(player: Player): Int {
        if (!Sclat.conf!!
                .playerStatus
                .contains("Status." + player.uniqueId + ".Ticket")
        ) {
            Sclat.conf!!
                .playerStatus
                .set("Status." + player.uniqueId + ".Ticket", 0)
        }
        val uuid: String = player.uniqueId.toString()
        return Sclat.conf!!
            .playerStatus
            .getInt("Status." + uuid + ".Ticket")
    }
}
