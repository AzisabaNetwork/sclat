package be4rjp.sclat.weapon;

import be4rjp.dadadachecker.ClickType;
import be4rjp.sclat.*;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.List;

public class Hound {
    public static void HoundRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);

                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }

                if(!data.getIsUsingManeuver() && data.getCanShoot()){
                    ClickType clickType = Main.dadadaCheckerAPI.getPlayerClickType(player);
                    if((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) && data.isInMatch() && data.getCanRollerShoot()){
                        Hound.Shoot(p);
                        data.setCanRollerShoot(false);
                        HoundCooltime(p);
                    }
                }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootTick());
    }
    public static void HoundCooltime(Player player){
        PlayerData data = DataMgr.getPlayerData(player);
        BukkitRunnable delay1 = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(player);
                data.setCanRollerShoot(true);
            }
        };
        delay1.runTaskLater(Main.getPlugin(), data.getWeaponClass().getMainWeapon().getCoolTime());
    }
    public static void HoundEXRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            PlayerData data = DataMgr.getPlayerData(p);
            @Override
            public void run(){
                try {
                    if (!data.isInMatch() || !p.isOnline()) {
                        data.setIsSliding(false);
                        cancel();
                        return;
                    }
                    if (!data.getIsSneaking() && data.getIsSliding()) {
                        data.setIsSneaking(false);
                        data.setIsSliding(false);
                    }
                }catch(Exception e){
                    cancel();
                }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static void Shoot(Player player){
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = DataMgr.getPlayerData(player);
        Vector pVector = player.getEyeLocation().getDirection();
        Vector vec = new Vector(pVector.getX(), 0, pVector.getZ()).normalize().multiply(data.getWeaponClass().getMainWeapon().getShootSpeed());
        BukkitRunnable task = new BukkitRunnable() {
            Vector aVec = vec.clone();
            Location bloc;
            int i = 0;
            ArmorStand as1;
            double heightdiff = 0;
            //半径
            double maxDist = 1;
            double saveY =0;
            int explodetick = data.getWeaponClass().getMainWeapon().getRollerShootQuantity();
            float climbSpeed = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getRollerNeedInk();

            @Override
            public void run() {
                try {
                    if(i == 0){
                        saveY =player.getLocation().getY();
                        player.setExp(player.getExp() - (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));

                        as1 = player.getWorld().spawn(player.getLocation(), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setSmall(true);
                        });
                        GlowingAPI.setGlowing(as1, player, true);
                        data.setArmorlist(as1);
                    }

                    Location aloc = as1.getLocation().add(0, -0.4, 0);
                    aloc.setYaw(90);
                    Location as1l = as1.getLocation();

                    if(i >= 5 ){
                        if((bloc.getX() == as1l.getX() || bloc.getZ() == as1l.getZ())) {
                            if(EntityWallHit(as1,pVector)) {
                                aVec = new Vector(pVector.getX() * 0.03, climbSpeed, pVector.getZ() * 0.03);
                            }else{
                                aVec = new Vector(vec.getX(), -0.4, vec.getZ());
                            }
                            //壁を塗る
                            for(int i = 0; i <= 1; i++){
                                List<Location> p_locs = Sphere.getSphere(as1l, i, 30);
                                for(Location loc : p_locs){
                                    PaintMgr.Paint(loc, player, false);
                                }
                            }
                        }else if(aVec.getY()>0 && !EntityWallHit(as1,pVector)){
                            aVec = new Vector(vec.getX(), 0, vec.getZ());
                        }else if(!as1.isOnGround()){
                            aVec = new Vector(vec.getX(), -0.4, vec.getZ());
                        }
                    }

                    as1.setVelocity(aVec);

                    PaintMgr.PaintHightestBlock(as1l, player, false, true);

                    bloc = as1l.clone();

                    if(i >= explodetick-20 && i <= explodetick-10){
                        if(i % 2 == 0)
                            player.getWorld().playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.6F);
                    }

                    //エフェクト
                    if(i % 2 == 0){
                        org.bukkit.block.data.BlockData bd = data.getTeam().getTeamColor().getWool().createBlockData();
                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb())
                                if(target.getWorld() == player.getWorld())
                                    if(target.getLocation().distanceSquared(as1l) < Main.PARTICLE_RENDER_DISTANCE_SQUARED)
                                        target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, as1l, 2, 0, 0, 0, 1, bd);
                        }
                    }

                    if(i == explodetick || !player.isOnline() || !data.isInMatch() || (data.getIsSneaking() && data.getArmorlist(0)==as1 && !data.getIsSliding())){
                        if(data.getIsSneaking()){
                            data.setIsSliding(true);
                        }
                        heightdiff=as1.getLocation().getY()-saveY;
                        if(heightdiff>7.9){
                            maxDist=data.getWeaponClass().getMainWeapon().getBlasterExHankei()+2;
                        }else if(1.8<heightdiff&&heightdiff<=7.9){
                            maxDist=data.getWeaponClass().getMainWeapon().getBlasterExHankei()+1;
                        }else if(-1.5<=heightdiff&&heightdiff<=1.8){
                            maxDist=data.getWeaponClass().getMainWeapon().getBlasterExHankei();
                        }else if(-10<=heightdiff&&heightdiff<-1.5){
                            maxDist=data.getWeaponClass().getMainWeapon().getBlasterExHankei()-1;
                            if(maxDist<=0){
                                maxDist=1;
                            }
                        }else if(heightdiff<-10){
                            maxDist=data.getWeaponClass().getMainWeapon().getBlasterExHankei()-2;
                            if(maxDist<=0){
                                maxDist=1;
                            }
                        }

                        data.subArmorlist(as1);

                        //爆発音
                        player.getWorld().playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

                        //爆発エフェクト
                        Sclat.createInkExplosionEffect(as1l, maxDist, 15, player);

                        //バリアをはじく
                        Sclat.repelBarrier(as1l, (int)(maxDist/2), player);

                        //塗る
                        for(int i = 0; i <= maxDist; i++){
                            List<Location> p_locs = Sphere.getSphere(as1l, i, 20);
                            for(Location loc : p_locs){
                                PaintMgr.Paint(loc, player, false);
                            }
                        }



                        //攻撃判定の処理

                        for(Entity as : player.getWorld().getEntities()){
                            if (as.getLocation().distance(as1l) <= maxDist){
                                if(as instanceof ArmorStand){
                                    if(as.getCustomName() != null){
                                        try {
                                            if (as.getCustomName().equals("Kasa")) {
                                                KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
                                                if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() != data.getTeam()) {
                                                    as1.remove();
                                                    cancel();
                                                }
                                            } else if (as.getCustomName().equals("SplashShield")) {
                                                SplashShieldData splashShieldData = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
                                                if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() != data.getTeam()) {
                                                    as1.remove();
                                                    cancel();
                                                }
                                            }
                                        }catch (Exception e){}
                                    }
                                }
                            }
                        }

                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(!DataMgr.getPlayerData(target).isInMatch() || target.getWorld() != player.getWorld())
                                continue;
                            if (target.getLocation().distance(as1l) <= maxDist) {
                                double damage = exdamage(heightdiff,maxDist - target.getLocation().distance(as1l),data.getWeaponClass().getMainWeapon().getBlasterExDamage() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP));
                                if(data.getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)){
                                    Sclat.giveDamage(player, target, damage, "killed");

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
                            if (as.getLocation().distance(as1l) <= maxDist){
                                if(as instanceof ArmorStand){
                                    double damage = exdamage(heightdiff,maxDist - as.getLocation().distance(as1l),data.getWeaponClass().getMainWeapon().getBlasterExDamage() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP));
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, player);
                                    if(as.getCustomName() != null){
                                        if(as.getCustomName().equals("SplashShield") || as.getCustomName().equals("Kasa"))
                                            break;
                                    }
                                }
                            }
                        }

                        as1.remove();
                        cancel();
                    }

                    i++;
                }catch(Exception e){
                    as1.remove();
                    data.subArmorlist(as1);
                    cancel();
                }
            }
        };
        if(player.getExp() > (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)) )
            task.runTaskTimer(Main.getPlugin(), 0, 1);
        else{
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
        }

