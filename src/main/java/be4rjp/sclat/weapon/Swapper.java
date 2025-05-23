package be4rjp.sclat.weapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.WeaponClassMgr;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Swapper {
    public static void SwapperRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            boolean sw_recharge=true;

            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);

                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }
                //スワッパ―系
                if(data.getWeaponClass().getMainWeapon().getIsSwap()){
                    if (data.getIsSneaking() && sw_recharge == true && p.getInventory().getItemInMainHand().getType().equals(data.getWeaponClass().getMainWeapon().getWeaponIteamStack().getType())) {
                        data.setStoprun(true);
                        player.getInventory().clear();
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4F, 1.5F);
                        sw_recharge=false;
                        BukkitRunnable swapset = new BukkitRunnable() {//チャージャーとローラーのみ対応
                                @Override
                                public void run() {
                                    String swapname = data.getWeaponClass().getMainWeapon().getSwap();
                                    data.setStoprun(false);
                                    data.setWeaponClass(DataMgr.getWeaponClass(swapname));
                                    data.setCanRollerShoot(true);
                                    DataMgr.getPlayerData(p).setIsUsingManeuver(false);
                                    if(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getWeaponType().equals("Shooter")){
                                        if(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getSlidingShootTick()>1) {
                                            DataMgr.getPlayerData(p).setIsUsingManeuver(true);
                                        }else{
                                            Shooter.ShooterRunnable(p);
                                        }
                                    }
                                    WeaponClassMgr.setWeaponClass(p);
                                }
                        };
                        BukkitRunnable task = new BukkitRunnable() {//クールタイムを管理しています
                                @Override
                                public void run() {
                                    sw_recharge = true;
                                }
                            };
                        swapset.runTaskLater(Main.getPlugin(), 5);
                        task.runTaskLater(Main.getPlugin(), 30);
                    }
                }
                //loc = ploc;
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, 1);
    }

}
