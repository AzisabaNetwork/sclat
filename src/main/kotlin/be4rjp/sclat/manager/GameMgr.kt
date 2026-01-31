package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.player.PlayerSettings
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.beaconMap
import be4rjp.sclat.data.DataMgr.getBeaconFromplayer
import be4rjp.sclat.data.DataMgr.getMatchFromId
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSprinklerFromplayer
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.data.DataMgr.playerIsQuitMap
import be4rjp.sclat.data.DataMgr.setPaintDataFromBlock
import be4rjp.sclat.data.DataMgr.setPlayerData
import be4rjp.sclat.data.DataMgr.setPlayerIsQuit
import be4rjp.sclat.data.DataMgr.setUUIDData
import be4rjp.sclat.data.DataMgr.sprinklerMap
import be4rjp.sclat.data.PaintData
import be4rjp.sclat.gui.LootBox
import be4rjp.sclat.gui.OpenGUI
import be4rjp.sclat.lobby.LobbyScoreboardRunnable
import be4rjp.sclat.packet.PacketHandler
import be4rjp.sclat.plugin
import be4rjp.sclat.server.EquipmentClient
import be4rjp.sclat.server.EquipmentServerManager.doCommands
import be4rjp.sclat.tutorial.Tutorial
import be4rjp.sclat.tutorial.Tutorial.setInkResetTimer
import be4rjp.sclat.weapon.Brush
import be4rjp.sclat.weapon.Bucket.bucketHealRunnable
import be4rjp.sclat.weapon.Buckler.bucklerRunnable
import be4rjp.sclat.weapon.Charger.chargerRunnable
import be4rjp.sclat.weapon.Decoy.decoyRunnable
import be4rjp.sclat.weapon.Funnel.funnelFloat
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
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import java.util.Random
import java.util.concurrent.Callable

/**
 *
 * @author Be4rJP
 */
