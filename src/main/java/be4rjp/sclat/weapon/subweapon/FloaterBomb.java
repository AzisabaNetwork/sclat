
package be4rjp.sclat.weapon.subweapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.Sphere;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.weapon.Gear;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 *
 * @author Be4rJP
 */
public class FloaterBomb {
    
    public static void FloaterBombRunnable(Player player){
        BukkitRunnable task = new BukkitRunnable(){
            Player p = player;
            Vector p_vec;
            double x = 0;
            double z = 0;
            boolean block_check = false;
            int c = 0;
            Item drop;
            Snowball ball;
            boolean onground = false;
            boolean turn =false;
            @Override
            public void run(){
                try{
                    if(c == 0){
                        turn = false;
                        onground = player.isOnGround();
                        p_vec = p.getEyeLocation().getDirection();
                        if(!onground){
                            p_vec =p_vec.normalize().multiply(1.1);
                        }else{
                            p_vec =p_vec.normalize().multiply(0.95);
                        }
                        if(!DataMgr.getPlayerData(player).getIsBombRush())
                            p.setExp(p.getExp() - 0.47F);
                        ItemStack bom = new ItemStack(DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool()).clone();
                        ItemMeta bom_m = bom.getItemMeta();
                        bom_m.setLocalizedName(String.valueOf(Main.getNotDuplicateNumber()));
                        bom.setItemMeta(bom_m);
                        drop = p.getWorld().dropItem(p.getEyeLocation(), bom);
                        drop.setVelocity(p_vec.clone());
                        //雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                        ball = player.launchProjectile(Snowball.class);
                        ball.setVelocity(new Vector(0, 0, 0));
                        DataMgr.setSnowballIsHit(ball, false);

                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            PlayerConnection connection = ((CraftPlayer) o_player).getHandle().playerConnection;
                            connection.sendPacket(new PacketPlayOutEntityDestroy(ball.getEntityId()));
                        }
                        p_vec = p.getEyeLocation().getDirection();
                    }

                    if(!drop.isOnGround() && !(drop.getVelocity().getX() == 0 && drop.getVelocity().getZ() != 0) && !(drop.getVelocity().getX() != 0 && drop.getVelocity().getZ() == 0))
                        ball.setVelocity(drop.getVelocity());
                    if(c==9&&onground){
                        p_vec = p.getEyeLocation().getDirection().normalize().multiply(0.8);
                        drop.setVelocity(p_vec);
                        ball.setVelocity(p_vec);
                        turn = true;
                        player.getWorld().playSound(drop.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
                    }
                    if(c==12&&!onground){
                        p_vec = p.getEyeLocation().getDirection().normalize().multiply(0.6);
                        drop.setVelocity(p_vec);
                        ball.setVelocity(p_vec);
                        turn =true;
                        player.getWorld().playSound(drop.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
                    }

                    if(DataMgr.getSnowballIsHit(ball) || drop.isOnGround()){

                        //半径
                        double maxDist = 3;
                        if(!turn){
                            maxDist = 2;
                        }
                        //爆発ダメージ
                        double ExDamage = 4.0;
                        //if(onground) {
                        //    ExDamage = 4.0;
                        //}

                        //爆発音
                        player.getWorld().playSound(drop.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

                        //爆発エフェクト
                        Sclat.createInkExplosionEffect(drop.getLocation(), maxDist, 15, player);
    
                        //バリアをはじく
                        Sclat.repelBarrier(drop.getLocation(), maxDist, player);
                        
                        //塗る
                        for(int i = 0; i <= maxDist; i++){
                            List<Location> p_locs = Sphere.getSphere(drop.getLocation(), i, 20);
                            for(Location loc : p_locs){
                                PaintMgr.Paint(loc, p, false);
                            }
                        }


                        
                        //攻撃判定の処理
                        for (Entity as : player.getWorld().getEntities()) {
                            if (as.getLocation().distance(drop.getLocation()) <= maxDist) {
                                if (as instanceof ArmorStand) {
                                    if (as.getCustomName() != null) {
                                        try {
                                            if (as.getCustomName().equals("Kasa")) {
                                                KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
                                                if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() != DataMgr.getPlayerData(p).getTeam()) {
                                                    drop.remove();
                                                    cancel();
                                                }
                                            } else if (as.getCustomName().equals("SplashShield")) {
                                                SplashShieldData splashShieldData = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
                                                if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() != DataMgr.getPlayerData(p).getTeam()) {
                                                    drop.remove();
                                                    cancel();
                                                }
                                            }
                                        }catch (Exception e){}
                                    }
                                }
                            }
                        }

                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(!DataMgr.getPlayerData(target).isInMatch() || target.getWorld() != p.getWorld())
                                continue;
                            if (target.getLocation().distance(drop.getLocation()) <= maxDist) {
                                double damage = (maxDist - target.getLocation().distance(drop.getLocation())*0.7) * ExDamage * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP);
                                if(!turn){
                                    damage = damage * 0.9;
                                }
                                if(DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)){
                                    Sclat.giveDamage(player, target, damage, "subWeapon");

                                    //AntiNoDamageTime
                                    BukkitRunnable task = new BukkitRunnable(){
                                        Player p = target;
                                        @Override
                                        public void run(){
                                            target.setNoDamageTicks(0);
                                        }
                                    };
                                    task.runTaskLater(Main.getPlugin(), 1);


                                }
                            }
                        }

                        for(Entity as : player.getWorld().getEntities()){
                            if (as.getLocation().distance(drop.getLocation()) <= maxDist){
                                if(as instanceof ArmorStand){
                                    double damage = (maxDist - as.getLocation().distance(drop.getLocation())*0.7) * ExDamage * Gear.getGearInfluence(p, Gear.Type.SUB_SPEC_UP);
                                    if(!turn){
                                        damage = damage * 0.9;
                                    }
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, p);
                                    if(as.getCustomName() != null){
                                        if(as.getCustomName().equals("SplashShield") || as.getCustomName().equals("Kasa"))
                                            break;
                                    }
                                }
                            }
                        }
                        drop.remove();
                        cancel();
                        return;
                    }

                    //ボムの視認用エフェクト
                    for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                        if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_Bomb()){
                            if(o_player.getWorld() == drop.getLocation().getWorld()) {
                                if (o_player.getLocation().distanceSquared(drop.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                    Particle.DustOptions dustOptions = new Particle.DustOptions(DataMgr.getPlayerData(p).getTeam().getTeamColor().getBukkitColor(), 1);
                                    o_player.spawnParticle(Particle.REDSTONE, drop.getLocation(), 1, 0, 0, 0, 50, dustOptions);
                                }
                            }
                        }
                    }

                    c++;
                    x = drop.getLocation().getX();
                    z = drop.getLocation().getZ();


                    if(c > 500){
                        drop.remove();
                        cancel();
                        return;
                    }
                }catch(Exception e){
                    drop.remove();
                    cancel();
                    Main.getPlugin().getLogger().warning(e.getMessage());
                }
            }
        };
        
        BukkitRunnable cooltime = new BukkitRunnable(){
            @Override
            public void run(){
                DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
            }
        };
        cooltime.runTaskLater(Main.getPlugin(), 10);
                
        if(player.getExp() > 0.48 || DataMgr.getPlayerData(player).getIsBombRush())
            task.runTaskTimer(Main.getPlugin(), 0, 1);
        else{
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
        }
    }
}
