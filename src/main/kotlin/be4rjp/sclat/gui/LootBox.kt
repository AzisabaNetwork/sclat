package be4rjp.sclat.gui

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.manager.PlayerStatusMgr.addMoney
import be4rjp.sclat.manager.PlayerStatusMgr.addWeapon
import be4rjp.sclat.manager.PlayerStatusMgr.getTicket
import be4rjp.sclat.manager.PlayerStatusMgr.haveWeapon
import be4rjp.sclat.manager.PlayerStatusMgr.subTicket
import net.azisaba.sclat.core.enums.MessageType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

object LootBox {
    private const val FIRST_PRIZE = 3000
    private const val SECOND_PRIZE = 1000
    private const val THIRD_PRIZE = 500
    private const val FOURTH_PRIZE = 200
    private const val FIFTH_PRIZE = 50

    fun turnLootBox(player: Player) {
        if (getTicket(player) < 10) {
            sendMessage(ChatColor.RED.toString() + "ガチャを引くには1回10Ticket必要です", MessageType.PLAYER, player)
            return
        }
        var isHit = false
        val lootSeed = Math.random() * 100
        var nextLootSeed = 0.0
        ""
        for (ClassName in Sclat.conf!!
            .classConfig!!
            .getConfigurationSection("WeaponClass")!!
            .getKeys(false)) {
            if (getWeaponClass(ClassName)!!.mainWeapon!!.islootbox) {
                val lootpro = getWeaponClass(ClassName)!!.mainWeapon!!.lootpro
                if (nextLootSeed <= lootSeed && lootSeed < lootpro + nextLootSeed) {
                    isHit = true
                    if (!haveWeapon(player, ClassName)) {
                        addWeapon(player, ClassName)
                        sendMessage(
                            ChatColor.GREEN.toString() + ClassName + "が当たったよ、おめでとう！",
                            MessageType.PLAYER,
                            player,
                        )
                    } else {
                        sendMessage(
                            ChatColor.GREEN.toString() + ClassName + "が重複したよ +" + FIRST_PRIZE + "coin",
                            MessageType.PLAYER,
                            player,
                        )
                        addMoney(player, FIRST_PRIZE)
                    }
                }
                nextLootSeed += lootpro
            }
        }
        if (nextLootSeed < 5 && !isHit && lootSeed < 5) {
            isHit = true
            addMoney(player, FIRST_PRIZE)
            sendMessage(
                ChatColor.GREEN.toString() + "「1等!」おめでとう! +" + FIRST_PRIZE + "coin",
                MessageType.PLAYER,
                player,
            )
        } else if (nextLootSeed < 10 && !isHit && lootSeed < 10) {
            isHit = true
            addMoney(player, SECOND_PRIZE)
            sendMessage(
                ChatColor.GREEN.toString() + "「2等!」ラッキー! +" + SECOND_PRIZE + "coin",
                MessageType.PLAYER,
                player,
            )
        } else if (nextLootSeed < 30 && !isHit && lootSeed < 30) {
            isHit = true
            addMoney(player, THIRD_PRIZE)
            sendMessage(ChatColor.GREEN.toString() + "「3等」 +" + THIRD_PRIZE + "coin", MessageType.PLAYER, player)
        } else if (nextLootSeed < 70 && !isHit && lootSeed < 70) {
            isHit = true
            addMoney(player, FOURTH_PRIZE)
            sendMessage(ChatColor.GREEN.toString() + "「4等」 +" + FOURTH_PRIZE + "coin", MessageType.PLAYER, player)
        } else {
            isHit = true
            addMoney(player, FIFTH_PRIZE)
            sendMessage(ChatColor.GREEN.toString() + "「5等」 +" + FIFTH_PRIZE + "coin", MessageType.PLAYER, player)
        }
        subTicket(player, 10)
    }

