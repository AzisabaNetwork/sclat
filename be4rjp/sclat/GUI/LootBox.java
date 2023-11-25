package be4rjp.sclat.GUI;

import be4rjp.sclat.MessageType;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static be4rjp.sclat.Main.conf;

public class LootBox {
    public static void turnLootBox(Player player){
        int Firstprize =5000;
        int Secondprize =2000;
        int Thirdprize =1000;
        int Fourthprize =100;
        int Fifthprize =10;
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
                        Sclat.sendMessage(ChatColor.GREEN + ClassName +"が重複したよ　+" + Firstprize + "coin", MessageType.PLAYER, player);
                        PlayerStatusMgr.addMoney(player, Firstprize);
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
    }
}
