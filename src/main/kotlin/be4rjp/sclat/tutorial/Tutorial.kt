package be4rjp.sclat.tutorial

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.playGameSound
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.data.BlockUpdater
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.setPaintDataFromBlock
import be4rjp.sclat.data.DataMgr.spongeMap
import be4rjp.sclat.data.Match
import be4rjp.sclat.data.PaintData
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.manager.PathMgr
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.server.StatusClient
import net.azisaba.sclat.core.enums.MessageType
import net.azisaba.sclat.core.enums.SoundType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

object Tutorial {
    var bar: BossBar? = null

    @JvmField
    var clearList: MutableList<Player?> = ArrayList()
    var clearPlayerCount: Int = 0

    @JvmStatic
    fun setupTutorial(match: Match) {
        val time = Sclat.conf?.config!!.getInt("InkResetPeriod")
        bar =
            plugin.server.createBossBar(
                "§a§lインクリセットまで残り §c§l$time §a§l秒",
                BarColor.WHITE,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        PathMgr.setupPath(match)
        inkResetRunnable(time, match)
    }

    @JvmStatic
    fun trainLightRunnable() {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    if (clearPlayerCount >= 1) trainLightRunRunnable()
                    if (i % 20 == 0) {
                        for (`as` in Sclat.lobby!!.world!!.entities) {
                            if (`as` is ArmorStand) {
                                if (`as`.customName == null) {
                                    `as`.remove()
                                } else if (`as`.customName!!.isEmpty()) {
                                    `as`.remove()
                                }
                            }
                        }
                    }
                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 60)
    }

    fun trainLightRunRunnable() {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var `as`: ArmorStand? = null
                var from: Location? = null
                var to: Location? = null
                var vec: Vector? = null
                var i: Int = 0

                override fun run() {
                    if (i == 0) {
                        val worldName = Sclat.conf?.config!!.getString("Train.LFrom.WorldName")
                        val w = Bukkit.getWorld(worldName!!)
                        val ix = Sclat.conf?.config!!.getInt("Train.LFrom.X")
                        val iy = Sclat.conf?.config!!.getInt("Train.LFrom.Y")
                        val iz = Sclat.conf?.config!!.getInt("Train.LFrom.Z")
                        from = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

                        Sclat.conf?.config!!.getString("Train.LTo.WorldName") // Todo: why unused?
                        val w1 = Bukkit.getWorld(worldName)
                        val ix1 = Sclat.conf?.config!!.getInt("Train.LTo.X")
                        val iy1 = Sclat.conf?.config!!.getInt("Train.LTo.Y")
                        val iz1 = Sclat.conf?.config!!.getInt("Train.LTo.Z")
                        to = Location(w1, ix1 + 0.5, iy1.toDouble(), iz1 + 0.5)

                        vec = Vector(ix1 - ix, iy1 - iy, iz1 - iz).normalize()

                        `as` =
                            w!!.spawn(
                                from!!,
                                ArmorStand::class.java,
                            ) { armorStand: ArmorStand ->
                                armorStand.isVisible = false
                                armorStand.setBasePlate(false)
                                armorStand.setHelmet(ItemStack(Material.SEA_LANTERN))
                            }
                    }

                    `as`!!.velocity = vec!!

                    if (`as`!!.isDead || `as`!!.isOnGround || i == 100 || clearPlayerCount == 0) {
                        `as`!!.remove()
                        cancel()
                    }

                    if (`as`!!.world === to!!.world) {
                        if (`as`!!.location.distance(to!!) <= 3) {
                            `as`!!.remove()
                            cancel()
                        }
                    }
                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)

        val task1: BukkitRunnable =
            object : BukkitRunnable() {
                var `as`: ArmorStand? = null
                var from: Location? = null
                var to: Location? = null
                var vec: Vector? = null
                var i: Int = 0

                override fun run() {
                    if (i == 0) {
                        val worldName = Sclat.conf?.config!!.getString("Train.RFrom.WorldName")
                        val w = Bukkit.getWorld(worldName!!)
                        val ix = Sclat.conf?.config!!.getInt("Train.RFrom.X")
                        val iy = Sclat.conf?.config!!.getInt("Train.RFrom.Y")
                        val iz = Sclat.conf?.config!!.getInt("Train.RFrom.Z")
                        from = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

                        Sclat.conf?.config!!.getString("Train.RTo.WorldName")
                        val w1 = Bukkit.getWorld(worldName)
                        val ix1 = Sclat.conf?.config!!.getInt("Train.RTo.X")
                        val iy1 = Sclat.conf?.config!!.getInt("Train.RTo.Y")
                        val iz1 = Sclat.conf?.config!!.getInt("Train.RTo.Z")
                        to = Location(w1, ix1 + 0.5, iy1.toDouble(), iz1 + 0.5)

                        vec = Vector(ix1 - ix, iy1 - iy, iz1 - iz).normalize()

                        `as` =
                            w!!.spawn(
                                from!!,
                                ArmorStand::class.java,
                            ) { armorStand: ArmorStand ->
                                armorStand.isVisible = false
                                armorStand.setBasePlate(false)
                                armorStand.setHelmet(ItemStack(Material.SEA_LANTERN))
                            }
                    }

                    `as`!!.velocity = vec!!

                    if (`as`!!.isDead || `as`!!.isOnGround || i == 100 || clearPlayerCount == 0) {
                        `as`!!.remove()
                        cancel()
                    }

                    if (`as`!!.world === to!!.world) {
                        if (`as`!!.location.distance(to!!) <= 3) {
                            `as`!!.remove()
                            cancel()
                        }
                    }
                    i++
                }
            }
        task1.runTaskTimer(plugin, 0, 1)
    }

