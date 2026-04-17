package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.Animation.areaResultAnimation
import be4rjp.sclat.api.Animation.resultAnimation
import be4rjp.sclat.api.Animation.tdmResultAnimation
import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.Plugins
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.restartServer
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.SclatUtil.sendWorldBorderWarningClearPacket
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.api.player.PlayerSettings
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.api.utils.ObjectiveUtil
import be4rjp.sclat.data.BlockUpdater
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.armorStandMap
import be4rjp.sclat.data.DataMgr.beaconMap
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.colorShuffle
import be4rjp.sclat.data.DataMgr.getColorRandom
import be4rjp.sclat.data.DataMgr.getMapRandom
import be4rjp.sclat.data.DataMgr.getMatchFromId
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getPlayerIsQuit
import be4rjp.sclat.data.DataMgr.mapDataShuffle
import be4rjp.sclat.data.DataMgr.setMatch
import be4rjp.sclat.data.DataMgr.setPlayerIsQuit
import be4rjp.sclat.data.DataMgr.setTeam
import be4rjp.sclat.data.DataMgr.spongeMap
import be4rjp.sclat.data.DataMgr.sprinklerMap
import be4rjp.sclat.data.Match
import be4rjp.sclat.gui.OpenGUI
import be4rjp.sclat.plugin
import be4rjp.sclat.server.EquipmentServerManager.doCommands
import be4rjp.sclat.server.StatusClient
import be4rjp.sclat.weapon.Brush
import be4rjp.sclat.weapon.Bucket.bucketHealRunnable
import be4rjp.sclat.weapon.Buckler.bucklerRunnable
import be4rjp.sclat.weapon.Charger.chargerRunnable
import be4rjp.sclat.weapon.Decoy.decoyRunnable
import be4rjp.sclat.weapon.Funnel.funnelFloat
import be4rjp.sclat.weapon.Gear
import be4rjp.sclat.weapon.Gear.getGearInfluence
import be4rjp.sclat.weapon.Hound.houndEXRunnable
import be4rjp.sclat.weapon.Hound.houndRunnable
import be4rjp.sclat.weapon.Kasa.kasaRunnable
import be4rjp.sclat.weapon.Manuber
import be4rjp.sclat.weapon.Reeler.reelerRunnable
import be4rjp.sclat.weapon.Reeler.reelerShootRunnable
import be4rjp.sclat.weapon.Roller
import be4rjp.sclat.weapon.Shooter
import be4rjp.sclat.weapon.Shooter.maneuverShootRunnable
import be4rjp.sclat.weapon.Shooter.shooterRunnable
import be4rjp.sclat.weapon.Spinner.spinnerRunnable
import be4rjp.sclat.weapon.Swapper.swapperRunnable
import be4rjp.sclat.weapon.spweapon.SuperArmor.setArmor
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import java.util.TreeMap
import java.util.function.Consumer

/**
 *
 * @author Be4rJP
 */
object MatchMgr {
    @JvmField
    var matchcount: Int = 0
    var mapcount: Int = 0
    var volume: Byte = 20

    @JvmField
    var canRollback: Boolean = true

    var matchedPlayerList: MutableList<String?> = ArrayList()

