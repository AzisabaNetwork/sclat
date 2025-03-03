package be4rjp.sclat.weapon;

import be4rjp.dadadachecker.ClickType;
import be4rjp.sclat.GlowingAPI;
import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.raytrace.RayTrace;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

import static be4rjp.sclat.Main.conf;

public class Reeler {
    public static void ReelerShootRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            int sl = 0;
            boolean check = true;

            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);

                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }

                if(data.getIsUsingManeuver()){
                    ClickType clickType = Main.dadadaCheckerAPI.getPlayerClickType(player);
                    if((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) && data.isInMatch()){
                        ReelerShoot(p);
                        data.setTick(data.getTick() + DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootTick());
                    }
                }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getSlidingShootTick());
    }
    public static void ReelerRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            Location loc = player.getLocation();
            //int sl = 0;
            //スライドの仕様改変
            boolean sl_recharge_1=true;
            int killcount=DataMgr.getPlayerData(p).getKillCount();
            int gr_recharge = 100;
            //スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
            boolean check = true;

            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);
                Location ploc = p.getLocation();

                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }

                Location location = p.getLocation();

                Vector vec = p.getEyeLocation().getDirection();

                //float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                if(gr_recharge<=100){
                    gr_recharge++;
                }
                if(killcount < data.getKillCount()){
                    gr_recharge=100;
                    killcount=data.getKillCount();
                }
                if (data.getIsSneaking() && gr_recharge >=100 && sl_recharge_1 && !data.getIsSliding() && p.getInventory().getItemInMainHand().getType().equals(data.getWeaponClass().getMainWeapon().getWeaponIteamStack().getType())) {
                    Vector jvec = (new Vector(vec.getX(), 0, vec.getZ())).normalize().multiply(3);
                    Vector ev = jvec.clone().normalize().multiply(-2);
                        //エフェクト
                    org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                    double random = 1.0;
                    for (int i = 0; i < 35; i++) {
                        Vector randomVector = new Vector(Math.random() * random - random / 2, Math.random() * random - random / 2, Math.random() * random - random / 2);
                        Vector erv = ev.clone().add(randomVector);
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if (DataMgr.getPlayerData(o_player).getSettings().ShowEffect_BombEx()) {
                                if (o_player.getWorld() == location.getWorld()) {
                                    if (o_player.getLocation().distanceSquared(location) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                        o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST,
                                                location.clone().add(0, 0.7, 0).add(randomVector.getX(), randomVector.getY(), randomVector.getZ()),
                                                0, erv.getX(), erv.getY(), erv.getZ(), 1, bd);
                                    }
                                }
                            }
                        }
                    }
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4F, 1.5F);

                    BukkitRunnable task1 = new BukkitRunnable() {
                        @Override
                        public void run() {
                            data.setIsSliding(true);
                        }
                    };
                    BukkitRunnable task = new BukkitRunnable() {
                        int i = 1;

                        @Override
                        public void run() {
                            if (i == 3) {
                                data.setCanShoot(true);
                                cancel();
                            }
                            i++;
                        }
                    };
                    //リーラ―起動部分
                    if(!conf.getConfig().getString("WorkMode").equals("Trial")) {
                        Player dest = grap(player);
                        if(dest!=player){
                            grapple(player,dest);
                            gr_recharge=0;
                            data.setCanShoot(false);
                            task1.runTaskLater(Main.getPlugin(), 9);
                            task.runTaskTimer(Main.getPlugin(), 0, 1);
                        }
                    }else{
                        ArmorStand destarm =null;
                        destarm = graptest(player);
                        if(destarm != null){
                            grappletest(player,destarm);
                            gr_recharge=0;
                            data.setCanShoot(false);
                            task1.runTaskLater(Main.getPlugin(), 9);
                            task.runTaskTimer(Main.getPlugin(), 0, 1);
                        }
                    }
                    data.setIsSneaking(false);
                    //優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                    sl_recharge_1=false;
                    //sl++;
