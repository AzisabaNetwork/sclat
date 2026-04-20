package be4rjp.sclat.command

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.emblem.EmblemManager
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.manager.MapLoader
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.server.EquipmentClient
import net.azisaba.sclat.core.DelegatedLogger
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser
import java.io.IOException

object SclatCommands {
    private val logger by DelegatedLogger()
    const val PERMISSION_ADMIN = "sclat.admin"

    fun init(plugin: JavaPlugin) {
        val commandManager =
            LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.simpleCoordinator(),
            )

        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions()
        } else {
            logger.warn("Are you using old spigot? We can't handle command over this cloud framework.")
        }

        val sclat = commandManager.commandBuilder("sclat").permission(PERMISSION_ADMIN)
        commandManager.command(
            sclat
                .literal("setUpdateRate", "sur")
                .required("rate", IntegerParser.integerParser())
                .handler { context ->
                    val rate = context.get<Int>("rate")
                    Sclat.conf?.config!!.set("BlockUpdateRate", rate)
                    context.sender().sendMessage("setConfig [BlockUpdateRate]  :  $rate")
                },
        )

        commandManager.command(
            sclat
                .literal("fly")
                .required("player", PlayerParser.playerParser())
                .handler { context ->
                    val player = context.get<Player>("player")
                    Sclat.flyList.add(player.name)
                },
        )

        val refreshConfig = sclat.literal("refresh-config")
        commandManager.command(
            refreshConfig
                .literal("emblemuserdata")
                .handler { context -> Sclat.conf?.loadEmblemUserData() },
        )
        commandManager.command(
            refreshConfig
                .literal("emblemloredata")
                .handler { context ->
                    Sclat.conf?.loadEmblemLoreData()
                },
        )

        commandManager.command(
            sclat.literal("migrate-emblems").handler { context ->
                if (!Sclat.conf?.emblemsFile!!.exists()) {
                    context.sender().sendMessage(ChatColor.RED.toString() + "Old emblem userdata file isn't exists.")
                }

                val oldData = YamlConfiguration.loadConfiguration(Sclat.conf?.emblemsFile!!)
                val dataUuids = oldData.getKeys(false)
                for (_uuid in dataUuids) {
                    val userEmblems = oldData.getStringList(_uuid)
                    for (emblem in userEmblems) {
                        Sclat.conf?.emblemUserdata!!.set("$_uuid.$emblem", 1)
                    }
                }
                context.sender().sendMessage(ChatColor.GREEN.toString() + "Migration was succeeded!")

                try {
                    Sclat.conf?.saveEmblemUserdata()
                    context.sender().sendMessage(ChatColor.GREEN.toString() + "Successfully to save emblem userdata")
                } catch (e: IOException) {
                    context.sender().sendMessage(ChatColor.RED.toString() + "Failed to save emblem userdata")
                    e.printStackTrace()
                }
            },
        )

        commandManager.command(
            sclat.literal("save-emblems").handler { context ->
                try {
                    Sclat.conf?.saveEmblemUserdata()
                    context.sender().sendMessage(ChatColor.GREEN.toString() + "Successfully to save emblem userdata")
                } catch (e: IOException) {
                    context.sender().sendMessage(ChatColor.RED.toString() + "Failed to save emblem userdata")
                    e.printStackTrace()
                }
            },
        )

        commandManager.command(
            sclat.literal("check-emblems").handler { context ->
                val dataMap = EmblemManager.dataMap
                for (key in dataMap.keys) {
                    context.sender().sendMessage(key)
                    val playerUuids = dataMap.getOrDefault(key, HashMap<String?, Int?>())
                    playerUuids.forEach { (k: String?, v: Int?) -> context.sender().sendMessage("- $k: $v") }
                }
            },
        )

        commandManager.command(
            sclat
                .literal("mod")
                .required("serverName", StringParser.stringParser())
                .handler { context ->
                    val serverName = context.get<String>("serverName")
                    val sender = context.sender()
                    if (sender !is Player) return@handler
                    for (ss in ServerStatusManager.serverList) {
                        if (ss.serverName == serverName) {
                            val commands: MutableList<String?> = ArrayList()
                            commands.add("mod " + context.sender().name)
                            commands.add("stop")
                            // Todo: use redis. fallbacks PluginMessaging
                            val sc =
                                EquipmentClient(
                                    Sclat.conf?.config!!.getString("EquipShare.$serverName.Host"),
                                    Sclat.conf?.config!!.getInt("EquipShare.$serverName.Port"),
                                    commands,
                                )
                            sc.startClient()

                            context.sender().sendMessage("Moderatorとして転送中...")
                            context.sender().sendMessage("2秒後に転送されます")

                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    override fun run() {
                                        try {
                                            BungeeCordMgr.playerSendServer(sender, ss.serverName)
                                            DataMgr.getPlayerData(sender)?.setServerName(ss.displayName)
                                        } catch (ignored: Exception) {
                                        }
                                    }
                                }
                            task.runTaskLater(be4rjp.sclat.plugin, 40)
                        }
                    }
                },
        )

        val setStatus = sclat.literal("setStatus", "ss")
        commandManager.command(
            setStatus
                .literal("mt")
                .required("serverName", StringParser.stringParser())
                .required("flag", BooleanParser.booleanParser())
                .handler { context ->
                    val serverName = context.get<String>("serverName")
                    val flag = context.get<Boolean>("flag")
                    for (ss in ServerStatusManager.serverList) {
                        if (ss.serverName == serverName) {
                            ss.isMaintenance = flag
                            context.sender().sendMessage(
                                (
                                    "Switched " + ss.displayName + " §rto " +
                                        (if (flag) "§cMAINTENANCE" else "§6NORMAL")
                                ),
                            )
                        }
                    }
                },
        )

        val tutorial = sclat.literal("tutorial")
        commandManager.command(
            tutorial
                .literal("add")
                .required("serverName", StringParser.stringParser())
                .handler { context ->
                    val serverName = context.get<String>("serverName")
                    val list = Sclat.tutorialServers?.getConfig()!!.getStringList("server-list")
                    if (!list.contains(serverName)) {
                        list.add(serverName)
                        Sclat.tutorialServers?.getConfig()!!.set("server-list", list)
                    } else {
                        context.sender().sendMessage(ChatColor.RED.toString() + "This server is already exist.")
                    }
                },
        )
        commandManager.command(
            tutorial
                .literal("list")
                .handler { context ->
                    val list = Sclat.tutorialServers?.getConfig()!!.getStringList("server-list")
                    context.sender().sendMessage(list.toString())
                },
        )
        commandManager.command(
            tutorial
                .literal("reload")
                .handler { context -> Sclat.tutorialServers!!.reloadConfig() },
        )

        val map = sclat.literal("map")
        commandManager.command(
            map
                .literal("preload")
                .required("mapName", StringParser.stringParser())
                .handler { context ->
                    val mapName = context.get<String>("mapName")
                    if (mapName == "all") {
                        // preload all maps
                        DataMgr.maplist.forEach { m -> MapLoader.incrementUsage(m) }
                        context.sender().sendMessage("Preloaded al maps (requested)")
                    } else {
                        // preload specific map
                        val map =
                            DataMgr.maplist.find { it.mapName == mapName } ?: run {
                                context.sender().sendMessage("Map not found: $mapName")
                                return@handler
                            }
                        try {
                            MapLoader.incrementUsage(map)
                            context.sender().sendMessage("Preloaded map: $mapName")
                        } catch (e: Exception) {
                            context.sender().sendMessage("Failed to preload map: $mapName")
                        }
                    }
                },
        )
        commandManager.command(
            map
                .literal("unload")
                .required("mapName", StringParser.stringParser())
                .handler { context ->
                    val sender = context.sender()
                    val mapName = context.get<String>("mapName")
                    if (mapName == "all") {
                        try {
                            MapLoader.unloadAllLoadedMaps()
                            sender.sendMessage("Unload requested for all maps")
                        } catch (e: Exception) {
                            sender.sendMessage("Failed to request unload for all maps")
                        }
                        return@handler
                    }
                    val map =
                        DataMgr.maplist.find { it.mapName == mapName }
                            ?: run {
                                sender.sendMessage("Map not found: $mapName")
                                return@handler
                            }
                    try {
                        MapLoader.attemptUnload(map, true)
                        sender.sendMessage("Unload requested for map: $mapName")
                    } catch (e: Exception) {
                        sender.sendMessage("Failed to request unload for map: $mapName")
                    }
                },
        )
        commandManager.command(
            map
                .literal("status", "list")
                .optional("serverName", StringParser.stringParser())
                .handler { context ->
                    val sender = context.sender()
                    val mapName = context.getOrDefault("serverName", "")
                    if (mapName.isNotEmpty()) {
                        val map = DataMgr.maplist.find { it.mapName == mapName }
                        if (map == null) {
                            sender.sendMessage("Map not found: $mapName")
                            return@handler
                        }
                        val loaded = if (map.team0Loc != null || map.wiremeshListTask != null) "LOADED" else "UNLOADED"
                        sender.sendMessage("$mapName : $loaded")
                        return@handler
                    }
                    sender.sendMessage("Maps:")
                    for (m in DataMgr.maplist) {
                        val loaded = if (m.team0Loc != null || m.wiremeshListTask != null) "LOADED" else "UNLOADED"
                        sender.sendMessage(" - ${m.mapName} : $loaded")
                    }
                    return@handler
                },
        )
        commandManager.command(
            map
                .literal("metrics")
                .required("mapName", StringParser.stringParser())
                .handler { context ->
                    val sender = context.sender()
                    val mapName = context.get<String>("mapName")
                    if (mapName == "all") {
                        for (m in DataMgr.maplist) {
                            val metrics = MapLoader.getMetricsString(m.mapName)
                            sender.sendMessage("${m.mapName} : ${metrics ?: "no metrics"}")
                        }
                        return@handler
                    }

                    val map = DataMgr.maplist.find { it.mapName == mapName }
                    if (map == null) {
                        sender.sendMessage("Map not found: $mapName")
                        return@handler
                    }
                    val metrics = MapLoader.getMetricsString(map.mapName)
                    sender.sendMessage("${map.mapName} : ${metrics ?: "no metrics"}")
                },
        )
    }
}
