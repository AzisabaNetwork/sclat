@file:Suppress("DEPRECATION")

package be4rjp.sclat

import be4rjp.blockstudio.BlockStudio
import be4rjp.dadadachecker.DADADACheckerAPI
import be4rjp.sclat.api.SclatUtil.sendRestartedServerInfo
import be4rjp.sclat.api.async.AsyncPlayerListener
import be4rjp.sclat.api.async.AsyncThreadManager.setup
import be4rjp.sclat.api.async.AsyncThreadManager.shutdownAll
import be4rjp.sclat.api.holo.PlayerHolograms
import be4rjp.sclat.command.SclatCommands
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.armorStandMap
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getMatchFromId
import be4rjp.sclat.gui.ClickListener
import be4rjp.sclat.listener.SquidListener
import be4rjp.sclat.lunachat.LunaChatListener
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.ColorMgr
import be4rjp.sclat.manager.GameMgr
import be4rjp.sclat.manager.MainWeaponMgr
import be4rjp.sclat.manager.MapDataMgr
import be4rjp.sclat.manager.MapLoader
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.manager.NoteBlockAPIMgr
import be4rjp.sclat.manager.PlayerReturnManager
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.manager.RankMgr
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.protocollib.SclatPacketListener.init
import be4rjp.sclat.server.EquipmentServer
import be4rjp.sclat.server.StatusServer
import be4rjp.sclat.tutorial.Tutorial
import be4rjp.sclat.tutorial.Tutorial.clearRegionRunnable
import be4rjp.sclat.tutorial.Tutorial.lobbyRegionRunnable
import be4rjp.sclat.tutorial.Tutorial.lobbySetStatusRunnable
import be4rjp.sclat.tutorial.Tutorial.trainLightRunnable
import be4rjp.sclat.tutorial.Tutorial.weaponRemoveRunnable
import be4rjp.sclat.weapon.MainWeapon
import be4rjp.sclat.weapon.SPWeapon
import be4rjp.sclat.weapon.SnowballListener
import be4rjp.sclat.weapon.SubWeapon
import com.google.common.io.ByteStreams
import fr.mrmicky.fastboard.FastBoard
import net.azisaba.sclat.core.DelegatedLogger
import net.azisaba.sclat.core.Plugins
import net.azisaba.sclat.core.Plugins.Companion.onInit
import net.azisaba.sclat.core.config.Config
import net.azisaba.sclat.core.config.CustomConfig
import net.azisaba.sclat.core.config.NewConfig
import net.azisaba.sclat.core.enums.ServerType
import net.azisaba.sclat.core.status.ServerStatus
import net.azisaba.sclat.core.status.StatusLine
import net.azisaba.sclat.core.utils.TextAnimation
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Team
import java.util.UUID
import kotlin.math.pow

/**
 *
 * @author Be4rJP
 */