class GameMgr : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.getPlayer()
        player.inventory.clear()
        player.inventory.heldItemSlot = 0

        player.isCollidable = false

        // player.setDisplayName(player.getName());
        if (PlayerReturnManager.isReturned(
                player.uniqueId.toString(),
            )
        ) {
            e.joinMessage = ChatColor.GOLD.toString() + player.name + " returned from a match."
        }

        player.gameMode = GameMode.ADVENTURE
        val data = PlayerData(player)

        val uuid: String = player.uniqueId.toString()
        val settings = PlayerSettings(player)
        data.settings = settings
        data.weaponClass = (
            getWeaponClass(
                Sclat.conf!!
                    .config!!
                    .getString("DefaultClass"),
            )
        )
        setPlayerData(player, data)

        // ((LivingEntity)player).setCollidable(false);
        PlayerStatusMgr.setupPlayerStatus(player)

        Sclat.conf!!
            .uUIDCash
            .set(player.uniqueId.toString(), player.name)
        if (Sclat.type == ServerType.LOBBY) {
            // Add user-specific hologram
            // RankingHolograms rankingHolograms = new RankingHolograms(player);
            // DataMgr.setRankingHolograms(player, rankingHolograms);
            // PlayerStatusMgr.HologramUpdateRunnable(player);
            Sclat.playerHolograms.add(player)
        }

        if (Sclat.type != ServerType.MATCH) {
            data.gearNumber = PlayerStatusMgr.getGear(player)
            data.weaponClass = (getWeaponClass(PlayerStatusMgr.getEquiptClass(player)))
        }
        // 処理の分散
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    when (i) {
                        0 -> {
                            run {
                                // ----------------------------------------------------------------------------
                                if (Sclat.conf!!
                                        .config!!
                                        .getString("WorkMode") != "Trial" &&
                                    Sclat.type != ServerType.MATCH
                                ) {
                                    PlayerStatusMgr.sendHologram(
                                        player,
                                    )
                                }
                            }
                            run {
                                // ----------------------------------------------------------------------------
                                SettingMgr.setSettings(settings, player)
                            }
                            run {
                                // ----------------------------------------------------------------------------
                                val head: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            val item = ItemStack(Material.PLAYER_HEAD)
                                            val meta = item.itemMeta as SkullMeta?
                                            meta!!.owningPlayer = player
                                            meta.setDisplayName(player.name)
                                            item.itemMeta = meta
                                            data.playerHead = (CraftItemStack.asNMSCopy(item))
                                        }
                                    }
                                head.runTaskAsynchronously(plugin)
                                if (Sclat.type == ServerType.MATCH) {
                                    if (Sclat.modList.contains(player.name)) {
                                        Sclat.modList.remove(player.name)
                                    } else {
                                        MatchMgr.playerJoinMatch(player)
                                    }
                                }
                            }
                            run {
                                cancel()
                            }
                        }

                        1 -> {
                            run {
                                SettingMgr.setSettings(settings, player)
                            }
                            run {
                                val head: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            val item = ItemStack(Material.PLAYER_HEAD)
                                            val meta = item.itemMeta as SkullMeta?
                                            meta!!.owningPlayer = player
                                            meta.setDisplayName(player.name)
                                            item.itemMeta = meta
                                            data.playerHead = (CraftItemStack.asNMSCopy(item))
                                        }
                                    }
                                head.runTaskAsynchronously(plugin)
                                if (Sclat.type == ServerType.MATCH) {
                                    if (Sclat.modList.contains(player.name)) {
                                        Sclat.modList.remove(player.name)
                                    } else {
                                        MatchMgr.playerJoinMatch(player)
                                    }
                                }
                            }
                            run {
                                cancel()
                            }
                        }

                        2 -> {
                            run {
                                val head: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            val item = ItemStack(Material.PLAYER_HEAD)
                                            val meta = item.itemMeta as SkullMeta?
                                            meta!!.owningPlayer = player
                                            meta.setDisplayName(player.name)
                                            item.itemMeta = meta
                                            data.playerHead = (CraftItemStack.asNMSCopy(item))
                                        }
                                    }
                                head.runTaskAsynchronously(plugin)
                                if (Sclat.type == ServerType.MATCH) {
                                    if (Sclat.modList.contains(player.name)) {
                                        Sclat.modList.remove(player.name)
                                    } else {
                                        MatchMgr.playerJoinMatch(player)
                                    }
                                }
                            }
                            run {
                                cancel()
                            }
                        }

                        3 -> {
                            cancel()
                        }
                    }

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 5)

        // PacketHandler
        val packetHandler = PacketHandler(player)
        val pipeline =
            (player as CraftPlayer)
                .handle
                .playerConnection.networkManager.channel
                .pipeline()
        pipeline.addBefore("packet_handler", "SclatPacketInjector:" + player.name, packetHandler)

        // 試し撃ちモード
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            val match = getMatchFromId(MatchMgr.matchcount)
            data.match = match
            data.team = match!!.team0
            player.teleport(Sclat.lobby!!)
            val join = ItemStack(Material.CHEST)
            val joinmeta = join.itemMeta
            joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
            join.itemMeta = joinmeta
            player.inventory.clear()
            SquidMgr.squidRunnable(player)
            SquidMgr.squidShowRunnable(player)
            player.exp = 0.99f
            player.inventory.setItem(7, join)

            if (Sclat.tutorial) {
                setInkResetTimer(player)
                Tutorial.clearList.add(player)
            }

            val delay: BukkitRunnable =
                object : BukkitRunnable() {
                    val p: Player = player

                    override fun run() {
                        // WeaponClassMgr.weaponClass = (p);
                        player.inventory.clear()
                        val join = ItemStack(Material.CHEST)
                        val joinmeta = join.itemMeta
                        joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
                        join.itemMeta = joinmeta
                        if (!Sclat.tutorial) player.inventory.setItem(7, join)
                        player.exp = 0f
                        SPWeaponMgr.spWeaponRunnable(player)
                        SPWeaponMgr.armorRunnable(p)
                        SquidMgr.squidShowRunnable(player)
                        if (!Sclat.tutorial) {
                            doCommands()
                            OpenGUI.openWeaponSelect(p, "Main", "null", false)
                        } else {
                            player.inventory.clear()
                            getPlayerData(player)!!.reset()
                            getPlayerData(player)!!.isInMatch = false
                            getPlayerData(player)!!.isJoined = false

                            for (`as` in beaconMap.values) {
                                if (getBeaconFromplayer(player) === `as`) `as`!!.remove()
                            }
                            for (`as` in sprinklerMap.values) {
                                if (getSprinklerFromplayer(player) === `as`) `as`!!.remove()
                            }

                            val delay: BukkitRunnable =
                                object : BukkitRunnable() {
                                    val p: Player = player

                                    override fun run() {
                                        getPlayerData(p)!!.isInMatch = (true)
                                        getPlayerData(p)!!.isJoined = (true)
                                        getPlayerData(p)!!.mainItemGlow = (false)
                                        getPlayerData(p)!!.tick = 10
                                        val wc =
                                            getWeaponClass(
                                                Sclat.conf!!
                                                    .config!!
                                                    .getString("DefaultClass"),
                                            )
                                        getPlayerData(p)!!.weaponClass = (wc)
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
                                        if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Spinner") {
                                            spinnerRunnable(
                                                p,
                                            )
                                        }
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
                                        WeaponClassMgr.setWeaponClass(p)
                                        player.exp = 0.99f

                                        SPWeaponMgr.spWeaponRunnable(player)
                                        SquidMgr.squidShowRunnable(player)
                                    }
                                }
                            delay.runTaskLater(plugin, 15)
                        }
                    }
                }
            delay.runTaskLater(plugin, 15)

            val armor: BukkitRunnable =
                object : BukkitRunnable() {
                    override fun run() {
                        ArmorStandMgr.armorStandSetup(player)
                    }
                }
            if (ArmorStandMgr.isSpawned) return
            armor.runTaskLater(plugin, 50)
            ArmorStandMgr.isSpawned = true

            val blocks: MutableList<Block> = ArrayList()
            val b0 = Sclat.lobby!!.block.getRelative(BlockFace.DOWN)
            blocks.add(b0)
            blocks.add(b0.getRelative(BlockFace.EAST))
            blocks.add(b0.getRelative(BlockFace.NORTH))
            blocks.add(b0.getRelative(BlockFace.SOUTH))
            blocks.add(b0.getRelative(BlockFace.WEST))
            blocks.add(b0.getRelative(BlockFace.NORTH_EAST))
            blocks.add(b0.getRelative(BlockFace.NORTH_WEST))
            blocks.add(b0.getRelative(BlockFace.SOUTH_EAST))
            blocks.add(b0.getRelative(BlockFace.SOUTH_WEST))
            for (block in blocks) {
                if (block.type == Material.WHITE_STAINED_GLASS) {
                    val pdata = PaintData(block)
                    pdata.match = match
                    pdata.team = match.team0
                    pdata.setOrigianlType(block.type)
                    setPaintDataFromBlock(block, pdata)
                    block.type = match.team0!!.teamColor!!.glass!!
                }
            }

            // Equipment
            player.inventory.clear()

            for (`as` in beaconMap.values) {
                if (getBeaconFromplayer(player) === `as`) `as`!!.remove()
            }
            for (`as` in sprinklerMap.values) {
                if (getSprinklerFromplayer(player) === `as`) `as`!!.remove()
            }

            return
        }

        setUUIDData(player.uniqueId.toString(), data)
        player.walkSpeed = 0.2f
        SquidMgr.squidRunnable(player)

        player.inventory.clear()
        if (Sclat.type != ServerType.LOBBY) {
            player.teleport(Sclat.lobby!!)
        } else {
            if (PlayerStatusMgr.getTutorialState(player.uniqueId.toString()) == 1) {
                val worldName =
                    Sclat.conf!!
                        .config!!
                        .getString("Tutorial.WorldName")
                val w = Bukkit.getWorld(worldName!!)
                val ix =
                    Sclat.conf!!
                        .config!!
                        .getInt("Tutorial.X")
                val iy =
                    Sclat.conf!!
                        .config!!
                        .getInt("Tutorial.Y")
                val iz =
                    Sclat.conf!!
                        .config!!
                        .getInt("Tutorial.Z")
                val iyaw =
                    Sclat.conf!!
                        .config!!
                        .getInt("Tutorial.Yaw")
                val tutorial = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
                tutorial.yaw = iyaw.toFloat()
                player.teleport(tutorial)
            } else {
                player.teleport(Sclat.lobby!!)
            }
        }
        if (Sclat.type != ServerType.MATCH) {
            if (PlayerStatusMgr.getTutorialState(player.uniqueId.toString()) == 2) {
                val join = ItemStack(Material.CHEST)
                val joinmeta = join.itemMeta
                joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
                join.itemMeta = joinmeta
                player.inventory.clear()
                player.inventory.setItem(0, join)
            }
        } else {
            val b = ItemStack(Material.BARRIER)
            val bmeta = b.itemMeta
            bmeta!!.setDisplayName("§c§n右クリックで退出")
            b.itemMeta = bmeta
            player.inventory.clear()
            player.inventory.setItem(8, b)

            val join = ItemStack(Material.LIME_STAINED_GLASS)
            val joinmeta = join.itemMeta
            joinmeta!!.setDisplayName("§a§n右クリックで参加")
            join.itemMeta = joinmeta
            player.inventory.setItem(0, join)
        }

        if (Sclat.type == ServerType.LOBBY) {
            // Scoreboard
            val runnable = LobbyScoreboardRunnable(player)
            runnable.runTaskTimerAsynchronously(plugin, 0, 10)
        }

        val match = getMatchFromId(Int.MAX_VALUE)
        data.match = match
        data.team = match!!.team0

        if (!playerIsQuitMap.containsKey(player.uniqueId.toString())) {
            setPlayerIsQuit(uuid, false)
        }

        if (!DataMgr.pul.contains(uuid)) DataMgr.pul.add(uuid)

        if (Sclat.type == ServerType.LOBBY) {
            // if(PlayerStatusMgr.getTutorialState(player.getUniqueId().toString()) == 0){
            if (PlayerStatusMgr.getTutorialState(player.uniqueId.toString()) == 0) {
                e.joinMessage = ChatColor.GREEN.toString() + player.name + " が初めてこのサーバーにログインしました！"
                PlayerStatusMgr.setTutorialState(player.uniqueId.toString(), 2)

                val join = ItemStack(Material.CHEST)
                val joinmeta = join.itemMeta
                joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
                join.itemMeta = joinmeta
                player.inventory.clear()
                player.inventory.setItem(0, join)
            }
            // 操作説明本
            val termsBook = ItemStack(Material.WRITTEN_BOOK)
            val bookMeta = termsBook.itemMeta as BookMeta?

            // 本のタイトルと著者を設定
            bookMeta!!.title = ChatColor.DARK_GREEN.toString() + "操作説明"
            bookMeta.author = ChatColor.GRAY.toString() + "Sclat運営"

            // 利用規約の内容を追加
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "目次\n\n" + ChatColor.RESET + "目次:P1\n\n" + "試合に参加するには:P2\n\n" +
                        "試合中の操作方法:P3~5\n\n" + "ロビーでの操作方法:P6~7\n\n" + "武器種紹介:P8~21\n\n" + "その他コラム:P22~25"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "試合に参加するには\n\n" + ChatColor.RESET + "正面にあるタワーの中にある\n" +
                        "看板を右クリックすると試合ロビーに移動できます\n" + "※試合がすでに始まっている場合や再起動中の鯖には参加できません"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "試合中の操作方法①\n\n" + ChatColor.RESET + "・試合が始まると武器が支給されます。\n\n" +
                        "・一番左のアイテムがメイン武器で右クリックで射撃できます。\n\n" + "・経験値バーがインクゲージとなっていて、これを消費し、射撃します。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "試合中の操作方法②\n\n" + ChatColor.RESET +
                        "・消費したインクゲージはイカになって自分のチームの色の床や壁に触れることで回復します。\n\n" + "・イカになるには手にアイテムを何も持たないとイカになります。\n\n" +
                        "・イカの状態では自分のチーム色の壁や床を移動できます。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "試合中の操作方法③\n\n" + ChatColor.RESET +
                        "・アイテムスロットの左から3番目のアイテムを右クリックでサブウェポンを使用できます。\n\n" +
                        "・画面上部のゲージがMAXの状態でアイテムスロットの真ん中のアイテムを右クリックでスペシャルを使用できます。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "ロビーでの操作方法①\n\n" + ChatColor.RESET + "・アイテムスロットのチェストを右クリックでメニューを開けます。\n\n" +
                        "・カーソルを合わせて左クリックで各項目を選択できます。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "ロビーでの操作方法②\n\n" + ChatColor.RESET +
                        "・メニューからは装備の購入・変更、テクスチャのインストールなどが可能です。\n\n" + "・インベントリを閉じることでメニューを閉じることができます"
                ),
            )
            bookMeta.addPage(
                ChatColor.BOLD.toString() + "武器紹介「シューター」\n" + ChatColor.RESET + "右クリックで射撃\n" + "汎用性に長けていてクセもなく、使い勝手がよい。",
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「ブラスター」\n" + ChatColor.RESET + "右クリックで爆発する弾を発射する。\n" +
                        "爆風でダメージを入れやすく、\n" + "弾を直撃させることで大ダメージを与えることができる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「バーストシューター」\n" + ChatColor.RESET + "一度の右クリックで弾を数発射撃する。\n" +
                        "射撃に間隔が開くため外すと隙ができるが、高い瞬間火力を誇る。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「ローラー」\n" + ChatColor.RESET + "右クリックで横広に弾をばら撒く。\n" +
                        "射撃の瞬間に空中にいると縦に広く弾をばら撒く。\n" + "右クリックを長押しすることで足元を塗りながら移動できる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「ブラシ」\n" + ChatColor.RESET + "右クリックで少量の弾をばら撒く。\n" +
                        "右クリックを長押しすることで足元を塗りながら高速で移動できる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「シェルター」\n" + ChatColor.RESET + "右クリックで大量の弾をばら撒く。\n" +
                        "シフトで盾を作り、離すか一定時間経過で盾を前進させる。"
                ),
            )
            bookMeta.addPage(
                ChatColor.BOLD.toString() + "武器紹介「スロッシャー」\n" + ChatColor.RESET + "右クリックで弾をばら撒く。\n" + "シフトで一定時間追加HPを獲得する。",
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「チャージャー」\n" + ChatColor.RESET + "右クリック長押しでチャージし離すと射撃。\n" +
                        "敵の背後から攻撃することでダメージが上昇する。\n" + "シフトでデコイを作ることができる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「スピナー」\n" + ChatColor.RESET + "右クリック長押しでチャージし離すと射撃。\n" +
                        "射程と射撃時間がチャージの量で変化する。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「マニューバ」\n" + ChatColor.RESET + "右クリックで射撃。\n" +
                        "シフトで2回ブリンク可能、ブリンク後に移動するまで火力と連射力が上がる。\n" + "その代わり通常時の性能が著しく低い。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「ハウンド」\n" + ChatColor.RESET + "右クリックで壁を登る弾を発射。\n" +
                        "シフトで起爆し、弾が射撃地点より高い場所であればあるほど火力と範囲が上がる。\n" + "逆に、低い場所で起爆させると火力と範囲が下がる"
                ),
            )
            bookMeta.addPage(
                ChatColor.BOLD.toString() + "武器紹介「スワッパー」\n" + ChatColor.RESET + "右クリックで射撃。\n" + "シフトで変形し、武器の性能が変化する。",
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「ドラグーン」\n" + ChatColor.RESET + "右クリックで射撃。\n" +
                        "シフトでタレットに敵を追尾させ、自動で攻撃する。\n" + "射撃命中時にタレットが追撃する。\n" + "タレットが追尾中の敵は追撃の火力が上がる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "武器紹介「リーラ―」\n" + ChatColor.RESET + "右クリックで射撃。\n" + "シフトで敵に向かって飛ぶ事ができる。\n" +
                        "チャクチするまでの間武器の性能が変化する。\n" + "敵をキルすることでスキルがリチャージされる。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "その他コラム①\n\n" + ChatColor.RESET +
                        "・武器によってはシフト(キーコンフィグを変更している場合はしゃがむ)で固有のスキルを使用することができます。\n\n" +
                        "・試合中左クリックでもサブウェポンを使用でき、武器を持っていたりイカになっていても使用できます。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "その他コラム②\n\n" + ChatColor.RESET +
                        "・てきとうなアイテムを持ってQキーでもスペシャルを使うことができます。\n\n" + "・爆風は壁を貫通してダメージを与える事ができます。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "その他コラム③\n\n" + ChatColor.RESET + "・チャージャーのバックスタブの判定はかなり広い。\n\n" +
                        "・敵のドラグーンのタレットは破壊可能。"
                ),
            )
            bookMeta.addPage(
                (
                    ChatColor.BOLD.toString() + "その他コラム④\n\n" + ChatColor.RESET +
                        "・マニューバはSclatメニューの設定のチャージキープをDisableにすることで飛距離が変化しにくい方式に変わります。\n\n" +
                        "・Optifineを導入することでバリエーション違いの武器の見た目が変化する"
                ),
            )

            // 作成したBookMetaを設定
            termsBook.itemMeta = bookMeta

            // プレイヤーのインベントリをクリアし、利用規約の本をアイテムスロットに追加
            // player.getInventory().clear();
            player.inventory.setItem(2, termsBook)
            // 操作説明本終
            // player.sendTitle("", "チュートリアルサーバーへ転送中...", 0, 20, 0);
            // Sclat.sendMessage("§bチュートリアルサーバーへ転送中...", MessageType.PLAYER, player);
            // BukkitRunnable run = new BukkitRunnable() {
            // @Override
            // public void run() {
            // List<String> list =
            // Main.tutorialServers.getConfig().getStringList("server-list");
            // BungeeCordMgr.PlayerSendServer(player, list.get(new
            // Random().nextInt(list.size())));
            // DataMgr.getPlayerData(player).setServerName(conf.getServers().getString("Tutorial.DisplayName"));
            // }
            // };
            // run.runTaskLater(Main.getPlugin(), 20);
            // }
        }

        // player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        val player = event.getPlayer()
        if (getPlayerData(player)!!.isInMatch) OpenGUI.superJumpGUI(player)
        event.isCancelled = true
    }

    @EventHandler
    fun onDamageByFall(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL || event.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.isCancelled = true
        }
        if (event.getEntity() is Player) {
            val target = event.getEntity() as Player
            if (event.cause == EntityDamageEvent.DamageCause.POISON) {
                getPlayerData(target)!!.isPoisonCoolTime = (true)
                SquidMgr.poisonCoolTime(target)
            }
            // AntiDamageTime
            val task: BukkitRunnable =
                object : BukkitRunnable() {
                    val p: Player = target

                    override fun run() {
                        target.noDamageTicks = 0
                    }
                }
            task.runTaskLater(plugin, 1)

            /*
             * Timer timer = new Timer(false); TimerTask t = new TimerTask(){ Player p =
             * target;
             *
             * @Override public void run(){ try{ target.setNoDamageTicks(0); timer.cancel();
             * }catch(Exception e){ timer.cancel(); } } }; timer.schedule(t, 25);
             */
        }
    }

    @EventHandler
    fun onPlaceBlockByEntity(event: EntityChangeBlockEvent) {
        if (event.getEntity() !is Player) {
            event.isCancelled = true
            if (event
                    .block
                    .type
                    .toString()
                    .contains("CONCRETE")
            ) {
                event
                    .block
                    .state
                    .update(false, false)
            }
        }
    }

    // @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerChat(event: AsyncPlayerChatEvent?) {
        // event.setCancelled(true);
