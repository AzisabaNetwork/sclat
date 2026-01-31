package be4rjp.sclat.gui

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.data.BlockUpdater
import be4rjp.sclat.data.DataMgr.beaconMap
import be4rjp.sclat.data.DataMgr.getBeaconFromplayer
import be4rjp.sclat.data.DataMgr.getMatchFromId
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSprinklerFromplayer
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.data.DataMgr.setPaintDataFromBlock
import be4rjp.sclat.data.DataMgr.sprinklerMap
import be4rjp.sclat.data.PaintData
import be4rjp.sclat.manager.ArmorStandMgr.beaconArmorStandSetup
import be4rjp.sclat.manager.ArmorStandMgr.sprinklerArmorStandSetup
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.manager.BungeeCordMgr.PlayerSendServer
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.manager.MatchMgr.playerJoinMatch
import be4rjp.sclat.manager.MatchMgr.rollBack
import be4rjp.sclat.manager.PlayerStatusMgr.addGear
import be4rjp.sclat.manager.PlayerStatusMgr.addWeapon
import be4rjp.sclat.manager.PlayerStatusMgr.getMoney
import be4rjp.sclat.manager.PlayerStatusMgr.sendHologramUpdate
import be4rjp.sclat.manager.PlayerStatusMgr.setGear
import be4rjp.sclat.manager.PlayerStatusMgr.subMoney
import be4rjp.sclat.manager.SPWeaponMgr.SPWeaponRunnable
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.manager.ServerStatusManager.openServerList
import be4rjp.sclat.manager.SquidMgr.SquidShowRunnable
import be4rjp.sclat.manager.SuperJumpMgr
import be4rjp.sclat.manager.SuperJumpMgr.SuperJumpCollTime
import be4rjp.sclat.manager.WeaponClassMgr.setWeaponClass
import be4rjp.sclat.plugin
import be4rjp.sclat.tutorial.Tutorial
import be4rjp.sclat.weapon.Brush
import be4rjp.sclat.weapon.Bucket.bucketHealRunnable
import be4rjp.sclat.weapon.Buckler.BucklerRunnable
import be4rjp.sclat.weapon.Charger.ChargerRunnable
import be4rjp.sclat.weapon.Decoy.DecoyRunnable
import be4rjp.sclat.weapon.Funnel.funnelFloat
import be4rjp.sclat.weapon.Gear.getGearName
import be4rjp.sclat.weapon.Gear.getGearPrice
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
import be4rjp.sclat.weapon.Swapper.SwapperRunnable
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class ClickListener : Listener {
    @EventHandler
    fun onGUIClick(event: InventoryClickEvent) {
        if (event.getCurrentItem() == null ||
            event.getCurrentItem()!!.getItemMeta() == null ||
            event
                .getCurrentItem()
                ?.getItemMeta()
                ?.getDisplayName() == null
        ) {
            return
        }

        val name = event.getCurrentItem()!!.getItemMeta()!!.getDisplayName()
        val player = event.getWhoClicked() as Player

        if (name == ".") {
            event.setCancelled(true)
            return
        }

        if (name.isEmpty()) {
            return
        } else {
            player.closeInventory()
        }

        // player.sendMessage(name);
        when (name) {
            "試合に参加 / JOIN THE MATCH" -> {
                if (Sclat.type == ServerType.LOBBY) {
                    openServerList(player)
                } else {
                    playerJoinMatch(player)
                }
            }

            "装備変更 / EQUIPMENT" -> {
                OpenGUI.equipmentGUI(player, false)
            }

            "§bギア変更 / GEAR" -> {
                OpenGUI.gearGUI(player, false)
            }

            "§6武器変更 / WEAPON" -> {
                OpenGUI.openWeaponSelect(player, "Main", "null", false)
            }

            "§bギア購入 / GEAR" -> {
                OpenGUI.gearGUI(player, true)
            }

            "§6武器購入 / WEAPON" -> {
                OpenGUI.openWeaponSelect(player, "Main", "null", true)
            }

            "設定 / SETTINGS" -> {
                OpenGUI.openSettingsUI(player)
            }

            "ショップを開く / OPEN SHOP" -> {
                OpenGUI.equipmentGUI(player, true)
            }

            "塗りをリセット / RESET INK" -> {
                if (MatchMgr.canRollback) {
                    sendMessage("§a§lインクがリセットされました！", MessageType.ALL_PLAYER)
                    sendMessage("§a§l3分後に再リセットできるようになります", MessageType.ALL_PLAYER)
                    for (op in plugin.getServer().getOnlinePlayers()) playGameSound(op, SoundType.SUCCESS)
                }
                val match = getPlayerData(player)!!.match
                match!!.blockUpdater!!.stop()
                rollBack()
                player.setExp(0.99f)
                val bur = BlockUpdater()
                if (Sclat.Companion.conf!!
                        .config!!
                        .contains("BlockUpdateRate")
                ) {
                    bur.setMaxBlockInOneTick(
                        Sclat.Companion.conf!!.config!!.getInt(
                            "BlockUpdateRate",
                        ),
                    )
                }
                bur.start()
                match.blockUpdater = bur
                val blocks: MutableList<Block> = ArrayList<Block>()
                val b0 = Sclat.lobby!!.getBlock().getRelative(BlockFace.DOWN)
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
                    if (block.getType() == Material.WHITE_STAINED_GLASS) {
                        val pdata = PaintData(block)
                        pdata.match = (match)
                        pdata.team = (match.team0)
                        pdata.setOrigianlType(block.getType())
                        setPaintDataFromBlock(block, pdata)
                        block.setType(match.team0!!.teamColor!!.glass!!)
                    }
                }
            }

            "ロビーへ戻る / RETURN TO LOBBY" -> {
                if (Sclat.type != ServerType.LOBBY) {
                    PlayerSendServer(player, "sclat")
                    getPlayerData(player)!!.setServerName("Sclat")
                } else {
                    PlayerSendServer(player, "lobby")
                    getPlayerData(player)!!.setServerName("Lobby")
                }
            }

            "称号 / EMBLEM" -> {
                OpenGUI.openEmblemMenu(player)
            }

            "試し打ちサーバーへ接続 / TRAINING FIELD" -> {
                PlayerSendServer(player, "sclattest")
                getPlayerData(player)!!.setServerName("sclattest")
            }

            "チームデスマッチサーバーへ接続 / CONNECT TO TDM SERVER" -> {
                PlayerSendServer(player, "tdm")
                getPlayerData(player)!!.setServerName("TDM")
            }

            "ナワバリバトル" -> {
                val ma = getMatchFromId(MatchMgr.matchcount)
                ma!!.addnawabariTCount()
            }

            "チームデスマッチ" -> {
                val m = getMatchFromId(MatchMgr.matchcount)
                m!!.addtdmTCount()
            }

            "ガチエリア" -> {
                val m2 = getMatchFromId(MatchMgr.matchcount)
                m2!!.addgatiareaTCount()
            }

            "戻る" -> {
                if (name != "武器選択" || name != "Shop") OpenGUI.openMenu(player)
            }
        }
        if (name == "リソースパックをダウンロード / DOWNLOAD RESOURCEPACK") {
            player.setResourcePack(
                Sclat.Companion.conf!!.config!!.getString(
                    "ResourcePackURL",
                )!!,
            )
        }
        if (event.getView().getTitle() == "Gear") {
            var i = 0
            while (i <= 9) {
                if (getGearName(i) == name) {
                    getPlayerData(player)!!.gearNumber = i
                    setGear(player, i)
                    sendMessage(
                        "ギア[" + ChatColor.AQUA + name + ChatColor.RESET + "]を選択しました",
                        MessageType.PLAYER,
                        player,
                    )
                    break
                }
                i++
            }
        } else if (event.getView().getTitle() == "Gear shop") {
            var i = 0
            while (i <= 9) {
                if (getGearName(i) == name) {
                    if (getMoney(player) >= getGearPrice(i)) {
                        addGear(player, i)
                        subMoney(player, getGearPrice(i))
                        sendMessage(ChatColor.GREEN.toString() + "購入に成功しました", MessageType.PLAYER, player)
                        playGameSound(player, SoundType.SUCCESS)
                        sendHologramUpdate(player)
                    } else {
                        sendMessage(ChatColor.RED.toString() + "お金が足りません", MessageType.PLAYER, player)
                        playGameSound(player, SoundType.ERROR)
                    }
                    break
                }
                i++
            }
        }
        if (event.getView().getTitle() == "Server List") {
            for (ss in ServerStatusManager.serverList) {
                if (ss.displayName == name) {
                    if (ss.restartingServer) {
                        sendMessage("§c§nこのサーバーは再起動中です1~2分程度お待ちください", MessageType.PLAYER, player)
                        playGameSound(player, SoundType.ERROR)
                        return
                    }
                    if (ss.isOnline) {
                        if (ss.playerCount < ss.maxPlayer) {
                            if (ss.runningMatch) {
                                sendMessage("§c§nこのサーバーは試合中のため参加できません", MessageType.PLAYER, player)
                                playGameSound(player, SoundType.ERROR)
                                return
                            }
                            BungeeCordMgr.PlayerSendServer(player, ss.serverName!!)
                            getPlayerData(player)!!.setServerName(ss.displayName)
                        } else {
                            sendMessage("§c§nこのサーバーは満員のため参加できません", MessageType.PLAYER, player)
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

        if (event.getView().getTitle() == "武器選択") {
            if (name == "装備選択へ戻る" ||
                name == "戻る" ||
                name == "シューター" ||
                name == "ローラー" ||
                name == "チャージャー" ||
                name == "ブラスター" ||
                name == "バーストシューター" ||
                name == "スロッシャー" ||
                name == "シェルター" ||
                name == "ブラシ" ||
                name == "スピナー" ||
                name == "マニューバー" ||
                name == "ハウンド" ||
                name == "スワッパー" ||
                name == "ドラグーン" ||
                name == "リーラー" ||
                name == "バックラー"
            ) {
                when (name) {
                    "シューター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Shooter", false)
                    "ブラスター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Blaster", false)
                    "バーストシューター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Burst", false)
                    "ローラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Roller", false)
                    "スロッシャー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Slosher", false)
                    "シェルター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Kasa", false)
                    "ブラシ" -> OpenGUI.openWeaponSelect(player, "Weapon", "Hude", false)
                    "スピナー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Spinner", false)
                    "チャージャー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Charger", false)
                    "マニューバー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Maneu", false)
                    "ハウンド" -> OpenGUI.openWeaponSelect(player, "Weapon", "Hound", false)
                    "スワッパー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Swapper", false)
                    "ドラグーン" -> OpenGUI.openWeaponSelect(player, "Weapon", "Funnel", false)
                    "リーラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Reeler", false)
                    "バックラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Buckler", false)
                    "戻る" -> OpenGUI.openWeaponSelect(player, "Main", "null", false)
                    "装備選択へ戻る" -> OpenGUI.equipmentGUI(player, false)
                }
                return
            }
            if (name.contains("§6レベル")) {
                sendMessage("§cレベルが足りないため、まだ選択できません", MessageType.PLAYER, player)
                playGameSound(player, SoundType.ERROR)
                return
            }
            // 試しうちモード
            if (Sclat.Companion.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
                player.getInventory().clear()
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
                            getPlayerData(p)!!.isInMatch = true
                            getPlayerData(p)!!.isJoined = true
                            getPlayerData(p)!!.mainItemGlow = false
                            getPlayerData(p)!!.tick = 10
                            val wc = getWeaponClass(name)
                            getPlayerData(p)!!.weaponClass = (wc)
                            if (getPlayerData(p)!!.weaponClass!!.subWeaponName == "ビーコン") beaconArmorStandSetup(p)
                            if (getPlayerData(p)!!.weaponClass!!.subWeaponName == "スプリンクラー") {
                                sprinklerArmorStandSetup(
                                    p,
                                )
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.getIsSwap()) {
                                SwapperRunnable(p)
                                if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.slidingShootTick > 1) {
                                    maneuverShootRunnable(p)
                                    getPlayerData(p)!!.isUsingManeuver = true
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
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Charger") {
                                ChargerRunnable(p)
                                DecoyRunnable(p)
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
                                getPlayerData(p)!!.mainItemGlow = true
                                setWeaponClass(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Buckler") {
                                shooterRunnable(p)
                                BucklerRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Bucket") {
                                bucketHealRunnable(p, 1)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Slosher") {
                                bucketHealRunnable(p, 0)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Hound") {
                                houndRunnable(p)
                                houndEXRunnable(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Funnel") {
                                shooterRunnable(p)
                                funnelFloat(p)
                            }
                            setWeaponClass(p)
                            player.setExp(0.99f)

                            // p.setScoreboard(DataMgr.getPlayerData(p).getMatch().getScoreboard());
                            // DataMgr.getPlayerData(p).getTeam().getTeam().addEntry(p.getName());
                            SPWeaponRunnable(player)
                            SquidShowRunnable(player)
                        }
                    }
                delay.runTaskLater(plugin, 15)
            } else {
                getPlayerData(player)!!.weaponClass = (getWeaponClass(name))
            }
            sendMessage(
                "ブキ[" + ChatColor.GOLD + name + ChatColor.RESET + "]を選択しました",
                MessageType.PLAYER,
                player,
            )
        }

        if (event.getView().getTitle() == "Shop") {
            if (name == "装備選択へ戻る" ||
                name == "戻る" ||
                name == "シューター" ||
                name == "ローラー" ||
                name == "チャージャー" ||
                name == "ブラスター" ||
                name == "バーストシューター" ||
                name == "スロッシャー" ||
                name == "シェルター" ||
                name == "ブラシ" ||
                name == "スピナー" ||
                name == "マニューバー" ||
                name == "ハウンド" ||
                name == "スワッパー" ||
                name == "ドラグーン" ||
                name == "リーラー" ||
                name == "バックラー"
            ) {
                when (name) {
                    "シューター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Shooter", true)
                    "ブラスター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Blaster", true)
                    "バーストシューター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Burst", true)
                    "ローラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Roller", true)
                    "スロッシャー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Slosher", true)
                    "シェルター" -> OpenGUI.openWeaponSelect(player, "Weapon", "Kasa", true)
                    "ブラシ" -> OpenGUI.openWeaponSelect(player, "Weapon", "Hude", true)
                    "スピナー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Spinner", true)
                    "チャージャー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Charger", true)
                    "マニューバー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Maneu", true)
                    "ハウンド" -> OpenGUI.openWeaponSelect(player, "Weapon", "Hound", true)
                    "スワッパー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Swapper", true)
                    "ドラグーン" -> OpenGUI.openWeaponSelect(player, "Weapon", "Funnel", true)
                    "リーラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Reeler", true)
                    "バックラー" -> OpenGUI.openWeaponSelect(player, "Weapon", "Buckler", true)
                    "戻る" -> OpenGUI.openWeaponSelect(player, "Main", "null", true)
                    "装備選択へ戻る" -> OpenGUI.equipmentGUI(player, true)
                }
                return
            }
            if (name.contains("§6レベル")) {
                sendMessage("§cレベルが足りないため、まだ購入できません", MessageType.PLAYER, player)
                playGameSound(player, SoundType.ERROR)
                return
            }
            if (name.contains("§6ガチャ武器です")) {
                sendMessage("§cガチャから手に入るよ", MessageType.PLAYER, player)
                playGameSound(player, SoundType.ERROR)
                return
            }

            player.closeInventory()
            if (getWeaponClass(name)!!.mainWeapon!!.islootbox) {
                // Todo: thinking
            } else if (getMoney(player) >= getWeaponClass(name)!!.mainWeapon!!.money) {
                addWeapon(player, name)
                subMoney(player, getWeaponClass(name)!!.mainWeapon!!.money)
                sendMessage(ChatColor.GREEN.toString() + "購入に成功しました", MessageType.PLAYER, player)
                playGameSound(player, SoundType.SUCCESS)
                sendHologramUpdate(player)
            } else {
                sendMessage(ChatColor.RED.toString() + "お金が足りません", MessageType.PLAYER, player)
                playGameSound(player, SoundType.ERROR)
            }
        }

        if (event.getView().getTitle() == "Chose Target") {
            if (name == "§r§6リスポーン地点へジャンプ") {
                var loc: Location? = Sclat.lobby!!.clone()
                if (Sclat.Companion.conf!!
                        .config!!
                        .getString("WorkMode") != "Trial"
                ) {
                    loc =
                        getPlayerData(player)!!.matchLocation
                }
                SuperJumpMgr.SuperJumpCollTime(player, loc!!, false)
            }
            if (name == "§r§6ロビーへジャンプ") {
                val worldName =
                    Sclat.Companion.conf!!
                        .config!!
                        .getString("LobbyJump.WorldName")
                val w = Bukkit.getWorld(worldName!!)
                val ix =
                    Sclat.Companion.conf!!
                        .config!!
                        .getInt("LobbyJump.X")
                val iy =
                    Sclat.Companion.conf!!
                        .config!!
                        .getInt("LobbyJump.Y")
                val iz =
                    Sclat.Companion.conf!!
                        .config!!
                        .getInt("LobbyJump.Z")
                val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
                SuperJumpCollTime(player, loc, true)
            }
            var nearspwan = true
            var spawnloc: Location? = Sclat.lobby!!.clone()
            if (Sclat.Companion.conf!!
                    .config!!
                    .getString("WorkMode") != "Trial"
            ) {
                spawnloc =
                    getPlayerData(player)!!.matchLocation
            }
            if (spawnloc!!.getWorld() === player.getWorld()) {
                if (player
                        .getLocation()
                        .distance(spawnloc) > 10 &&
                    !Tutorial.clearList.contains(player)
                ) {
                    if (!Sclat.tutorial) {
                        nearspwan = false
                    }
                }
            }
            for (p in plugin.getServer().getOnlinePlayers()) {
                if (p.getName() == name) {
                    if (event.getCurrentItem()!!.getType() == Material.PLAYER_HEAD) {
                        if (p.getGameMode() == GameMode.SPECTATOR) {
                            sendMessage("§c今そのプレイヤーにはジャンプできない！", MessageType.PLAYER, player)
                            playGameSound(player, SoundType.ERROR)
                            break
                        }
                        SuperJumpMgr.SuperJumpCollTime(
                            player,
                            getPlayerData(p)!!.playerGroundLocation!!,
                            nearspwan,
                        )
                    }
                    if (event.getCurrentItem()!!.getType() == Material.IRON_TRAPDOOR) {
                        SuperJumpCollTime(
                            player,
                            getBeaconFromplayer(p)!!.getLocation(),
                            nearspwan,
                        )
                    }
                }
            }
        }

        if (event.getView().getTitle() == "設定") {
            if (name == "戻る") {
                OpenGUI.openMenu(player)
                return
            }

            when (name) {
                "メインウエポンのインクエフェクト" -> getPlayerData(player)!!.settings!!.sShowEffectMainWeaponInk()
                "チャージャーのレーザー" -> getPlayerData(player)!!.settings!!.sShowEffectChargerLine()
                "スペシャルウエポンのエフェクト" -> getPlayerData(player)!!.settings!!.sShowEffectSPWeapon()
                "スペシャルウエポンの範囲エフェクト" -> getPlayerData(player)!!.settings!!.sShowEffectSPWeaponRegion()
                "弾の表示" -> getPlayerData(player)!!.settings!!.sShowSnowBall()
                "BGM" -> getPlayerData(player)!!.settings!!.sPlayBGM()
                "投擲武器の視認用エフェクト" -> getPlayerData(player)!!.settings!!.sShowEffectBomb()
                "爆発エフェクト" -> getPlayerData(player)!!.settings!!.sShowEffectBombEx()
                "チャージキープ" -> getPlayerData(player)!!.settings!!.sDoChargeKeep()
            }

            OpenGUI.openSettingsUI(player)

            player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.C))

            val b = if (getPlayerData(player)!!.settings!!.playBGM()) "1" else "0"
            val eS = if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) "1" else "0"
            val eCL = if (getPlayerData(player)!!.settings!!.showEffectChargerLine()) "1" else "0"
            val eCS = if (getPlayerData(player)!!.settings!!.showEffectSPWeapon()) "1" else "0"
            val eRR = if (getPlayerData(player)!!.settings!!.showEffectSPWeaponRegion()) "1" else "0"
            val eRS = if (getPlayerData(player)!!.settings!!.showSnowBall()) "1" else "0"
            // String E_BGM = DataMgr.getPlayerData(player).getSettings().PlayBGM() ? "1" :
            // "0";
            val eB = if (getPlayerData(player)!!.settings!!.showEffectBomb()) "1" else "0"
            val eBEx = if (getPlayerData(player)!!.settings!!.showEffectBombEx()) "1" else "0"
            val ck = if (getPlayerData(player)!!.settings!!.doChargeKeep()) "1" else "0"

            val sData = b + eS + eCL + eCS + eRR + eRS + eB + eBEx + ck

            val uuid: String = player.getUniqueId().toString()
            Sclat.Companion.conf!!
                .playerSettings
                .set("Settings." + uuid, sData)
        }

        if (player.getGameMode() != GameMode.CREATIVE) event.setCancelled(true)
    }

    @EventHandler
    fun onOpenMainMenu(event: PlayerInteractEvent) {
        val player = event.getPlayer()
        val action = event.getAction()

        if (player
                .getInventory()
                .getItemInMainHand()
                .getItemMeta() == null ||
            player
                .getInventory()
                .getItemInMainHand()
                .getItemMeta()
                ?.getDisplayName() == null
        ) {
            return
        }

        if (action == Action.RIGHT_CLICK_AIR ||
            action == Action.RIGHT_CLICK_BLOCK ||
            action == Action.LEFT_CLICK_AIR ||
            action == Action.LEFT_CLICK_BLOCK
        ) {
            if (player.getInventory().getItemInMainHand().getType() == Material.CHEST) OpenGUI.openMenu(player)
            when (
                player
                    .getInventory()
                    .getItemInMainHand()
                    .getItemMeta()!!
                    .getDisplayName()
            ) {
                "スーパージャンプ" -> {
                    OpenGUI.superJumpGUI(player)
                }

                "§c§n右クリックで退出" -> {
                    PlayerSendServer(player, "sclat")
                    getPlayerData(player)!!.setServerName("Sclat")
                }

                "§a§n右クリックで参加" -> {
                    playerJoinMatch(player)
                }
            }
            if (player
                    .getInventory()
                    .getItemInMainHand()
                    .getItemMeta()!!
                    .getDisplayName() == "スーパージャンプ"
            ) {
                OpenGUI.superJumpGUI(player)
            }
        }
    }
}
