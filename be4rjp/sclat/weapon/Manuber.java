package be4rjp.sclat.weapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.data.SplashShieldData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;

public class Manuber {
    public static void ManeuverRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            Location loc = player.getLocation();
            Location loc2 = player.getLocation();
            Location before = player.getLocation();
            Location before_2 = player.getLocation();
            //int sl = 0;
            //スライドの仕様改変
            boolean sl_recharge_1=true;
            boolean sl_recharge_2=true;
            //スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
            double distcheck = 0;
            boolean check =false;//スライドの解除後判定用


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
                        Vector jvec = (new Vector(vec.getX(), 0, vec.getZ())).normalize();
                        Vector ev = jvec.clone().normalize().multiply(-2);
                        check=true;

                        //p.setExp(p.getExp() - ink);

                        //エフェクト
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                        double random = 1.0;

                        if(DataMgr.getPlayerData(player).getArmor()>9999) {
                            DataMgr.getPlayerData(player).setArmor(0);
                        }
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4F, 1.5F);

                            distcheck=EntityWallHit(p,jvec.clone());
                            while(distcheck>0 && !isSafeLocation(p,location.clone().add(jvec.clone().multiply(distcheck)))){
                                distcheck=distcheck-0.2;
                                //p.sendMessage("テレポート位置に障害物があります");
                                if(distcheck<=0){
                                    //p.sendMessage("テレポート距離が０になりました");
                                    distcheck=0;
                                }
                            }
                            //p.sendMessage("X "+verif.getX()+"Y "+verif.getY()+"Z "+verif.getZ());
                            p.teleport(location.clone().add(jvec.clone().multiply(EntityArmorstandHit(p,jvec.clone(),distcheck))));
                            //effect

                            for (int i = 0; i < 8; i++) {
                                Vector randomVector = new Vector(Math.random() * random - random / 2, Math.random() * random - random / 2, Math.random() * random - random / 2);
                                Vector erv = ev.clone().add(randomVector);
                                for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                    if (DataMgr.getPlayerData(o_player).getSettings().ShowEffect_BombEx()) {
                                        if (o_player.getWorld() == location.getWorld()) {
                                            if (o_player.getLocation().distanceSquared(location) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                                for(int i2=0; i2<4;i2++) {
                                                    o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST,
                                                            location.clone().add(jvec.clone().multiply(i*distcheck/8.0f)).add(0, 1.1, 0).add(randomVector.getX(), randomVector.getY(), randomVector.getZ()),
                                                            0, erv.getX(), erv.getY(), erv.getZ(), 1, bd);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
                            BukkitRunnable task0_1 = new BukkitRunnable() {
                                int i = 1;

                                @Override
                                public void run() {
                                    if (i == 3) {
                                        p.setVelocity(new Vector(0, 0, 0));
                                        data.setIsUsingManeuver(true);
                                        data.setCanShoot(true);
                                    }

                                    if(i==9){
                                        loc = p.getLocation();
                                        data.setIsUsingManeuver(true);
                                    }

                                    if (i == 10) {
                                        //data.setIsUsingManeuver(false);
//                                        if(sl_recharge_2) {
                                            data.setIsSliding(false);
//                                        }
                                        cancel();
                                    }
                                    i++;
                                }
                            };
                            BukkitRunnable task0_0 = new BukkitRunnable() {
                                int i = 1;

                                @Override
                                public void run() {
                                    if (i == 3) {
                                        p.setVelocity(new Vector(0, 0, 0));
                                        data.setIsUsingManeuver(true);
                                        data.setCanShoot(true);
                                    }

                                    if(i==9){
                                        loc2 = p.getLocation();
                                        data.setIsUsingManeuver(true);
                                    }

                                    if (i == 10) {
                                        //data.setIsUsingManeuver(false);
                                        data.setIsSliding(false);
                                        cancel();
                                    }
                                    i++;
                                }
                            };
                            if (sl_recharge_2) {
                                //task0_1.runTaskTimer(Main.getPlugin(), 0, 1);
                                task0_1.runTaskTimer(Main.getPlugin(), 0, 1);
                            } else {
                                task0_0.runTaskTimer(Main.getPlugin(), 0, 1);
                            }
//                            BukkitRunnable task2 = new BukkitRunnable() {
//                                @Override
//                                public void run() {
//                                    sl = 0;
//                                    check = true;
//                                }
//                            };
                        //booleam型の変数で二つのスライドをそれぞれ表現している、優先順位が低い方がTrueのときは高い方が使われた後のため高い方のリチャージをする（優先順位が高い方は2秒、低い方は2.2秒）
                        //check = false;
                    }
                    //}else{
                    //p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                    //player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                    //}
                }

                if(!data.getIsSliding()) {
                    if ((loc.getX() == ploc.getX() && loc.getZ() == ploc.getZ())||(loc2.getX() == ploc.getX() && loc2.getZ() == ploc.getZ())) {
                        data.setIsUsingManeuver(true);
                    }else {
                            if(check) {
                                //p.sendMessage("現在地はX＝"+p.getLocation().getX()+" Y="+p.getLocation().getY()+" Z="+p.getLocation().getZ());
                                //p.sendMessage("LOC1はX＝"+loc.getX()+" Y="+loc.getY()+" Z="+loc.getZ());
                                //p.sendMessage("LOC2はX＝"+loc2.getX()+" Y="+loc2.getY()+" Z="+loc2.getZ());
                                BukkitRunnable task4 = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        sl_recharge_1 = true;
                                        //p.sendMessage("スライド１リチャージ完了です");
                                        //check = true;
                                    }
                                };

                                BukkitRunnable task5 = new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        sl_recharge_1 = true;
                                        sl_recharge_2 = true;
                                        //p.sendMessage("スライド2リチャージ完了です");
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

    private static double EntityWallHit(Player p, Vector direction){
        Location entityLocation = p.getLocation().clone();
        double distance = 5.2; // レイの長さ
        World world = p.getWorld();
        RayTraceResult rayresult = world.rayTraceBlocks(entityLocation, direction, distance);
        //if (result != null && result.getHitBlock() != null) {
        if(rayresult !=null && rayresult.getHitBlock()!=null) {
            Location hitlocation = rayresult.getHitPosition().toLocation(world);
            double raydistance =entityLocation.distance(hitlocation);
            if(raydistance -0.4 > 0 ) {
                return raydistance - 0.4;
            }else{
                return 0;
            }
        }else{
            return 4.9;
        }
    }

    private static double EntityArmorstandHit(Player p, Vector direction,double dist){
        Location entityLocation = p.getLocation().clone();
        double distance = dist; // レイの長さ
        double distance2 =dist;
        World world = p.getWorld();
        //if (result != null && result.getHitBlock() != null) {
        Predicate<Entity> isArmorStand = entity -> entity.getType() == EntityType.ARMOR_STAND;

        // rayTraceEntitiesでエンティティとの衝突を判定
        RayTraceResult result = world.rayTraceEntities(entityLocation, direction, distance, 0.5, isArmorStand);

        if (result != null && result.getHitEntity() != null) {
            // 衝突までの距離を計算
            Entity hitEntity = result.getHitEntity();
            ArmorStand armorStand = (ArmorStand) hitEntity;
            if(armorStand.getCustomName() != null){
                if(armorStand.getCustomName().equals("SplashShield")){
                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand(armorStand);
                    if(DataMgr.getPlayerData(p).getTeam() != DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() && ssdata.getIsDeploy()){
                        Location hitLocation = result.getHitPosition().toLocation(world);
                        distance2 = entityLocation.distance(hitLocation);
                        if(dist-distance2>0.7){
                            distance2=distance2+0.4;
                        }
                        return distance2;
                    }
                }
            }
        }
        return dist;
    }

    public static boolean isSafeLocation(Player player, Location location) {
        World world = location.getWorld();
        boolean contact=true;
        if (world == null) return false;
        //player.sendMessage("テレポートの位置はX"+LX+"Y"+LY+"Z"+LZ);

        // プレイヤーの体が占める範囲 (高さ2ブロック、幅1ブロック)
        BoundingBox playerBoundingBox = new BoundingBox(
                location.getX() - 0.4, location.getY(), location.getZ() - 0.4,
                location.getX() + 0.4, location.getY() + 1.9, location.getZ() + 0.4
//                location.clone().getX() - 0.4, location.clone().getY(), location.clone().getZ() - 0.4,
//                location.clone().getX() + 0.4, location.clone().getY() + 1.9, location.clone().getZ() + 0.4
        );

        // プレイヤーの足元と頭の位置のブロックを取得
        //Block blockAtFeet = location.getBlock();
        //Block blockAtHead = location.clone().add(0, 1, 0).getBlock();
        List<Block> blocks = new ArrayList<>();
//        for (int x = (int)(LX-0.4); x <= (int)(LX+0.4); x++) {
//            for (int y = (int)LY; y <= (int)LY+1.8; y++) {
//                for (int z = (int)(LZ-0.4); z <= (int)(LZ+0.4); z++) {
//                    player.sendMessage("ブロックのx座標"+x+"ブロックのy座標"+y+"ブロックのｚ座標"+z);
//                    blocks.add(location.getWorld().getBlockAt(location.clone().add(x, y, z)));
//                }
//            }
//        }

        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.4, 0, 0.4)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(-0.4, 0, 0.4)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0.4, 0, -0.4)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(-0.4, 0, -0.4)));
        blocks.add(location.getWorld().getBlockAt(location.clone().add(0, 1.0, 0)));
        for (Block pblock :blocks ){
            //player.sendMessage("ブロックの位置はX"+pblock.getLocation().getX()+"Y"+pblock.getLocation().getY()+"Z"+pblock.getLocation().getZ());
            if(hasCollision(pblock, playerBoundingBox)){
                contact=false;
            }
        }

        //player.sendMessage("検証の結果"+contact);
        // どちらかのブロックが危険なコライダーを持っていないか確認
        return contact;
    }

    // ブロックがプレイヤーの体と衝突するかを判定する関数衝突するとTRUE
    private static boolean hasCollision(Block block, BoundingBox playerBoundingBox) {
        if (block.isEmpty() || block.isPassable()) return false; // 空気や通り抜け可能ならOK

        // ブロックのバウンディングボックス（ヒットボックス）を取得
        BoundingBox blockBoundingBox = block.getBoundingBox();

        // プレイヤーの体とブロックが重なるかどうかを判定
        return playerBoundingBox.overlaps(blockBoundingBox);
    }

}