    @JvmStatic
    fun playerJoinMatch(player: Player) {
        val data = getPlayerData(player)

        /*
         * if(DataMgr.getPlayerIsQuit(player.getUniqueId().toString())){
         * Sclat.sendMessage("§c§n途中で退出した場合再参加はできません", MessageType.PLAYER, player);
         * Sclat.playGameSound(player, SoundType.ERROR); return; }
         */
        if (!DataMgr.joinedList.contains(player)) {
            val match = getMatchFromId(matchcount)
            if (match!!.canJoin()) {
                match.addPlayerCount()
                match.playerCount
                if (match.joinedPlayerCount <
                    Sclat.conf!!
                        .config!!
                        .getInt("MaxPlayerCount")
                ) {
                    match.addJoinedPlayerCount()

                    data!!.match = match
                    data.isJoined = (true)

                    DataMgr.joinedList.add(player)

                    sendMessage(
                        "§b§n" + player.displayName + " joined the match",
                        MessageType.ALL_PLAYER,
                    )

                    // Teleport player to waiting spot; be defensive against
                    // missing map runtime data (may not be loaded yet).
                    val taiki = match.mapData?.taikibayso ?: match.mapData?.team0Loc ?: match.mapData?.intro
                    if (taiki != null) {
                        player.teleport(taiki)
                    } else {
                        // Fallback: teleport to plugin lobby or player's world spawn
                        try {
                            val fallback = Sclat.lobby ?: player.world.spawnLocation
                            player.teleport(fallback)
                        } catch (_: Exception) {
                        }
                    }
                    if (Sclat.conf!!
                            .config!!
                            .getBoolean("CanVoting") &&
                        !getPlayerIsQuit(player.uniqueId.toString())
                    ) {
                        OpenGUI.matchTohyoGUI(player)
                    }

                    val startPlayerCount =
                        Sclat.conf!!
                            .config!!
                            .getInt("StartPlayerCount")

                    if (match.joinedPlayerCount < startPlayerCount) {
                        sendMessage("§a人数が足りないため試合を開始することができません", MessageType.ALL_PLAYER)
                        sendMessage(
                            "§aあと§c" + (startPlayerCount - match.joinedPlayerCount) + "§a人必要です",
                            MessageType.ALL_PLAYER,
                        )
                    }

                    if (match.joinedPlayerCount == startPlayerCount && !match.isStarted && !match.isStartedCount) {
                        match.isStarted = (true)
                        match.isStartedCount = (true)
                        val task: BukkitRunnable =
                            object : BukkitRunnable() {
                                var s: Int = 0
                                val p: Player = player

                                override fun run() {
                                    if (match.joinedPlayerCount < startPlayerCount) {
                                        sendMessage(
                                            "§a人数が足りないため試合を開始することができません",
                                            MessageType.ALL_PLAYER,
                                        )
                                        sendMessage(
                                            "§aあと§c" + (startPlayerCount - match.joinedPlayerCount) + "§a人必要です",
                                            MessageType.ALL_PLAYER,
                                        )
                                        match.isStartedCount = (false)
                                        match.isStarted = (false)
                                        // Send match status
                                        if (Sclat.type == ServerType.MATCH) {
                                            val commands: MutableList<String> =
                                                mutableListOf(
                                                    "cdc " +
                                                        Sclat.conf!!
                                                            .servers!!
                                                            .getString("ServerName"),
                                                    "stop",
                                                )
                                            val sc =
                                                StatusClient(
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getString("StatusShare.Host"),
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getInt("StatusShare.Port"),
                                                    commands,
                                                )
                                            sc.startClient()
                                        }
                                        cancel()
                                    }
                                    if (s == 0) {
                                        // Send match status
                                        if (Sclat.type == ServerType.MATCH) {
                                            val commands: MutableList<String> =
                                                mutableListOf(
                                                    "cd " +
                                                        Sclat.conf!!
                                                            .servers!!
                                                            .getString("ServerName") + " " +
                                                        (System.currentTimeMillis() / 1000 + 30),
                                                    "stop",
                                                )
                                            val sc =
                                                StatusClient(
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getString("StatusShare.Host"),
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getInt("StatusShare.Port"),
                                                    commands,
                                                )
                                            sc.startClient()
                                        }
                                        sendMessage("§a試合開始まで後§c30§a秒", MessageType.ALL_PLAYER)
                                    }
                                    if (s == 10) sendMessage("§a試合開始まで後§c20§a秒", MessageType.ALL_PLAYER)
                                    if (s == 20) sendMessage("§a試合開始まで後§c10§a秒", MessageType.ALL_PLAYER)
                                    if (s == 25) sendMessage("§a試合開始まで後§c5§a秒", MessageType.ALL_PLAYER)
                                    if (s == 26) sendMessage("§a試合開始まで後§c4§a秒", MessageType.ALL_PLAYER)
                                    if (s == 27) sendMessage("§a試合開始まで後§c3§a秒", MessageType.ALL_PLAYER)
                                    if (s == 28) sendMessage("§a試合開始まで後§c2§a秒", MessageType.ALL_PLAYER)
                                    if (s == 29) sendMessage("§a試合開始まで後§c1§a秒", MessageType.ALL_PLAYER)
                                    if (s == 30) {
                                        match.setCanJoin(false)

                                        // かぶらないようにマッピング
                                        val playerMap: MutableMap<Int?, Player?> = HashMap()
                                        for (jp in DataMgr.joinedList) {
                                            var rate = PlayerStatusMgr.getRank(jp!!)
                                            while (playerMap.containsKey(rate)) {
                                                rate++
                                            }
                                            playerMap[rate] = jp
                                        }

                                        // ソート
                                        var sortedMember: MutableList<Player?> = ArrayList()
                                        if (Sclat.conf!!
                                                .config!!
                                                .getBoolean("RateMatch")
                                        ) {
                                            val treeMap: MutableMap<Int?, Player?> = TreeMap<Int?, Player?>(playerMap)
                                        /*
                                         * if(match.getJoinedPlayerCount() == 3){ List<Player> list = new ArrayList<>();
                                         * for (Integer key : treeMap.keySet()) list.add(treeMap.get(key));
                                         * sortedMember.add(list.get(0)); sortedMember.add(list.get(2));
                                         * sortedMember.add(list.get(1)); }else{
                                         */
                                            for ((index, key) in treeMap.keys.withIndex()) {
                                                sortedMember.add(treeMap[key])
                                            }
                                            sortedMember.shuffle()
                                            // }
                                        } else {
                                            sortedMember = DataMgr.joinedList
                                            sortedMember.shuffle()
                                        }

                                        for ((i, jp) in sortedMember.withIndex()) {
                                            val data = getPlayerData(jp)
                                            if (i % 2 == 0) {
                                                data!!.team = match.team0
                                            } else {
                                                data!!.team = match.team1
                                            }
                                        }

                                        var playerNumber = 1
                                        for (jp in sortedMember) {
                                            val data = getPlayerData(jp)
                                            if (jp!!.isOnline) {
                                                data!!.playerNumber = (playerNumber)
                                                data.team!!.addRateTotal(PlayerStatusMgr.getRank(jp))
                                                // jp.setDisplayName(data.getTeam().getTeamColor().getColorCode() +
                                                // jp.getName());
                                            }
                                            playerNumber++
                                        }

                                        // Settings
                                        if (Sclat.type == ServerType.MATCH) {
                                            for (op in plugin.server.onlinePlayers) {
                                                val playerSettings = PlayerSettings(op)
                                                SettingMgr.setSettings(playerSettings, op)
                                            }
                                        }

                                        sendMessage("§6試合が開始されました", MessageType.BROADCAST)
                                        if (Sclat.conf!!
                                                .config!!
                                                .getBoolean("RateMatch")
                                        ) {
                                            sendMessage("", MessageType.ALL_PLAYER)
                                            sendMessage(
                                                "§b試合の総合レート : §r" +
                                                    (match.team0!!.rateTotal + match.team1!!.rateTotal),
                                                MessageType.ALL_PLAYER,
                                            )
                                        }
                                        doCommands()
                                        if (Sclat.conf!!
                                                .config!!
                                                .getBoolean("CanVoting")
                                        ) {
                                            if (match.nawabariTCount >= match.tdmTCount &&
                                                match.nawabariTCount >= match.gatiareaTCount
                                            ) {
                                                Sclat.conf!!
                                                    .config!!
                                                    .set("WorkMode", "Normal")
                                            } else if (match.tdmTCount >= match.gatiareaTCount) {
                                                Sclat.conf!!
                                                    .config!!
                                                    .set("WorkMode", "TDM")
                                            } else {
                                                Sclat.conf!!
                                                    .config!!
                                                    .set("WorkMode", "Area")
                                            }
                                        }
                                        startMatch(match)
                                        for (entity in p.world.entities) {
                                            if (entity !is Player && entity !is FallingBlock) {
                                                entity.remove()
                                            }
                                        }

                                        // Send match status
                                        if (Sclat.type == ServerType.MATCH) {
                                            val commands: MutableList<String> =
                                                mutableListOf(
                                                    "started " +
                                                        Sclat.conf!!
                                                            .servers!!
                                                            .getString("ServerName") + " " +
                                                        System.currentTimeMillis() / 1000,
                                                    "stop",
                                                )
                                            val sc =
                                                StatusClient(
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getString("StatusShare.Host"),
                                                    Sclat.conf!!
                                                        .config!!
                                                        .getInt("StatusShare.Port"),
                                                    commands,
                                                )
                                            sc.startClient()
                                        }

                                        cancel()
                                    }
                                    s++
                                }
                            }
                        task.runTaskTimer(plugin, 0, 20)
                    }
                } else {
                    sendMessage("§c§n上限人数を超えているため参加できません", MessageType.PLAYER, player)
                    playGameSound(player, SoundType.ERROR)
                }
            } else {
                sendMessage("§c§nこのマッチには既に開始しているため参加できません", MessageType.PLAYER, player)
                playGameSound(player, SoundType.ERROR)
            }
        } else {
            sendMessage("§c§n既にチームに参加しています", MessageType.PLAYER, player)
            playGameSound(player, SoundType.ERROR)
        }
    }

