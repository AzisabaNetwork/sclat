package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.ServerStatus
import be4rjp.sclat.plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object ServerStatusManager {
    var inv: Inventory = Bukkit.createInventory(null, 18, "Server List")

    @JvmField
    var serverList: MutableList<ServerStatus> = ArrayList<ServerStatus>()

    var task: BukkitRunnable? = null

    fun setupServerStatusGUI() {
        for (server in Sclat.Companion.conf!!
            .servers!!
            .getConfigurationSection("Servers")!!
            .getKeys(false)) {
            val serverName =
                Sclat.Companion.conf!!
                    .servers!!
                    .getString("Servers." + server + ".Server")
            val displayName =
                Sclat.Companion.conf!!
                    .servers!!
                    .getString("Servers." + server + ".DisplayName")
            val maxPlayer =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".MaxPlayer")
            val host =
                Sclat.Companion.conf!!
                    .servers!!
                    .getString("Servers." + server + ".Host")
            val port =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".Port")
            val period =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".Period")

            val WorldName =
                Sclat.Companion.conf!!
                    .servers!!
                    .getString("Servers." + server + ".Sign.WorldName")
            val w = Bukkit.getWorld(WorldName!!)
            val ix =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".Sign.X")
            val iy =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".Sign.Y")
            val iz =
                Sclat.Companion.conf!!
                    .servers!!
                    .getInt("Servers." + server + ".Sign.Z")
            val loc = Location(w, ix.toDouble(), iy.toDouble(), iz.toDouble())

            var info: String? = ""
            if (Sclat.Companion.conf!!
                    .servers!!
                    .contains("Servers." + server + ".Info")
            ) {
                info =
                    Sclat.Companion.conf!!
                        .servers!!
                        .getString("Servers." + server + ".Info")
            }

            val ss =
                ServerStatus(
                    serverName,
                    displayName!!,
                    host,
                    port,
                    maxPlayer,
                    period,
                    loc.getBlock(),
                    info,
                )

            if (Sclat.Companion.conf!!
                    .servers!!
                    .contains("Servers." + server + ".maintenance")
            ) {
                ss.isMaintenance =
                    Sclat.Companion.conf!!
                        .servers!!
                        .getBoolean("Servers." + server + ".maintenance")
            }

            serverList.add(ss)
        }

        task =
            object : BukkitRunnable() {
                override fun run() {
                    inv.clear()

                    run {
                        var i = 0
                        while (i <= 17) {
                            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                            val ism = `is`.getItemMeta()
                            ism!!.setDisplayName(".")
                            `is`.setItemMeta(ism)
                            inv.setItem(i, `is`)
                            i++
                        }
                    }

                    val ism = ItemStack(Material.OAK_DOOR)
                    val ismm = ism.getItemMeta()
                    ismm!!.setDisplayName("戻る")
                    ism.setItemMeta(ismm)
                    inv.setItem(17, ism)

                    var i = 0
                    for (ss in serverList) {
                        var mt = Material.LIME_STAINED_GLASS
                        if (ss.runningMatch) mt = Material.YELLOW_STAINED_GLASS
                        if (ss.playerCount >= ss.maxPlayer) mt = Material.RED_STAINED_GLASS
                        if (!ss.isOnline || ss.restartingServer) mt = Material.IRON_BARS

                        val `is` = ItemStack(mt)
                        val itemMeta = `is`.getItemMeta()
                        itemMeta!!.setDisplayName(ss.displayName)
                        val role: MutableList<String?> = ArrayList<String?>()
                        if (ss.restartingServer) {
                            role.add("")
                            role.add("§r§7[Status]  §eRESTARTING...")
                        } else {
                            if (ss.isOnline) {
                                var amount = 1
                                if (0 < ss.playerCount && ss.playerCount <= 64) amount = ss.playerCount
                                `is`.setAmount(amount)
                                role.add("")
                                role.add("§r§7[Player]  §r§a" + ss.playerCount + "§r§7 / " + ss.maxPlayer)
                                role.add("")
                                role.add("§r§7[Status]  §aONLINE")
                                role.add("")
                                role.add("§r§7[Match]  " + (if (ss.runningMatch) "§cACTIVE" else "§aINACTIVE"))
                                if (!ss.mapName!!.isEmpty()) {
                                    role.add("")
                                    role.add("§r§7[Map]  §f§l" + ss.mapName)
                                }
                                if (!ss.info!!.isEmpty()) {
                                    role.add("")
                                    role.add("§r§7[Info]  §f§l" + ss.info)
                                }
                            } else {
                                role.add("")
                                role.add(if (ss.isMaintenance) "§r§7[Status]  §cMAINTENANCE" else "§r§7[Status]  §cOFFLINE")
                            }
                        }
                        itemMeta.setLore(role)
                        `is`.setItemMeta(itemMeta)

                        inv.setItem(i, `is`)
                        i++
                    }
                }
            }
        task!!.runTaskTimer(plugin, 0, 40)
    }

    @JvmStatic
    fun openServerList(player: Player) {
        player.openInventory(inv)
    }

    fun stopTask() {
        task!!.cancel()
    }
}
