package be4rjp.sclat.weapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Buckler {
    public static void BucklerRunnable(Player player){
        BukkitRunnable delay3 = new BukkitRunnable(){
            Player p = player;
            int Etime = 80;
            int Ctime = 120;
            boolean bk_recharge=true;

            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);
                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }
                if (data.getIsSneaking() && bk_recharge == true && player.getGameMode().equals(GameMode.ADVENTURE) && p.getInventory().getItemInMainHand().getType().equals(data.getWeaponClass().getMainWeapon().getWeaponIteamStack().getType())) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Etime,0));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,Ctime,0));
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8F, 0.8F);
                    bk_recharge=false;
                    BukkitRunnable healtask = new BukkitRunnable() {//クールタイムを管理しています
                        @Override
                        public void run() {
                            bk_recharge = true;
                        }
                    };
                    healtask.runTaskLater(Main.getPlugin(), Ctime);
                }
            }
        };
        delay3.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}
