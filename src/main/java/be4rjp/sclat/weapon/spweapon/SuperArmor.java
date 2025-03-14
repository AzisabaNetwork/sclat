
package be4rjp.sclat.weapon.spweapon;

import be4rjp.sclat.Main;
import static be4rjp.sclat.Main.conf;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.SPWeaponMgr;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Be4rJP
 */
public class SuperArmor {
    public static void setArmor(Player player, double armor, long delay, boolean effect){
        
        if(effect){
            if(armor != 60){
                DataMgr.getPlayerData(player).setIsUsingSP(true);
                SPWeaponMgr.setSPCoolTimeAnimation(player, (int)delay);
                if (armor == 30) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0));
                }
            }
        }
        
        PlayerData data = DataMgr.getPlayerData(player);
        if(armor > data.getArmor())
            data.setArmor(armor);
        
        //エフェクト
        BukkitRunnable effect_r = new BukkitRunnable(){
            @Override
            public void run(){
                if(!data.isInMatch() || !player.getGameMode().equals(GameMode.ADVENTURE)){
                    if(armor != 60 || armor != 1 )
                        DataMgr.getPlayerData(player).setIsUsingSP(false);
                    cancel();
                }
                for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_SPWeapon() && !o_player.equals(player)){
                        if(o_player.getWorld() == player.getWorld()){
                            if(o_player.getLocation().distanceSquared(player.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED){
                                Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                o_player.spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 5, 0.5, 0.4, 0.5, 5, dustOptions);
                            }
                        }
                    }
                }
                if(data.getArmor() <= 0){
                    player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 3.5F, 1.8F);
                    player.sendMessage("§c§l！ アーマーが破壊された ！");
                    cancel();
                }
            }
        };
        if(effect)
            effect_r.runTaskTimer(Main.getPlugin(), 0, 1);
        
        BukkitRunnable task = new BukkitRunnable(){
            @Override
            public void run(){
                data.setArmor(0);
                if(effect){
                    effect_r.cancel();
                    DataMgr.getPlayerData(player).setIsUsingSP(false);
                    //player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                }
            }
        };
        task.runTaskLater(Main.getPlugin(), delay);
        
    }
    public static void setRegeneArmor(Player player, double armor, long delay,double regene ,boolean effect){

        if(effect){
                DataMgr.getPlayerData(player).setIsUsingSP(true);
                SPWeaponMgr.setSPCoolTimeAnimation(player, (int)delay);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 64, 0));
        }

        PlayerData data = DataMgr.getPlayerData(player);
        if(armor > data.getArmor())
            data.setArmor(regene);

        BukkitRunnable rgene_r = new BukkitRunnable(){
            int c = 0;
            int regenearmor=1;
            boolean canregene =true;
            double beforeArmor =0.0;
            @Override
            public void run(){
                if(!data.isInMatch() || !player.getGameMode().equals(GameMode.ADVENTURE)){
                    DataMgr.getPlayerData(player).setIsUsingSP(false);
                    cancel();
                }
                if((c>=1 && beforeArmor > data.getArmor())){
                    canregene = false;
                }
                if(canregene){
                    regenearmor=1+c/4;
                    if(data.getArmor() + regenearmor<armor) {
                        data.setArmor(data.getArmor() + regenearmor);
                    }else{
                        data.setArmor(armor);
                        canregene = false;
                    }
                }
                beforeArmor = data.getArmor();
                if(data.getArmor() <= 0){
                    cancel();
                }
                c++;
            }
        };
        rgene_r.runTaskTimer(Main.getPlugin(), 0, 2);
        //エフェクト
        BukkitRunnable effect_r = new BukkitRunnable(){
            @Override
            public void run(){
                if(!data.isInMatch() || !player.getGameMode().equals(GameMode.ADVENTURE)){
                    DataMgr.getPlayerData(player).setIsUsingSP(false);
                    cancel();
                }
                for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_SPWeapon() && !o_player.equals(player)){
                        if(o_player.getWorld() == player.getWorld()){
                            if(o_player.getLocation().distanceSquared(player.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED){
                                Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                o_player.spawnParticle(Particle.REDSTONE, player.getEyeLocation(), 5, 0.5, 0.4, 0.5, 5, dustOptions);
                            }
                        }
                    }
                }
                if(data.getArmor() <= 0){
                    player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 3.5F, 1.8F);
                    player.sendMessage("§c§l！ アーマーが破壊された ！");
                    cancel();
                }
            }
        };
        if(effect)
            effect_r.runTaskTimer(Main.getPlugin(), 0, 1);

        BukkitRunnable task = new BukkitRunnable(){
            @Override
            public void run(){
                data.setArmor(0);
                if(effect){
                    effect_r.cancel();
                    DataMgr.getPlayerData(player).setIsUsingSP(false);
                    //player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 2);
                }
            }
        };
        task.runTaskLater(Main.getPlugin(), delay);

    }
}
