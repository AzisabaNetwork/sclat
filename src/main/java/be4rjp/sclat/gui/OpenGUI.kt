package be4rjp.sclat.gui

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.ServerType
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
            val ism = `is`.getItemMeta()
            ism!!.setDisplayName(".")
            `is`.setItemMeta(ism)
            inv.setItem(i, `is`)
            i++
        }

        val join = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val joinmeta = join.getItemMeta()
        joinmeta!!.setDisplayName("試合に参加 / JOIN THE MATCH")
        join.setItemMeta(joinmeta)
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") != "Trial"
        ) {
            inv.setItem(10, join)
        }

        val setting = ItemStack(Material.COMPARATOR)
        val setting_m = setting.getItemMeta()
        setting_m!!.setDisplayName("設定 / SETTINGS")
        setting.setItemMeta(setting_m)
        inv.setItem(14, setting)

        val w = ItemStack(Material.LEATHER_CHESTPLATE)
        val wmeta = w.getItemMeta()
        wmeta!!.setDisplayName("装備変更 / EQUIPMENT")
        w.setItemMeta(wmeta)
        inv.setItem(12, w)
        player.openInventory(inv)

        val t = ItemStack(Material.GRASS_BLOCK)
        val tmeta = t.getItemMeta()
        tmeta!!.setDisplayName("リソースパックをダウンロード / DOWNLOAD RESOURCEPACK")
        t.setItemMeta(tmeta)
        inv.setItem(28, t)

        val r = ItemStack(Material.MILK_BUCKET)
        val rmeta = r.getItemMeta()
        rmeta!!.setDisplayName("塗りをリセット / RESET INK")
        r.setItemMeta(rmeta)
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
            val bmeta = b.getItemMeta()
            bmeta!!.setDisplayName("ロビーへ戻る / RETURN TO LOBBY")
            b.setItemMeta(bmeta)
            inv.setItem(16, b)
        } else {
            val ta = ItemStack(Material.ARMOR_STAND)
            val tameta = ta.getItemMeta()
            tameta!!.setDisplayName("試し打ちサーバーへ接続 / TRAINING FIELD")
            ta.setItemMeta(tameta)
            inv.setItem(30, ta)

            val b = ItemStack(Material.CHEST)
            val bmeta = b.getItemMeta()
            bmeta!!.setDisplayName("ショップを開く / OPEN SHOP")
            b.setItemMeta(bmeta)
            inv.setItem(16, b)

            val data = getPlayerData(player)
            var status = ItemStack(Material.PLAYER_HEAD)
            if (data!!.playerHead != null) status = CraftItemStack.asBukkitCopy(data.playerHead).clone()
            val statusMeta = status.getItemMeta()
            statusMeta!!.setDisplayName("§r§e" + player.getName() + " のステータス")
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
            statusMeta.setLore(lores)
            status.setItemMeta(statusMeta)
            inv.setItem(32, status)
        }

        if (Sclat.type == ServerType.LOBBY) {
            val b = ItemStack(Material.EGG)
            val bmeta = b.getItemMeta()
            bmeta!!.setDisplayName("称号 / EMBLEM")
            b.setItemMeta(bmeta)
            inv.setItem(34, b)
        }

        val b = ItemStack(Material.BARRIER)
        val bmeta = b.getItemMeta()
        bmeta!!.setDisplayName("閉じる")
        b.setItemMeta(bmeta)
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
                        val nmeta = n.getItemMeta()
                        nmeta!!.setDisplayName(".")
                        n.setItemMeta(nmeta)
                        inv.setItem(i, n)
                        i++
                        continue
                    }

                    val n = ItemStack(getGearMaterial(i))
                    val nmeta = n.getItemMeta()
                    nmeta!!.setDisplayName(getGearName(i))
                    val list: MutableList<String?> = ArrayList<String?>()
                    list.add("")
                    list.add("§r§bMoney : " + getGearPrice(i))
                    nmeta.setLore(list)
                    n.setItemMeta(nmeta)
                    inv.setItem(i, n)
                    i++
                }
            }
            var i = 10
            while (i <= 17) {
                val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                val nmeta = n.getItemMeta()
                nmeta!!.setDisplayName(".")
                n.setItemMeta(nmeta)
                inv.setItem(i, n)
                i++
            }
        } else {
            run {
                var i = 0
                while (i <= 9) {
                    if (!(
                            haveGear(player, i) || Sclat.conf!!
                                .config!!
                                .getString("WorkMode") == "Trial" ||
                                !Sclat.shop
                            )
                    ) {
                        val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                        val nmeta = n.getItemMeta()
                        nmeta!!.setDisplayName(".")
                        n.setItemMeta(nmeta)
                        inv.setItem(i, n)
                        i++
                        continue
                    }

                    val n = ItemStack(getGearMaterial(i))
                    val nmeta = n.getItemMeta()
                    nmeta!!.setDisplayName(getGearName(i))
                    n.setItemMeta(nmeta)
                    inv.setItem(i, n)
                    i++
                }
            }
            var i = 10
            while (i <= 17) {
                val n = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                val nmeta = n.getItemMeta()
                nmeta!!.setDisplayName(".")
                n.setItemMeta(nmeta)
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
            val ism = `is`.getItemMeta()
            ism!!.setDisplayName(".")
            `is`.setItemMeta(ism)
            inv.setItem(i, `is`)
            i++
        }

        val n = ItemStack(getGearMaterial(getPlayerData(player)!!.gearNumber))
        val nmeta = n.getItemMeta()
        nmeta!!.setDisplayName(if (shop) "§bギア購入 / GEAR" else "§bギア変更 / GEAR")
        n.setItemMeta(nmeta)
        inv.setItem(15, n)

        val t =
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .weaponIteamStack!!
                .clone()
        val tmeta = t.getItemMeta()
        tmeta!!.setDisplayName(if (shop) "§6武器購入 / WEAPON" else "§6武器変更 / WEAPON")
        t.setItemMeta(tmeta)
        inv.setItem(11, t)

        val `is` = ItemStack(Material.OAK_DOOR)
        val ism = `is`.getItemMeta()
        ism!!.setDisplayName("戻る")
        `is`.setItemMeta(ism)
        inv.setItem(26, `is`)

        player.openInventory(inv)
    }

    fun MatchTohyoGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 18, "Chose a Gamemode")

        var i = 0
        while (i <= 17) {
            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.getItemMeta()
            ism!!.setDisplayName(".")
            `is`.setItemMeta(ism)
            inv.setItem(i, `is`)
            i++
        }

        val n = ItemStack(Material.SNOWBALL)
        val nmeta = n.getItemMeta()
        nmeta!!.setDisplayName("ナワバリバトル")
        n.setItemMeta(nmeta)
        inv.setItem(2, n)

        val t = ItemStack(Material.DIAMOND_SWORD)
        val tmeta = t.getItemMeta()
        tmeta!!.setDisplayName("チームデスマッチ")
        t.setItemMeta(tmeta)
        inv.setItem(4, t)

        val nu = ItemStack(Material.GLASS)
        val numeta = nu.getItemMeta()
        numeta!!.setDisplayName("ガチエリア")
        nu.setItemMeta(numeta)
        val match = getMatchFromId(MatchMgr.matchcount)
        if (match!!.mapData!!.canAreaBattle) inv.setItem(6, nu)

        player.openInventory(inv)
    }

    fun SuperJumpGUI(player: Player) {
        val inv = Bukkit.createInventory(null, 18, "Chose Target")

        val `is` = ItemStack(getPlayerData(player)!!.team!!.teamColor!!.glass!!)
        val ism = `is`.getItemMeta()
        ism!!.setDisplayName(if (Sclat.tutorial) "§r§6ロビーへジャンプ" else "§r§6リスポーン地点へジャンプ")
        `is`.setItemMeta(ism)
        var loc = Sclat.lobby!!.clone()
        if (Sclat.conf!!
                .config!!
                .getString("WorkMode") != "Trial"
        ) {
            loc =
                getPlayerData(player)!!.matchLocation!!.clone()
        }
        if (loc.getWorld() === player.getWorld()) {
            if (player
                    .getLocation()
                    .distance(loc) > 10 && !Tutorial.clearList.contains(player)
            ) {
                if (!Sclat.tutorial) {
                    inv.setItem(0, `is`)
                }
            }
        }

        var slotnum = 1

        for (p in plugin.getServer().getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue
            if (getPlayerData(p)!!.team!!.iD == getPlayerData(player)!!.team!!.iD && p.getWorld() === player.getWorld() && p !== player) {
                if (getPlayerData(p)!!.playerGroundLocation!!.distance(player.getLocation()) > 10 &&
                    getPlayerData(p)!!.playerHead != null
                ) {
                    if (slotnum <= 17) {
                        val head = CraftItemStack.asBukkitCopy(getPlayerData(p)!!.playerHead).clone()
                        val headM = head.getItemMeta()
                        val lores: MutableList<String> = ArrayList()
                        lores.add("§r§aプレイヤーへジャンプ")
                        headM!!.setLore(lores)
                        head.setItemMeta(headM)
                        inv.setItem(slotnum, head)
                    }
                    slotnum++
                }
            }
        }
        for (`as` in beaconMap.values) {
            if (`as`!!.getCustomName() == "21") {
                val p = getArmorStandPlayer(`as`)
                if (getPlayerData(player)!!.team == getPlayerData(p)!!.team) {
                    if (`as`.getWorld() === player.getWorld()) {
                        if (`as`.getLocation().distance(player.getLocation()) > 10) {
                            val item = ItemStack(Material.IRON_TRAPDOOR)
                            val im = item.getItemMeta()
                            im!!.setDisplayName(p!!.getName())
                            val lores: MutableList<String> = ArrayList()
                            lores.add("§r§6プレイヤーのビーコンへジャンプ")
                            im.setLore(lores)
                            item.setItemMeta(im)
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
            val itemm = item.getItemMeta()
            itemm!!.setDisplayName(ClassName)
            val lores: MutableList<String> = ArrayList()
            lores.add(
                "§r§6SubWeapon : " +
                    Sclat.conf!!
                        .classConfig!!
                        .getString("WeaponClass." + ClassName + ".SubWeaponName"),
            )
            lores.add(
                "§r§6SPWeapon  : " +
                    Sclat.conf!!
                        .classConfig!!
                        .getString("WeaponClass." + ClassName + ".SPWeaponName"),
            )
            itemm.setLore(lores)
            item.setItemMeta(itemm)
            if (slotnum <= 44 && (
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
                    val itemm = item.getItemMeta()
                    itemm!!.setDisplayName(ClassName)
                    val lores: MutableList<String> = ArrayList()
                    lores.add(
                        "§r§6SubWeapon : " +
                            Sclat.conf!!
                                .classConfig!!
                                .getString("WeaponClass." + ClassName + ".SubWeaponName"),
                    )
                    lores.add(
                        "§r§6SPWeapon  : " +
                            Sclat.conf!!
                                .classConfig!!
                                .getString("WeaponClass." + ClassName + ".SPWeaponName"),
                    )
                    if (shop) {
                        lores.add("")
                        lores.add("§r§bMoney : " + getWeaponClass(ClassName)!!.mainWeapon!!.money)
                    }
                    itemm.setLore(lores)
                    item.setItemMeta(itemm)

                    val list: MutableList<String> = ArrayList<String>()
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
                                    val gmeta = glass.getItemMeta()
                                    gmeta!!.setDisplayName(
                                        "§6レベル§c" + getWeaponClass(ClassName)!!.mainWeapon!!.level + "§6で解禁",
                                    )
                                    glass.setItemMeta(gmeta)
                                    shooter.setItem(slotnum, glass)
                                } else if (getWeaponClass(ClassName)!!.mainWeapon!!.islootbox) {
                                    val glass = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                                    val gmeta = glass.getItemMeta()
                                    gmeta!!.setDisplayName("§6ガチャ武器です")
                                    glass.setItemMeta(gmeta)
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
                                    val gmeta = glass.getItemMeta()
                                    gmeta!!.setDisplayName(
                                        "§6レベル§c" + getWeaponClass(ClassName)!!.mainWeapon!!.level + "§6で解禁",
                                    )
                                    glass.setItemMeta(gmeta)
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
                    val ism = `is`.getItemMeta()
                    ism!!.setDisplayName("戻る")
                    `is`.setItemMeta(ism)
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
                val sm = s.getItemMeta()
                sm!!.setDisplayName("シューター")
                s.setItemMeta(sm)

                val b = ItemStack(Material.DIAMOND_SHOVEL)
                val bm = b.getItemMeta()
                bm!!.setDisplayName("ブラスター")
                b.setItemMeta(bm)

                val ba = ItemStack(Material.DIAMOND_AXE)
                val bam = ba.getItemMeta()
                bam!!.setDisplayName("バーストシューター")
                ba.setItemMeta(bam)

                val r = ItemStack(Material.STONE_PICKAXE)
                val rm = r.getItemMeta()
                rm!!.setDisplayName("ローラー")
                r.setItemMeta(rm)

                val f = ItemStack(Material.CARROT_ON_A_STICK)
                val fm = f.getItemMeta()
                fm!!.setDisplayName("ブラシ")
                f.setItemMeta(fm)

                val sy = ItemStack(Material.KELP)
                val sym = sy.getItemMeta()
                sym!!.setDisplayName("シェルター")
                sy.setItemMeta(sym)

                val sr = ItemStack(Material.NETHER_BRICK)
                val srm = sr.getItemMeta()
                srm!!.setDisplayName("スロッシャー")
                sr.setItemMeta(srm)

                val c = ItemStack(Material.WOODEN_SWORD)
                val cm = c.getItemMeta()
                cm!!.setDisplayName("チャージャー")
                c.setItemMeta(cm)

                val sp = ItemStack(Material.IRON_INGOT)
                val spm = sp.getItemMeta()
                spm!!.setDisplayName("スピナー")
                sp.setItemMeta(spm)

                val m = ItemStack(Material.GOLDEN_HOE)
                val mm = m.getItemMeta()
                mm!!.setDisplayName("マニューバー")
                m.setItemMeta(mm)

                val hd = ItemStack(Material.GLOWSTONE_DUST)
                val hdm = hd.getItemMeta()
                hdm!!.setDisplayName("ハウンド")
                hd.setItemMeta(hdm)

                val swp = ItemStack(Material.IRON_HORSE_ARMOR)
                val swpm = swp.getItemMeta()
                swpm!!.setDisplayName("スワッパー")
                swp.setItemMeta(swpm)

                val fnl = ItemStack(Material.LEATHER)
                val fnlm = fnl.getItemMeta()
                fnlm!!.setDisplayName("ドラグーン")
                fnl.setItemMeta(fnlm)

                val grp = ItemStack(Material.ORANGE_DYE)
                val grpm = grp.getItemMeta()
                grpm!!.setDisplayName("リーラー")
                grp.setItemMeta(grpm)

                val bck = ItemStack(Material.SLIME_BALL)
                val bckm = bck.getItemMeta()
                bckm!!.setDisplayName("バックラー")
                bck.setItemMeta(bckm)

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
                val ism = `is`.getItemMeta()
                ism!!.setDisplayName("装備選択へ戻る")
                `is`.setItemMeta(ism)
                wm.setItem(17, `is`)
            }
        }
    }

    fun openSettingsUI(player: Player) {
        val inv = Bukkit.createInventory(null, 36, "設定")

        var i = 0
        while (i <= 35) {
            val `is` = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
            val ism = `is`.getItemMeta()
            ism!!.setDisplayName(".")
            `is`.setItemMeta(ism)
            inv.setItem(i, `is`)
            i++
        }

        val `is` = ItemStack(Material.OAK_DOOR)
        val ism = `is`.getItemMeta()
        ism!!.setDisplayName("戻る")
        `is`.setItemMeta(ism)
        inv.setItem(35, `is`)

        val shooter = ItemStack(Material.WOODEN_HOE)
        val shooter_m = shooter.getItemMeta()
        shooter_m!!.setDisplayName("メインウエポンのインクエフェクト")
        val shooter_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_MainWeaponInk()) {
            shooter_r.add("§a§l[Enable]")
        } else {
            shooter_r.add("§7§l[Disable]")
        }
        shooter_r.add("")
        shooter_r.add("§b[INFO]§r")
        shooter_r.add("メインウエポンの弾の軌跡にインクのエフェクトを描画します。")
        shooter_r.add("無効化するとクライアントのパーティクル描画負担と")
        shooter_r.add("通信量を削減することができます。")
        shooter_m.setLore(shooter_r)
        shooter.setItemMeta(shooter_m)
        inv.setItem(9, shooter)

        val shooter_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_MainWeaponInk()) {
            shooter_p = ItemStack(Material.LIME_DYE)
        } else {
            shooter_p = ItemStack(Material.GUNPOWDER)
        }
        val shooter_p_m = shooter_p.getItemMeta()
        shooter_p_m!!.setDisplayName("メインウエポンのインクエフェクト")
        val shooter_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_MainWeaponInk()) {
            shooter_p_r.add("§a§l[Enable]")
        } else {
            shooter_p_r.add("§7§l[Disable]")
        }
        shooter_p_m.setLore(shooter_p_r)
        shooter_p.setItemMeta(shooter_p_m)
        inv.setItem(18, shooter_p)

        val charger = ItemStack(Material.WOODEN_SWORD)
        val charger_m = charger.getItemMeta()
        charger_m!!.setDisplayName("チャージャーのレーザー")
        val charger_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_ChargerLine()) {
            charger_r.add("§a§l[Enable]")
        } else {
            charger_r.add("§7§l[Disable]")
        }
        charger_r.add("")
        charger_r.add("§b[INFO]§r")
        charger_r.add("他のプレイヤーがチャージして狙っているときに")
        charger_r.add("方向と射撃距離を表すレーザーを描画します。")
        charger_r.add("無効化するとクライアントのパーティクル描画負担と")
        charger_r.add("通信量を削減することができます。")
        charger_m.setLore(charger_r)
        charger.setItemMeta(charger_m)
        inv.setItem(10, charger)

        val charger_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_ChargerLine()) {
            charger_p = ItemStack(Material.LIME_DYE)
        } else {
            charger_p = ItemStack(Material.GUNPOWDER)
        }
        val charger_p_m = charger_p.getItemMeta()
        charger_p_m!!.setDisplayName("チャージャーのレーザー")
        val charger_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_ChargerLine()) {
            charger_p_r.add("§a§l[Enable]")
        } else {
            charger_p_r.add("§7§l[Disable]")
        }
        charger_p_m.setLore(charger_p_r)
        charger_p.setItemMeta(charger_p_m)
        inv.setItem(19, charger_p)

        val chargerS = ItemStack(Material.END_CRYSTAL)
        val chargerS_m = chargerS.getItemMeta()
        chargerS_m!!.setDisplayName("スペシャルウエポンのエフェクト")
        val chargerS_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeapon()) {
            chargerS_r.add("§a§l[Enable]")
        } else {
            chargerS_r.add("§7§l[Disable]")
        }
        chargerS_r.add("")
        chargerS_r.add("§b[INFO]§r")
        chargerS_r.add("プレイヤーがスペシャルウエポンを使用しているときに")
        chargerS_r.add("演出用のエフェクトを描画します。")
        chargerS_r.add("無効化するとクライアントのパーティクル描画負担と")
        chargerS_r.add("通信量を削減することができます。")
        chargerS_m.setLore(chargerS_r)
        chargerS.setItemMeta(chargerS_m)
        inv.setItem(11, chargerS)

        val chargerS_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeapon()) {
            chargerS_p = ItemStack(Material.LIME_DYE)
        } else {
            chargerS_p = ItemStack(Material.GUNPOWDER)
        }
        val chargerS_p_m = chargerS_p.getItemMeta()
        chargerS_p_m!!.setDisplayName("スペシャルウエポンのエフェクト")
        val chargerS_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeapon()) {
            chargerS_p_r.add("§a§l[Enable]")
        } else {
            chargerS_p_r.add("§7§l[Disable]")
        }
        chargerS_p_m.setLore(chargerS_p_r)
        chargerS_p.setItemMeta(chargerS_p_m)
        inv.setItem(20, chargerS_p)

        val rollaerL = ItemStack(Material.SHULKER_SHELL)
        val rollaerL_m = rollaerL.getItemMeta()
        rollaerL_m!!.setDisplayName("スペシャルウエポンの範囲エフェクト")
        val rollaerL_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeaponRegion()) {
            rollaerL_r.add("§a§l[Enable]")
        } else {
            rollaerL_r.add("§7§l[Disable]")
        }
        rollaerL_r.add("")
        rollaerL_r.add("§b[INFO]§r")
        rollaerL_r.add("プレイヤーがスペシャルウエポンを使用しているときに")
        rollaerL_r.add("スペシャルウエポンの効果範囲を表すエフェクトを描画します。")
        rollaerL_r.add("無効化するとクライアントのパーティクル描画負担と")
        rollaerL_r.add("通信量を削減することができますが")
        rollaerL_r.add("スペシャルウエポンの効果範囲を把握しづらくなります。")
        rollaerL_m.setLore(rollaerL_r)
        rollaerL.setItemMeta(rollaerL_m)
        inv.setItem(12, rollaerL)

        val rollaerL_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeaponRegion()) {
            rollaerL_p = ItemStack(Material.LIME_DYE)
        } else {
            rollaerL_p = ItemStack(Material.GUNPOWDER)
        }
        val rollaerL_p_m = rollaerL_p.getItemMeta()
        rollaerL_p_m!!.setDisplayName("スペシャルウエポンの範囲エフェクト")
        val rollaerL_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_SPWeaponRegion()) {
            rollaerL_p_r.add("§a§l[Enable]")
        } else {
            rollaerL_p_r.add("§7§l[Disable]")
        }
        rollaerL_p_m.setLore(rollaerL_p_r)
        rollaerL_p.setItemMeta(rollaerL_p_m)
        inv.setItem(21, rollaerL_p)

        val rollerS = ItemStack(Material.SNOWBALL)
        val rollerS_m = rollerS.getItemMeta()
        rollerS_m!!.setDisplayName("弾の表示")
        val rollerS_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowSnowBall()) {
            rollerS_r.add("§a§l[Enable]")
        } else {
            rollerS_r.add("§7§l[Disable]")
        }
        rollerS_r.add("")
        rollerS_r.add("§b[INFO]§r")
        rollerS_r.add("メインウエポンから発射された弾を描画します。")
        rollerS_r.add("無効化するとクライアントのエンティティ描画負担と")
        rollerS_r.add("通信量を削減することができます。")
        rollerS_m.setLore(rollerS_r)
        rollerS.setItemMeta(rollerS_m)
        inv.setItem(13, rollerS)

        val rollerS_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowSnowBall()) {
            rollerS_p = ItemStack(Material.LIME_DYE)
        } else {
            rollerS_p = ItemStack(Material.GUNPOWDER)
        }
        val rollerS_p_m = rollerS_p.getItemMeta()
        rollerS_p_m!!.setDisplayName("弾の表示")
        val rollerS_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowSnowBall()) {
            rollerS_p_r.add("§a§l[Enable]")
        } else {
            rollerS_p_r.add("§7§l[Disable]")
        }
        rollerS_p_m.setLore(rollerS_p_r)
        rollerS_p.setItemMeta(rollerS_p_m)
        inv.setItem(22, rollerS_p)

        val bgm_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.PlayBGM()) {
            bgm_p = ItemStack(Material.LIME_DYE)
        } else {
            bgm_p = ItemStack(Material.GUNPOWDER)
        }
        val bgm_p_m = bgm_p.getItemMeta()
        bgm_p_m!!.setDisplayName("BGM")
        val bgm_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.PlayBGM()) {
            bgm_p_r.add("§a§l[Enable]")
        } else {
            bgm_p_r.add("§7§l[Disable]")
        }
        bgm_p_m.setLore(bgm_p_r)
        bgm_p.setItemMeta(bgm_p_m)

        // if(Main.NoteBlockAPI)
        // inv.setItem(26, bgm_p);
        val bgm = ItemStack(Material.MUSIC_DISC_13)
        val bgm_m = bgm.getItemMeta()
        bgm_m!!.setDisplayName("BGM")
        val bgm_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.PlayBGM()) {
            bgm_r.add("§a§l[Enable]")
        } else {
            bgm_r.add("§7§l[Disable]")
        }
        bgm_m.setLore(bgm_r)
        bgm.setItemMeta(bgm_m)

        // if(Main.NoteBlockAPI)
        // inv.setItem(17, bgm);
        val bomb = ItemStack(Material.WHITE_STAINED_GLASS)
        val bomb_m = bomb.getItemMeta()
        bomb_m!!.setDisplayName("投擲武器の視認用エフェクト")
        val bomb_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_Bomb()) {
            bomb_r.add("§a§l[Enable]")
        } else {
            bomb_r.add("§7§l[Disable]")
        }
        bomb_r.add("")
        bomb_r.add("§b[INFO]§r")
        bomb_r.add("サブウエポン等の投擲武器の軌跡にエフェクトを描画します。")
        bomb_r.add("無効化するとクライアントのパーティクル描画負担と")
        bomb_r.add("通信量を削減することができます。")
        bomb_m.setLore(bomb_r)
        bomb.setItemMeta(bomb_m)
        inv.setItem(14, bomb)

        val bomb_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_Bomb()) {
            bomb_p = ItemStack(Material.LIME_DYE)
        } else {
            bomb_p = ItemStack(Material.GUNPOWDER)
        }
        val bomb_p_m = bomb_p.getItemMeta()
        bomb_p_m!!.setDisplayName("投擲武器の視認用エフェクト")
        val bomb_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_Bomb()) {
            bomb_p_r.add("§a§l[Enable]")
        } else {
            bomb_p_r.add("§7§l[Disable]")
        }
        bomb_p_m.setLore(bomb_p_r)
        bomb_p.setItemMeta(bomb_p_m)
        inv.setItem(23, bomb_p)

        val bombEx = ItemStack(Material.TNT)
        val bombEx_m = bombEx.getItemMeta()
        bombEx_m!!.setDisplayName("爆発エフェクト")
        val bombEx_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_BombEx()) {
            bombEx_r.add("§a§l[Enable]")
        } else {
            bombEx_r.add("§7§l[Disable]")
        }
        bombEx_r.add("")
        bombEx_r.add("§b[INFO]§r")
        bombEx_r.add("ボム等の爆発エフェクトを描画します。")
        bombEx_r.add("無効化するとクライアントのパーティクル描画負担と")
        bombEx_r.add("通信量を削減することができます。")
        bombEx_m.setLore(bombEx_r)
        bombEx.setItemMeta(bombEx_m)
        inv.setItem(15, bombEx)

        val bombEx_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.ShowEffect_BombEx()) {
            bombEx_p = ItemStack(Material.LIME_DYE)
        } else {
            bombEx_p = ItemStack(Material.GUNPOWDER)
        }
        val bombEx_p_m = bombEx_p.getItemMeta()
        bombEx_p_m!!.setDisplayName("爆発エフェクト")
        val bombEx_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.ShowEffect_BombEx()) {
            bombEx_p_r.add("§a§l[Enable]")
        } else {
            bombEx_p_r.add("§7§l[Disable]")
        }
        bombEx_p_m.setLore(bombEx_p_r)
        bombEx_p.setItemMeta(bombEx_p_m)
        inv.setItem(24, bombEx_p)

        val ck = ItemStack(Material.GOLDEN_SWORD)
        val ck_m = ck.getItemMeta()
        ck_m!!.setDisplayName("チャージキープ")
        val ck_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
            ck_r.add("§a§l[Enable]")
        } else {
            ck_r.add("§7§l[Disable]")
        }
        ck_r.add("")
        ck_r.add("§b[INFO]§r")
        ck_r.add("チャージャー等のチャージキープ機能を発動できるようになります。")
        ck_r.add("(チャージキープは十分チャージした後にイカ状態に切り替えると発動します。)")
        ck_m.setLore(ck_r)
        ck.setItemMeta(ck_m)
        inv.setItem(16, ck)

        val ck_p: ItemStack?
        if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
            ck_p = ItemStack(Material.LIME_DYE)
        } else {
            ck_p = ItemStack(Material.GUNPOWDER)
        }
        val ck_p_m = ck_p.getItemMeta()
        ck_p_m!!.setDisplayName("チャージキープ")
        val ck_p_r = ArrayList<String?>()
        if (getPlayerData(player)!!.settings!!.doChargeKeep()) {
            ck_p_r.add("§a§l[Enable]")
        } else {
            ck_p_r.add("§7§l[Disable]")
        }
        ck_p_m.setLore(ck_p_r)
        ck_p.setItemMeta(ck_p_m)
        inv.setItem(25, ck_p)

        player.openInventory(inv)
    }
}
