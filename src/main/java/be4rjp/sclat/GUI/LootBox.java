package be4rjp.sclat.GUI;

import be4rjp.sclat.MessageType;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static be4rjp.sclat.Main.conf;

public class LootBox {
    private static final int Firstprize =3000;
    private static final int Secondprize =1000;
    private static final int Thirdprize =500;
    private static final int Fourthprize =200;
    private static final int Fifthprize =50;
    public static void turnLootBox(Player player){
        if (PlayerStatusMgr.getTicket(player)<10){
            Sclat.sendMessage(ChatColor.RED + "ガチャを引くには1回10Ticket必要です", MessageType.PLAYER, player);
            return;
        }
        boolean isHit = false;
        double LootSeed = Math.random()*100;
        double nextLootSeed = 0;
        String name ="";
        for (String ClassName : conf.getClassConfig().getConfigurationSection("WeaponClass").getKeys(false)) {
            if(DataMgr.getWeaponClass(ClassName).getMainWeapon().getIslootbox()){
                double lootpro = DataMgr.getWeaponClass(ClassName).getMainWeapon().getLootpro();
                if(nextLootSeed <= LootSeed && LootSeed < lootpro + nextLootSeed){
                    isHit = true;
                    if(!PlayerStatusMgr.haveWeapon(player, ClassName)){
                        PlayerStatusMgr.addWeapon(player, ClassName);
                        Sclat.sendMessage(ChatColor.GREEN + ClassName + "が当たったよ、おめでとう！", MessageType.PLAYER, player);
                    }else{
                        Sclat.sendMessage(ChatColor.GREEN + ClassName +"が重複したよ +" + Firstprize + "coin", MessageType.PLAYER, player);
                        PlayerStatusMgr.addMoney(player, Firstprize);
                    }
                }
                nextLootSeed += lootpro;
            }
        }
        if(nextLootSeed<5 && !isHit && LootSeed<5){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Firstprize);
            Sclat.sendMessage(ChatColor.GREEN + "「1等!」おめでとう! +" + Firstprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<10 && !isHit && LootSeed<10){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Secondprize);
            Sclat.sendMessage(ChatColor.GREEN + "「2等!」ラッキー! +" + Secondprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<30 && !isHit && LootSeed<30){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Thirdprize);
            Sclat.sendMessage(ChatColor.GREEN + "「3等」 +" + Thirdprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<70 && !isHit && LootSeed<70){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Fourthprize);
            Sclat.sendMessage(ChatColor.GREEN + "「4等」 +" + Fourthprize + "coin", MessageType.PLAYER, player);
        }else {
            isHit=true;
            PlayerStatusMgr.addMoney(player, Fifthprize);
            Sclat.sendMessage(ChatColor.GREEN + "「5等」 +" + Fifthprize + "coin", MessageType.PLAYER, player);
        }
        PlayerStatusMgr.subTicket(player,10);
    }

    public static void LootBoxInfo(Player player){
        Inventory shooter = Bukkit.createInventory(null, 54, "ガチャ詳細");
        int slotnum=0;
        double nextLootpro=0;
        for (String ClassName : conf.getClassConfig().getConfigurationSection("WeaponClass").getKeys(false)) {
            if(DataMgr.getWeaponClass(ClassName).getMainWeapon().getIslootbox()) {
                double Lootpro=DataMgr.getWeaponClass(ClassName).getMainWeapon().getLootpro();
                if(Lootpro!=0) {
                    ItemStack item = new ItemStack(DataMgr.getWeaponClass(ClassName).getMainWeapon().getWeaponIteamStack());
                    ItemMeta itemm = item.getItemMeta();
                    itemm.setDisplayName(ClassName);
                    List lores = new ArrayList();
                    lores.add("§r§6SubWeapon : " + conf.getClassConfig().getString("WeaponClass." + ClassName + ".SubWeaponName"));
                    lores.add("§r§6SPWeapon  : " + conf.getClassConfig().getString("WeaponClass." + ClassName + ".SPWeaponName"));
                    lores.add("");
                    lores.add("§r§b : " + String.valueOf(Lootpro) + "％");
                    itemm.setLore(lores);
                    item.setItemMeta(itemm);
                    shooter.setItem(slotnum, item);
                    slotnum += 1;
                    nextLootpro += Lootpro;
                }
            }
        }
        for(int i=1;i<=5;i+=1){
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta pmeta = paper.getItemMeta();
            List paperlores = new ArrayList();
            switch (i){
                case 1:
                    pmeta.setDisplayName("1等 "+Firstprize+"coin");
                    if(5 - nextLootpro>0) {
                        paperlores.add((5 - nextLootpro) + "％");
                        nextLootpro = 5;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 2:
                    pmeta.setDisplayName("2等 "+Secondprize+"coin");
                    if(10 - nextLootpro>0) {
                        paperlores.add((10 - nextLootpro) + "％");
                        nextLootpro = 10;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 3:
                    pmeta.setDisplayName("3等 "+Thirdprize+"coin");
                    if(30 - nextLootpro>0) {
                        paperlores.add((30 - nextLootpro) + "％");
                        nextLootpro = 30;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 4:
                    pmeta.setDisplayName("4等 "+Fourthprize+"coin");
                    if(70 - nextLootpro>0) {
                        paperlores.add((70 - nextLootpro) + "％");
                        nextLootpro = 70;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 5:
                    pmeta.setDisplayName("5等 "+Fifthprize+"coin");
                    if(100 - nextLootpro>0) {
                        paperlores.add((100 - nextLootpro) + "％");
                        nextLootpro = 100;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
            }
            pmeta.setLore(paperlores);
            paper.setItemMeta(pmeta);
            shooter.setItem(slotnum, paper);
            slotnum += 1;
        }
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta pmeta = paper.getItemMeta();

        pmeta.setDisplayName("数値の表記に0.000001未満の誤差が生じることがあります、ご了承ください");
        paper.setItemMeta(pmeta);
        shooter.setItem(slotnum, paper);
        player.openInventory(shooter);
    }
    public static void GiftWeapon(Player player,String Weapon){
        String ClassName = Weapon;
        if(!PlayerStatusMgr.haveWeapon(player, ClassName)){
            PlayerStatusMgr.addWeapon(player, ClassName);
            Sclat.sendMessage(ChatColor.GREEN + ClassName + "が手に入ったよ", MessageType.PLAYER, player);
        }else{
            Sclat.sendMessage(ChatColor.GREEN + ClassName +"はすでに持っているよ", MessageType.PLAYER, player);
        }
    }
    public static void changeteam(Player player){
        if(DataMgr.getPlayerData(player).getTeam()==DataMgr.getPlayerData(player).getMatch().getTeam1()){
            DataMgr.getPlayerData(player).setTeam(DataMgr.getPlayerData(player).getMatch().getTeam0());
        }else{
            DataMgr.getPlayerData(player).setTeam(DataMgr.getPlayerData(player).getMatch().getTeam1());
        }
    }

    public  static void Giftbook(Player player){

        //操作説明本
        ItemStack termsBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) termsBook.getItemMeta();

        // 本のタイトルと著者を設定
        bookMeta.setTitle(ChatColor.DARK_GREEN + "アニバーサリー記念・裏話本");
        bookMeta.setAuthor(ChatColor.GRAY + "Sclat運営");

        // 利用規約の内容を追加
        bookMeta.addPage("まずはSclatをプレイしていただきありがとうございます。\n" +
                "ここではアップデート再開からの開発の裏話について語っていきたいと思います。\n");
        bookMeta.addPage(ChatColor.BOLD + "開発の方針的なもの編\n\n" +
                ChatColor.RESET + " プレイヤー同士が積極的に戦っていけるゲームを目指すと共に、眠りや麻痺のような行動不能にさせるようなデバフや強力なデバフ効果のあるものはできるだけ実装を避けていきたいと考えています。\n" +
                " 前者に関しては長い間芋環境だった戒めから、後者に関してはこのようなデバフ効果が多いとかなりストレスフルなゲームとなってしまうためです。");
        bookMeta.addPage("レスフルなゲームとなってしまうためです。");
        bookMeta.addPage(ChatColor.BOLD + "開発最初期編\n\n" +
                ChatColor.RESET + " ある程度方針が決まる前は環境上位の武器以外全て強化して行く可能性もありました。\n" +
                " 例えば短射程武器は相手を視界に入れているだけでダメージを与えるようにしたり、敵を十数秒間スタンさせるスキル、受けたダメージをそのまま反射など\n" +
                " 他にも漫画のラスボスみたいな能力をつける案がたく");
        bookMeta.addPage("さんありましたが、対戦ゲームとして成り立たない可能性が高いと判断して強い武器は弱体化して弱い武器は強化する方針になりました。");
        bookMeta.addPage(ChatColor.BOLD + "オリジナル武器種編\n\n" +
                ChatColor.RESET + " 最も最初に考案された武器種はリーラーで、この時は壁にビーコンをくっつけて、好きなタイミングで飛べるというものでした\n" +
                " 最初の新武器種開発の候補にハウンドとスワッパーがいましたが、この中で最も芋環境の対策になるものを考えた結果、高台の芋を制圧しやすく、高台で芋ることが困難なハウンドの制");
        bookMeta.addPage("作が決まりました。\n" +
                " リーラーが4番目の実装になったのは、3つ目の武器種を決める時に運営関係者にファンネル(今のドラグーン)とフックショット(今のリーラー)のどっちが好きか聞いて回ったところ圧倒的にファンネルの方がが人気だったからです");
        bookMeta.addPage(ChatColor.BOLD + "リワーク編\n\n" +
                ChatColor.RESET + " これまでいくつもの武器をリワークしてきましたが、それでも作ってきた中の一部で構想段階やほとんど完成した後で没になったものもあります。\n" +
                " その一つがマルチミサイルで使用者から発射され、着弾するまでターゲットに追尾し続けるように変更する予定でした。ですが、あまりにも躱すのが難しかっ");
        bookMeta.addPage("たため没になりました。\n" +
                " リッター5Gはチャージャーのリワークの構想を引き継いだもので、1回目ののリワークの案の一つに「専用スペシャル使用中のみ1確」というものがあり、それが紆余曲折あって今の形に落ち着きました。");
        bookMeta.addPage(ChatColor.BOLD + "没編\n\n" +
                ChatColor.RESET + " リワーク以外でも没になったものがあり、ブラインダーはそんな中でも最も完成に近い段階で没になりました。\n" +
                " 芋を攻略しやすくするために盲目を付与するサブとして考案されましたが、開発方針にそぐわなかったため実装されませんでした。\n" +
                " 具体的な性能は至近距離の相手には鈍足、中遠距離");
        bookMeta.addPage("の相手には盲目を付与するものでした。武器種によって効果時間が異なり、チャージャーやブラスターは効果時間が長く、バケツや傘はかなり短くなる予定でした。");
        bookMeta.addPage(ChatColor.BOLD + "クアドロアームズ編\n\n" +
                ChatColor.RESET + " クアドロアームズはリーラーとほぼ同時期に考案されました。\n" +
                " クアドロアームズの最初期は４つすべて同時に使える予定で、Redは敵をホーミングする予定でした。今思うとやめてよかったです。\n" +
                " ちなみにクアドロアームズのカラーは四神がモデルになっていてクアドロブルーは蒼龍、グリーンが玄武");
        bookMeta.addPage("、レッドが朱雀、ホワイトが白虎をイメージして作られています。");
        bookMeta.addPage(ChatColor.BOLD + "バグ編\n\n" +
                ChatColor.RESET + " 今まで様々なバグを直してきました。その中には細かすぎたり、報告されてないのでこっそり修正してパッチノートに書いてなかったりするのもありました。\n" +
                " 開発中のバグの中にはコイン増殖バグなんかもあり、これは実装前に発見して修正しましたが、実装した後にヤバいバグが見つかるのではと今でも内心ひやひ");
        bookMeta.addPage("やです。");
        bookMeta.addPage(ChatColor.BOLD + "アニバーサリー記念武器編\n\n" +
                ChatColor.RESET + " アニバーサリー記念でプレイヤーモチーフの武器を作ることにし、人気投票を行って早いうちに記入してもらったプレイヤーのカラーリングの武器を作りました。\n" +
                " 投票時期が遅かった方は申し訳ございません、全て何パターンか製作してその中から出来が良いモノを選んでいる都合上製作期間が足");
        bookMeta.addPage("りず作り切れませんでした。\n" +
                " 中身の性能は投票してもらった武器と同じなのでどちらの方が強いとかはまったくございません。");
        bookMeta.addPage(ChatColor.BOLD + "最後に\n\n" +
                ChatColor.RESET + " Sclatをプレイしていただきありがとうございます。\n" +
                " 度重なるリワークやバグでご迷惑をおかけしてきましたが、それでも遊んでくれる皆様には感謝の気持ちでいっぱいです。\n" +
                " プレイヤーの皆様に楽しんでプレイしてもらえるよう、これからも開発に努めて参ります。ですのでこれ");
        bookMeta.addPage("からも何卒宜しくお願い致します。");

        // 作成したBookMetaを設定
        termsBook.setItemMeta(bookMeta);

        // プレイヤーのインベントリをクリアし、利用規約の本をアイテムスロットに追加
        //player.getInventory().clear();
        player.getInventory().setItem(4, termsBook);
    }
}
