package be4rjp.sclat.gui

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.beaconMap
import be4rjp.sclat.data.DataMgr.getArmorStandPlayer
import be4rjp.sclat.data.DataMgr.getMatchFromId
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.emblem.EmblemManager.handleInv
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.manager.PlayerStatusMgr.getKill
import be4rjp.sclat.manager.PlayerStatusMgr.getLv
import be4rjp.sclat.manager.PlayerStatusMgr.getMoney
import be4rjp.sclat.manager.PlayerStatusMgr.getPaint
import be4rjp.sclat.manager.PlayerStatusMgr.getRank
import be4rjp.sclat.manager.PlayerStatusMgr.haveGear
import be4rjp.sclat.manager.PlayerStatusMgr.haveWeapon
import be4rjp.sclat.manager.RankMgr.toABCRank
import be4rjp.sclat.plugin
import be4rjp.sclat.tutorial.Tutorial
import be4rjp.sclat.weapon.Gear.getGearMaterial
import be4rjp.sclat.weapon.Gear.getGearName
import be4rjp.sclat.weapon.Gear.getGearPrice
import net.azisaba.sclat.core.enums.ServerType
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 *
 * @author Be4rJP
 */
object OpenGUI {
    fun openMenu(player: Player) {
        val inv = Bukkit.createInventory(null, 45, "メインメニュー")

        var i = 0
        while (i <= 44) {
            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.itemMeta
            ism!!.setDisplayName(".")
            `is`.itemMeta = ism
            inv.setItem(i, `is`)
            i++
        }

        val join = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val joinmeta = join.itemMeta
        joinmeta!!.setDisplayName("試合に参加 / JOIN THE MATCH")
        join.itemMeta = joinmeta
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") != "Trial"
        ) {
            inv.setItem(10, join)
        }

        val setting = ItemStack(Material.COMPARATOR)
        val settingM = setting.itemMeta
        settingM!!.setDisplayName("設定 / SETTINGS")
        setting.itemMeta = settingM
        inv.setItem(14, setting)

        val w = ItemStack(Material.LEATHER_CHESTPLATE)
        val wmeta = w.itemMeta
        wmeta!!.setDisplayName("装備変更 / EQUIPMENT")
        w.itemMeta = wmeta
        inv.setItem(12, w)
        player.openInventory(inv)

        val t = ItemStack(Material.GRASS_BLOCK)
        val tmeta = t.itemMeta
        tmeta!!.setDisplayName("リソースパックをダウンロード / DOWNLOAD RESOURCEPACK")
        t.itemMeta = tmeta
        inv.setItem(28, t)

