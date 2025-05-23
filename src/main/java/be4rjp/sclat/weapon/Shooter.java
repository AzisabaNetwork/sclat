package be4rjp.sclat.weapon;

import be4rjp.dadadachecker.ClickType;
import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.raytrace.RayTrace;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class Shooter {
    public static void ShooterRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            int sl = 0;
            boolean check = true;
            int maxRandomCount = 0;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);

                if(!data.isInMatch() || !p.isOnline() || data.getStoprun()){
                    cancel();
                    return;
                }

                if(!data.getIsUsingManeuver() && data.getCanShoot()){
                    ClickType clickType = Main.dadadaCheckerAPI.getPlayerClickType(player);
                    if((clickType == ClickType.FIRST_CLICK || clickType == ClickType.RENDA || clickType == ClickType.NAGAOSI) && data.isInMatch()){
                        Shooter.Shoot(p, false, false, maxRandomCount >= data.getWeaponClass().getMainWeapon().getMaxRandomCount());
                        data.setTick(data.getTick() + DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootTick());
                        if(data.getWeaponClass().getMainWeapon().getMaxRandom() != 0
                                && maxRandomCount <= data.getWeaponClass().getMainWeapon().getMaxRandomCount() * 2) {
                            maxRandomCount++;
                        }
                    }else{
                        if(data.getWeaponClass().getMainWeapon().getMaxRandom() != 0 && maxRandomCount >= 0)
                            maxRandomCount-=2;
                    }
                }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootTick());
    }

    public static void ManeuverShootRunnable(Player player){
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
                        Shooter.Shoot(p, true, false, false);
                        data.setTick(data.getTick() + DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootTick());
                    }
                }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getSlidingShootTick());
    }

    public static void ManeuverRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            Location loc = player.getLocation();
            Location before = player.getLocation();
            Location before_2 = player.getLocation();
            //int sl = 0;
            //スライドの仕様改変
            boolean sl_recharge_1=true;
            boolean sl_recharge_2=true;
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

                double x = location.getX() - before.getX();
                double z = location.getZ() - before.getZ();
                Vector vec = p.getEyeLocation().getDirection();
                if(x != 0 || z != 0) {
                    vec = new Vector(x, 0, z);
                }else{
                    x = location.getX() - before_2.getX();
                    z = location.getZ() - before_2.getZ();
                    if(x != 0 || z != 0){
                        vec = new Vector(x, 0, z);
                    }
                }
                before_2 = before.clone();
                before = location.clone();

                //float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                //マニューバー系
                if(data.getWeaponClass().getMainWeapon().getIsManeuver()){
                    //if(p.getExp() >= ink) {
                    if (data.getIsSneaking() && sl_recharge_2 == true && !data.getIsSliding() && p.getInventory().getItemInMainHand().getType().equals(data.getWeaponClass().getMainWeapon().getWeaponIteamStack().getType())) {//slをsl_recharge_2に変更することで優先順位が低い方のスライドが残っている時のみ使えるようにしました
                        Vector jvec = (new Vector(vec.getX(), 0, vec.getZ())).normalize().multiply(3);
                        Vector ev = jvec.clone().normalize().multiply(-2);
                        check = true;

                        //p.setExp(p.getExp() - ink);

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

                        if(DataMgr.getPlayerData(player).getArmor()>9999) {
                            DataMgr.getPlayerData(player).setArmor(0);
                        }
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4F, 1.5F);


                        p.setVelocity(jvec.clone().setY(p.isOnGround() ? 0 : -0.4));
                        data.setIsSneaking(false);
                        data.setIsSliding(true);
                        data.setCanShoot(false);
                        //優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                        if(!sl_recharge_1){
                            sl_recharge_2=false;
                        }else{
                            sl_recharge_1=false;
                        }
                        //sl++;
                        BukkitRunnable task = new BukkitRunnable() {
                            int i = 1;

                            @Override
                            public void run() {
                                if (i == 3) {
                                    p.setVelocity(new Vector(0, 0, 0));
                                    data.setIsUsingManeuver(true);
                                    data.setCanShoot(true);
                                }

                                if (i == 10) {
                                    data.setIsUsingManeuver(false);
                                    loc = p.getLocation();
                                    cancel();
                                }
                                i++;
                            }
                        };
                        task.runTaskTimer(Main.getPlugin(), 0, 1);

                        BukkitRunnable task1 = new BukkitRunnable() {
                            @Override
                            public void run() {
                                data.setIsSliding(false);
                            }
                        };
                        task1.runTaskLater(Main.getPlugin(), 10);
//                            BukkitRunnable task2 = new BukkitRunnable() {
//                                @Override
//                                public void run() {
//                                    sl = 0;
//                                    check = true;
//                                }
//                            };
//                        BukkitRunnable task2 = new BukkitRunnable() {//二つのtaskの追加でそれぞれのスライドを管理しています
//                            @Override
//                            public void run() {
//                                sl_recharge_1 = true;
//                                //check = true;
//                            }
//                        };
//                        BukkitRunnable task3 = new BukkitRunnable() {
//                            @Override
//                            public void run() {
//                                sl_recharge_2 = true;
//                                //check = true;
//                            }
//                        };
                        //スライド仕様変更の改変
//                        if( sl_recharge_2 == true){task2.runTaskLater(Main.getPlugin(), 64);}
//                        else{task3.runTaskLater(Main.getPlugin(), 64);}
                        //booleam型の変数で二つのスライドをそれぞれ表現している、優先順位が低い方がTrueのときは高い方が使われた後のため高い方のリチャージをする（優先順位が高い方は2秒、低い方は2.2秒）
                        //check = false;
                    }
                    //}else{
                    //p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                    //player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                    //}
                }

                if(!data.getIsSliding()) {
                    if (loc.getX() == ploc.getX() && loc.getZ() == ploc.getZ())
                        data.setIsUsingManeuver(true);
                    else {
                        if(check) {
                            BukkitRunnable task4 = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    sl_recharge_1 = true;
                                    //check = true;
                                }
                            };

                            BukkitRunnable task5 = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    sl_recharge_1 = true;
                                    sl_recharge_2 = true;
                                    //check = true;
                                }
                            };
                            if (sl_recharge_2 == true) {
                                task4.runTaskLater(Main.getPlugin(), 64);
                                check = false;
                            } else {
                                task5.runTaskLater(Main.getPlugin(), 64);
                                check = false;
                            }
                        }
                        data.setIsUsingManeuver(false);
                    }
                }

                //loc = ploc;
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void Shoot(Player player, boolean slided, boolean sound, boolean maxRandom){

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
        }else{
            if(!player.isOnGround()) maxRandom = true;
        }

        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true);

        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
        Vector vec = player.getLocation().getDirection().multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootSpeed());
        double random = data.getWeaponClass().getMainWeapon().getRandom();
        if(maxRandom) random = data.getWeaponClass().getMainWeapon().getMaxRandom();
        if(isLockOnPlayer)
            random /= 2.0;
        if(slided)
            random /= 10.0;
        int distick = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getDistanceTick();
        vec.add(new Vector(Math.random() * random - random/2, 0, Math.random() * random - random/2));
        ball.setVelocity(vec);
        ball.setShooter(player);
        //スライド時かどうかをSnowballListenerに渡すためのnameの改変
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        if(slided) {
            buf.append("#slided");
        }
        String name = buf.toString();
        //String name = String.valueOf(Main.getNotDuplicateNumber());//ここで改変終わり
        DataMgr.mws.add(name);
        if(sound || slided)
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
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed()/17);
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
