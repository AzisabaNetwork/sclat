package be4rjp.sclat.weapon.spweapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.Sphere;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.manager.SPWeaponMgr;
import be4rjp.sclat.manager.WeaponClassMgr;
import be4rjp.sclat.raytrace.BoundingBox;
import be4rjp.sclat.raytrace.RayTrace;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LitterFiveG {
    private static HashMap<Player, Integer> Hash_charge = new HashMap<>();
    private static HashMap<Player, Integer> Hash_cps = new HashMap<>();
    private static int max_charge = 44;
    public static void setLitterFiveG(Player player){
        DataMgr.getPlayerData(player).setIsUsingSP(true);
        DataMgr.getPlayerData(player).setIsUsingSS(true);
        SPWeaponMgr.setSPCoolTimeAnimation(player, 280);
        if(Hash_charge.containsKey(player)) {
            Hash_charge.replace(player,0);
            Hash_cps.replace(player,1);
        }else{
            Hash_charge.put(player,0);
            Hash_cps.put(player,1);
        }
        BukkitRunnable it = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                player.getInventory().clear();
                player.updateInventory();
                ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("右クリックで射撃!");
                item.setItemMeta(meta);
                for (int count = 0; count < 9; count++){
                    player.getInventory().setItem(count, item);
                }
                player.updateInventory();
                charge_bar(player);
            }
        };
        it.runTaskLater(Main.getPlugin(), 2);

        BukkitRunnable task = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                if(DataMgr.getPlayerData(p).isInMatch()){
                    DataMgr.getPlayerData(p).setIsUsingSP(false);
                    DataMgr.getPlayerData(p).setIsUsingSS(false);
                    player.getInventory().clear();
                    WeaponClassMgr.setWeaponClass(p);
                }
            }
        };
        task.runTaskLater(Main.getPlugin(), 280);
    }
    public static void charge_bar(Player player){
        BossBar bar = Main.getPlugin().getServer().createBossBar(DataMgr.getPlayerData(player).getTeam().getTeamColor().getColorCode() + "§cCharge", BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG);
        bar.setProgress(0);
        bar.addPlayer(player);

        BukkitRunnable overheat_anime = new BukkitRunnable(){
            Player p = player;
            boolean bell = false;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);
                if(Hash_charge.containsKey(p)){
                    if( Hash_charge.get(p)< max_charge) {
                        bar.setProgress((double)Hash_charge.get(p) / max_charge);
                        if (!bar.getPlayers().contains(p)) {
                            bar.addPlayer(p);
                        }
                        if(bell){
                            bell = false;
                        }
                    }else {
                        bar.setProgress(1);
                        if (!bar.getPlayers().contains(p)) {
                            bar.addPlayer(p);
                        }
                        if(!bell){
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RETURN, 2.2F, 1.3F);
                            bell = true;
                        }
                    }
                    if(!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline()){
                        bar.removeAll();
                        cancel();
                    }
                    if(!DataMgr.getPlayerData(p).getIsUsingSS()){
                        bar.removeAll();
                        cancel();
                    }
                    if(Hash_charge.get(p) < max_charge) {
                        Hash_charge.replace(p,Hash_charge.get(p)+Hash_cps.get(p));
                    }
                    if(player.getInventory().getItemInMainHand().getItemMeta() == null){
                        Hash_charge.replace(p,0);
                        Hash_cps.replace(p,1);
                    }
                    RayTrace rayTrace = new RayTrace(p.getEyeLocation().toVector(),p.getEyeLocation().getDirection());
                    double range = Hash_charge.get(p);
                    if(range>max_charge){
                        range=max_charge;
                    }
                    ArrayList<Vector> positions = rayTrace.traverse((int)(range * 1.8),0.7);
                    check : for(int i = 0; i < positions.size();i++){
                        Location position = positions.get(i).toLocation(p.getLocation().getWorld());
                        if(!position.getBlock().getType().equals(Material.AIR)){
                            break check;
                        }
                        if(i % 5 == 0){
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if (target.equals(p) || !DataMgr.getPlayerData(target).getSettings().ShowEffect_ChargerLine())
                                    continue;
                                if (target.getWorld() == p.getWorld()) {
                                    if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                        Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                        target.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 10, dustOptions);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        overheat_anime.runTaskTimer(Main.getPlugin(), 0, 2);
    }

    public static void Shoot_LitterFiveG(Player player){
        if(player.getGameMode() == GameMode.SPECTATOR || !DataMgr.getPlayerData(player).getIsUsingSP()) return;
        int range = Hash_charge.get(player);
        double damage = Hash_charge.get(player)/5;
        //半径
        double maxDist = 2;
        if(range>=max_charge){
            range=max_charge;
            damage =22.1;
            maxDist = 4;
            Hash_cps.replace(player,Hash_cps.get(player)+1);
        }else{
            Hash_cps.replace(player,1);
        }
        int reach = (int)(range*1.8);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 5);
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse((int)(reach), 0.2);
        Hash_charge.replace(player,0);

        loop : for(int i = 0; i < positions.size();i++){

            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if(!block.getType().equals(Material.AIR)){
                for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if (DataMgr.getPlayerData(o_player).getSettings().ShowEffect_MainWeaponInk()) {
                        //爆発音
                        player.getWorld().playSound(position, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

                        //爆発エフェクト
                        Sclat.createInkExplosionEffect(position, maxDist, 25, player);

                        //塗る
                        for (int in = 0; in <= maxDist - 1; in++) {
                            List<Location> p_locs = Sphere.getSphere(position, in, 20);
                            for (Location loc : p_locs) {
                                PaintMgr.Paint(loc, player, false);
                                PaintMgr.PaintHightestBlock(loc, player, false, false);
                            }
                        }
                    }
                }
                break loop;
            }
            //PaintMgr.PaintHightestBlock(position, player, false, true);
                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if (!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
                        continue;
                    if (target.getWorld() == position.getWorld()) {
                        if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                            org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                            target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
                        }
                    }
                }


            double maxDistSquad = 4 /* 2*2 */;
            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                if(!DataMgr.getPlayerData(target).isInMatch())
                    continue;
                if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                    if(target.getLocation().distanceSquared(position) <= maxDistSquad){
                        if(rayTrace.intersects(new BoundingBox((Entity)target), (int)(reach), 0.05)){
                            boolean death;
                            death = Sclat.giveDamage(player, target, damage, "spWeapon");
                            if(death) {
                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                player.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                            }
                            else {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2F, 1.3F);
                            }

                            //AntiNoDamageTime
                            BukkitRunnable task = new BukkitRunnable(){
                                Player p = target;
                                @Override
                                public void run(){
                                    target.setNoDamageTicks(0);
                                }
                            };
                            task.runTaskLater(Main.getPlugin(), 1);
                            break loop;
                        }
                    }
                }
            }

            for(Entity as : player.getWorld().getEntities()){
                if (as instanceof ArmorStand){
                    if(as.getLocation().distanceSquared(position) <= maxDistSquad){
                        if(rayTrace.intersects(new BoundingBox((Entity)as), (int)(reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)), 0.05)){
                            if(as.getCustomName() != null){
                                if(as.getCustomName().equals("SplashShield")){
                                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand)as);
                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, player);
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                }else if(as.getCustomName().equals("Kasa")){
                                    KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand)as);
                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, player);
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                }else{
                                    if(Sclat.isNumber(as.getCustomName()))
                                        if(!as.getCustomName().equals("21") && !as.getCustomName().equals("100"))
                                            if(((ArmorStand) as).isVisible())
                                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                    break loop;
                                }
                            }
                            ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                        }
                    }
                }
            }
        }

        BukkitRunnable task2 = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                DataMgr.getPlayerData(p).setCanUseSubWeapon(true);
            }
        };
        task2.runTaskLater(Main.getPlugin(), 8);
    }
}