    fun lootBoxInfo(player: Player) {
        val shooter = Bukkit.createInventory(null, 54, "ガチャ詳細")
        var slotnum = 0
        var nextLootpro = 0.0
        for (ClassName in Sclat.conf!!
            .classConfig!!
            .getConfigurationSection("WeaponClass")!!
            .getKeys(false)) {
            if (getWeaponClass(ClassName)!!.mainWeapon!!.islootbox) {
                val lootpro = getWeaponClass(ClassName)!!.mainWeapon!!.lootpro
                if (lootpro != 0.0) {
                    val item = ItemStack(getWeaponClass(ClassName)!!.mainWeapon!!.weaponIteamStack!!)
                    val itemm = item.itemMeta
                    itemm!!.setDisplayName(ClassName)
                    val lores: MutableList<String?> = ArrayList()
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
                    lores.add("")
                    lores.add("§r§b : $lootpro％")
                    itemm.lore = lores
                    item.itemMeta = itemm
                    shooter.setItem(slotnum, item)
                    slotnum += 1
                    nextLootpro += lootpro
                }
            }
        }
        var i = 1
        while (i <= 5) {
            val paper = ItemStack(Material.PAPER)
            val pmeta = paper.itemMeta
            val paperlores: MutableList<String?> = ArrayList()
            when (i) {
                1 -> {
                    pmeta!!.setDisplayName("1等 " + FIRST_PRIZE + "coin")
                    if (5 - nextLootpro > 0) {
                        paperlores.add((5 - nextLootpro).toString() + "％")
                        nextLootpro = 5.0
                    } else {
                        paperlores.add("0％")
                    }
                }

                2 -> {
                    pmeta!!.setDisplayName("2等 " + SECOND_PRIZE + "coin")
                    if (10 - nextLootpro > 0) {
                        paperlores.add((10 - nextLootpro).toString() + "％")
                        nextLootpro = 10.0
                    } else {
                        paperlores.add("0％")
                    }
                }

                3 -> {
                    pmeta!!.setDisplayName("3等 " + THIRD_PRIZE + "coin")
                    if (30 - nextLootpro > 0) {
                        paperlores.add((30 - nextLootpro).toString() + "％")
                        nextLootpro = 30.0
                    } else {
                        paperlores.add("0％")
                    }
                }

                4 -> {
                    pmeta!!.setDisplayName("4等 " + FOURTH_PRIZE + "coin")
                    if (70 - nextLootpro > 0) {
                        paperlores.add((70 - nextLootpro).toString() + "％")
                        nextLootpro = 70.0
                    } else {
                        paperlores.add("0％")
                    }
                }

                5 -> {
                    pmeta!!.setDisplayName("5等 " + FIFTH_PRIZE + "coin")
                    if (100 - nextLootpro > 0) {
                        paperlores.add((100 - nextLootpro).toString() + "％")
                        nextLootpro = 100.0
                    } else {
                        paperlores.add("0％")
                    }
                }
            }
            pmeta!!.lore = paperlores
            paper.itemMeta = pmeta
            shooter.setItem(slotnum, paper)
            slotnum += 1
            i += 1
        }
        val paper = ItemStack(Material.PAPER)
        val pmeta = paper.itemMeta

        pmeta!!.setDisplayName("数値の表記に0.000001未満の誤差が生じることがあります、ご了承ください")
        paper.itemMeta = pmeta
        shooter.setItem(slotnum, paper)
        player.openInventory(shooter)
    }

    fun giftWeapon(
        player: Player,
        Weapon: String?,
    ) {
        if (!haveWeapon(player, Weapon)) {
            addWeapon(player, Weapon)
            sendMessage(ChatColor.GREEN.toString() + Weapon + "が手に入ったよ", MessageType.PLAYER, player)
        } else {
            sendMessage(ChatColor.GREEN.toString() + Weapon + "はすでに持っているよ", MessageType.PLAYER, player)
        }
    }

    fun changeteam(player: Player?) {
        if (getPlayerData(player)!!.team == getPlayerData(player)!!.match!!.team1) {
            getPlayerData(player)!!.team = getPlayerData(player)!!.match!!.team0
        } else {
            getPlayerData(player)!!.team = getPlayerData(player)!!.match!!.team1
        }
    }