    @JvmStatic
    fun weaponRemoveRunnable() {
        val worldName = Sclat.conf?.config!!.getString("WeaponRemove.WorldName")
        val w = Bukkit.getWorld(worldName!!)
        val ix = Sclat.conf?.config!!.getInt("WeaponRemove.X")
        val iy = Sclat.conf?.config!!.getInt("WeaponRemove.Y")
        val iz = Sclat.conf?.config!!.getInt("WeaponRemove.Z")
        val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (player in plugin.server.onlinePlayers) {
                        if (player.world !== w) continue
                        if (player.location.distance(loc) < 8) {
                            player.inventory.clear()
                            getPlayerData(player)!!.isInMatch = false
                            getPlayerData(player)!!.isJoined = false
                        }
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 5)
    }

    @JvmStatic
    fun clearRegionRunnable() {
        val worldName = Sclat.conf?.config!!.getString("TutorialClear.WorldName")
        val w = Bukkit.getWorld(worldName!!)
        val ix = Sclat.conf?.config!!.getInt("TutorialClear.X")
        val iy = Sclat.conf?.config!!.getInt("TutorialClear.Y")
        val iz = Sclat.conf?.config!!.getInt("TutorialClear.Z")
        val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (player in plugin.server.onlinePlayers) {
                        if (player.world !== w) continue
                        if (player.location.distance(loc) < 5) {
                            clearList.remove(player)
                            val worldName = Sclat.conf?.config!!.getString("LobbyJump.WorldName")
                            val w = Bukkit.getWorld(worldName!!)
                            val ix = Sclat.conf?.config!!.getInt("LobbyJump.X")
                            val iy = Sclat.conf?.config!!.getInt("LobbyJump.Y")
                            val iz = Sclat.conf?.config!!.getInt("LobbyJump.Z")
                            val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)
                            player.teleport(loc)
                        }
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 10)
    }

    @JvmStatic
    fun lobbyRegionRunnable() {
        val worldName = Sclat.conf?.config!!.getString("LobbyJump.WorldName")
        val w = Bukkit.getWorld(worldName!!)
        val ix = Sclat.conf?.config!!.getInt("LobbyJump.X")
        val iy = Sclat.conf?.config!!.getInt("LobbyJump.Y")
        val iz = Sclat.conf?.config!!.getInt("LobbyJump.Z")
        val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var list: MutableList<Player?> = ArrayList()

                override fun run() {
                    for (player in plugin.server.onlinePlayers) {
                        if (player.world !== w) continue
                        if (player.location.distance(loc) < 5 && !list.contains(player)) {
                            list.add(player)
                            sendPlayerRunnable(player)
                        }
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 10)
    }

    @JvmStatic
    fun lobbySetStatusRunnable() {
        val worldName = Sclat.conf?.config!!.getString("TutorialClear.WorldName")
        val w = Bukkit.getWorld(worldName!!)
        val ix = Sclat.conf?.config!!.getInt("TutorialClear.X")
        val iy = Sclat.conf?.config!!.getInt("TutorialClear.Y")
        val iz = Sclat.conf?.config!!.getInt("TutorialClear.Z")
        val loc = Location(w, ix + 0.5, iy.toDouble(), iz + 0.5)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    for (player in plugin.server.onlinePlayers) {
                        if (player.world !== w) continue
                        if (player.location.distance(loc) < 10) {
                            if (PlayerStatusMgr.getTutorialState(player.uniqueId.toString()) == 1) {
                                PlayerStatusMgr.setTutorialState(player.uniqueId.toString(), 2)

                                val join = ItemStack(Material.CHEST)
                                val joinmeta = join.itemMeta
                                joinmeta!!.setDisplayName(ChatColor.GOLD.toString() + "右クリックでメインメニューを開く")
                                join.itemMeta = joinmeta
                                player.inventory.clear()
                                player.inventory.setItem(0, join)

                                sendMessage("§6Sclatへようこそ！", MessageType.PLAYER, player)
                                player.sendMessage("§aチェストをもって右クリックするとメインメニューを開くことができます。")
                                player.sendMessage("§a初期から使える武器がいくつかあります。")
                                player.sendMessage("§aメインメニューの装備変更から武器を選んで、試合に参加してみましょう！")
                                sendMessage(
                                    "§6初回ログインボーナスを受け取りました！ §bMoney +10000",
                                    MessageType.PLAYER,
                                    player,
                                )
                                playGameSound(player, SoundType.CONGRATULATIONS)
                            }
                        }
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 10)
    }

    fun sendPlayerRunnable(player: Player) {
        clearPlayerCount++

        val commands: MutableList<String> =
            mutableListOf(
                "tutorial " + player.uniqueId.toString(),
                "stop",
            )
        val sc =
            StatusClient(
                Sclat.conf?.config!!.getString("StatusShare.Host"),
                Sclat.conf?.config!!.getInt("StatusShare.Port"),
                commands,
            )
        sc.startClient()

        player.sendTitle("", "§7ロビーへ転送中...", 10, 40, 10)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0

                override fun run() {
                    if (!player.isOnline) {
                        clearPlayerCount--
                        cancel()
                    }
                    player.playSound(player.location, Sound.ENTITY_MINECART_INSIDE, 0.7f, 1f)
                    if (i == 2) {
                        BungeeCordMgr.playerSendServer(player, "sclat")
                        getPlayerData(player)!!.setServerName("Sclat")
                    }
                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 100)
    }

    fun inkResetRunnable(
        period: Int,
        match: Match,
    ) {
        val match = match
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var time: Int = 0

                override fun run() {
                    bar!!.setTitle("§a§lインクリセットまで残り §c§l" + (period - time) + " §a§l秒")
                    bar!!.progress = ((period - time).toDouble()) / (period.toDouble())

                    if (time == period) {
                        for (path in match.mapData!!.pathList) {
                            path!!.setTeam(null)
                        }
                        // ロールバック
                        match.blockUpdater!!.stop()
                        // ------------------------------------------------------------
                        for (data in blockDataMap.values) {
                            var data = data
                            data!!.block!!.type = data.originalType!!
                            if (data.blockData != null) data.block.setBlockData(data.blockData!!)
                            data = null
                        }
                        blockDataMap.clear()
                        spongeMap.clear()
                        // ------------------------------------------------------------
                        for (player in plugin.server.onlinePlayers) player.exp = 0.99f
                        val bur = BlockUpdater()
                        if (Sclat.conf?.config!!.contains("BlockUpdateRate")) {
                            bur.setMaxBlockInOneTick(
                                Sclat.conf?.config!!.getInt(
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

                        sendMessage("§a§lインクがリセットされました！", MessageType.ALL_PLAYER)
                        for (op in plugin.server.onlinePlayers) playGameSound(op, SoundType.SUCCESS)
                        time = 0
                    }
                    time++
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }

    @JvmStatic
    fun setInkResetTimer(player: Player) {
        bar!!.addPlayer(player)
    }
}