        val r = ItemStack(Material.MILK_BUCKET)
        val rmeta = r.itemMeta
        rmeta!!.setDisplayName("塗りをリセット / RESET INK")
        r.itemMeta = rmeta
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            inv.setItem(10, r)
        }

        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") == "Trial"
        ) {
            val b = ItemStack(Material.OAK_DOOR)
            val bmeta = b.itemMeta
            bmeta!!.setDisplayName("ロビーへ戻る / RETURN TO LOBBY")
            b.itemMeta = bmeta
            inv.setItem(16, b)
        } else {
            val ta = ItemStack(Material.ARMOR_STAND)
            val tameta = ta.itemMeta
            tameta!!.setDisplayName("試し打ちサーバーへ接続 / TRAINING FIELD")
            ta.itemMeta = tameta
            inv.setItem(30, ta)

            val b = ItemStack(Material.CHEST)
            val bmeta = b.itemMeta
            bmeta!!.setDisplayName("ショップを開く / OPEN SHOP")
            b.itemMeta = bmeta
            inv.setItem(16, b)

            val data = getPlayerData(player)
            var status = ItemStack(Material.PLAYER_HEAD)
            if (data!!.playerHead != null) status = CraftItemStack.asBukkitCopy(data.playerHead).clone()
            val statusMeta = status.itemMeta
            statusMeta!!.setDisplayName("§r§e" + player.name + " のステータス")
            val lores: MutableList<String> = ArrayList()
            lores.add(
                (
                    "§r§6Rank : §r" + getRank(player) + " [ §b" +
                        toABCRank(getRank(player)) + " §r]"
                ),
            )
            lores.add("§r§6Lv : §r" + getLv(player))
            lores.add("§r§bKill(s) : §r" + getKill(player))
            lores.add("§r§bPaint(s) : §r" + getPaint(player))
            lores.add("§r§aMoney : §r" + getMoney(player))
            statusMeta.lore = lores
            status.itemMeta = statusMeta
            inv.setItem(32, status)
        }

        if (Sclat.type == ServerType.LOBBY) {
            val b = ItemStack(Material.EGG)
            val bmeta = b.itemMeta
            bmeta!!.setDisplayName("称号 / EMBLEM")
            b.itemMeta = bmeta
            inv.setItem(34, b)
        }

        val b = ItemStack(Material.BARRIER)
        val bmeta = b.itemMeta
        bmeta!!.setDisplayName("閉じる")
        b.itemMeta = bmeta
        inv.setItem(44, b)

        player.openInventory(inv)
    }

    fun gearGUI(
        player: Player,
        shop: Boolean,
    ) {
        val inv = Bukkit.createInventory(null, 18, if (shop) "Gear shop" else "Gear")

        if (shop) {
            run {
                var i = 0
                while (i <= 9) {
                    if (haveGear(player, i)) {
                        val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                        val nmeta = n.itemMeta
                        nmeta!!.setDisplayName(".")
                        n.itemMeta = nmeta
                        inv.setItem(i, n)
                        i++
                        continue
                    }

                    val n = ItemStack(getGearMaterial(i))
                    val nmeta = n.itemMeta
                    nmeta!!.setDisplayName(getGearName(i))
                    val list: MutableList<String?> = ArrayList()
                    list.add("")
                    list.add("§r§bMoney : " + getGearPrice(i))
                    nmeta.lore = list
                    n.itemMeta = nmeta
                    inv.setItem(i, n)
                    i++
                }
            }
            var i = 10
            while (i <= 17) {
                val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                val nmeta = n.itemMeta
                nmeta!!.setDisplayName(".")
                n.itemMeta = nmeta
                inv.setItem(i, n)
                i++
            }
        } else {
            run {
                var i = 0
                while (i <= 9) {
                    if (!(
                            haveGear(player, i) ||
                                Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "Trial" ||
                                !Sclat.shop
                        )
                    ) {
                        val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                        val nmeta = n.itemMeta
                        nmeta!!.setDisplayName(".")
                        n.itemMeta = nmeta
                        inv.setItem(i, n)
                        i++
                        continue
                    }

                    val n = ItemStack(getGearMaterial(i))
                    val nmeta = n.itemMeta
                    nmeta!!.setDisplayName(getGearName(i))
                    n.itemMeta = nmeta
                    inv.setItem(i, n)
                    i++
                }
            }
            var i = 10
            while (i <= 17) {
                val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                val nmeta = n.itemMeta
                nmeta!!.setDisplayName(".")
                n.itemMeta = nmeta
                inv.setItem(i, n)
                i++
            }
        }

        player.openInventory(inv)
    }

    fun equipmentGUI(
        player: Player,
        shop: Boolean,
    ) {
        val inv = Bukkit.createInventory(null, 27, if (shop) "Equipment shop" else "Equipment")

        var i = 0
        while (i <= 26) {
            val `is` = ItemStack(if (shop) Material.WHITE_STAINED_GLASS_PANE else Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.itemMeta
            ism!!.setDisplayName(".")
            `is`.itemMeta = ism
            inv.setItem(i, `is`)
            i++
        }

        val n = ItemStack(getGearMaterial(getPlayerData(player)!!.gearNumber))
        val nmeta = n.itemMeta
        nmeta!!.setDisplayName(if (shop) "§bギア購入 / GEAR" else "§bギア変更 / GEAR")
        n.itemMeta = nmeta
        inv.setItem(15, n)

        val t =
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .weaponIteamStack!!
                .clone()
        val tmeta = t.itemMeta
        tmeta!!.setDisplayName(if (shop) "§6武器購入 / WEAPON" else "§6武器変更 / WEAPON")
        t.itemMeta = tmeta
        inv.setItem(11, t)

        val `is` = ItemStack(Material.OAK_DOOR)
        val ism = `is`.itemMeta
        ism!!.setDisplayName("戻る")
        `is`.itemMeta = ism
        inv.setItem(26, `is`)

        player.openInventory(inv)
    }

    fun matchTohyoGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 18, "Chose a Gamemode")

        var i = 0
        while (i <= 17) {
            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.itemMeta
            ism!!.setDisplayName(".")
            `is`.itemMeta = ism
            inv.setItem(i, `is`)
            i++
        }

        val n = ItemStack(Material.SNOWBALL)
        val nmeta = n.itemMeta
        nmeta!!.setDisplayName("ナワバリバトル")
        n.itemMeta = nmeta
        inv.setItem(2, n)

        val t = ItemStack(Material.DIAMOND_SWORD)
        val tmeta = t.itemMeta
        tmeta!!.setDisplayName("チームデスマッチ")
        t.itemMeta = tmeta
        inv.setItem(4, t)

        val nu = ItemStack(Material.GLASS)
        val numeta = nu.itemMeta
        numeta!!.setDisplayName("ガチエリア")
        nu.itemMeta = numeta
        val match = getMatchFromId(MatchMgr.matchcount)
        if (match!!.mapData!!.canAreaBattle) inv.setItem(6, nu)

        player.openInventory(inv)
    }

    fun superJumpGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 18, "Chose Target")

        val `is` = ItemStack(getPlayerData(player)!!.team!!.teamColor!!.glass!!)
        val ism = `is`.itemMeta
        ism!!.setDisplayName(if (Sclat.tutorial) "§r§6ロビーへジャンプ" else "§r§6リスポーン地点へジャンプ")
        `is`.itemMeta = ism
        var loc = Sclat.lobby!!.clone()
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") != "Trial"
        ) {
            loc =
                getPlayerData(player)!!.matchLocation!!.clone()
        }
        if (loc.world === player.world) {
            if (player
                    .location
                    .distance(loc) > 10 &&
                !Tutorial.clearList.contains(player)
            ) {
                if (!Sclat.tutorial) {
                    inv.setItem(0, `is`)
                }
            }
        }

        var slotnum = 1

        for (p in plugin.server.onlinePlayers) {
            if (p.gameMode == GameMode.SPECTATOR) continue
            if (getPlayerData(p)!!.team!!.iD == getPlayerData(player)!!.team!!.iD && p.world === player.world && p !== player) {
                if (getPlayerData(p)!!.playerGroundLocation!!.distance(player.location) > 10 &&
                    getPlayerData(p)!!.playerHead != null
                ) {
                    if (slotnum <= 17) {
                        val head = CraftItemStack.asBukkitCopy(getPlayerData(p)!!.playerHead).clone()
                        val headM = head.itemMeta
                        val lores: MutableList<String> = ArrayList()
                        lores.add("§r§aプレイヤーへジャンプ")
                        headM!!.lore = lores
                        head.itemMeta = headM
                        inv.setItem(slotnum, head)
                    }
                    slotnum++
                }
            }
        }
        for (`as` in beaconMap.values) {
            if (`as`!!.customName == "21") {
                val p = getArmorStandPlayer(`as`)
                if (getPlayerData(player)!!.team == getPlayerData(p)!!.team) {
                    if (`as`.world === player.world) {
                        if (`as`.location.distance(player.location) > 10) {
                            val item = ItemStack(Material.IRON_TRAPDOOR)
                            val im = item.itemMeta
                            im!!.setDisplayName(p!!.name)
                            val lores: MutableList<String> = ArrayList()
                            lores.add("§r§6プレイヤーのビーコンへジャンプ")
                            im.lore = lores
                            item.itemMeta = im
                            if (slotnum <= 17) {
                                inv.setItem(slotnum, item)
                            }
                            slotnum++
                        }
                    }
                }
            }
        }
        player.openInventory(inv)
    }

    fun openEmblemMenu(player: Player) {
        val emblemInv = Bukkit.createInventory(null, 54, "称号")
        handleInv(emblemInv, player)
        player.openInventory(emblemInv)
    }

    fun openShop(player: Player) {
        var slotnum = 0
        val shooter = Bukkit.createInventory(null, 54, "武器選択")
        for (ClassName in Sclat.conf!!
            .classConfig!!
            .getConfigurationSection("WeaponClass")!!
            .getKeys(false)) {
            val item = ItemStack(getWeaponClass(ClassName)!!.mainWeapon!!.weaponIteamStack!!)
            val itemm = item.itemMeta
            itemm!!.setDisplayName(ClassName)
            val lores: MutableList<String> = ArrayList()
            lores.add(
                "§r§6SubWeapon : " +
                    Sclat.conf!!
                        .classConfig!!
                        .getString("WeaponClass.$ClassName.SubWeaponName"),
            )
            lores.add(
                "§r§6SPWeapon  : " +
                    Sclat.conf!!
                        .classConfig!!
                        .getString("WeaponClass.$ClassName.SPWeaponName"),
            )
            itemm.lore = lores
            item.itemMeta = itemm
            if (slotnum <= 44 &&
                (
                    getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Shooter" ||
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Burst" ||
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Blaster" ||
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Spinner"
                )
            ) {
                if (getWeaponClass(ClassName)!!.mainWeapon!!.money == 0) {
                    shooter.setItem(slotnum, item)
                    slotnum++
                } else if (haveWeapon(player, ClassName)) {
                    shooter.setItem(slotnum, item)
                    slotnum++
                }
            }
        }
    }

    fun openWeaponSelect(
        player: Player,
        type: String,
        weaponType: String,
        shop: Boolean,
    ) {
        when (type) {
            "Weapon" -> {
                var slotnum = 0
                val shooter =
                    if (shop) {
                        Bukkit.createInventory(null, 54, "Shop")
                    } else {
                        Bukkit.createInventory(null, 54, "武器選択")
                    }
                for (ClassName in Sclat.conf!!
                    .classConfig!!
                    .getConfigurationSection("WeaponClass")!!
                    .getKeys(false)) {
                    val item = ItemStack(getWeaponClass(ClassName)!!.mainWeapon!!.weaponIteamStack!!)
                    val itemm = item.itemMeta
                    itemm!!.setDisplayName(ClassName)
                    val lores: MutableList<String> = ArrayList()
                    lores.add(
                        "§r§6SubWeapon : " +
                            Sclat.conf!!
                                .classConfig!!
                                .getString("WeaponClass.$ClassName.SubWeaponName"),
                    )
                    lores.add(
                        "§r§6SPWeapon  : " +
                            Sclat.conf!!
                                .classConfig!!
                                .getString("WeaponClass.$ClassName.SPWeaponName"),
                    )
                    if (shop) {
                        lores.add("")
                        lores.add("§r§bMoney : " + getWeaponClass(ClassName)!!.mainWeapon!!.money)
                    }
                    itemm.lore = lores
                    item.itemMeta = itemm

                    val list: MutableList<String> = ArrayList()
                    list.add(weaponType)

                    when (weaponType) {
                        "Slosher" -> list.add("Bucket")
                        "Kasa" -> list.add("Camping")
                    }
                    var equals = false
                    for (wtype in list) {
                        if (wtype == getWeaponClass(ClassName)!!.mainWeapon!!.weaponType) equals = true
                    }
                    if (weaponType == "Hude" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Roller"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.isHude) equals = true
                    }
                    if (weaponType == "Roller" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Roller"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.isHude) equals = false
                    }
                    if (weaponType == "Burst" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Burst"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.getIsSwap()) equals = false
                    }

                    if (weaponType == "Maneu" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Shooter"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.isManeuver) equals = true
                    }
                    if (weaponType == "Swapper" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Shooter"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.getIsSwap()) equals = true
                    }
                    if (weaponType == "Shooter" &&
                        getWeaponClass(ClassName)!!.mainWeapon!!.weaponType == "Shooter"
                    ) {
                        if (getWeaponClass(ClassName)!!.mainWeapon!!.isManeuver ||
                            getWeaponClass(ClassName)!!.mainWeapon!!.getIsSwap()
                        ) {
                            equals = false
                        }
                    }

                    if (slotnum <= 52 && equals) {
                        if (shop) {
                            if (getWeaponClass(ClassName)!!.mainWeapon!!.money != 0 &&
                                !haveWeapon(player, ClassName)
                            ) {
                                if (getWeaponClass(ClassName)!!.mainWeapon!!.level > getLv(player)) {
                                    val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                                    val gmeta = glass.itemMeta
                                    gmeta!!.setDisplayName(
                                        "§6レベル§c" + getWeaponClass(ClassName)!!.mainWeapon!!.level + "§6で解禁",
                                    )
                                    glass.itemMeta = gmeta
                                    shooter.setItem(slotnum, glass)
                                } else if (getWeaponClass(ClassName)!!.mainWeapon!!.islootbox) {
                                    val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                                    val gmeta = glass.itemMeta
                                    gmeta!!.setDisplayName("§6ガチャ武器です")
                                    glass.itemMeta = gmeta
                                    shooter.setItem(slotnum, glass)
                                } else {
                                    shooter.setItem(slotnum, item)
                                }
                                slotnum++
                            }
                        } else {
                            if (getWeaponClass(ClassName)!!.mainWeapon!!.money == 0 ||
                                Sclat.conf!!
                                    .config!!
                                    .getString("WorkMode") == "Trial"
                            ) {
                                if (getWeaponClass(ClassName)!!.mainWeapon!!.level > getLv(player)) {
                                    val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                                    val gmeta = glass.itemMeta
                                    gmeta!!.setDisplayName(
                                        "§6レベル§c" + getWeaponClass(ClassName)!!.mainWeapon!!.level + "§6で解禁",
                                    )
                                    glass.itemMeta = gmeta
                                    shooter.setItem(slotnum, glass)
                                } else {
                                    shooter.setItem(slotnum, item)
                                }
                                slotnum++
                            } else if (haveWeapon(player, ClassName) || !Sclat.shop) {
                                shooter.setItem(slotnum, item)
                                slotnum++
                            }
                        }
                    }
                }
                if (!Sclat.tutorial) {
                    val `is` = ItemStack(Material.OAK_DOOR)
                    val ism = `is`.itemMeta
                    ism!!.setDisplayName("戻る")
                    `is`.itemMeta = ism
                    shooter.setItem(53, `is`)
                }

                player.openInventory(shooter)
            }

            "Main" -> {
                val wm =
                    if (shop) {
                        Bukkit.createInventory(null, 18, "Shop")
                    } else {
                        Bukkit.createInventory(null, 18, "武器選択")
                    }

                val s = ItemStack(Material.WOODEN_HOE)
                val sm = s.itemMeta
                sm!!.setDisplayName("シューター")
                s.itemMeta = sm

                val b = ItemStack(Material.DIAMOND_SHOVEL)
                val bm = b.itemMeta
                bm!!.setDisplayName("ブラスター")
                b.itemMeta = bm

                val ba = ItemStack(Material.DIAMOND_AXE)
                val bam = ba.itemMeta
                bam!!.setDisplayName("バーストシューター")
                ba.itemMeta = bam

                val r = ItemStack(Material.STONE_PICKAXE)
                val rm = r.itemMeta
                rm!!.setDisplayName("ローラー")
                r.itemMeta = rm

                val f = ItemStack(Material.CARROT_ON_A_STICK)
                val fm = f.itemMeta
                fm!!.setDisplayName("ブラシ")
                f.itemMeta = fm

                val sy = ItemStack(Material.KELP)
                val sym = sy.itemMeta
                sym!!.setDisplayName("シェルター")
                sy.itemMeta = sym

                val sr = ItemStack(Material.NETHER_BRICK)
                val srm = sr.itemMeta
                srm!!.setDisplayName("スロッシャー")
                sr.itemMeta = srm

                val c = ItemStack(Material.WOODEN_SWORD)
                val cm = c.itemMeta
                cm!!.setDisplayName("チャージャー")
                c.itemMeta = cm

                val sp = ItemStack(Material.IRON_INGOT)
                val spm = sp.itemMeta
                spm!!.setDisplayName("スピナー")
                sp.itemMeta = spm

                val m = ItemStack(Material.GOLDEN_HOE)
                val mm = m.itemMeta
                mm!!.setDisplayName("マニューバー")
                m.itemMeta = mm

                val hd = ItemStack(Material.GLOWSTONE_DUST)
                val hdm = hd.itemMeta
                hdm!!.setDisplayName("ハウンド")
                hd.itemMeta = hdm

                val swp = ItemStack(Material.IRON_HORSE_ARMOR)
                val swpm = swp.itemMeta
                swpm!!.setDisplayName("スワッパー")
                swp.itemMeta = swpm

                val fnl = ItemStack(Material.LEATHER)
                val fnlm = fnl.itemMeta
                fnlm!!.setDisplayName("ドラグーン")
                fnl.itemMeta = fnlm

                val grp = ItemStack(Material.ORANGE_DYE)
                val grpm = grp.itemMeta
                grpm!!.setDisplayName("リーラー")
                grp.itemMeta = grpm

                val bck = ItemStack(Material.SLIME_BALL)
                val bckm = bck.itemMeta
                bckm!!.setDisplayName("バックラー")
                bck.itemMeta = bckm

                wm.setItem(0, s)
                wm.setItem(1, b)
                wm.setItem(2, ba)
                wm.setItem(3, r)
                wm.setItem(4, f)
                wm.setItem(5, sy)
                wm.setItem(6, sr)
                wm.setItem(7, c)
                wm.setItem(8, sp)
                wm.setItem(9, m)
                wm.setItem(10, hd)
                wm.setItem(11, swp)
                wm.setItem(12, fnl)
                wm.setItem(13, grp)
                wm.setItem(14, bck)

                player.openInventory(wm)

                val `is` = ItemStack(Material.OAK_DOOR)
                val ism = `is`.itemMeta
                ism!!.setDisplayName("装備選択へ戻る")
                `is`.itemMeta = ism
                wm.setItem(17, `is`)
            }
        }
    }

    fun openSettingsUI(player: Player) {
        val inv = Bukkit.createInventory(null, 36, "設定")

        var i = 0
        while (i <= 35) {
            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.itemMeta
            ism!!.setDisplayName(".")
            `is`.itemMeta = ism
            inv.setItem(i, `is`)
            i++
        }

        val `is` = ItemStack(Material.OAK_DOOR)
        val ism = `is`.itemMeta
        ism!!.setDisplayName("戻る")
        `is`.itemMeta = ism
        inv.setItem(35, `is`)

        val shooter = ItemStack(Material.WOODEN_HOE)
        val shooterM = shooter.itemMeta
        shooterM!!.setDisplayName("メインウエポンのインクエフェクト")
        val shooterR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
            shooterR.add("§a§l[Enable]")
        } else {
            shooterR.add("§7§l[Disable]")
        }
        shooterR.add("")
        shooterR.add("§b[INFO]§r")
        shooterR.add("メインウエポンの弾の軌跡にインクのエフェクトを描画します。")
        shooterR.add("無効化するとクライアントのパーティクル描画負担と")
        shooterR.add("通信量を削減することができます。")
        shooterM.lore = shooterR
        shooter.itemMeta = shooterM
        inv.setItem(9, shooter)

        val shooterP: ItemStack?
        shooterP =
            if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val shooterPM = shooterP.itemMeta
        shooterPM!!.setDisplayName("メインウエポンのインクエフェクト")
        val shooterPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectMainWeaponInk()) {
            shooterPR.add("§a§l[Enable]")
        } else {
            shooterPR.add("§7§l[Disable]")
        }
        shooterPM.lore = shooterPR
        shooterP.itemMeta = shooterPM
        inv.setItem(18, shooterP)

        val charger = ItemStack(Material.WOODEN_SWORD)
        val chargerM = charger.itemMeta
        chargerM!!.setDisplayName("リッター5Gの自動チャージ")
        val chargerR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectChargerLine()) {
            chargerR.add("§a§l[Enable]")
        } else {
            chargerR.add("§7§l[Disable]")
        }
        chargerR.add("")
        chargerR.add("§b[INFO]§r")
        chargerR.add("他のプレイヤーがチャージして狙っているときに")
        chargerR.add("方向と射撃距離を表すレーザーを描画します。")
        chargerR.add("無効化するとクライアントのパーティクル描画負担と")
        chargerR.add("通信量を削減することができます。")
        chargerM.lore = chargerR
        charger.itemMeta = chargerM
        inv.setItem(10, charger)

        val chargerP: ItemStack?
        chargerP =
            if (getPlayerData(player)!!.settings!!.showEffectChargerLine()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val chargerPM = chargerP.itemMeta
        chargerPM!!.setDisplayName("リッター5Gの自動チャージ")
        val chargerPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectChargerLine()) {
            chargerPR.add("§a§l[Enable]")
        } else {
            chargerPR.add("§7§l[Disable]")
        }
        chargerPM.lore = chargerPR
        chargerP.itemMeta = chargerPM
        inv.setItem(19, chargerP)

        val chargerS = ItemStack(Material.END_CRYSTAL)
        val chargersM = chargerS.itemMeta
        chargersM!!.setDisplayName("スペシャルウエポンのエフェクト")
        val chargersR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectSPWeapon()) {
            chargersR.add("§a§l[Enable]")
        } else {
            chargersR.add("§7§l[Disable]")
        }
        chargersR.add("")
        chargersR.add("§b[INFO]§r")
        chargersR.add("プレイヤーがスペシャルウエポンを使用しているときに")
        chargersR.add("演出用のエフェクトを描画します。")
        chargersR.add("無効化するとクライアントのパーティクル描画負担と")
        chargersR.add("通信量を削減することができます。")
        chargersM.lore = chargersR
        chargerS.itemMeta = chargersM
        inv.setItem(11, chargerS)

        val chargersP: ItemStack?
        chargersP =
            if (getPlayerData(player)!!.settings!!.showEffectSPWeapon()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val chargersPM = chargersP.itemMeta
        chargersPM!!.setDisplayName("スペシャルウエポンのエフェクト")
        val chargersPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectSPWeapon()) {
            chargersPR.add("§a§l[Enable]")
        } else {
            chargersPR.add("§7§l[Disable]")
        }
        chargersPM.lore = chargersPR
        chargersP.itemMeta = chargersPM
        inv.setItem(20, chargersP)

        val rollaerL = ItemStack(Material.SHULKER_SHELL)
        val rollaerlM = rollaerL.itemMeta
        rollaerlM!!.setDisplayName("スペシャルウエポンの範囲エフェクト")
        val rollaerlR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectSPWeaponRegion()) {
            rollaerlR.add("§a§l[Enable]")
        } else {
            rollaerlR.add("§7§l[Disable]")
        }
        rollaerlR.add("")
        rollaerlR.add("§b[INFO]§r")
        rollaerlR.add("プレイヤーがスペシャルウエポンを使用しているときに")
        rollaerlR.add("スペシャルウエポンの効果範囲を表すエフェクトを描画します。")
        rollaerlR.add("無効化するとクライアントのパーティクル描画負担と")
        rollaerlR.add("通信量を削減することができますが")
        rollaerlR.add("スペシャルウエポンの効果範囲を把握しづらくなります。")
        rollaerlM.lore = rollaerlR
        rollaerL.itemMeta = rollaerlM
        inv.setItem(12, rollaerL)

        val rollaerlP: ItemStack?
        rollaerlP =
            if (getPlayerData(player)!!.settings!!.showEffectSPWeaponRegion()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val rollaerlPM = rollaerlP.itemMeta
        rollaerlPM!!.setDisplayName("スペシャルウエポンの範囲エフェクト")
        val rollaerlPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectSPWeaponRegion()) {
            rollaerlPR.add("§a§l[Enable]")
        } else {
            rollaerlPR.add("§7§l[Disable]")
        }
        rollaerlPM.lore = rollaerlPR
        rollaerlP.itemMeta = rollaerlPM
        inv.setItem(21, rollaerlP)

        val rollerS = ItemStack(Material.SNOWBALL)
        val rollersM = rollerS.itemMeta
        rollersM!!.setDisplayName("弾の表示")
        val rollersR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showSnowBall()) {
            rollersR.add("§a§l[Enable]")
        } else {
            rollersR.add("§7§l[Disable]")
        }
        rollersR.add("")
        rollersR.add("§b[INFO]§r")
        rollersR.add("メインウエポンから発射された弾を描画します。")
        rollersR.add("無効化するとクライアントのエンティティ描画負担と")
        rollersR.add("通信量を削減することができます。")
        rollersM.lore = rollersR
        rollerS.itemMeta = rollersM
        inv.setItem(13, rollerS)

        val rollersP: ItemStack?
        rollersP =
            if (getPlayerData(player)!!.settings!!.showSnowBall()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val rollersPM = rollersP.itemMeta
        rollersPM!!.setDisplayName("弾の表示")
        val rollersPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showSnowBall()) {
            rollersPR.add("§a§l[Enable]")
        } else {
            rollersPR.add("§7§l[Disable]")
        }
        rollersPM.lore = rollersPR
        rollersP.itemMeta = rollersPM
        inv.setItem(22, rollersP)

        val bgmP: ItemStack?
        bgmP =
            if (getPlayerData(player)!!.settings!!.playBGM()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val bgmPM = bgmP.itemMeta
        bgmPM!!.setDisplayName("BGM")
        val bgmPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.playBGM()) {
            bgmPR.add("§a§l[Enable]")
        } else {
            bgmPR.add("§7§l[Disable]")
        }
        bgmPM.lore = bgmPR
        bgmP.itemMeta = bgmPM

        // if(Main.NoteBlockAPI)
        // inv.setItem(26, bgm_p);
        val bgm = ItemStack(Material.MUSIC_DISC_13)
        val bgmM = bgm.itemMeta
        bgmM!!.setDisplayName("BGM")
        val bgmR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.playBGM()) {
            bgmR.add("§a§l[Enable]")
        } else {
            bgmR.add("§7§l[Disable]")
        }
        bgmM.lore = bgmR
        bgm.itemMeta = bgmM

        // if(Main.NoteBlockAPI)
        // inv.setItem(17, bgm);
        val bomb = ItemStack(Material.WHITE_STAINED_GLASS)
        val bombM = bomb.itemMeta
        bombM!!.setDisplayName("投擲武器の視認用エフェクト")
        val bombR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectBomb()) {
            bombR.add("§a§l[Enable]")
        } else {
            bombR.add("§7§l[Disable]")
        }
        bombR.add("")
        bombR.add("§b[INFO]§r")
        bombR.add("サブウエポン等の投擲武器の軌跡にエフェクトを描画します。")
        bombR.add("無効化するとクライアントのパーティクル描画負担と")
        bombR.add("通信量を削減することができます。")
        bombM.lore = bombR
        bomb.itemMeta = bombM
        inv.setItem(14, bomb)

        val bombP: ItemStack?
        bombP =
            if (getPlayerData(player)!!.settings!!.showEffectBomb()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val bombPM = bombP.itemMeta
        bombPM!!.setDisplayName("投擲武器の視認用エフェクト")
        val bombPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectBomb()) {
            bombPR.add("§a§l[Enable]")
        } else {
            bombPR.add("§7§l[Disable]")
        }
        bombPM.lore = bombPR
        bombP.itemMeta = bombPM
        inv.setItem(23, bombP)

        val bombEx = ItemStack(Material.TNT)
        val bombexM = bombEx.itemMeta
        bombexM!!.setDisplayName("爆発エフェクト")
        val bombexR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectBombEx()) {
            bombexR.add("§a§l[Enable]")
        } else {
            bombexR.add("§7§l[Disable]")
        }
        bombexR.add("")
        bombexR.add("§b[INFO]§r")
        bombexR.add("ボム等の爆発エフェクトを描画します。")
        bombexR.add("無効化するとクライアントのパーティクル描画負担と")
        bombexR.add("通信量を削減することができます。")
        bombexM.lore = bombexR
        bombEx.itemMeta = bombexM
        inv.setItem(15, bombEx)

        val bombexP: ItemStack?
        bombexP =
            if (getPlayerData(player)!!.settings!!.showEffectBombEx()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val bombexPM = bombexP.itemMeta
        bombexPM!!.setDisplayName("爆発エフェクト")
        val bombexPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.showEffectBombEx()) {
            bombexPR.add("§a§l[Enable]")
        } else {
            bombexPR.add("§7§l[Disable]")
        }
        bombexPM.lore = bombexPR
        bombexP.itemMeta = bombexPM
        inv.setItem(24, bombexP)

        // description
        val ck = ItemStack(Material.GOLDEN_SWORD)
        val ckM = ck.itemMeta
        ckM!!.setDisplayName("チャージキープ/ブリンクの旧挙動")
        val ckR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
            ckR.add("§a§l[Enable]")
        } else {
            ckR.add("§7§l[Disable]")
        }
        ckR.add("")
        ckR.add("§b[INFO]§r")
        ckR.add("マニューバ系統のブリンクの処理を古いものに切り替えることができます。")
        ckR.add("(無効だと、新しい挙動のブリンクの処理になります。)")
        ckM.lore = ckR
        ck.itemMeta = ckM
        inv.setItem(16, ck)

        // button
        val ckP: ItemStack?
        ckP =
            if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
                ItemStack(Material.LIME_DYE)
            } else {
                ItemStack(Material.GUNPOWDER)
            }
        val ckPM = ckP.itemMeta
        ckPM!!.setDisplayName("チャージキープ/ブリンクの旧挙動")
        val ckPR = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
            ckPR.add("§a§l[Enable]")
        } else {
            ckPR.add("§7§l[Disable]")
        }
        ckPM.lore = ckPR
        ckP.itemMeta = ckPM
        inv.setItem(25, ckP)

        player.openInventory(inv)
    }
}