    fun giftbook(player: Player) {
        // 操作説明本

        val termsBook = ItemStack(Material.WRITTEN_BOOK)
        val bookMeta = termsBook.itemMeta as BookMeta?

        // 本のタイトルと著者を設定
        bookMeta!!.title = ChatColor.DARK_GREEN.toString() + "アニバーサリー記念・裏話本"
        bookMeta.author = ChatColor.GRAY.toString() + "Sclat運営"

        // 利用規約の内容を追加
        bookMeta.addPage("まずはSclatをプレイしていただきありがとうございます。\n" + "ここではアップデート再開からの開発の裏話について語っていきたいと思います。\n")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "開発の方針的なもの編\n\n" + ChatColor.RESET +
                    " プレイヤー同士が積極的に戦っていけるゲームを目指すと共に、眠りや麻痺のような行動不能にさせるようなデバフや強力なデバフ効果のあるものはできるだけ実装を避けていきたいと考えています。\n" +
                    " 前者に関しては長い間芋環境だった戒めから、後者に関してはこのようなデバフ効果が多いとかなりストレスフルなゲームとなってしまうためです。"
            ),
        )
        bookMeta.addPage("レスフルなゲームとなってしまうためです。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "開発最初期編\n\n" + ChatColor.RESET + " ある程度方針が決まる前は環境上位の武器以外全て強化して行く可能性もありました。\n" +
                    " 例えば短射程武器は相手を視界に入れているだけでダメージを与えるようにしたり、敵を十数秒間スタンさせるスキル、受けたダメージをそのまま反射など\n" +
                    " 他にも漫画のラスボスみたいな能力をつける案がたく"
            ),
        )
        bookMeta.addPage("さんありましたが、対戦ゲームとして成り立たない可能性が高いと判断して強い武器は弱体化して弱い武器は強化する方針になりました。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "オリジナル武器種編\n\n" + ChatColor.RESET +
                    " 最も最初に考案された武器種はリーラーで、この時は壁にビーコンをくっつけて、好きなタイミングで飛べるというものでした\n" +
                    " 最初の新武器種開発の候補にハウンドとスワッパーがいましたが、この中で最も芋環境の対策になるものを考えた結果、高台の芋を制圧しやすく、高台で芋ることが困難なハウンドの制"
            ),
        )
        bookMeta.addPage(
            "作が決まりました。\n" +
                " リーラーが4番目の実装になったのは、3つ目の武器種を決める時に運営関係者にファンネル(今のドラグーン)とフックショット(今のリーラー)のどっちが好きか聞いて回ったところ圧倒的にファンネルの方がが人気だったからです",
        )
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "リワーク編\n\n" + ChatColor.RESET +
                    " これまでいくつもの武器をリワークしてきましたが、それでも作ってきた中の一部で構想段階やほとんど完成した後で没になったものもあります。\n" +
                    " その一つがマルチミサイルで使用者から発射され、着弾するまでターゲットに追尾し続けるように変更する予定でした。ですが、あまりにも躱すのが難しかっ"
            ),
        )
        bookMeta.addPage(
            "たため没になりました。\n" +
                " リッター5Gはチャージャーのリワークの構想を引き継いだもので、1回目ののリワークの案の一つに「専用スペシャル使用中のみ1確」というものがあり、それが紆余曲折あって今の形に落ち着きました。",
        )
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "没編\n\n" + ChatColor.RESET +
                    " リワーク以外でも没になったものがあり、ブラインダーはそんな中でも最も完成に近い段階で没になりました。\n" +
                    " 芋を攻略しやすくするために盲目を付与するサブとして考案されましたが、開発方針にそぐわなかったため実装されませんでした。\n" + " 具体的な性能は至近距離の相手には鈍足、中遠距離"
            ),
        )
        bookMeta.addPage("の相手には盲目を付与するものでした。武器種によって効果時間が異なり、チャージャーやブラスターは効果時間が長く、バケツや傘はかなり短くなる予定でした。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "クアドロアームズ編\n\n" + ChatColor.RESET + " クアドロアームズはリーラーとほぼ同時期に考案されました。\n" +
                    " クアドロアームズの最初期は４つすべて同時に使える予定で、Redは敵をホーミングする予定でした。今思うとやめてよかったです。\n" +
                    " ちなみにクアドロアームズのカラーは四神がモデルになっていてクアドロブルーは蒼龍、グリーンが玄武"
            ),
        )
        bookMeta.addPage("、レッドが朱雀、ホワイトが白虎をイメージして作られています。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "バグ編\n\n" + ChatColor.RESET +
                    " 今まで様々なバグを直してきました。その中には細かすぎたり、報告されてないのでこっそり修正してパッチノートに書いてなかったりするのもありました。\n" +
                    " 開発中のバグの中にはコイン増殖バグなんかもあり、これは実装前に発見して修正しましたが、実装した後にヤバいバグが見つかるのではと今でも内心ひやひ"
            ),
        )
        bookMeta.addPage("やです。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "アニバーサリー記念武器編\n\n" + ChatColor.RESET +
                    " アニバーサリー記念でプレイヤーモチーフの武器を作ることにし、人気投票を行って早いうちに記入してもらったプレイヤーのカラーリングの武器を作りました。\n" +
                    " 投票時期が遅かった方は申し訳ございません、全て何パターンか製作してその中から出来が良いモノを選んでいる都合上製作期間が足"
            ),
        )
        bookMeta.addPage("りず作り切れませんでした。\n" + " 中身の性能は投票してもらった武器と同じなのでどちらの方が強いとかはまったくございません。")
        bookMeta.addPage(
            (
                ChatColor.BOLD.toString() + "最後に\n\n" + ChatColor.RESET + " Sclatをプレイしていただきありがとうございます。\n" +
                    " 度重なるリワークやバグでご迷惑をおかけしてきましたが、それでも遊んでくれる皆様には感謝の気持ちでいっぱいです。\n" +
                    " プレイヤーの皆様に楽しんでプレイしてもらえるよう、これからも開発に努めて参ります。ですのでこれ"
            ),
        )
        bookMeta.addPage("からも何卒宜しくお願い致します。")

        // 作成したBookMetaを設定
        termsBook.itemMeta = bookMeta

        // プレイヤーのインベントリをクリアし、利用規約の本をアイテムスロットに追加
        // player.getInventory().clear();
        player.inventory.setItem(4, termsBook)
    }
}
