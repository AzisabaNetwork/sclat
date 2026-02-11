package be4rjp.sclat.manager

import be4rjp.sclat.api.GaugeAPI.toGauge
import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.spweapon.AirStrike.airStrikeRunnable
import be4rjp.sclat.weapon.spweapon.Amehurasi.amehurasiDropRunnable
import be4rjp.sclat.weapon.spweapon.Barrier.barrierRunnable
import be4rjp.sclat.weapon.spweapon.BombRush.bombRushRunnable
import be4rjp.sclat.weapon.spweapon.JetPack.jetPackRunnable
import be4rjp.sclat.weapon.spweapon.LitterFiveG.setLitterFiveG
import be4rjp.sclat.weapon.spweapon.MegaLaser.megaLaserRunnable
import be4rjp.sclat.weapon.spweapon.MultiMissile.mmLockRunnable
import be4rjp.sclat.weapon.spweapon.QuadroArms.setQuadroArms
import be4rjp.sclat.weapon.spweapon.SuperArmor.setArmor
import be4rjp.sclat.weapon.spweapon.SuperSensor.superSensorRunnable
import be4rjp.sclat.weapon.spweapon.SuperShot.setSuperShot
import be4rjp.sclat.weapon.spweapon.SuperTyakuti.superTyakutiRunnable
import be4rjp.sclat.weapon.spweapon.SwordMord.setSwordMord
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object SPWeaponMgr {
    fun addSPCharge(player: Player?) {
        val data = getPlayerData(player)
        if (data!!.sPGauge < 100) data.addSPGauge()
    }

    fun resetSPCharge(player: Player?) {
        val data = getPlayerData(player)
        if (data!!.sPGauge > 20) data.sPGauge = (20)
    }

    fun getSPGauge(player: Player?): String {
        val data = getPlayerData(player)
        if (data!!.sPGauge == 100) return "§b§n! READY !"
        return toGauge(data.sPGauge / 5, 20, "§a", "§7")
    }

    fun spWeaponHuriRunnable(player: Player) {
        val data = getPlayerData(player)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    if (!player.isOnline || !data!!.isInMatch) cancel()
                    try {
                        var myTeam = 0
                        var enemyTeam = 0
                        for (op in plugin.server.onlinePlayers) {
                            val opdata = getPlayerData(op)
                            if (data!!.match == opdata!!.match) {
                                if (data.team == opdata.team) {
                                    if (!opdata.isDead) myTeam++
                                } else {
                                    if (!opdata.isDead) enemyTeam++
                                }
                            }
                        }

                        if (myTeam < enemyTeam) {
                            addSPCharge(player)
                            addSPCharge(player)
                        }
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 20)
    }

    @JvmStatic
    fun spWeaponRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val data = getPlayerData(p)
                    if (data!!.sPGauge == 100) {
                        if (!data.isSP) {
                            setSPWeapon(p)
                            p.playSound(p.location, Sound.BLOCK_CHEST_OPEN, 0.8f, 2f)
                            sendMessage("§6§l！ スペシャルウエポン使用可能 ！", MessageType.PLAYER, player)
                            data.isUsingSP = (false)
                        }
                        getPlayerData(p)!!.isSP = (true)
                    } else {
                        if (!(
                                data.weaponClass!!.sPWeaponName == "インクストライク" ||
                                    data.weaponClass!!.sPWeaponName == "ジェットパック" ||
                                    data.weaponClass!!.sPWeaponName == "スーパーショット" ||
                                    data.weaponClass!!.sPWeaponName == "クアドロアームズ" ||
                                    data.weaponClass!!.sPWeaponName == "セイバーモード" ||
                                    data.weaponClass!!.sPWeaponName == "リッター5G"
                            )
                        ) {
                            p.inventory.setItem(
                                4,
                                ItemStack(
                                    Material.AIR,
                                ),
                            )
                        }
                        getPlayerData(p)!!.isSP = (false)
                    }
                    if (!getPlayerData(p)!!.isInMatch) cancel()
                }
            }
        task.runTaskTimer(plugin, 0, 5)

        val bar =
            plugin.server.createBossBar(
                "§6§lSpecial Weapon",
                BarColor.GREEN,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        bar.progress = 0.0
        bar.addPlayer(player)

        val anime: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    bar.progress = (getPlayerData(p)!!.sPGauge).toDouble() / 100.0
                    if (getPlayerData(p)!!.sPGauge == 100) {
                        bar.setTitle("§b§lREADY")
                    } else if (getPlayerData(p)!!.isUsingSP) {
                        bar.setTitle("§6§lIn Use : §r" + getPlayerData(p)!!.sPGauge + "%")
                    } else {
                        bar.setTitle("§6§lSpecial Weapon : §r" + getPlayerData(p)!!.sPGauge + "%")
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) {
                        getPlayerData(p)!!.isUsingSP = (false)
                        bar.removeAll()
                        cancel()
                    }
                }
            }
        anime.runTaskTimer(plugin, 0, 2)
    }

    fun armorRunnable(player: Player) {
        val bar =
            plugin.server.createBossBar(
                getPlayerData(player)!!.team!!.teamColor!!.colorCode + "§lInk Armor",
                BarColor.YELLOW,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        bar.progress = 0.0
        bar.addPlayer(player)

        val anime: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val data = getPlayerData(p)
                    if (data!!.armor > 0) {
                        bar.progress = if (data.armor >= 30) 1.0 else data.armor / 30.0
                        if (!bar.players.contains(p)) bar.addPlayer(p)
                    } else {
                        bar.removeAll()
                    }
                    if (!getPlayerData(p)!!.isInMatch || !p.isOnline) {
                        bar.removeAll()
                        cancel()
                    }
                }
            }
        anime.runTaskTimer(plugin, 0, 5)
    }

    fun setSPCoolTimeAnimation(
        player: Player,
        tick: Int,
    ) {
        val data = getPlayerData(player)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var t: Double = tick.toDouble()
                val i: Double = tick.toDouble()

                override fun run() {
                    if (t == tick.toDouble()) data!!.isUsingSP = (true)
                    t--
                    val sp = (t / i * 100).toInt()
                    data!!.sPGauge = (sp)
                    if (t <= 0) {
                        data.isUsingSP = (false)
                        if (data.isInMatch) {
                            val sync: BukkitRunnable =
                                object : BukkitRunnable() {
                                    override fun run() {
                                        p.playSound(p.location, Sound.BLOCK_CHEST_CLOSE, 1f, 2f)
                                        WeaponClassMgr.setWeaponClass(p)
                                    }
                                }
                            sync.runTask(plugin)
                        }
                        cancel()
                    }
                }
            }
        task.runTaskTimerAsynchronously(plugin, 0, 1)
    }

    fun setSPWeapon(p: Player) {
        val data = getPlayerData(p)
        when (data!!.weaponClass!!.sPWeaponName) {
            "インクアーマー" -> {
                val `is` = ItemStack(Material.TOTEM_OF_UNDYING)
                val ism = `is`.itemMeta
                ism!!.setDisplayName("インクアーマー")
                `is`.itemMeta = ism
                p.inventory.setItem(4, `is`)
            }

            "バリア" -> {
                val b = ItemStack(Material.END_CRYSTAL)
                val bm = b.itemMeta
                bm!!.setDisplayName("バリア")
                b.itemMeta = bm
                p.inventory.setItem(4, b)
            }

            "ボムラッシュ" -> {
                val is1 = ItemStack(Material.FEATHER)
                val ism1 = is1.itemMeta
                ism1!!.setDisplayName("ボムラッシュ")
                is1.itemMeta = ism1
                p.inventory.setItem(4, is1)
            }

            "スーパーセンサー" -> {
                val is2 = ItemStack(Material.NETHER_STAR)
                val ism2 = is2.itemMeta
                ism2!!.setDisplayName("スーパーセンサー")
                is2.itemMeta = ism2
                p.inventory.setItem(4, is2)
            }

            "インクストライク" -> {
                val is3 = ItemStack(Material.ARROW)
                val ism3 = is3.itemMeta
                ism3!!.setDisplayName("インクストライク")
                is3.itemMeta = ism3
                p.inventory.setItem(4, is3)
            }

            "アメフラシ" -> {
                val is4 = ItemStack(Material.BEACON)
                val ism4 = is4.itemMeta
                ism4!!.setDisplayName("アメフラシ")
                is4.itemMeta = ism4
                p.inventory.setItem(4, is4)
            }

            "マルチミサイル" -> {
                val is5 = ItemStack(Material.PRISMARINE_SHARD)
                val ism5 = is5.itemMeta
                ism5!!.setDisplayName("マルチミサイル")
                is5.itemMeta = ism5
                p.inventory.setItem(4, is5)
            }

            "メガホンレーザー" -> {
                val is6 = ItemStack(Material.SHULKER_SHELL)
                val ism6 = is6.itemMeta
                ism6!!.setDisplayName("メガホンレーザー")
                is6.itemMeta = ism6
                p.inventory.setItem(4, is6)
            }

            "ジェットパック" -> {
                val is7 = ItemStack(Material.QUARTZ)
                val ism7 = is7.itemMeta
                ism7!!.setDisplayName("ジェットパック")
                is7.itemMeta = ism7
                p.inventory.setItem(4, is7)
            }

            "スーパーショット" -> {
                val is8 = ItemStack(Material.SUGAR_CANE)
                val ism8 = is8.itemMeta
                ism8!!.setDisplayName("スーパーショット")
                is8.itemMeta = ism8
                p.inventory.setItem(4, is8)
            }

            "スーパーチャクチ" -> {
                val is9 = ItemStack(Material.RABBIT_HIDE)
                val ism9 = is9.itemMeta
                ism9!!.setDisplayName("スーパーチャクチ")
                is9.itemMeta = ism9
                p.inventory.setItem(4, is9)
            }

            "クアドロアームズ" -> {
                val is10 = ItemStack(Material.SUGAR)
                val ism10 = is10.itemMeta
                ism10!!.setDisplayName("クアドロアームズ")
                is10.itemMeta = ism10
                p.inventory.setItem(4, is10)
            }

            "セイバーモード" -> {
                val is11 = ItemStack(Material.WHEAT)
                val ism11 = is11.itemMeta
                ism11!!.setDisplayName("セイバーモード")
                is11.itemMeta = ism11
                p.inventory.setItem(4, is11)
            }

            "リッター5G" -> {
                val is12 = ItemStack(Material.NAUTILUS_SHELL)
                val ism12 = is12.itemMeta
                ism12!!.setDisplayName("リッター5G")
                is12.itemMeta = ism12
                p.inventory.setItem(4, is12)
            }
        }
    }

    fun useSPWeapon(
        player: Player,
        name: String,
    ) {
        val data = getPlayerData(player)

        if (data!!.isJumping && name != "スーパーチャクチ") return

        when (name) {
            "カーソルを合わせて右クリックで発射" -> airStrikeRunnable(player, false)
            "カーソルを合わせて右クリックで発射!" -> airStrikeRunnable(player, true)
            "プレイヤーを狙って右クリックで発射" -> getPlayerData(player)?.isUsingMM = false
            "狙って右クリックで発射" -> getPlayerData(player)?.isUsingMM = false
        }

        if (data.isUsingSP) return
        when (name) {
            "インクアーマー" -> {
                setArmor(player, 30.0, 160, true)
                // for (Player op : Main.getPlugin().getServer().getOnlinePlayers()) {
                // if(player != op && DataMgr.getPlayerData(player).getTeam() ==
                // DataMgr.getPlayerData(op).getTeam()){
                // SuperArmor.setArmor(op, 20, 80, true);
                // }
                // }
                var inventnum = 0
                while (inventnum < 9) {
                    if (player.inventory.getItem(inventnum) != null) {
                        if (player.inventory.getItem(inventnum)!!.type == Material.TOTEM_OF_UNDYING) {
                            player.inventory.setItem(inventnum, ItemStack(Material.AIR))
                        }
                    }
                    inventnum++
                }
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "バリア" -> {
                barrierRunnable(player)
                var inventnum = 0
                while (inventnum < 9) {
                    if (player.inventory.getItem(inventnum) != null) {
                        if (player.inventory.getItem(inventnum)!!.type == Material.END_CRYSTAL) {
                            player.inventory.setItem(inventnum, ItemStack(Material.AIR))
                        }
                    }
                    inventnum++
                }
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "ボムラッシュ" -> {
                bombRushRunnable(player)
                var inventnum = 0
                while (inventnum < 9) {
                    if (player.inventory.getItem(inventnum) != null) {
                        if (player.inventory.getItem(inventnum)!!.type == Material.FEATHER) {
                            player.inventory.setItem(inventnum, ItemStack(Material.AIR))
                        }
                    }
                    inventnum++
                }
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "スーパーセンサー" -> {
                superSensorRunnable(player)
                var inventnum = 0
                while (inventnum < 9) {
                    if (player.inventory.getItem(inventnum) != null) {
                        if (player.inventory.getItem(inventnum)!!.type == Material.NETHER_STAR) {
                            player.inventory.setItem(inventnum, ItemStack(Material.AIR))
                        }
                    }
                    inventnum++
                }
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "インクストライク" -> {
                MapKitMgr.setMapKit(player)
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "カーソルを合わせて右クリックで発射！", 3, 30, 3)
            }

            "アメフラシ" -> {
                amehurasiDropRunnable(player)
                var inventnum = 0
                while (inventnum < 9) {
                    if (player.inventory.getItem(inventnum) != null) {
                        if (player.inventory.getItem(inventnum)!!.type == Material.BEACON) {
                            player.inventory.setItem(inventnum, ItemStack(Material.AIR))
                        }
                    }
                    inventnum++
                }
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "マルチミサイル" -> {
                data.isUsingSP = (true)
                mmLockRunnable(player)
                player.inventory.setItem(4, ItemStack(Material.AIR))
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "プレイヤーを狙って右クリックで発射！", 3, 30, 3)
            }

            "ジェットパック" -> {
                data.isUsingSP = (true)
                jetPackRunnable(player)
                player.inventory.setItem(4, ItemStack(Material.AIR))
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "右クリックで発射！", 5, 20, 5)
            }

            "スーパーショット" -> {
                player.inventory.setItem(4, ItemStack(Material.AIR))
                setSuperShot(player)
                // player.getInventory().setItem(1, new ItemStack(Material.AIR));
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "右クリックで発射！", 5, 20, 5)
            }

            "セイバーモード" -> {
                player.inventory.setItem(4, ItemStack(Material.AIR))
                setSwordMord(player)
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "右クリックで斬撃！", 5, 20, 5)
            }

            "クアドロアームズ" -> {
                player.inventory.setItem(4, ItemStack(Material.AIR))
                setQuadroArms(player)
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "右クリックで発射！", 5, 20, 5)
            }

            "リッター5G" -> {
                player.inventory.setItem(4, ItemStack(Material.AIR))
                setLitterFiveG(player)
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
                player.sendTitle("", "右クリックで射撃！", 5, 20, 5)
            }

            "スーパーチャクチ" -> {
                player.inventory.setItem(4, ItemStack(Material.AIR))
                superTyakutiRunnable(player)
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }

            "メガホンレーザー" -> {
                data.isUsingSP = (true)
                megaLaserRunnable(player)
                player.inventory.setItem(4, ItemStack(Material.AIR))
                player.exp = 0.99f
                player.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1f, 2f)
            }
        }
    }
}