    @Synchronized
    fun matchSetup() {
        // 再起動オプション
        if (Sclat.conf!!
                .config!!
                .contains("RestartMatchCount")
        ) {
            if (Sclat.conf!!
                    .config!!
                    .getInt("RestartMatchCount") ==
                matchcount
            ) {
                restartServer()
            }
        }

        val id = matchcount
        val match = Match(id)
        val team0 = Team(id * 2)
        val team1 = Team(id * 2 + 1)
        setTeam(id * 2, team0)
        setTeam(id * 2 + 1, team1)

        val bur = BlockUpdater()
        if (Sclat.conf!!
                .config!!
                .contains("BlockUpdateRate")
        ) {
            bur.setMaxBlockInOneTick(
                Sclat.conf!!.config!!.getInt(
                    "BlockUpdateRate",
                ),
            )
        }
        bur.start()
        match.blockUpdater = bur

        colorShuffle()
        val color0 = getColorRandom(0)
        val color1 = getColorRandom(1)
        team0.teamColor = color0
        team1.teamColor = color1

        match.team0 = (team0)
        match.team1 = (team1)

        if (id == 0) mapDataShuffle()

        val map = getMapRandom(mapcount)
        match.mapData = map
        // Load map runtime data when a match is assigned
        MapLoader.incrementUsage(map)

        mapcount++

        if (mapcount == MapDataMgr.allmapcount) {
            mapcount = 0
            // DataMgr.MapDataShuffle();
        }

        setMatch(id, match)

        // lobby待機者用
        val id2 = Int.MAX_VALUE
        val lobbyM = Match(id2)
        val lobbyT0 = Team(id2)
        val lobbyT1 = Team(id2 - 1)
        setTeam(id2, lobbyT0)
        setTeam(id2 - 1, lobbyT1)

        colorShuffle()
        val lc0 = getColorRandom(0)
        val lc1 = getColorRandom(1)
        lobbyT0.teamColor = lc0
        lobbyT1.teamColor = lc1

        lobbyM.team0 = (lobbyT0)
        lobbyM.team1 = (lobbyT1)

        val map1 = getMapRandom(0)
        lobbyM.mapData = map1
        // Preload lobby map assignment so waiting players have map data ready
        MapLoader.incrementUsage(map1)

        setMatch(id2, lobbyM)

        // TeamLoc teamloc = new TeamLoc(map);
        // teamloc.SetupTeam0Loc();
        // teamloc.SetupTeam1Loc();
        // DataMgr.setTeamLoc(map, teamloc);
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            val manager = Bukkit.getScoreboardManager()
            val scoreboard = manager!!.newScoreboard

            val bteam0 = scoreboard.registerNewTeam(match.team0!!.teamColor!!.colorName!!)
            bteam0.color = match.team0!!.teamColor!!.chatColor!!
            bteam0.setOption(
                org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS,
            )
            bteam0.setOption(
                org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM,
            )

            val bteam1 = scoreboard.registerNewTeam(match.team1!!.teamColor!!.colorName!!)
            bteam1.color = match.team1!!.teamColor!!.chatColor!!
            bteam1.setOption(
                org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
                org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS,
            )
            bteam1.setOption(
                org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM,
            )

            team0.team = bteam0
            team1.team = bteam1

            match.scoreboard = (scoreboard)
        }
    }

    @JvmStatic
    fun rollBack() {
        if (!canRollback &&
            Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            return
        }
        for (data in blockDataMap.values) {
            var data = data
            data!!.block!!.type = data.originalType!!
            if (data.blockData != null) data.block.setBlockData(data.blockData!!)
            data = null
        }
        blockDataMap.clear()
        spongeMap.clear()
        canRollback = false

        /*
         * for(Block block : DataMgr.rblist){ block.setType(Material.AIR);
         * DataMgr.rblist.remove(block); }
         */
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    canRollback = true
                }
            }
        task.runTaskLater(plugin, 3600)
    }

    fun startCount(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var i: Int = 0

                override fun run() {
                    if (i == 10) p.sendTitle("R§7EADY?", "", 0, 56, 0)
                    if (i == 12) p.sendTitle("RE§7ADY?", "", 0, 46, 0)
                    if (i == 14) p.sendTitle("REA§7DY?", "", 0, 36, 0)
                    if (i == 16) p.sendTitle("READ§7Y?", "", 0, 26, 0)
                    if (i == 18) p.sendTitle("READY§7?", "", 0, 16, 0)
                    if (i == 20) p.sendTitle("READY?", "", 0, 6, 2)
                    if (i == 47) p.sendTitle(getPlayerData(p)!!.team!!.teamColor!!.colorCode + "GO!", "", 2, 6, 2)
                    i++
                }
            }
        task.runTaskTimer(plugin, 230, 1)
    }

    fun matchRunnable(
        player: Player,
        match: Match,
    ) {
        val task: BukkitRunnable?
        task =
            object : BukkitRunnable() {
                var s: Int = 0
                val p: Player = player
                val w: World? = plugin.server.getWorld(match.mapData!!.worldName!!)
                var intromove: Location? = null

                // EntitySquid squid;
                var squid: LivingEntity? = null

                // LivingEntity npcle;
                override fun run() {
                    try {
                        if (s == 0) {
                            p.setDisplayName(getPlayerData(p)!!.team!!.teamColor!!.colorCode + p.name)

                            getPlayerData(p)!!.canFly = (true)

                            if (getPlayerData(p)!!.playerNumber == 1) {
                                PaintMgr.paintGlass(match)

                                p.world.entities.forEach(
                                    Consumer { entity: Entity? ->
                                        if (entity!!.type == EntityType.SHULKER) {
                                            entity.remove()
                                        }
                                    },
                                )

                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") == "Area"
                                ) {
                                    for (area in match.mapData!!.areaList) {
                                        area!!.setup(match)
                                    }
                                }
                            }

                            when (
                                Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode")
                            ) {
                                "Normal" -> {
                                    sendMessage("§6ゲームモード : §b§lナワバリバトル", MessageType.PLAYER, p)
                                    sendMessage("§f§l敵よりもたくさんナワバリを確保しろ！", MessageType.PLAYER, p)
                                }

                                "TDM" -> {
                                    sendMessage("§6ゲームモード : §b§lチームデスマッチ", MessageType.PLAYER, p)
                                    sendMessage("§f§l敵よりもキルをしろ！", MessageType.PLAYER, p)
                                }

                                "Area" -> {
                                    sendMessage("§6ゲームモード : §b§lガチエリア", MessageType.PLAYER, p)
                                    sendMessage("§f§lエリアを確保して守り抜け！", MessageType.PLAYER, p)
                                }
                            }

                            if (getPlayerData(p)!!.team == match.team0) {
                                val l = getPlayerData(p)!!.match!!.mapData!!.team0Loc
                                val i = (getPlayerData(p)!!.playerNumber + 1) / 2
                                var sl: Location? = null
                                when (i) {
                                    1 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 1.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 1.5,
                                            )
                                    }

                                    2 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX - 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 1.5,
                                            )
                                    }

                                    3 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 1.5,
                                                l.blockY.toDouble(),
                                                l.blockZ - 0.5,
                                            )
                                    }

                                    4 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX - 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ - 0.5,
                                            )
                                    }

                                    else -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 0.5,
                                            )
                                    }
                                }
                                sl.yaw = l.yaw
                                getPlayerData(p)!!.matchLocation = (sl)
                            }
                            if (getPlayerData(p)!!.team == match.team1) {
                                val l = getPlayerData(p)!!.match!!.mapData!!.team1Loc
                                val i = getPlayerData(p)!!.playerNumber / 2
                                var sl: Location? = null
                                when (i) {
                                    1 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 1.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 1.5,
                                            )
                                    }

                                    2 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX - 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 1.5,
                                            )
                                    }

                                    3 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 1.5,
                                                l.blockY.toDouble(),
                                                l.blockZ - 0.5,
                                            )
                                    }

                                    4 -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX - 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ - 0.5,
                                            )
                                    }

                                    else -> {
                                        sl =
                                            Location(
                                                l!!.world,
                                                l.blockX + 0.5,
                                                l.blockY.toDouble(),
                                                l.blockZ + 0.5,
                                            )
                                    }
                                }
                                sl.yaw = l.yaw
                                getPlayerData(p)!!.matchLocation = (sl)
                            }

                            if (getPlayerData(p)!!.playerNumber <= 8) {
                                val e =
                                    getPlayerData(p)!!
                                        .matchLocation!!
                                        .world!!
                                        .spawnEntity(getPlayerData(p)!!.matchLocation!!, EntityType.SQUID)
                                squid = e as LivingEntity
                                squid!!.setAI(false)
                                squid!!.isSwimming = true
                                squid!!.customName = p.name
                                squid!!.isCustomNameVisible = true
                            }

                            p.gameMode = GameMode.SPECTATOR
                            p.inventory.clear()
                            val introl = match.mapData!!.intro
                            p.teleport(introl!!)
                            getPlayerData(p)!!.matchLocation

                            if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "TDM"
                            ) {
                                p.sendTitle(
                                    "§l" + match.mapData!!.mapName,
                                    "§7チームデスマッチ",
                                    10,
                                    70,
                                    20,
                                )
                            } else if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "Area"
                            ) {
                                p.sendTitle(
                                    "§l" + match.mapData!!.mapName,
                                    "§7ガチエリア",
                                    10,
                                    70,
                                    20,
                                )
                            } else {
                                p.sendTitle("§l" + match.mapData!!.mapName, "§7ナワバリバトル", 10, 70, 20)
                            }

                            startCount(p)

                            val manager = Bukkit.getScoreboardManager()
                            val scoreboard = manager!!.newScoreboard

                            val objective =
                                scoreboard.registerNewObjective(
                                    "match",
                                    "intro",
                                    "§6§lSclat§r " + Sclat.VERSION,
                                )
                            objective.displaySlot = DisplaySlot.SIDEBAR

                            val lines: MutableList<String> = mutableListOf()

                            lines.add("")
                            lines.add("§a§lマップ名 » §6" + getPlayerData(p)!!.match!!.mapData!!.mapName)
                            lines.add(" ")

                            if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "TDM"
                            ) {
                                lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §rチームデスマッチ")
                            } else if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "Area"
                            ) {
                                lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §rガチエリア")
                            } else {
                                lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §rナワバリバトル")
                            }
                            lines.add("  ")
                            lines.add("§b§l残り時間 » §r3:00")

                            ObjectiveUtil.setLine(objective, lines)

                            p.scoreboard = scoreboard

                            for (player in plugin.server.onlinePlayers) {
                                p.hidePlayer(plugin, player)
                            }
                        }
                        if (s in 1..100) {
                            if (s == 1) intromove = match.mapData!!.intro!!.clone()
                            val map = getPlayerData(p)!!.match!!.mapData
                            intromove!!.add(map!!.introMoveX, map.introMoveY, map.introMoveZ)
                            p.teleport(intromove!!)
                        }

                        if (s in 100..160) {
                            val introl =
                                match.mapData!!
                                    .team0Intro!!
                                    .clone()
                                    .add(0.5, 0.0, 0.5)
                            p.teleport(introl)
                            if (getPlayerData(p)!!.team == match.team0) {
                                if (s in 101..120) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team!!
                                            .teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    introl.world!!.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        getPlayerData(p)!!.matchLocation!!,
                                        10,
                                        0.3,
                                        0.4,
                                        0.3,
                                        1.0,
                                        bd,
                                    )
                                }
                                if (s == 120) {
                                    if (getPlayerData(p)!!.playerNumber <= 8) squid!!.remove()
                                }
                                if (s == 100) {
                                    if (getPlayerData(p)!!.playerNumber <= 8) {
                                        introl.world!!.playSound(
                                            getPlayerData(p)!!.matchLocation!!,
                                            Sound.ENTITY_PLAYER_SWIM,
                                            1f,
                                            1f,
                                        )
                                        NPCMgr.createNPC(p, p.name, getPlayerData(p)!!.matchLocation!!)
                                    }
                                }
                            }
                        }
                        if (s in 160..220) {
                            val introl =
                                match.mapData!!
                                    .team1Intro!!
                                    .clone()
                                    .add(0.5, 0.0, 0.5)
                            p.teleport(introl)
                            if (getPlayerData(p)!!.team == match.team1) {
                                if (s in 161..180) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team!!
                                            .teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    introl.world!!.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        getPlayerData(p)!!.matchLocation!!,
                                        10,
                                        0.3,
                                        0.4,
                                        0.3,
                                        1.0,
                                        bd,
                                    )
                                }
                                if (s == 180) {
                                    if (getPlayerData(p)!!.playerNumber <= 8) squid!!.remove()
                                }
                                if (s == 160) {
                                    if (getPlayerData(p)!!.playerNumber <= 8) {
                                        introl.world!!.playSound(
                                            getPlayerData(p)!!.matchLocation!!,
                                            Sound.ENTITY_PLAYER_SWIM,
                                            1f,
                                            1f,
                                        )
                                        NPCMgr.createNPC(p, p.name, getPlayerData(p)!!.matchLocation!!)
                                    }
                                }
                            }
                        }

                        if (s == 221) {
                            for (player in plugin.server.onlinePlayers) {
                                p.showPlayer(plugin, player)
                            }
                        }

                        if (s in 221..280) {
                            p.inventory.setItem(0, ItemStack(Material.AIR))
                            p.gameMode = GameMode.ADVENTURE
                            p.exp = 0.99f
                            val introl = getPlayerData(p)!!.matchLocation
                            p.teleport(introl!!)
                        }

                        if (s == 281) {
                            getPlayerData(p)!!.canFly = (false)

                            // playerclass
                            if (getPlayerData(p)!!.weaponClass!!.subWeaponName == "ビーコン") {
                                ArmorStandMgr.beaconArmorStandSetup(
                                    p,
                                )
                            }
                            if (getPlayerData(p)!!.weaponClass!!.subWeaponName == "スプリンクラー") {
                                ArmorStandMgr.sprinklerArmorStandSetup(
                                    p,
                                )
                            }
                            WeaponClassMgr.setWeaponClass(p)

                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.getIsSwap()) {
                                swapperRunnable(p)
                                if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.slidingShootTick > 1) {
                                    maneuverShootRunnable(p)
                                    getPlayerData(p)!!.isUsingManeuver = (true)
                                }
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Shooter") {
                                shooterRunnable(p)
                                if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.isManeuver) {
                                    if (getPlayerData(p)!!.settings!!.doChargeKeep()) {
                                        Shooter.maneuverRunnable(p)
                                    } else {
                                        Manuber.maneuverRunnable(p)
                                    }
                                    maneuverShootRunnable(p)
                                }
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Reeler") {
                                shooterRunnable(p)
                                reelerRunnable(p)
                                reelerShootRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Blaster") {
                                if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.isManeuver) {
                                    Shooter.maneuverRunnable(p)
                                }
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Buckler") {
                                shooterRunnable(p)
                                bucklerRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Bucket") {
                                bucketHealRunnable(
                                    p,
                                    1,
                                )
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Slosher") {
                                bucketHealRunnable(
                                    p,
                                    0,
                                )
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Charger") {
                                chargerRunnable(p)
                                decoyRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Spinner") spinnerRunnable(p)
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Roller") {
                                if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.isHude) {
                                    Brush.holdRunnable(p)
                                    Brush.rollPaintRunnable(p)
                                } else {
                                    Roller.holdRunnable(p)
                                    Roller.rollPaintRunnable(p)
                                }
                            }

                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Kasa") {
                                kasaRunnable(p, false)
                            }

                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Camping") {
                                kasaRunnable(p, true)
                                getPlayerData(p)!!.mainItemGlow = (true)
                                WeaponClassMgr.setWeaponClass(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Hound") {
                                houndRunnable(p)
                                houndEXRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Funnel") {
                                shooterRunnable(p)
                                funnelFloat(p)
                            }

                            SquidMgr.squidShowRunnable(p)

                            p.equipment!!.helmet = getPlayerData(p)!!.team!!.teamColor!!.bougu

                            setArmor(p, 31.0, 100, false)
                            SPWeaponMgr.spWeaponRunnable(p)
                            SPWeaponMgr.armorRunnable(p)

                            getPlayerData(p)!!.tick = 10

                            // Shooter.ShooterRunnable(p);

                            // SquidMgr.SquidRunnable(p);
                            getPlayerData(p)!!.isInMatch = (true)
                            p.exp = 0.99f
                            if (getPlayerData(p)!!.playerNumber == 1) {
                                inMatchCounter(p)
                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") == "Area"
                                ) {
                                    for (area in match.mapData!!.areaList) {
                                        area!!.setupAreaTeam()
                                    }
                                }
                            }
                            p.playSound(p.location, Sound.ENTITY_ZOMBIE_INFECT, 10.0f, 2.0f)

                            @Suppress("DEPRECATION")
                            if (getGearInfluence(p, Gear.Type.MAX_HEALTH_UP) == 1.2) {
                                p.maxHealth = 22.0
                            } else {
                                p.maxHealth = 20.0
                            }

                            SPWeaponMgr.spWeaponHuriRunnable(p)

                            // p.setPlayerListName(DataMgr.getPlayerData(p).getTeam().getTeamColor().getColorCode()
                            // + p.displayName);
                            if (getPlayerData(p)!!.playerNumber == 1 && Plugins.NOTEBLOCKAPI.isLoaded) {
                                val nbs = NoteBlockAPIMgr.randomNormalSong
                                val song = nbs.song
                                val radio = RadioSongPlayer(song)
                                radio.setVolume(volume)
                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.settings!!.playBGM() &&
                                        getPlayerData(oplayer)!!.isJoined
                                    ) {
                                        radio.addPlayer(oplayer)
                                        oplayer.spigot().sendMessage(
                                            ChatMessageType.ACTION_BAR,
                                            *TextComponent.fromLegacyText("§7Now playing : §6" + nbs.songName),
                                        )
                                    }
                                }
                                radio.isPlaying = true
                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") == "Area"
                                ) {
                                    stopMusic(
                                        radio,
                                        6000,
                                        match,
                                    )
                                } else {
                                    stopMusic(radio, 2400, match)
                                }
                            }

                            if (getPlayerData(p)!!.playerNumber == 1) {
                                PathMgr.setupPath(match)
                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") == "Area"
                                ) {
                                    for (area in match.mapData!!.areaList) {
                                        area!!.start()
                                    }
                                }

                                try {
                                    for (wiremesh in match.mapData!!.wiremeshListTask!!.wiremeshsList) {
                                        wiremesh!!.startTask()
                                    }
                                } catch (e: Exception) {
                                }
                            }

                            p.isCollidable = true

                            cancel()
                        }
                        s++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun stopMusic(
        radio: RadioSongPlayer,
        delay: Long,
        match: Match,
    ) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    radio.isPlaying = false
                }
            }
        task.runTaskLater(plugin, delay)

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    if (match.isFinished) {
                        radio.isPlaying = false
                        task.cancel()
                        cancel()
                    }
                }
            }
        task2.runTaskTimer(plugin, 0, 1)
    }

    fun startMatch(match: Match) {
        for (player in plugin.server.onlinePlayers) {
            val data = getPlayerData(player)
            if (data!!.match == match) {
                matchRunnable(player, match)
                data.lastAttack = player
            }
        }
        /*
         * Player leader = match.getLeaderPlayer();
         * if(DataMgr.getPlayerIsQuit(leader.getUniqueId().toString()))
         * MatchRunnable(leader, match);
         */
    }

    fun inMatchCounter(player: Player) {
        val manager = Bukkit.getScoreboardManager()
        val scoreboard = manager!!.newScoreboard

        val match = getPlayerData(player)!!.match
        match!!.scoreboard = (scoreboard)

        val bteam0 = scoreboard.registerNewTeam(match.team0!!.teamColor!!.colorName!!)
        bteam0.color = match.team0!!.teamColor!!.chatColor!!
        bteam0.setOption(
            org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
            org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS,
        )
        // bteam0.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        bteam0.setOption(
            org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
            org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM,
        )
        bteam0.prefix = match.team0!!.teamColor!!.colorCode!!

        val bteam1 = scoreboard.registerNewTeam(match.team1!!.teamColor!!.colorName!!)
        bteam1.color = match.team1!!.teamColor!!.chatColor!!
        // bteam1.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
        bteam1.setOption(
            org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
            org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS,
        )
        bteam1.setOption(
            org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
            org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM,
        )
        bteam1.prefix = match.team1!!.teamColor!!.colorCode!!

        match.team0!!.team = bteam0
        match.team1!!.team = bteam1

        for (oplayer in plugin.server.onlinePlayers) {
            if (getPlayerData(oplayer)!!.isJoined) {
                oplayer.scoreboard = scoreboard
                if (match.team0 == getPlayerData(oplayer)!!.team) bteam0.addEntry(oplayer.name)
                if (match.team1 == getPlayerData(oplayer)!!.team) bteam1.addEntry(oplayer.name)
            }
        }

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val sb: Scoreboard = scoreboard
                var objective: Objective? = sb.registerNewObjective("match", "run", "§6§lSclat§r " + Sclat.VERSION)
                var s: Int = 180
                val p: Player = player

                var team0nokori: Boolean = false
                var team1nokori: Boolean = false

                override fun run() {
                    try {
                        if (objective != null) objective!!.unregister()

                        objective = scoreboard.registerNewObjective("match", "intro", "§6§lSclat§r " + Sclat.VERSION)
                        objective!!.displaySlot = DisplaySlot.SIDEBAR

                        val min = String.format("%02d", s % 60)

                        val lines: MutableList<String> = mutableListOf()

                        lines.add("")
                        lines.add("§a§lマップ名 » §6" + getPlayerData(p)!!.match!!.mapData!!.mapName)
                        lines.add(" ")

                        if (Sclat.conf!!
                                .config!!
                                .getString("WorkMode") == "TDM"
                        ) {
                            lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §6チームデスマッチ")
                        } else if (Sclat.conf!!
                                .config!!
                                .getString("WorkMode") == "Area"
                        ) {
                            lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §6ガチエリア")
                        } else {
                            lines.add(ChatColor.YELLOW.toString() + "§lゲームモード » §6ナワバリバトル")
                        }
                        lines.add("  ")
                        lines.add("§b§l残り時間 » §r" + s / 60 + ":" + min)

                        var gcteam: Team? = null
                        var isgc = false
                        var entyo = false

                        // ガチエリアカウント
                        if (Sclat.conf!!
                                .config!!
                                .getString("WorkMode") == "Area"
                        ) {
                            val list: MutableList<Team?> = ArrayList()
                            for (area in match.mapData!!.areaList) {
                                list.add(area!!.team)
                            }

                            var `is` = true
                            var t: Team? = null
                            for ((i, team) in list.withIndex()) {
                                if (i == 0) {
                                    if (team != null) {
                                        t = team
                                    } else {
                                        `is` = false
                                        break
                                    }
                                } else {
                                    if (team != null) {
                                        if (team != t) {
                                            `is` = false
                                            break
                                        }
                                    } else {
                                        `is` = false
                                        break
                                    }
                                }
                            }

                            if (list.size == 1) {
                                if (list[0] != null) {
                                    `is` = true
                                }
                            }

                            if (`is`) {
                                val wteam = t // エリアを確保しているチーム
                                var lteam = t // エリアを確保できていないチーム
                                lteam =
                                    if (match.team0 == t) {
                                        match.team1
                                    } else {
                                        match.team0
                                    }

                                if (wteam!!.gatiCount == lteam!!.gatiCount) {
                                    if (wteam.gatiCount + 1 > lteam.gatiCount) {
                                        if (wteam.gatiCount != 0) {
                                            for (player in plugin.server.onlinePlayers) {
                                                val data = getPlayerData(player)
                                                if (data!!.team == null || !data.isInMatch) continue

                                                if (data.team == wteam) {
                                                    sendMessage("§b§lカウントリードした!", MessageType.PLAYER, player)
                                                    player.sendTitle("", "§b§lカウントリードした!", 10, 20, 10)
                                                    playGameSound(player, SoundType.CONGRATULATIONS)
                                                } else {
                                                    sendMessage("§c§lカウントリードされた!", MessageType.PLAYER, player)
                                                    player.sendTitle("", "§c§lカウントリードされた!", 10, 20, 10)
                                                    player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 3f)
                                                }
                                            }
                                        }
                                    }
                                }

                                list[0]!!.addGatiCount()
                                isgc = `is`
                                gcteam = list[0]
                            }

                            lines.add("   ")
                            lines.add("§lカウント » ")
                            lines.add(
                                (
                                    match.team0!!.teamColor!!.colorCode + match.team0!!.teamColor!!.colorName +
                                        " : " + (100 - match.team0!!.gatiCount) + "  " +
                                        match.team1!!.teamColor!!.colorCode + match.team1!!.teamColor!!.colorName +
                                        " : " + (100 - match.team1!!.gatiCount)
                                ),
                            )

                            if (isgc) {
                                var ngcteam: Team = match.team0!!
                                if (match.team0 == gcteam) ngcteam = match.team1!!
                                if (gcteam!!.gatiCount <= ngcteam.gatiCount) entyo = true
                            }

                            if (s == 0 && entyo) {
                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isInMatch) {
                                        oplayer.sendTitle("", "§7延長戦！", 10, 20, 10)
                                        sendMessage("§7延長戦！", MessageType.PLAYER, oplayer)
                                    }
                                }
                            }

                            if (match.team0!!.gatiCount == 100 || match.team1!!.gatiCount == 100) {
                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isJoined && p !== oplayer) {
                                        oplayer.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
                                        oplayer.inventory.clear()
                                        finishMatch(oplayer)
                                    }
                                }
                                finishMatch(p)
                                cancel()
                            }
                            if ((match.team0!!.gatiCount == 95 && !team0nokori) ||
                                (match.team1!!.gatiCount == 95 && !team1nokori)
                            ) {
                                if (match.team0!!.gatiCount == 95) team0nokori = true
                                if (match.team1!!.gatiCount == 95) team1nokori = true

                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isInMatch) {
                                        oplayer.sendTitle("", "§7残りカウントあとわずか！", 10, 20, 10)
                                        sendMessage("§7残りカウントあとわずか！", MessageType.PLAYER, oplayer)
                                        p.playSound(p.location, Sound.ENTITY_ZOMBIE_INFECT, 8.0f, 2.0f)
                                    }
                                }
                            }
                        }

                        ObjectiveUtil.setLine(objective!!, lines)

                        if (s == 60 &&
                            Sclat.conf!!
                                .config!!
                                .getString("WorkMode") != "Area"
                        ) {
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (getPlayerData(oplayer)!!.isJoined) {
                                    sendMessage("§6§n残り1分！", MessageType.PLAYER, oplayer)
                                }
                            }
                            if (getPlayerData(p)!!.playerNumber == 1 && Plugins.NOTEBLOCKAPI.isLoaded) {
                                val nbs = NoteBlockAPIMgr.randomFinalSong
                                val song = nbs.song
                                val radio = RadioSongPlayer(song)
                                radio.setVolume(volume)
                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.settings!!.playBGM() &&
                                        getPlayerData(oplayer)!!.isJoined
                                    ) {
                                        radio.addPlayer(oplayer)
                                        oplayer.spigot().sendMessage(
                                            ChatMessageType.ACTION_BAR,
                                            *TextComponent.fromLegacyText("§7Now playing : §6" + nbs.songName),
                                        )
                                    }
                                }
                                radio.isPlaying = true
                                stopMusic(radio, 1200, match)
                            }
                        }
                        if (s <= 0 && !entyo) {
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (getPlayerData(oplayer)!!.isJoined && p !== oplayer) {
                                    oplayer.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
                                    oplayer.inventory.clear()
                                    finishMatch(oplayer)
                                }
                            }
                            finishMatch(p)
                            cancel()
                        }

                        if (s <= -60) {
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (getPlayerData(oplayer)!!.isJoined && p !== oplayer) {
                                    oplayer.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
                                    oplayer.inventory.clear()
                                    finishMatch(oplayer)
                                }
                            }
                            finishMatch(p)
                            cancel()
                        }

                        if (s in 1..5) {
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (getPlayerData(oplayer)!!.isInMatch) {
                                    oplayer.sendTitle(
                                        ChatColor.GRAY.toString() + s.toString(),
                                        "",
                                        0,
                                        30,
                                        4,
                                    )
                                }
                            }
                        }

                        // Main.getPlugin().getServer().broadcastMessage(ChatColor.GOLD + "試合終了まで: " +
                        // String.valueOf(s));
                        s--
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }

    fun finishMatch(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var loc: Location? = null
                var winteam: Team? = getPlayerData(player)!!.match!!.team0
                var i: Int = 0
                var bestkills: Int = 0
                var bestpaint: Int = 0

                override fun run() {
                    try {
                        if (i == 0) {
                            p.setDisplayName(p.name)

                            getPlayerData(p)!!.match!!.isFinished = true
                            if (getPlayerData(p)!!.playerNumber == 1) {
                                for (path in getPlayerData(p)!!.match!!.mapData!!.pathList) {
                                    path!!.stop()
                                    path.reset()
                                }
                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") == "Area"
                                ) {
                                    for (area in getPlayerData(p)!!.match!!.mapData!!.areaList) {
                                        area!!.stop()
                                    }
                                }
                                for (oplayer in plugin.server.onlinePlayers) {
                                    @Suppress("DEPRECATION")
                                    oplayer.maxHealth = 20.0
                                }

                                for (uuid in DataMgr.pul) {
                                    setPlayerIsQuit(uuid, false)
                                }

                                getPlayerData(p)!!.match!!.blockUpdater!!.stop()

                                for (target in plugin.server.onlinePlayers) {
                                    sendWorldBorderWarningClearPacket(target)
                                }

                                try {
                                    for (wiremesh in getPlayerData(p)!!
                                        .match!!
                                        .mapData!!
                                        .wiremeshListTask!!
                                        .wiremeshsList) {
                                        wiremesh!!.startTask()
                                    }
                                } catch (e: Exception) {
                                }
                            }
                            for (`as` in beaconMap.values) `as`!!.remove()
                            for (`as` in sprinklerMap.values) `as`!!.remove()
                            beaconMap.clear()
                            sprinklerMap.clear()
                            armorStandMap.clear()
                            getPlayerData(p)!!.isInMatch = (false)
                            if (p.hasPotionEffect(PotionEffectType.SLOW)) p.removePotionEffect(PotionEffectType.SLOW)
                            p.playSound(p.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 3f, 1.3f)
                            loc = p.location

                            p.inventory.clear()

                            // p.setPlayerListName(p.displayName);
                            val manager = Bukkit.getScoreboardManager()
                            val scoreboard = manager!!.newScoreboard

                        /*
                         * org.bukkit.scoreboard.Team team = scoreboard.registerNewTeam("");
                         * team.addPlayer(p); team.setColor(ChatColor.WHITE);
                         */
                            p.scoreboard = scoreboard
                        }
                        if (i == 2) {
                            getPlayerData(p)!!.canFly = (true)
                            p.resetTitle()
                            p.sendTitle(
                                ChatColor.YELLOW.toString() + "=========================== Finish! ===========================",
                                "",
                                3,
                                30,
                                10,
                            )
                        }

                        if (i in 1..45) {
                            p.teleport(loc!!)
                            p.inventory.clear()
                            if (p.hasPotionEffect(PotionEffectType.POISON)) p.removePotionEffect(PotionEffectType.POISON)
                        }
                        if (i == 46) {
                            for (player in plugin.server.onlinePlayers) {
                                p.hidePlayer(plugin, player)
                            }
                        }
                        if (i == 46 && getPlayerData(p)!!.playerNumber == 1) {
                            if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "TDM"
                            ) {
                                val match = getPlayerData(p)!!.match
                                val team0c: Int
                                val team1c: Int
                                val team0code: String?
                                val team1code: String?
                                winteam = match!!.team0
                                var hikiwake = false

                                team0c = match.team0!!.killCount
                                team1c = match.team1!!.killCount
                                team0code = match.team0!!.teamColor!!.colorCode
                                team1code = match.team1!!.teamColor!!.colorCode

                                if (team0c < team1c) {
                                    winteam = match.team1
                                } else if (team0c == team1c) {
                                    hikiwake = true
                                }

                                match.winTeam = winteam
                                match.isHikiwake = (hikiwake)

                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isJoined) {
                                        tdmResultAnimation(
                                            oplayer,
                                            team0c,
                                            team1c,
                                            team0code,
                                            team1code,
                                            winteam,
                                            hikiwake,
                                        )
                                    }
                                }
                            } else if (Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "Area"
                            ) {
                                val match = getPlayerData(p)!!.match
                                val team0: Int
                                val team1: Int
                                val dper: Double
                                var per: Int
                                val team0code: String?
                                val team1code: String?
                                winteam = match!!.team0
                                var hikiwake = false

                                team0 = match.team0!!.gatiCount
                                team1 = match.team1!!.gatiCount
                                team0code = match.team0!!.teamColor!!.colorCode
                                team1code = match.team1!!.teamColor!!.colorCode
                                dper = team0.toDouble() / (team0 + team1).toDouble() * 100
                                per = dper.toInt()

                                if (match.team0!!.gatiCount > match.team1!!.gatiCount) {
                                    winteam = match.team0
                                    per++
                                    // match.team0.addPaintCount();
                                } else if (match.team0!!.gatiCount == match.team1!!.gatiCount) {
                                    hikiwake = true
                                } else {
                                    winteam = match.team1
                                    per--
                                }

                                match.winTeam = winteam
                                match.isHikiwake = (hikiwake)

                                if (per > 100) per = 100
                                if (per < 0) per = 0

                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isJoined) {
                                        if ((per == 100 || per == 0) && !hikiwake) {
                                            areaResultAnimation(
                                                oplayer,
                                                per,
                                                100 - per,
                                                team0code,
                                                team1code,
                                                winteam,
                                            )
                                        } else if (team0 == 100) {
                                            areaResultAnimation(
                                                oplayer,
                                                100,
                                                0,
                                                team0code,
                                                team1code,
                                                winteam,
                                            )
                                        } else if (team1 == 100) {
                                            areaResultAnimation(
                                                oplayer,
                                                0,
                                                100,
                                                team0code,
                                                team1code,
                                                winteam,
                                            )
                                        } else {
                                            resultAnimation(
                                                oplayer,
                                                per,
                                                100 - per,
                                                team0code,
                                                team1code,
                                                winteam,
                                                hikiwake,
                                            )
                                        }
                                    }
                                }
                            } else {
                                val match = getPlayerData(p)!!.match
                                val team0: Int
                                val team1: Int
                                val dper: Double
                                var per: Int
                                val team0code: String?
                                val team1code: String?
                                winteam = match!!.team0
                                var hikiwake = false

                                team0 = match.team0!!.point
                                team1 = match.team1!!.point
                                team0code = match.team0!!.teamColor!!.colorCode
                                team1code = match.team1!!.teamColor!!.colorCode
                                dper = team0.toDouble() / (team0 + team1).toDouble() * 100
                                per = dper.toInt()

                                if (match.team0!!.point > match.team1!!.point) {
                                    winteam = match.team0
                                    per++
                                    // match.team0.addPaintCount();
                                } else if (match.team0!!.point == match.team1!!.point) {
                                    hikiwake = true
                                } else {
                                    winteam = match.team1
                                    per--
                                }

                                match.winTeam = winteam
                                match.isHikiwake = (hikiwake)

                                if (per > 100) per = 100
                                if (per < 0) per = 0

                                for (oplayer in plugin.server.onlinePlayers) {
                                    if (getPlayerData(oplayer)!!.isJoined) {
                                        resultAnimation(
                                            oplayer,
                                            per,
                                            100 - per,
                                            team0code,
                                            team1code,
                                            winteam,
                                            hikiwake,
                                        )
                                    }
                                }
                            }
                        }

                        if (i == 46 && p.isOnline) p.gameMode = GameMode.ADVENTURE

                        if (i in 46..156) {
                            p.teleport(getPlayerData(p)!!.match!!.mapData!!.resultLoc!!)
                        }

                        if (i == 80) {
                            getPlayerData(p)
                            val commands: MutableList<String> =
                                mutableListOf(
                                    "return ${p.uniqueId}",
                                    "stop",
                                )
                            val sc =
                                StatusClient(
                                    Sclat.conf!!
                                        .config!!
                                        .getString("StatusShare.Host"),
                                    Sclat.conf!!
                                        .config!!
                                        .getInt("StatusShare.Port"),
                                    commands,
                                )
                            sc.startClient()
                        }

                        if (i == 137) {
                            val data = getPlayerData(p)

                            // int kill = data.killCount;
                            // int paint = data.paintCount;
                            data!!.canFly = (false)

                            sendMessage("§a----------<< Match result >>----------", MessageType.PLAYER, p)
                            sendMessage("", MessageType.PLAYER, p)

                            for (op in plugin.server.onlinePlayers) {
                                val odata = getPlayerData(op)
                                if (!odata!!.isJoined) continue
                                if (odata.team!!.iD == data.team!!.iD) {
                                    if (op == p) {
                                        sendMessage(
                                            (
                                                odata.team!!.teamColor!!.colorCode + "§l[ §l" +
                                                    op.displayName + "§l ]" + ChatColor.RESET + "Kills : " +
                                                    ChatColor.YELLOW + odata.killCount + "   " + ChatColor.RESET + "Points : " +
                                                    ChatColor.YELLOW + odata.paintCount
                                            ),
                                            MessageType.PLAYER,
                                            p,
                                        )
                                        // p.sendMessage(odata.getTeam().getTeamColor().getColorCode() + "§l[ §l" +
                                        // op.displayName + "§l ]" + ChatColor.RESET + "Kills : " +
                                        // ChatColor.YELLOW + odata.killCount + " " + ChatColor.RESET + "Points : "
                                        // + ChatColor.YELLOW + odata.paintCount);
                                    } else {
                                        sendMessage(
                                            (
                                                odata.team!!.teamColor!!.colorCode + "[ " +
                                                    op.displayName + " ]" + ChatColor.RESET + "Kills : " +
                                                    ChatColor.YELLOW + odata.killCount + "   " + ChatColor.RESET + "Points : " +
                                                    ChatColor.YELLOW + odata.paintCount
                                            ),
                                            MessageType.PLAYER,
                                            p,
                                        )
                                        // p.sendMessage(odata.getTeam().getTeamColor().getColorCode() + "[ " +
                                        // op.displayName + " ]" + ChatColor.RESET + "Kills : " + ChatColor.YELLOW
                                        // + odata.killCount + " " + ChatColor.RESET + "Points : " +
                                        // ChatColor.YELLOW + odata.paintCount);
                                    }
                                }
                                if (bestkills < odata.killCount) {
                                    bestkills = odata.killCount
                                }
                                if (bestpaint < odata.paintCount) {
                                    bestpaint = odata.paintCount
                                }
                            }

                            for (op in plugin.server.onlinePlayers) {
                                val odata = getPlayerData(op)
                                if (!odata!!.isJoined) continue
                                if (odata.team!!.iD != data.team!!.iD) {
                                    if (op == p) {
                                        sendMessage(
                                            (
                                                odata.team!!.teamColor!!.colorCode + "§l[ §l" +
                                                    op.displayName + "§l ]" + ChatColor.RESET + "Kills : " +
                                                    ChatColor.YELLOW + odata.killCount + "   " + ChatColor.RESET + "Points : " +
                                                    ChatColor.YELLOW + odata.paintCount
                                            ),
                                            MessageType.PLAYER,
                                            p,
                                        )
                                        // p.sendMessage(odata.getTeam().getTeamColor().getColorCode() + "§l[ §l" +
                                        // op.displayName + "§l ]" + ChatColor.RESET + "Kills : " +
                                        // ChatColor.YELLOW + odata.killCount + " " + ChatColor.RESET + "Points : "
                                        // + ChatColor.YELLOW + odata.paintCount);
                                    } else {
                                        sendMessage(
                                            (
                                                odata.team!!.teamColor!!.colorCode + "[ " +
                                                    op.displayName + " ]" + ChatColor.RESET + "Kills : " +
                                                    ChatColor.YELLOW + odata.killCount + "   " + ChatColor.RESET + "Points : " +
                                                    ChatColor.YELLOW + odata.paintCount
                                            ),
                                            MessageType.PLAYER,
                                            p,
                                        )
                                        // p.sendMessage(odata.getTeam().getTeamColor().getColorCode() + "[ " +
                                        // op.displayName + " ]" + ChatColor.RESET + "Kills : " + ChatColor.YELLOW
                                        // + odata.killCount + " " + ChatColor.RESET + "Points : " +
                                        // ChatColor.YELLOW + odata.paintCount);
                                    }
                                }
                            }
                        }

                        if (i == 157) {
                            val data = getPlayerData(p)

                            val pMoney = (data!!.killCount.toDouble() * 100.0 + data.paintCount.toDouble() / 5.0).toInt()
                            var pTicket =
                                (data.killCount.toDouble() * 1 + 10 + data.paintCount.toDouble() / 750.0).toInt()
                            var pLv = 1
                            if (data.team == data.match!!.winTeam || data.match!!.isHikiwake) {
                                pLv = 2
                                pTicket += 5
                            }
                            var pRank = data.killCount * 3
                            if (data.team == data.match!!.winTeam) pRank += 25
                            if (data.killCount == bestkills) {
                                pRank += 20
                            }
                            if (data.paintCount == bestpaint) {
                                pRank += 10
                            }
                            // int pRank = -60 + (int)((double)data.killCount * 2.7D +
                            // (double)data.paintCount / 700D);
                            // if(data.getTeam() == data.getMatch().getWinTeam() ||
                            // data.getMatch().getIsHikiwake())
                            // pRank = 80 + (int)((double)data.killCount * 2.2D +
                            // (double)data.paintCount / 700D);
                            if (data.match!!.joinedPlayerCount == 1 ||
                                !Sclat.conf!!
                                    .config!!
                                    .getBoolean("RateMatch")
                            ) {
                                pRank =
                                    0
                            }

                            val pMoveRank = RankMgr.indicateRankPointmove(p, pRank)
                            PlayerStatusMgr.addRank(p, pRank)

                            PlayerStatusMgr.addLv(p, pLv)
                            PlayerStatusMgr.addMoney(p, pMoney)
                            PlayerStatusMgr.addTicket(p, pTicket)

                            PlayerStatusMgr.addPaint(p, data.paintCount)
                            PlayerStatusMgr.addKill(p, data.killCount)

                            if (Sclat.type == ServerType.MATCH) {
                                val commands: MutableList<String> =
                                    mutableListOf(
                                        "add money " + pMoney + " " + p.uniqueId,
                                        "add level " + pLv + " " + p.uniqueId,
                                        "add ticket " + pTicket + " " + p.uniqueId,
                                        "add rank " + pRank + " " + p.uniqueId,
                                        "add kill " + data.killCount + " " + p.uniqueId,
                                        "add paint " + data.paintCount + " " + p.uniqueId,
                                        "stop",
                                    )
                                val sc =
                                    StatusClient(
                                        Sclat.conf!!
                                            .config!!
                                            .getString("StatusShare.Host"),
                                        Sclat.conf!!
                                            .config!!
                                            .getInt("StatusShare.Port"),
                                        commands,
                                    )
                                sc.startClient()
                            }

                            sendMessage("", MessageType.PLAYER, p)
                            sendMessage("§a----------<< Match bonus >>----------", MessageType.PLAYER, p)

                            sendMessage("", MessageType.PLAYER, p)
                            sendMessage(
                                (
                                    ChatColor.GREEN.toString() + " Money : " + ChatColor.RESET + "+" + pMoney +
                                        ChatColor.AQUA + "  Lv : " + ChatColor.RESET + "+" + pLv + ChatColor.GOLD +
                                        " Ticket : " + ChatColor.RESET + pTicket
                                ),
                                MessageType.PLAYER,
                                p,
                            )
                            sendMessage("", MessageType.PLAYER, p)
                            if (pRank < 0) {
                                sendMessage(
                                    (
                                        ChatColor.GOLD.toString() + " RankPoint : " + ChatColor.RESET + pRank +
                                            (
                                                if (Sclat.type == ServerType.NORMAL) {
                                                    "  [ §b" + RankMgr.toABCRank(PlayerStatusMgr.getRank(player)) + " §r]"
                                                } else {
                                                    ""
                                                }
                                            )
                                    ),
                                    MessageType.PLAYER,
                                    p,
                                )
                            } else {
                                sendMessage(
                                    (
                                        ChatColor.GOLD.toString() + " RankPoint : " + ChatColor.RESET + "+" + pMoveRank +
                                            (
                                                if (Sclat.type == ServerType.NORMAL) {
                                                    "  [ §b" + RankMgr.toABCRank(PlayerStatusMgr.getRank(player)) + " §r]"
                                                } else {
                                                    ""
                                                }
                                            )
                                    ),
                                    MessageType.PLAYER,
                                    p,
                                )
                            }
                            sendMessage("", MessageType.PLAYER, p)
                            sendMessage("§a-----------------------------------", MessageType.PLAYER, p)

                        /*
                         * p.sendMessage(ChatColor.GREEN + "##########################");
                         * p.sendMessage(ChatColor.GREEN + "          試合結果");
                         * p.sendMessage(ChatColor.GOLD + "     Kills  : " + ChatColor.YELLOW + kill);
                         * p.sendMessage(ChatColor.GOLD + "     Points : " + ChatColor.YELLOW + paint);
                         * p.sendMessage(ChatColor.GREEN + "##########################");
                         */
                            val worldName =
                                Sclat.conf!!
                                    .config!!
                                    .getString("Lobby.WorldName")
                            val w = Bukkit.getServer().getWorld(worldName!!)

                            val ix =
                                Sclat.conf!!
                                    .config!!
                                    .getInt("Lobby.X")
                            val iy =
                                Sclat.conf!!
                                    .config!!
                                    .getInt("Lobby.Y")
                            val iz =
                                Sclat.conf!!
                                    .config!!
                                    .getInt("Lobby.Z")
                            val iyaw =
                                Sclat.conf!!
                                    .config!!
                                    .getInt("Lobby.Yaw")
                            val il = Location(w, ix.toDouble(), iy.toDouble(), iz.toDouble())
                            il.yaw = iyaw.toFloat()
                            val wc = getPlayerData(p)!!.weaponClass
                            p.teleport(il)
                            if (Sclat.type != ServerType.MATCH) {
                                val join = ItemStack(Material.CHEST)
                                val joinmeta = join.itemMeta
                                joinmeta!!.setDisplayName("メインメニュー")
                                join.itemMeta = joinmeta
                                p.inventory.clear()
                                p.inventory.setItem(0, join)
                            }

                            PlayerStatusMgr.sendHologram(p)

                            if (getPlayerData(p)!!.playerNumber == 1) {
                                // Capture the finished match map and rollback before setting up next match.
                                val finishedMap = getPlayerData(p)!!.match!!.mapData
                                rollBack()
                                // Release usage for the finished map; MapLoader is usage-counted and will
                                // actually unload only when no matches are using it. Only call when non-null.
                                try {
                                    if (finishedMap != null) MapLoader.releaseMap(finishedMap)
                                } catch (e: Exception) {
                                    // Best-effort: avoid crashing the finish flow if unload fails.
                                }
                                matchcount++
                                matchSetup()

                                // Send match status
                                if (Sclat.type == ServerType.MATCH) {
                                    val commands: MutableList<String> =
                                        mutableListOf(
                                            "stopped " +
                                                Sclat.conf!!
                                                    .servers!!
                                                    .getString("ServerName"),
                                            "map " +
                                                Sclat.conf!!
                                                    .servers!!
                                                    .getString("ServerName") + " " +
                                                getMapRandom(
                                                    if (mapcount == 0) 0 else mapcount - 1,
                                                ).mapName,
                                            "stop",
                                        )
                                    val sc =
                                        StatusClient(
                                            Sclat.conf!!
                                                .config!!
                                                .getString("StatusShare.Host"),
                                            Sclat.conf!!
                                                .config!!
                                                .getInt("StatusShare.Port"),
                                            commands,
                                        )
                                    sc.startClient()
                                }

                                // DataMgr.getPlayerData(p).reset();
                                for (uuid in DataMgr.pul) {
                                    setPlayerIsQuit(uuid, false)
                                }
                            }

                            // player.setDisplayName(player.getName());
                            getPlayerData(p)!!.reset()
                            getPlayerData(p)!!.weaponClass = (wc)

                            DataMgr.joinedList.clear()

                            p.walkSpeed = 0.2f
                            p.health = 20.0

                            p.gameMode = GameMode.ADVENTURE
                            // PlayerData data = new PlayerData(p);
                            // data.weaponClass = (wc);
                            // DataMgr.setPlayerData(p, data);
                            for (player in plugin.server.onlinePlayers) {
                                p.showPlayer(plugin, player)
                            }

                            if (Sclat.type == ServerType.MATCH) {
                                try {
                                    BungeeCordMgr.playerSendServer(p, "sclat")
                                    getPlayerData(p)!!.setServerName("Sclat")
                                } catch (e: Exception) {
                                }
                            }

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
}