//        BukkitRunnable cooltime = new BukkitRunnable(){
//            @Override
//            public void run(){
//                DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
//            }
//        };
//        cooltime.runTaskLater(Main.getPlugin(), 10);
    }
    private static boolean EntityWallHit(ArmorStand stand, Vector direction){
        Location entityLocation = stand.getLocation().clone().add(new Vector(0,0.3,0));
        double distance = 0.7; // レイの長さ
        //if (result != null && result.getHitBlockFace() != null) {
        if (stand.getWorld().rayTraceBlocks(entityLocation, direction, distance) != null) {
            // 壁に接触している場合の処理
            return true;
        } else {
            // 壁に接触していない場合の処理
            return false;
        }
    }
    private static double exdamage(double heightDiff,double mag,double dm){
        if(7.9<heightDiff){
            return mag * dm * 0.7 + dm*1.7;
        }else if(3.9<heightDiff&&heightDiff<=7.9){
            return mag * dm * 0.8 + dm*0.9;
        }else if(1.8<heightDiff&&heightDiff<=3.9){
            return mag * dm * 0.9 + dm*0.1;
        }else if(-1.5<=heightDiff&&heightDiff<=1.8){
            return mag * dm * 0.9 + dm*0.25;
        }else if(-5<=heightDiff&&heightDiff<-1.5){
            return mag * dm * 0.5 + dm*0.1;
        }else if(-10<=heightDiff&&heightDiff<-5){
            return mag * dm * 0.3;
        }else if(heightDiff<-10){
            return mag * dm * 0.2;
        }else {
            return 0;
        }
    }
}