class Sclat :
    JavaPlugin(),
    PluginMessageListener {
    internal val boards: MutableMap<UUID, FastBoard> = mutableMapOf()
    lateinit var text: String
    lateinit var textAnimation: TextAnimation

    override fun onEnable() {
        plugin = this
        glow = Glow()

        pdspList = ArrayList()

        // Setup async tick thread
        setup(1)

        // ----------------------------APICheck-------------------------------
        if (!onInit(server.pluginManager)) return
        logger.info("API check was completed.")
        init()

        dadadaCheckerAPI = DADADACheckerAPI(this)

        // -------------------------------------------------------------------

        // --------------------------Load config------------------------------
        logger.info("Loading config files...")
        conf = Config()
        conf?.loadConfig()
        NewConfig.load()
        // Map world creation is deferred by default. Use the config toggle
        // `deferredMapLoading` (default true) to control the behavior.
        val deferred = conf!!.config!!.getBoolean("deferredMapLoading", true)
        if (!deferred) {
            for (mapname in conf!!.mapConfig!!.getConfigurationSection("Maps")!!.getKeys(false)) {
                val worldName: String? = conf!!.mapConfig!!.getString("Maps.$mapname.WorldName")
                Bukkit.createWorld(WorldCreator(worldName!!))
                val world = Bukkit.getWorld(worldName)
                world!!.isAutoSave = false
            }
        } else {
            logger.info("Deferred map loading is enabled; maps will be loaded when assigned to matches.")
        }
        if (conf!!.config!!.contains("Tutorial")) tutorial = conf!!.config!!.getBoolean("Tutorial")
        if (conf!!.config!!.contains("Colors")) colors = conf!!.config!!.getStringList("Colors")

        particleRenderDistance = conf!!.config!!.getDouble("ParticlesRenderDistance")
        particleRenderDistanceSquared = particleRenderDistance.pow(2.0)

        // -------------------------------------------------------------------

        // --------------------------Lobby location---------------------------
        val worldName: String? = conf!!.config!!.getString("Lobby.WorldName")
        Bukkit.createWorld(WorldCreator(worldName!!))
        val w = Bukkit.getWorld(worldName)
        val ix: Int = conf!!.config!!.getInt("Lobby.X")
        val iy: Int = conf!!.config!!.getInt("Lobby.Y")
        val iz: Int = conf!!.config!!.getInt("Lobby.Z")
        val iyaw: Int = conf!!.config!!.getInt("Lobby.Yaw")
        lobby = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
        lobby!!.yaw = iyaw.toFloat()

        // -------------------------------------------------------------------

        // ------------------------RegisteringEvents--------------------------
        logger.info("Registering Events...")
        val pm = server.pluginManager
        pm.registerEvents(GameMgr(), this)
        pm.registerEvents(SquidListener(), this)
        pm.registerEvents(ClickListener(), this)
        pm.registerEvents(MainWeapon(), this)
        pm.registerEvents(SubWeapon(), this)
        pm.registerEvents(SPWeapon(), this)
        pm.registerEvents(SnowballListener(), this)
        pm.registerEvents(AsyncPlayerListener(), this)

        if (Plugins.LUNACHAT.isLoaded) pm.registerEvents(LunaChatListener(), this)

        // -------------------------------------------------------------------

        // ------------------------RegisteringCommands------------------------
        logger.info("Registering Commands...")
        SclatCommands.init(this)

        // -------------------------------------------------------------------

        // ------------------------Setup from config--------------------------
        logger.info("SetupColor...")
        ColorMgr.setupColor()
        logger.info("SetupMainWeapon...")
        MainWeaponMgr.setupMainWeapon()
        logger.info("WeaponClassSetup...")
        WeaponClassMgr.weaponClassSetup()
        logger.info("Setup Map...")
        logger.info("")
        logger.info("-----------------MAP LIST-----------------")
        MapDataMgr.setupMap()
        logger.info("------------------------------------------")
        logger.info("")
        logger.info("MatchSetup...")
        MatchMgr.matchSetup()
        logger.info("Setup is finished!")

        // -------------------------------------------------------------------

        // ---------------------Enable mode message---------------------------
        val length: Int = conf!!.config!!.getString("WorkMode")!!.length

        val buff = StringBuilder()
        buff.append("### This plugin started in [")
        buff.append(conf!!.config!!.getString("WorkMode"))
        buff.append("] mode! ")
        for (i in 0..<7 - length) {
            buff.append(" ")
        }
        buff.append("###")

        logger.info("##############################################")
        logger.info("###                                        ###")
        logger.info(buff.toString())
        logger.info("###                                        ###")
        logger.info("##############################################")

        // -------------------------------------------------------------------

        // ------------------------Only trial mode----------------------------
        if (conf!!.config!!.getString("WorkMode") == "Trial") {
            val manager = Bukkit.getScoreboardManager()
            val scoreboard = manager!!.newScoreboard

            val match = getMatchFromId(MatchMgr.matchcount)

            val bteam0 = scoreboard.registerNewTeam(match!!.team0!!.teamColor!!.colorName!!)
            bteam0.color = match.team0!!.teamColor!!.chatColor!!
            bteam0.nameTagVisibility = NameTagVisibility.HIDE_FOR_OTHER_TEAMS
            bteam0.setOption(
                Team.Option.COLLISION_RULE,
                Team.OptionStatus.NEVER,
            )

            val bteam1 = scoreboard.registerNewTeam(match.team1!!.teamColor!!.colorName!!)
            bteam1.color = match.team1!!.teamColor!!.chatColor!!
            bteam1.nameTagVisibility = NameTagVisibility.HIDE_FOR_OTHER_TEAMS
            bteam1.setOption(
                Team.Option.COLLISION_RULE,
                Team.OptionStatus.NEVER,
            )

            match.team0!!.team = bteam0
            match.team1!!.team = bteam1

            ArmorStandMgr.armorStandEquipPacketSender(w!!)
        }

        // -------------------------------------------------------------------

        // ------------------------BungeeCord setup---------------------------
        this.server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        this.server.messenger.registerIncomingPluginChannel(this, "BungeeCord", this)

        // -------------------------------------------------------------------

        // ------------------------Load NBS songs-----------------------------
        if (Plugins.NOTEBLOCKAPI.isLoaded) NoteBlockAPIMgr.loadSongFiles()

        // -------------------------------------------------------------------

        // --------------------------Server type------------------------------
        if (conf!!.config!!.contains("ServerType")) {
            when (conf!!.config!!.getString("ServerType")) {
                "NORMAL" -> type = ServerType.NORMAL
                "LOBBY" -> type = ServerType.LOBBY
                "MATCH" -> type = ServerType.MATCH
            }
        }

        // -------------------------------------------------------------------

        // ---------------------------Server status---------------------------
        if (type == ServerType.LOBBY) ServerStatusManager.setupServerStatusGUI()

        // -------------------------------------------------------------------

        // ----------------------Status server and client---------------------
        if (type == ServerType.LOBBY) {
            ss = StatusServer(conf!!.config!!.getInt("StatusShare.Port"))
            ss!!.start()
            logger.info("StatusServer is ready!")
        }

        // -------------------------------------------------------------------

        // ----------------------Equip server and client----------------------
        if (type == ServerType.MATCH || conf!!.config!!.getString("WorkMode") == "Trial") {
            es = EquipmentServer(conf!!.config!!.getInt("EquipShare.Port"))
            es!!.start()
            logger.info("StatusServer is ready!")
        }

        // -------------------------------------------------------------------

        // --------------------------Return task------------------------------
        PlayerReturnManager.runRemoveTask()

        // -------------------------------------------------------------------

        // --------------------Send restarted server info---------------------
        if (conf!!.config!!.contains("RestartMatchCount")) sendRestartedServerInfo()

        // -------------------------------------------------------------------

        // -----------------------------Shop----------------------------------
        if (conf!!.config!!.contains("Shop")) shop = conf!!.config!!.getBoolean("Shop")

        // -------------------------------------------------------------------

        // ----------------------------Tutorial-------------------------------
        if (conf!!.config!!.contains("Tutorial")) tutorial = conf!!.config!!.getBoolean("Tutorial")
        if (tutorial) {
            Tutorial.setupTutorial(getMatchFromId(MatchMgr.matchcount)!!)
            clearRegionRunnable()
            lobbyRegionRunnable()
            trainLightRunnable()
            weaponRemoveRunnable()
        } else {
            if (type == ServerType.LOBBY) {
                lobbySetStatusRunnable()
            }
        }

        // -------------------------------------------------------------------

        // -----------------------Ranking Holograms---------------------------
        if (type == ServerType.LOBBY) {
            RankMgr.makeRankingTask()
        }

        // -------------------------------------------------------------------

        // -----------------------Tutorial server list------------------------
        if (type == ServerType.LOBBY) {
            tutorialServers = CustomConfig(this, "tutorial.yml")
            tutorialServers!!.saveDefaultConfig()
            tutorialServers!!.getConfig()
        }

        // -------------------------------------------------------------------

        // ---------------------------BlockStudio-----------------------------
        logger.info("Loading all object data...")
        val api = BlockStudio.getBlockStudioAPI()
        api.loadAllObjectData()

        // -------------------------------------------------------------------

        // ------------------------Tutorial wire mesh-------------------------
        if (tutorial) {
            val eagerTutorial = conf!!.config!!.getBoolean("eagerLoadTutorialMaps", true)
            if (eagerTutorial) {
                // Preload tutorial maps so their runtime objects and wiremesh tasks exist.
                for (mData in DataMgr.maplist) {
                    try {
                        MapLoader.incrementUsage(mData)
                    } catch (e: Exception) {
                    }
                }
            }

            for (mData in DataMgr.maplist) {
                try {
                    mData.wiremeshListTask?.wiremeshsList?.forEach { it?.startTask() }
                } catch (e: Exception) {
                }
            }
        }

        // -------------------------------------------------------------------

        // -------------------------------News--------------------------------
        news = CustomConfig(this, "news.yml")
        news!!.saveDefaultConfig()
        news!!.getConfig()
        // -------------------------------------------------------------------

        // --- side scoreboard updater ---
        object : BukkitRunnable() {
            override fun run() {
                boards.forEach { (_, b) -> updateBoard(b) }
            }
        }.runTaskTimer(this, 0, 20)

        text = ChatColor.translateAlternateColorCodes('&', news?.getConfig()!!.getString("news-message")!!)

        textAnimation = TextAnimation(text, news?.getConfig()!!.getInt("scoreboard-length"))
    }

    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != "BungeeCord") {
            return
        }
        val `in` = ByteStreams.newDataInput(message)
        val subchannel = `in`.readUTF()
        if (subchannel == "SomeSubChannel") {
        }
    }

    override fun onDisable() {
        try {
            shutdownAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // -----------------------Tutorial server list------------------------
        if (type == ServerType.LOBBY) {
            tutorialServers = CustomConfig(this, "tutorial.yml")
            tutorialServers!!.saveConfig()
        }

        // -------------------------------------------------------------------

        // Wiremeshの停止
        try {
            for (mData in DataMgr.maplist) if (mData.wiremeshListTask != null) mData.wiremeshListTask!!.stopTask()
        } catch (e: Exception) {
        }

        // 塗りリセット
        for (data in blockDataMap.values) {
            data!!.block!!.type = data.originalType!!
            if (data.blockData != null) data.block.blockData = data.blockData!!
        }
        blockDataMap.clear()

        /*
         * for(Block block : DataMgr.rblist){ block.setType(Material.AIR);
         * DataMgr.rblist.remove(block); }
         */
        for (`as` in armorStandMap.keys) `as`!!.remove()
        conf!!.saveConfig()
        NewConfig.save()

        for (`as` in DataMgr.al) `as`!!.remove()

        // Unload all loaded maps using MapLoader so it can perform proper cleanup.
        // This replaces the previous eager world-unload loop.
        try {
            MapLoader.unloadAllLoadedMaps()
        } catch (e: Exception) {
        }

        if (type == ServerType.LOBBY) {
            ServerStatusManager.stopTask()
        }
    }

    fun updateBoard(board: FastBoard) {
        val player = board.player
        board.updateLines(
            "§7§m                                  ",
            "",
            "§6§lステータス »",
            "§e COIN: §r" + PlayerStatusMgr.getMoney(player),
            "§e TICKET: §r" + PlayerStatusMgr.getTicket(player),
            "§b RANK: §r" + RankMgr.toABCRank(PlayerStatusMgr.getRank(player)) + " [" +
                PlayerStatusMgr.getRank(player) + "]",
            " ",
            "§9§lサーバー »",
            *ServerStatusManager.serverList
                .filter { status -> !status.isMaintenance && status.isOnline }
                .mapNotNull { serverStatus ->
                    StatusLine.getLine(
                        ServerStatus(
                            serverStatus.isOnline,
                            serverStatus.isMaintenance,
                            serverStatus.displayName,
                            serverStatus.matchStartTime,
                            serverStatus.playerCount,
                            serverStatus.waitingEndTime,
                            serverStatus.runningMatch,
                        ),
                    )
                }.toTypedArray(),
            "  ",
            "§a§lNews »",
            textAnimation.next(),
            "   ", // Prevent from same name
            "§7§m                                  §r",
        )
    }

    companion object {
        private val logger by DelegatedLogger()

        @JvmField
        var conf: Config? = null

        @JvmField
        var lobby: Location? = null

        @JvmField
        var glow: Glow? = null

        var pdspList: MutableList<Player?>? = null

        @JvmField
        var colors: MutableList<String?> = ArrayList()

        @JvmField
        var tutorial: Boolean = false

        @JvmField
        var type: ServerType = ServerType.NORMAL

        @JvmField
        var shop: Boolean = true

        @JvmField
        var tutorialServers: CustomConfig? = null

        const val VERSION: String = "v2.0.0 - β"

        var news: CustomConfig? = null

        // StatusShare
        var ss: StatusServer? = null

        // EquipmentShare
        var es: EquipmentServer? = null

        // 重複しない数字
        // ボム等で使用
        private var nonDuplicateNumber = 0

        // for DADADAChecker
        @JvmField
        var dadadaCheckerAPI: DADADACheckerAPI? = null

        @JvmField
        var flyList: MutableList<String?> = ArrayList()

        @JvmField
        var modList: MutableList<String?> = ArrayList()

        @JvmField
        var particleRenderDistance: Double = 0.0

        @JvmField
        var particleRenderDistanceSquared: Double = 0.0

        @JvmField
        val playerHolograms: PlayerHolograms = PlayerHolograms()

        @JvmStatic
        val notDuplicateNumber: Int
            get() {
                nonDuplicateNumber++
                return nonDuplicateNumber
            }
    }
}