//        /*
//         * if(!Main.LunaChat){ Player player = event.getPlayer();
//         * if(DataMgr.getPlayerData(player).getIsJoined()) event.setFormat("<" +
//         * DataMgr.getPlayerData(player).getTeam().getTeamColor().getColorCode() +
//         * player.getName() + "§r> " + event.getMessage()); else event.setFormat("<" +
//         * player.getName() + "> " + event.getMessage()); }
//         */
    }

    @EventHandler
    fun onLeavesDecay(event: LeavesDecayEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockFall(event: BlockPhysicsEvent) {
        if (event.changedType.toString().contains("CONCRETE")) event.isCancelled = true
    }

    @EventHandler
    fun onPickItem(event: EntityPickupItemEvent) {
        if (event.getEntity() is Player) {
            if ((event.getEntity() as Player).gameMode != GameMode.CREATIVE) event.isCancelled = true
        }
    }

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        event.isCancelled = true
        val player = event.getPlayer()
        val data = getPlayerData(player)
        if (data!!.isInMatch && data.sPGauge == 100) {
            SPWeaponMgr.useSPWeapon(
                player,
                data.weaponClass!!.sPWeaponName!!,
            )
        }

        // if(data.isInMatch())
        // WeaponClassMgr.weaponClass = (player);
    }

    // sign
    @EventHandler
    fun onClickSign(e: PlayerInteractEvent) {
        val player = e.getPlayer()
        e.getAction()
        if (e.clickedBlock != null) {
            if (e
                    .clickedBlock!!
                    .type
                    .toString()
                    .endsWith("SIGN")
            ) {
                val sign = e.clickedBlock!!.state as Sign

                if (Sclat.type == ServerType.LOBBY) {
                    for (ss in ServerStatusManager.serverList) {
                        if (ss.sign == e.clickedBlock) {
                            if (ss.restartingServer) {
                                sendMessage(
                                    "§c§nこのサーバーは再起動中です1~2分程度お待ちください",
                                    MessageType.PLAYER,
                                    player,
                                )
                                playGameSound(player, SoundType.ERROR)
                                return
                            }
                            if (ss.isOnline) {
                                if (ss.playerCount < ss.maxPlayer) {
                                    if (ss.runningMatch) {
                                        sendMessage(
                                            "§c§nこのサーバーは試合中のため参加できません",
                                            MessageType.PLAYER,
                                            player,
                                        )
                                        playGameSound(player, SoundType.ERROR)
                                        return
                                    }
                                    BungeeCordMgr.playerSendServer(player, ss.serverName!!)
                                    getPlayerData(player)!!.setServerName(ss.displayName)
                                } else {
                                    sendMessage(
                                        "§c§nこのサーバーは満員のため参加できません",
                                        MessageType.PLAYER,
                                        player,
                                    )
                                    playGameSound(player, SoundType.ERROR)
                                }
                            } else {
                                if (ss.isMaintenance) {
                                    sendMessage(
                                        "§c§nこのサーバーは現在メンテナンス中のため参加できません",
                                        MessageType.PLAYER,
                                        player,
                                    )
                                } else {
                                    sendMessage(
                                        "§c§nこのサーバーは現在再起動中です1~2分程度お待ちください。",
                                        MessageType.PLAYER,
                                        player,
                                    )
                                }
                                playGameSound(player, SoundType.ERROR)
                            }
                            return
                        }
                    }
                }

                val line = sign.getLine(2)
                when (line) {
                    "[ Join ]" -> {
                        if (Sclat.type == ServerType.LOBBY) {
                            ServerStatusManager.openServerList(player)
                        } else {
                            MatchMgr.playerJoinMatch(player)
                        }
                    }

                    "[ Equipment ]" -> {
                        OpenGUI.equipmentGUI(player, false)
                    }

                    "[ Equip shop ]" -> {
                        OpenGUI.equipmentGUI(player, true)
                    }

                    "[ OpenMenu ]" -> {
                        OpenGUI.openMenu(player)
                    }

                    "Click to Download" -> {
                        // player.setResourcePack(conf.getConfig().getString("ResourcePackURL"));
                        player.sendMessage("以下のURLからリソースパックをダウンロードしてください")
                        player.sendMessage(
                            Sclat.conf!!
                                .config!!
                                .getString("ResourcePackURL")!!,
                        )
                    }

                    "Click to Vote" -> {
                        // player.setResourcePack(conf.getConfig().getString("ResourcePackURL"));
                        player.sendMessage("以下のURLから投票してね！")
                        player.sendMessage("https://minecraft.jp/servers/azisaba.net")
                    }

                    "Click To Download" -> {
                        player.setResourcePack(
                            Sclat.conf!!
                                .config!!
                                .getString("ResourcePackURL")!!,
                        )
                    }

                    "Click to Return" -> {
                        BungeeCordMgr.playerSendServer(player, "lobby")
                        getPlayerData(player)!!.setServerName("Lobby")
                    }

                    "[ Training Mode ]" -> {
                        BungeeCordMgr.playerSendServer(player, "sclattest")
                        getPlayerData(player)!!.setServerName("sclattest")
                    }

                    "[ Return to jg ]" -> {
                        BungeeCordMgr.playerSendServer(player, "jg")
                        getPlayerData(player)!!.setServerName("JG")
                    }

                    "Return to sclat" -> {
                        BungeeCordMgr.playerSendServer(player, "sclat")
                        getPlayerData(player)!!.setServerName("Sclat")
                    }

                    "[Charge special]" -> {
                        if (getPlayerData(player)!!.isInMatch && !getPlayerData(player)!!.isUsingSP) {
                            getPlayerData(
                                player,
                            )!!.sPGauge = (100)
                        }
                    }

                    "[ Sclat ]" -> {
                        BungeeCordMgr.playerSendServer(player, "sclat")
                        getPlayerData(player)!!.setServerName("Sclat")
                    }

                    "[ LootBox ]" -> {
                        LootBox.turnLootBox(player)
                    }

                    "[ LootBoxInfo ]" -> {
                        LootBox.lootBoxInfo(player)
                    }

                    "[ GiftForYou ]" -> {
                        LootBox.giftWeapon(player, "お年玉[巳]")
                    }

                    "[ EasterEgg ]" -> {
                        LootBox.giftbook(player)
                    }

                    "[ ChangeTeam ]" -> {
                        LootBox.changeteam(player)
                    }

                    "[ give chest ]" -> {
                        PlayerStatusMgr.setTutorialState(player.uniqueId.toString(), 2)
                        val chest = ItemStack(Material.CHEST)
                        val chestmeta = chest.itemMeta
                        chestmeta!!.setDisplayName("右クリックでメインメニューを開く")
                        chest.itemMeta = chestmeta
                        player.inventory.setItem(0, chest)
                    }

                    "[ trade ticket ]" -> {
                        if (PlayerStatusMgr.getMoney(player) > 1000) {
                            PlayerStatusMgr.subMoney(player, 1000)
                            PlayerStatusMgr.addTicket(player, 1)
                            sendMessage("1000coinを1ticketに交換しました", MessageType.PLAYER, player)
                        } else {
                            sendMessage("coinが足りません", MessageType.PLAYER, player)
                        }
                    }

                    "[ give ticket ]" -> {
                        PlayerStatusMgr.addTicket(player, 10)
                        sendMessage("10ticket付与しました", MessageType.PLAYER, player)
                    }

                    "[ Tutorial ]" -> {
                        val list = Sclat.tutorialServers!!.getConfig()!!.getStringList("server-list")
                        BungeeCordMgr.playerSendServer(player, list.get(Random().nextInt(list.size)))
                        getPlayerData(player)!!
                            .setServerName(
                                Sclat.conf!!
                                    .servers!!
                                    .getString("Tutorial.DisplayName"),
                            )
                    }

                    "[ Instructions ]" -> {
                        player.performCommand("torisetu")
                    }

                    "[ Shooter ]" -> {
                        OpenGUI.openWeaponSelect(player, "Weapon", "Shooter", false)
                    }

                    "[ Roller ]" -> {
                        OpenGUI.openWeaponSelect(player, "Weapon", "Roller", false)
                    }

                    "[ Charger ]" -> {
                        OpenGUI.openWeaponSelect(player, "Weapon", "Charger", false)
                    }

                    "[ PatchNote ]" -> {
                        val component = TextComponent()
                        component.text = "[パッチノートを見るにはここをクリック]"
                        component.color = net.md_5.bungee.api.ChatColor.AQUA
                        component.clickEvent =
                            ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                "https://be4rjp.github.io/Sclat-PatchNote/note/v102b/note.html",
                            )
                        player.spigot().sendMessage(component)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onFrameBreak(event: HangingBreakByEntityEvent) {
        if (event.remover !is Player) return
        val player = event.remover as Player?
        if (player!!.gameMode == GameMode.CREATIVE) return
        if (event.entity is ItemFrame) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.getPlayer()
        val data = getPlayerData(player)

        // PacketHandler
        val channel =
            (player as CraftPlayer)
                .handle
                .playerConnection.networkManager.channel
        channel.eventLoop().submit<Any?>(
            Callable {
                channel.pipeline().remove(player.name)
                null
            },
        )

        if (Sclat.type == ServerType.MATCH) {
            if (DataMgr.joinedList.contains(player)) {
                setPlayerIsQuit(player.uniqueId.toString(), true)
                if (data!!.match!!.canJoin()) data.match!!.subJoinedPlayerCount()

                val team = data.team
                team!!.subtractRateTotal(PlayerStatusMgr.getRank(player))

                DataMgr.joinedList.remove(player)
            }
        }

        val server = getPlayerData(player)!!.servername
        if (!server!!.isEmpty()) {
            event.quitMessage = "§6" + player.name + " switched to " + server

            if (Sclat.type == ServerType.LOBBY) {
                for (serverName in Sclat.conf!!
                    .servers!!
                    .getConfigurationSection("Servers")!!
                    .getKeys(false)) {
                    val name =
                        Sclat.conf!!
                            .servers!!
                            .getString("Servers." + serverName + ".Server")
                    val displayName =
                        Sclat.conf!!
                            .servers!!
                            .getString("Servers." + serverName + ".DisplayName")
                    if (displayName == server) {
                        val commands: MutableList<String?> = ArrayList<String?>()
                        commands.add("set weapon " + data!!.weaponClass!!.className + " " + player.uniqueId)
                        commands.add("set gear " + data.gearNumber + " " + player.uniqueId)
                        commands.add("set rank " + PlayerStatusMgr.getRank(player) + " " + player.uniqueId)
                        commands.add(
                            (
                                "setting " +
                                    Sclat.conf!!
                                        .playerSettings
                                        .getString("Settings." + player.uniqueId) +
                                    " " + player.uniqueId
                            ),
                        )
                        commands.add("stop")
                        val sc =
                            EquipmentClient(
                                Sclat.conf!!
                                    .config!!
                                    .getString("EquipShare." + name + ".Host"),
                                Sclat.conf!!
                                    .config!!
                                    .getInt("EquipShare." + name + ".Port"),
                                commands,
                            )
                        sc.startClient()
                    }
                }
                if (server == "sclattest") {
                    val commands: MutableList<String?> = ArrayList<String?>()
                    commands.add("set rank " + PlayerStatusMgr.getRank(player) + " " + player.uniqueId)
                    commands.add("set lv " + PlayerStatusMgr.getLv(player) + " " + player.uniqueId)
                    commands.add(
                        (
                            "setting " +
                                Sclat.conf!!
                                    .playerSettings
                                    .getString("Settings." + player.uniqueId) +
                                " " + player.uniqueId
                        ),
                    )
                    commands.add("stop")
                    val sc =
                        EquipmentClient(
                            Sclat.conf!!
                                .config!!
                                .getString("EquipShare.Trial.Host"),
                            Sclat.conf!!
                                .config!!
                                .getInt("EquipShare.Trial.Port"),
                            commands,
                        )
                    sc.startClient()
                }
            }
        }

        if (data!!.weaponClass!!.subWeaponName == "ビーコン" && data.isInMatch) {
            getBeaconFromplayer(player)!!.remove()
        }
        if (data.weaponClass!!.subWeaponName == "スプリンクラー" && data.isInMatch) {
            getSprinklerFromplayer(player)!!.remove()
        }

        if (data.weaponClass != null) PlayerStatusMgr.setEquiptClass(player, data.weaponClass!!.className)
    }
}
