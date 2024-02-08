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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static be4rjp.sclat.Main.conf;

public class LootBox {
    private static int Firstprize =5000;
    private static int Secondprize =1000;
    private static int Thirdprize =500;
    private static int Fourthprize =100;
    private static int Fifthprize =10;
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
                        Sclat.sendMessage(ChatColor.GREEN + ClassName +"が重複したよ +" + Secondprize + "coin", MessageType.PLAYER, player);
                        PlayerStatusMgr.addMoney(player, Secondprize);
                    }
                }
                nextLootSeed += lootpro;
            }
        }
        if(nextLootSeed<0.5 && !isHit && LootSeed<0.5){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Firstprize);
            Sclat.sendMessage(ChatColor.GREEN + "「1等!」おめでとう! +" + Firstprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<5 && !isHit && LootSeed<5){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Secondprize);
            Sclat.sendMessage(ChatColor.GREEN + "「2等!」ラッキー! +" + Secondprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<20 && !isHit && LootSeed<20){
            isHit=true;
            PlayerStatusMgr.addMoney(player, Thirdprize);
            Sclat.sendMessage(ChatColor.GREEN + "「3等」 +" + Thirdprize + "coin", MessageType.PLAYER, player);
        }else if(nextLootSeed<50 && !isHit && LootSeed<50){
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
                ItemStack item = new ItemStack(DataMgr.getWeaponClass(ClassName).getMainWeapon().getWeaponIteamStack());
                ItemMeta itemm = item.getItemMeta();
                itemm.setDisplayName(ClassName);
                double Lootpro=DataMgr.getWeaponClass(ClassName).getMainWeapon().getLootpro();
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
        for(int i=1;i<=5;i+=1){
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta pmeta = paper.getItemMeta();
            List paperlores = new ArrayList();
            switch (i){
                case 1:
                    pmeta.setDisplayName("1等 "+Firstprize+"coin");
                    if(0.5 - nextLootpro>0) {
                        paperlores.add((0.5 - nextLootpro) + "％");
                        nextLootpro = 0.5;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 2:
                    pmeta.setDisplayName("2等 "+Secondprize+"coin");
                    if(5 - nextLootpro>0) {
                        paperlores.add((5 - nextLootpro) + "％");
                        nextLootpro = 5;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 3:
                    pmeta.setDisplayName("3等 "+Thirdprize+"coin");
                    if(20 - nextLootpro>0) {
                        paperlores.add((20 - nextLootpro) + "％");
                        nextLootpro = 20;
                    }else{
                        paperlores.add("0％");
                    }
                    break;
                case 4:
                    pmeta.setDisplayName("4等 "+Fourthprize+"coin");
                    if(50 - nextLootpro>0) {
                        paperlores.add((50 - nextLootpro) + "％");
                        nextLootpro = 50;
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
    public static void GiftWeapon(Player player){
        String ClassName = "バレンタインチョコ[義理]";
        if(!PlayerStatusMgr.haveWeapon(player, ClassName)){
            PlayerStatusMgr.addWeapon(player, ClassName);
            Sclat.sendMessage(ChatColor.GREEN + ClassName + "が手に入ったよ", MessageType.PLAYER, player);
        }else{
            Sclat.sendMessage(ChatColor.GREEN + ClassName +"はすでに持っているよ", MessageType.PLAYER, player);
        }
    }
}