//                            BukkitRunnable task2 = new BukkitRunnable() {
//                                @Override
//                                public void run() {
//                                    sl = 0;
//                                    check = true;
//                                }
//                            };
                    BukkitRunnable task2 = new BukkitRunnable() {//二つのtaskの追加でそれぞれのスライドを管理しています
                        @Override
                        public void run() {
                            sl_recharge_1 = true;
                            //check = true;
                        }
                    };
                    task2.runTaskLater(Main.getPlugin(), 10);
                }
                    //}else{
                    //p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                    //player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                    //}

                if(data.getIsSliding()) {
                    if (p.isOnGround()) {
                        data.setIsUsingManeuver(false);
                        data.setIsSliding(false);
                    }
                    else {
                        data.setIsUsingManeuver(true);
                    }
                }

                //loc = ploc;
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void grapple(Player p,Player target){


        BukkitRunnable graptask = new BukkitRunnable() {
            Location beforeploc =p.getLocation();
            int i = 1;
            @Override
            public void run() {
                if(i==7){
                    if (!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline() || !DataMgr.getPlayerData(target).isInMatch() || !target.isOnline()) {
                        cancel();
                        return;
                    }
                    beforeploc =p.getLocation();
                }
                if(i==8) {
                    if (!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline() || !DataMgr.getPlayerData(target).isInMatch() || !target.isOnline()) {
                        cancel();
                        return;
                    }
                    Location tl = target.getLocation();
                    Location pl = p.getLocation();
                    //Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                    Vector shot = new Vector(tl.getX() - pl.getX(), (tl.getY() - pl.getY()) * (0.93), tl.getZ() - pl.getZ());
                    shot.multiply(0.2);
                    if(pl.getX() - beforeploc.getX()!=0 || pl.getZ() - beforeploc.getZ()!=0) {
                        shot.add(new Vector(pl.getX() - beforeploc.getX(), 0, pl.getZ() - beforeploc.getZ()).normalize().multiply(shot.length() * 0.4));
                    }
                    //shot.add(new Vector(eye.getX(),0,eye.getZ()).multiply(shot.length()*0.2));
                    if (p.getGameMode().equals(GameMode.ADVENTURE) && target.getGameMode().equals(GameMode.ADVENTURE)) {
                        if (p.isOnGround()) {
                            shot.multiply(0.68);
                            shot.add(new Vector(0, 0.6 - shot.getY(), 0));
                        } else {
                            shot.multiply(0.5);
                            shot.add(new Vector(0, 0.5, 0));
                        }
                        p.setVelocity(shot);
                        PlayerData pdata = DataMgr.getPlayerData(p);
                        pdata.setIsSliding(true);
                        pdata.setIsUsingManeuver(true);
                        if(pdata.getArmor()>9999) {
                            pdata.setArmor(0);
                        }
                    }
                    cancel();
                }
                i++;
            }
        };
        //graptask.runTaskLater(Main.getPlugin(), 8);
        graptask.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static void grappletest(Player p,ArmorStand target){
        BukkitRunnable graptask = new BukkitRunnable() {
            Location beforeploc =p.getLocation();
            int i = 1;
            @Override
            public void run() {
                if(i==7){
                    if (!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline()) {
                        cancel();
                        return;
                    }
                    beforeploc =p.getLocation();
                }
                if(i==8) {
                    if (!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline()) {
                        cancel();
                        return;
                    }
                    Location tl = target.getLocation();
                    Location pl = p.getLocation();
                    //Vector eye = p.getEyeLocation().getDirection().normalize().multiply(2);
                    Vector shot = new Vector(tl.getX() - pl.getX(), (tl.getY() - pl.getY()) * (0.93), tl.getZ() - pl.getZ());
                    shot.multiply(0.2);
                    if(pl.getX() - beforeploc.getX()!=0 || pl.getZ() - beforeploc.getZ()!=0) {
                        shot.add(new Vector(pl.getX() - beforeploc.getX(), 0, pl.getZ() - beforeploc.getZ()).normalize().multiply(shot.length() * 0.4));
                    }
                    //shot.add(new Vector(eye.getX(), 0, eye.getZ()).multiply(shot.length() * 0.2));
                    if (p.getGameMode().equals(GameMode.ADVENTURE)) {
                        if (p.isOnGround()) {
                            shot.multiply(0.68);
                            shot.add(new Vector(0, 0.6 - shot.getY(), 0));
                        } else {
                            shot.multiply(0.5);
                            shot.add(new Vector(0, 0.5, 0));
                        }
                        p.setVelocity(shot);
                        DataMgr.getPlayerData(p).setIsSliding(true);
                        DataMgr.getPlayerData(p).setIsUsingManeuver(true);
                    }
                }
                i++;
            }
        };
        graptask.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static Player grap(Player player){
        Player dest =player;
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(20, 0.2);

        loop : for(int it = 0; it < positions.size();it++) {

            Location position = positions.get(it).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if (!block.getType().equals(Material.AIR)) {
                break loop;
            }
            if (DataMgr.getPlayerData(player).getSettings().ShowEffect_MainWeaponInk()) {
                if (it < 10) {
                    if (player.getWorld() == position.getWorld()) {
                        if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                            org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                            player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
                        }
                    }
                }
            }

            double maxDistSquad = 6 /* 2*2 */;
            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                if (!DataMgr.getPlayerData(target).isInMatch())
                    continue;
                if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        //if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                        dest = target;
                        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,10,1));
                        player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2.0f, 2);
                        break loop;
                        //}
                    }
                }
            }
        }
        return dest;
    }
    public static ArmorStand graptest(Player player){
        ArmorStand dest =null;
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(20, 0.2);
        loop2 : for(int it = 0; it < positions.size();it++) {

            Location position = positions.get(it).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if (!block.getType().equals(Material.AIR)) {
                break loop2;
            }
            if (DataMgr.getPlayerData(player).getSettings().ShowEffect_MainWeaponInk()) {
                if (it < 10) {
                    if (player.getWorld() == position.getWorld()) {
                        if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                            org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                            player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
                        }
                    }
                }
            }

            double maxDistSquad = 6 /* 2*2 */;
            if(conf.getConfig().getString("WorkMode").equals("Trial")) {
                for (Entity as : player.getWorld().getEntities()) {
                    if (as instanceof ArmorStand) {
                        if (as.getLocation().distanceSquared(position) <= maxDistSquad) {
                            //if(rayTrace.intersects(new BoundingBox((Entity)as), (int)(30), 0.2)){
                            if (as.getCustomName() != null) {
                                if (as.getCustomName().equals("SplashShield")) {
//                                                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand)as);
//                                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
//                                                        break loop;
//                                                    }
                                } else if (as.getCustomName().equals("Kasa")) {
//                                                    KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand)as);
//                                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
//                                                        break loop;
//                                                    }
                                } else {
                                    if (Sclat.isNumber(as.getCustomName()))
                                        if (!as.getCustomName().equals("21") && !as.getCustomName().equals("100")) {
                                            if (((ArmorStand) as).isVisible()) {
                                                dest = (ArmorStand)as;
                                                player.playSound(player.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 2.0f, 2);
                                                //player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                            }
                                        }
                                }
                                break loop2;
                            }
                        }
                        //ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                        //}
                    }
                }
            }
        }
        return dest;
    }
    public static void ReelerShoot(Player player){

        if(player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = DataMgr.getPlayerData(player);
        if(player.getExp() <= (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP))){
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
            return;
        }
        player.setExp(player.getExp() - (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(data.getWeaponClass().getMainWeapon().getShootSpeed() * data.getWeaponClass().getMainWeapon().getDistanceTick(),0.7);
        boolean isLockOnPlayer = false;
        if(data.getWeaponClass().getMainWeapon().getMaxRandom() == 0) {
            check:
            for (int i = 0; i < positions.size(); i++) {
                Location position = positions.get(i).toLocation(player.getLocation().getWorld());
                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if (player != target && player.getWorld() == target.getWorld()) {
                        if (target.getLocation().distance(position) < 2) {
                            isLockOnPlayer = true;
                            break check;
                        }
                    }
                }

                for (Entity as : player.getWorld().getEntities()) {
                    if (as instanceof ArmorStand) {
                        if (as.getCustomName() != null) {
                            if (as.getLocation().distanceSquared(position) <= 4 /* 2*2 */) {
                                isLockOnPlayer = true;
                                break check;
                            }
                        }
                    }
                }
            }
        }

        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true);

        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
        Vector vec = player.getLocation().getDirection().multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getSlideNeedINK());
        double random = data.getWeaponClass().getMainWeapon().getChargeRatio();
        int distick = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getMaxCharge();
        vec.add(new Vector(Math.random() * random - random/2, 0, Math.random() * random - random/2));
        ball.setVelocity(vec);
        ball.setShooter(player);
        //スライド時かどうかをSnowballListenerに渡すためのnameの改変
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        buf.append("#slided");
        String name = buf.toString();
        //String name = String.valueOf(Main.getNotDuplicateNumber());//ここで改変終わり
        DataMgr.mws.add(name);
        DataMgr.tsl.add(name);
        ball.setCustomName(name);
        DataMgr.getMainSnowballNameMap().put(name, ball);
        DataMgr.setSnowballHitCount(name, 0);
        BukkitRunnable task = new BukkitRunnable(){
            int i = 0;
            int tick = distick;
            //Vector fallvec;
            Vector origvec = vec;
            Snowball inkball = ball;
            boolean addedFallVec = false;
            Player p = player;
            //Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed()/17);
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(0.01);
            @Override
            public void run(){
                inkball = DataMgr.getMainSnowballNameMap().get(name);

                if(!inkball.equals(ball)){
                    i+=DataMgr.getSnowballHitCount(name) - 1;
                    DataMgr.setSnowballHitCount(name, 0);
                }

                if(i != 0) {
                    org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                    for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                        if (DataMgr.getPlayerData(o_player).getSettings().ShowEffect_MainWeaponInk())
                            if (o_player.getWorld() == inkball.getWorld())
                                if (o_player.getLocation().distanceSquared(inkball.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED)
                                    o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 0, 0, -1, 0, 1, bd);
                    }
                }

                if(i >= tick && !addedFallVec){
                    inkball.setVelocity(fallvec);
                    addedFallVec = true;
                }
                if(i >= tick && i <= tick + 15)
                    inkball.setVelocity(inkball.getVelocity().add(new Vector(0, -0.1, 0)));
                //if(i != tick)
                if((new Random().nextInt(7)) == 0)
                    PaintMgr.PaintHightestBlock(inkball.getLocation(), p, false, true);
                if(inkball.isDead())
                    cancel();

                i++;
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}
