package be4rjp.sclat.gui

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
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
import be4rjp.sclat.manager.BungeeCordMgr.playerSendServer
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.manager.MatchMgr.playerJoinMatch
import be4rjp.sclat.manager.MatchMgr.rollBack
import be4rjp.sclat.manager.PlayerStatusMgr.addGear
import be4rjp.sclat.manager.PlayerStatusMgr.addWeapon
import be4rjp.sclat.manager.PlayerStatusMgr.getMoney
import be4rjp.sclat.manager.PlayerStatusMgr.sendHologramUpdate
import be4rjp.sclat.manager.PlayerStatusMgr.setGear
import be4rjp.sclat.manager.PlayerStatusMgr.subMoney
import be4rjp.sclat.manager.SPWeaponMgr.spWeaponRunnable
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.manager.ServerStatusManager.openServerList
import be4rjp.sclat.manager.SquidMgr.squidShowRunnable
import be4rjp.sclat.manager.SuperJumpMgr.superJumpCollTime
import be4rjp.sclat.manager.WeaponClassMgr.setWeaponClass
import be4rjp.sclat.plugin
import be4rjp.sclat.tutorial.Tutorial
import be4rjp.sclat.weapon.Brush
import be4rjp.sclat.weapon.Bucket.bucketHealRunnable
import be4rjp.sclat.weapon.Buckler.bucklerRunnable
import be4rjp.sclat.weapon.Charger.chargerRunnable
import be4rjp.sclat.weapon.Decoy.decoyRunnable
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
import be4rjp.sclat.weapon.Swapper.swapperRunnable
import net.azisaba.sclat.core.enums.MessageType
import net.azisaba.sclat.core.enums.ServerType
import net.azisaba.sclat.core.enums.SoundType
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
        val name = event.currentItem?.itemMeta?.displayName ?: return
        val player = event.whoClicked as? Player ?: return

        if (name == ".") {
            event.isCancelled = true
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
                    for (op in plugin.server.onlinePlayers) playGameSound(op, SoundType.SUCCESS)
                }
                val match = getPlayerData(player)!!.match
                match!!.blockUpdater!!.stop()
                rollBack()
                player.exp = 0.99f
                val bur = BlockUpdater(plugin)
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
                        pdata.match = (match)
                        pdata.team = (match.team0)
                        pdata.setOrigianlType(block.type)
                        setPaintDataFromBlock(block, pdata)
                        block.type = match.team0!!.teamColor!!.glass!!
                    }
                }
            }

            "ロビーへ戻る / RETURN TO LOBBY" -> {
                if (Sclat.type != ServerType.LOBBY) {
                    playerSendServer(player, "sclat")
                    getPlayerData(player)!!.setServerName("Sclat")
                } else {
                    playerSendServer(player, "lobby")
                    getPlayerData(player)!!.setServerName("Lobby")
                }
            }

            "称号 / EMBLEM" -> {
                OpenGUI.openEmblemMenu(player)
            }

            "試し打ちサーバーへ接続 / TRAINING FIELD" -> {
                playerSendServer(player, "sclattest")
                getPlayerData(player)!!.setServerName("sclattest")
            }

            "チームデスマッチサーバーへ接続 / CONNECT TO TDM SERVER" -> {
                playerSendServer(player, "tdm")
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
                Sclat.conf!!.config!!.getString(
                    "ResourcePackURL",
                )!!,
            )
        }
        if (event.view.title == "Gear") {
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
        } else if (event.view.title == "Gear shop") {
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
        if (event.view.title == "Server List") {
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
                            playerSendServer(player, ss.serverName!!)
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

        if (event.view.title == "武器選択") {
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
            if (Sclat.conf!!
                    .config!!
                    .getString("WorkMode") == "Trial"
            ) {
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
                                swapperRunnable(p)
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
                                getPlayerData(p)!!.mainItemGlow = true
                                setWeaponClass(p)
                            }
                            if (getPlayerData(p)!!.weaponClass!!.mainWeapon!!.weaponType == "Buckler") {
                                shooterRunnable(p)
                                bucklerRunnable(p)
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
                            player.exp = 0.99f

                            // p.setScoreboard(DataMgr.getPlayerData(p).getMatch().getScoreboard());
                            // DataMgr.getPlayerData(p).getTeam().getTeam().addEntry(p.getName());
                            spWeaponRunnable(player)
                            squidShowRunnable(player)
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

        if (event.view.title == "Shop") {
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

        if (event.view.title == "Chose Target") {
            if (name == "§r§6リスポーン地点へジャンプ") {
                var loc: Location? = Sclat.lobby!!.clone()
                if (Sclat.conf!!
                        .config!!
                        .getString("WorkMode") != "Trial"
                ) {
                    loc =
                        getPlayerData(player)!!.matchLocation
                }
                superJumpCollTime(player, loc!!, false)
            }
            if (name == "§r§6ロビーへジャンプ") {
                val worldName =
                    Sclat.conf!!
                        .config!!
                        .getString("LobbyJump.WorldName")
                val w = Bukkit.getWorld(worldName!!)
                val ix =
                    Sclat.conf!!
                        .config!!
                        .getInt("LobbyJump.X")
                val iy =
                    Sclat.conf!!
                        .config!!
                        .getInt("LobbyJump.Y")
                val iz =
                    Sclat.conf!!
                        .config!!
                        .getInt("LobbyJump.Z")
                val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
                superJumpCollTime(player, loc, true)
            }
            var nearspwan = true
            var spawnloc: Location? = Sclat.lobby!!.clone()
            if (Sclat.conf!!
                    .config!!
                    .getString("WorkMode") != "Trial"
            ) {
                spawnloc =
                    getPlayerData(player)!!.matchLocation
            }
            if (spawnloc!!.world === player.world) {
                if (player
                        .location
                        .distance(spawnloc) > 10 &&
                    !Tutorial.clearList.contains(player)
                ) {
                    if (!Sclat.tutorial) {
                        nearspwan = false
                    }
                }
            }
            for (p in plugin.server.onlinePlayers) {
                if (p.name == name) {
                    if (event.currentItem!!.type == Material.PLAYER_HEAD) {
                        if (p.gameMode == GameMode.SPECTATOR) {
                            sendMessage("§c今そのプレイヤーにはジャンプできない！", MessageType.PLAYER, player)
                            playGameSound(player, SoundType.ERROR)
                            break
                        }
                        superJumpCollTime(
                            player,
                            getPlayerData(p)!!.playerGroundLocation!!,
                            nearspwan,
                        )
                    }
                    if (event.currentItem!!.type == Material.IRON_TRAPDOOR) {
                        superJumpCollTime(
                            player,
                            getBeaconFromplayer(p)!!.location,
                            nearspwan,
                        )
                    }
                }
            }
        }

        if (event.view.title == "設定") {
            if (name == "戻る") {
                OpenGUI.openMenu(player)
                return
            }

            val playerSettings = getPlayerData(player)?.settings!!
            when (name) {
                "メインウエポンのインクエフェクト" -> playerSettings.sShowEffectMainWeaponInk()
                "リッター5Gの自動チャージ" -> playerSettings.sShowEffectChargerLine()
                "スペシャルウエポンのエフェクト" -> playerSettings.sShowEffectSPWeapon()
                "スペシャルウエポンの範囲エフェクト" -> playerSettings.sShowEffectSPWeaponRegion()
                "弾の表示" -> playerSettings.sShowSnowBall()
                "BGM" -> playerSettings.sPlayBGM()
                "投擲武器の視認用エフェクト" -> playerSettings.sShowEffectBomb()
                "爆発エフェクト" -> playerSettings.sShowEffectBombEx()
                "チャージキープ/ブリンクの旧挙動" -> playerSettings.sDoChargeKeep()
            }

            OpenGUI.openSettingsUI(player)

            player.playNote(player.location, Instrument.STICKS, Note.flat(1, Note.Tone.C))

            val b = if (playerSettings.playBGM()) "1" else "0"
            val eS = if (playerSettings.showEffectMainWeaponInk()) "1" else "0"
            val eCL = if (playerSettings.showEffectChargerLine()) "1" else "0"
            val eCS = if (playerSettings.showEffectSPWeapon()) "1" else "0"
            val eRR = if (playerSettings.showEffectSPWeaponRegion()) "1" else "0"
            val eRS = if (playerSettings.showSnowBall()) "1" else "0"
            val eB = if (playerSettings.showEffectBomb()) "1" else "0"
            val eBEx = if (playerSettings.showEffectBombEx()) "1" else "0"
            val ck = if (playerSettings.doChargeKeep()) "1" else "0"

            val sData = b + eS + eCL + eCS + eRR + eRS + eB + eBEx + ck

            val uuid: String = player.uniqueId.toString()
            Sclat.conf!!.playerSettings.set("Settings.$uuid", sData)
        }

        if (player.gameMode != GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onOpenMainMenu(event: PlayerInteractEvent) {
        val player = event.getPlayer()
        val action = event.getAction()

        if (player
                .inventory
                .itemInMainHand
                .itemMeta == null ||
            player
                .inventory
                .itemInMainHand
                .itemMeta
                ?.displayName == null
        ) {
            return
        }

        if (action == Action.RIGHT_CLICK_AIR ||
            action == Action.RIGHT_CLICK_BLOCK ||
            action == Action.LEFT_CLICK_AIR ||
            action == Action.LEFT_CLICK_BLOCK
        ) {
            if (player.inventory.itemInMainHand.type == Material.CHEST) OpenGUI.openMenu(player)
            when (
                player
                    .inventory
                    .itemInMainHand
                    .itemMeta!!
                    .displayName
            ) {
                "スーパージャンプ" -> {
                    OpenGUI.superJumpGUI(player)
                }

                "§c§n右クリックで退出" -> {
                    playerSendServer(player, "sclat")
                    getPlayerData(player)!!.setServerName("Sclat")
                }

                "§a§n右クリックで参加" -> {
                    playerJoinMatch(player)
                }
            }
            if (player
                    .inventory
                    .itemInMainHand
                    .itemMeta!!
                    .displayName == "スーパージャンプ"
            ) {
                OpenGUI.superJumpGUI(player)
            }
        }
    }
}
