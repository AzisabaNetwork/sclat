package be4rjp.sclat.commands

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil.isNumber
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.emblem.EmblemManager
import be4rjp.sclat.extension.component
import be4rjp.sclat.extension.sendMessage
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.plugin
import be4rjp.sclat.server.EquipmentClient
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.IOException
import java.util.Locale

// sclat Command
// Todo: use cloud command framework
class SclatCommandExecutor :
    CommandExecutor,
    TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        commandLabel: String,
        args: Array<String>?,
    ): Boolean {
        if (args == null) return false
        if (args.size == 0) return false

        // ------------------------Check sender type-----------------------------
        var type = CommanderType.CONSOLE
        if (sender is Player) {
            if (sender.hasPermission("sclat.admin")) {
                type = CommanderType.ADMIN
            } else {
                type = CommanderType.MEMBER
            }
        }

        // ----------------------------------------------------------------------

        // -------------------------/sclat setUpdateRate----------------------------
        if (args[0].equals("setUpdateRate", ignoreCase = true) || args[0].equals("sur", ignoreCase = true)) {
            if (args.size != 2) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(component("You don't have permission.", NamedTextColor.RED))
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            val num = args[1]
            if (isNumber(num)) {
                Sclat.conf?.config!!.set("BlockUpdateRate", num.toInt())
                sender.sendMessage(component("setConfig [BlockUpdateRate]", NamedTextColor.GREEN).append(component(" -> $num")))
                return true
            } else {
                sender.sendMessage(component("Please type with number", NamedTextColor.RED))
                return false
            }
        }

        // -------------------------------------------------------------------------

        // ----------------------------/sclat fly-----------------------------------
        if (args[0].equals("fly", ignoreCase = true)) {
            if (args.size != 2) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            val playerName = args[1]
            for (player in plugin.server.onlinePlayers) {
                if (playerName == player.name) {
                    Sclat.flyList.add(playerName)
                    return true
                }
            }
        }

        // -------------------------------------------------------------------------

        // ----- /sclat refresh-config-----
        if (args[0].equals("refresh-config", ignoreCase = true)) {
            if (args.size != 2) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            if (sender is Player) {
                val player = sender
                val targetConfig = args[1]
                sendMessage(String.format("%sの設定を再読み込み中...", targetConfig), MessageType.PLAYER, player)
                when (targetConfig.lowercase(Locale.getDefault())) {
                    "emblemuserdata" -> {
                        Sclat.conf?.loadEmblemUserData()
                    }

                    "emblemloredata" -> {
                        Sclat.conf?.loadEmblemLoreData()
                    }

                    else -> {
                        player.sendMessage(component("そのオプションは存在しません！", NamedTextColor.RED))
                        return true
                    }
                }
                player.sendMessage(component("再読み込み完了", NamedTextColor.GREEN))
            }
        }

        // --------------------------------

        // ----------------/sclat migrate-emblems-----------------
        if (args[0].equals("migrate-emblems", ignoreCase = true)) {
            if (args.size != 1) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(component("You don't have permission.", NamedTextColor.RED))
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            if (!Sclat.conf?.emblemsFile!!.exists()) {
                sender.sendMessage(component("Old emblem userdata file isn't exists.", NamedTextColor.RED))
                return true
            }

            val oldData = YamlConfiguration.loadConfiguration(Sclat.conf?.emblemsFile!!)
            val dataUuids = oldData.getKeys(false)
            for (_uuid in dataUuids) {
                val userEmblems = oldData.getStringList(_uuid)
                for (emblem in userEmblems) {
                    Sclat.conf?.emblemUserdata!!.set("$_uuid.$emblem", 1)
                }
            }
            sender.sendMessage(ChatColor.GREEN.toString() + "Migration was succeeded!")

            try {
                Sclat.conf?.saveEmblemUserdata()
                sender.sendMessage(component("Successfully to save emblem userdata", NamedTextColor.GREEN))
            } catch (e: IOException) {
                sender.sendMessage(component("Failed to save emblem userdata", NamedTextColor.RED))
                e.printStackTrace()
            }
            return true
        }

        // -------------------------------------------------------

        // ----------------/sclat save-emblems--------------------
        if (args[0].equals("save-emblems", ignoreCase = true)) {
            if (args.size != 1) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(component("You don't have permission.", NamedTextColor.RED))
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            try {
                Sclat.conf?.saveEmblemUserdata()
                sender.sendMessage(ChatColor.GREEN.toString() + "Successfully to save emblem userdata")
            } catch (e: IOException) {
                sender.sendMessage(ChatColor.RED.toString() + "Failed to save emblem userdata")
                e.printStackTrace()
            }
            return true
        }

        // -------------------------------------------------------

        // ----------------/sclat check-emblems-------------------
        if (args[0].equals("check-emblems", ignoreCase = true)) {
            if (args.size != 1) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            val dataMap = EmblemManager.dataMap
            for (_key in dataMap.keys) {
                sender.sendMessage(_key)
                dataMap
                    .getOrDefault(_key, HashMap<String?, Int?>())
                    .forEach { (k: String?, v: Int?) -> sender.sendMessage("- $k: $v") }
            }
        }

        // --------------------------------------------------------

        // ----------------------------/sclat mod-----------------------------------
        if (args[0].equals("mod", ignoreCase = true)) {
            if (args.size != 2) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            if (sender is Player) {
                val serverName: String = args[1]
                for (ss in ServerStatusManager.serverList) {
                    if (ss.serverName == serverName) {
                        val commands: MutableList<String?> = ArrayList()
                        commands.add("mod ${sender.name}")
                        commands.add("stop")
                        // Todo: use redis. fallbacks PluginMessaging
                        val sc =
                            EquipmentClient(
                                Sclat.conf?.config!!.getString("EquipShare.$serverName.Host"),
                                Sclat.conf?.config!!.getInt("EquipShare.$serverName.Port"),
                                commands,
                            )
                        sc.startClient()

                        sender.sendMessage("Moderatorとして転送中...")
                        sender.sendMessage("2秒後に転送されます")
                        playGameSound(sender, SoundType.SUCCESS)

                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                override fun run() {
                                    try {
                                        BungeeCordMgr.playerSendServer(sender, ss.serverName)
                                        DataMgr.getPlayerData(sender)?.setServerName(ss.displayName)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        task.runTaskLater(plugin, 40)
                    }
                }
                return true
            } else {
                return false
            }
        }

        // -------------------------------------------------------------------------

        // ------------------/sclat ss <status> <server> <flag>---------------------
        if (args[0].equals("ss", ignoreCase = true)) {
            if (args.size < 4 || Sclat.type != ServerType.LOBBY) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            if (args[1] == "mt") {
                val server: String = args[2]
                for (ss in ServerStatusManager.serverList) {
                    if (ss.serverName == server) {
                        ss.isMaintenance = (args[3] == "true")
                        sender.sendMessage(
                            (
                                "Switched " + ss.displayName + " §rto " +
                                    (if (args[3] == "true") "§cMAINTENANCE" else "§6NORMAL")
                            ),
                        )
                        return true
                    }
                }
            }
        }

        // -------------------------------------------------------------------------

        // ---------------------/sclat tutorial <option> <server>-------------------
        if (args[0].equals("tutorial", ignoreCase = true)) {
            if (args.size < 2 || Sclat.type != ServerType.LOBBY) return false

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED.toString() + "You don't have permission.")
                playGameSound(sender as Player, SoundType.ERROR)
                return true
            }

            if (args[1] == "add") {
                if (args.size < 3) return false
                val server: String = args[2]
                val list = Sclat.tutorialServers?.getConfig()!!.getStringList("server-list")
                if (!list.contains(server)) {
                    list.add(server)
                    Sclat.tutorialServers?.getConfig()!!.set("server-list", list)
                } else {
                    sender.sendMessage(ChatColor.RED.toString() + "This server is already exist.")
                }
                return true
            } else if (args[1] == "list") {
                val list = Sclat.tutorialServers?.getConfig()!!.getStringList("server-list")
                sender.sendMessage(list.toString())
                return true
            } else if (args[1] == "reload") {
                Sclat.tutorialServers!!.reloadConfig()
                return true
            }
        }
        // -------------------------------------------------------------------------
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String?>,
    ): MutableList<String?>? {
        // ------------------------Check sender type-----------------------------

        var type = CommanderType.CONSOLE
        if (sender is Player) {
            if (sender.hasPermission("sclat.admin")) {
                type = CommanderType.ADMIN
            } else {
                type = CommanderType.MEMBER
            }
        }

        // ----------------------------------------------------------------------

        // -----------------------------Tab complete-----------------------------
        if (args.size == 1) {
            val list: MutableList<String?> = ArrayList<String?>()

            list.add("help")

            if (type != CommanderType.MEMBER) {
                list.add("setUpdateRate")
                list.add("sur")
                list.add("fly")
                list.add("ss")
                list.add("tutorial")
                list.add("refresh-config")
                list.add("migrate-emblems")
                list.add("save-emblems")
                list.add("check-emblems")
            }

            return list
        } else if (args.size == 2) {
            val list: MutableList<String?> = ArrayList<String?>()
            if (type != CommanderType.MEMBER) {
                if (args[1].equals("refresh-config", ignoreCase = true)) {
                    list.add("emblemuserdata")
                    list.add("emblemloredata")
                }
            }
            return list
        }
        return null
        // ----------------------------------------------------------------------
    }
}
