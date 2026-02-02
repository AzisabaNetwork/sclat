@file:Suppress("DEPRECATION")

package be4rjp.sclat

import be4rjp.blockstudio.BlockStudio
import be4rjp.dadadachecker.DADADACheckerAPI
import be4rjp.sclat.api.Plugins
import be4rjp.sclat.api.Plugins.Companion.onInit
import be4rjp.sclat.api.SclatUtil.sendRestartedServerInfo
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.async.AsyncPlayerListener
import be4rjp.sclat.api.async.AsyncThreadManager.setup
import be4rjp.sclat.api.async.AsyncThreadManager.shutdownAll
import be4rjp.sclat.api.config.CustomConfig
import be4rjp.sclat.api.holo.PlayerHolograms
import be4rjp.sclat.commands.SclatCommandExecutor
import be4rjp.sclat.config.Config
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
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.manager.NoteBlockAPIMgr
import be4rjp.sclat.manager.PlayerReturnManager
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
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Team
import kotlin.math.pow

/**
 *
 * @author Be4rJP
 */
class Sclat :
    JavaPlugin(),
    PluginMessageListener {
    override fun onEnable() {
        plugin = this
        glow = Glow()

        pdspList = ArrayList()

        // Setup async tick thread
        setup(1)

        // ----------------------------APICheck-------------------------------
        if (!onInit()) return
        sclatLogger.info("API check was completed.")

        protocolManager = ProtocolLibrary.getProtocolManager()
        init()

        dadadaCheckerAPI = DADADACheckerAPI(this)

        // -------------------------------------------------------------------

        // --------------------------Load config------------------------------
        sclatLogger.info("Loading config files...")
        conf = Config()
        conf!!.loadConfig()
        for (mapname in conf!!.mapConfig!!.getConfigurationSection("Maps")!!.getKeys(false)) {
            val worldName: String? = conf!!.mapConfig!!.getString("Maps." + mapname + ".WorldName")
            Bukkit.createWorld(WorldCreator(worldName!!))
            val world = Bukkit.getWorld(worldName)
            world!!.isAutoSave = false
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
        sclatLogger.info("Registering Events...")
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
        sclatLogger.info("Registering Commands...")
        getCommand("sclat")!!.setExecutor(SclatCommandExecutor())
        getCommand("sclat")!!.tabCompleter = SclatCommandExecutor()

        // -------------------------------------------------------------------

        // ------------------------Setup from config--------------------------
        sclatLogger.info("SetupColor...")
        ColorMgr.setupColor()
        sclatLogger.info("SetupMainWeapon...")
        MainWeaponMgr.setupMainWeapon()
        sclatLogger.info("WeaponClassSetup...")
        WeaponClassMgr.weaponClassSetup()
        sclatLogger.info("Setup Map...")
        sclatLogger.info("")
        sclatLogger.info("-----------------MAP LIST-----------------")
        MapDataMgr.setupMap()
        sclatLogger.info("------------------------------------------")
        sclatLogger.info("")
        sclatLogger.info("MatchSetup...")
        MatchMgr.matchSetup()
        sclatLogger.info("Setup is finished!")

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

        sclatLogger.info("##############################################")
        sclatLogger.info("###                                        ###")
        sclatLogger.info(buff.toString())
        sclatLogger.info("###                                        ###")
        sclatLogger.info("##############################################")

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
            sclatLogger.info("StatusServer is ready!")
        }

        // -------------------------------------------------------------------

        // ----------------------Equip server and client----------------------
        if (type == ServerType.MATCH || conf!!.config!!.getString("WorkMode") == "Trial") {
            es = EquipmentServer(conf!!.config!!.getInt("EquipShare.Port"))
            es!!.start()
            sclatLogger.info("StatusServer is ready!")
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
        sclatLogger.info("Loading all object data...")
        val api = BlockStudio.getBlockStudioAPI()
        api.loadAllObjectData()

        // -------------------------------------------------------------------

        // ------------------------Tutorial wire mesh-------------------------
        if (tutorial) {
            for (mData in DataMgr.maplist) {
                for (wiremesh in mData.wiremeshListTask!!.wiremeshsList) {
                    wiremesh!!.startTask()
                }
            }
        }

        // -------------------------------------------------------------------

        // -------------------------------News--------------------------------
        news = CustomConfig(this, "news.yml")
        news!!.saveDefaultConfig()
        news!!.getConfig()
        // -------------------------------------------------------------------
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

        for (`as` in DataMgr.al) `as`!!.remove()

        // Worldが保存される前にアンロードして塗られた状態で保存されるのを防ぐ
        if (type == ServerType.LOBBY) {
            for (mapname in conf!!.mapConfig!!.getConfigurationSection("Maps")!!.getKeys(false)) {
                val worldName: String? = conf!!.mapConfig!!.getString("Maps." + mapname + ".WorldName")
                Bukkit.unloadWorld(worldName!!, false)
            }
        }

        if (type == ServerType.LOBBY) {
            ServerStatusManager.stopTask()
        }
    }

    companion object {
        @JvmField
        var conf: Config? = null

        @JvmField
        var lobby: Location? = null

        @JvmField
        var glow: Glow? = null

        var pdspList: MutableList<Player?>? = null

        @JvmField
        var colors: MutableList<String?> = ArrayList<String?>()

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

        // for ProtocolLib
        lateinit var protocolManager: ProtocolManager

        // for DADADAChecker
        @JvmField
        var dadadaCheckerAPI: DADADACheckerAPI? = null

        @JvmField
        var flyList: MutableList<String?> = ArrayList<String?>()

        @JvmField
        var modList: MutableList<String?> = ArrayList<String?>()

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
