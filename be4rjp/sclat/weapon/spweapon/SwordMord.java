package be4rjp.sclat.weapon.spweapon;

import be4rjp.dadadachecker.ClickType;
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
import be4rjp.sclat.raytrace.RayTrace;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SwordMord {
    public static void setSwordMord(Player player){
        DataMgr.getPlayerData(player).setIsUsingSP(true);
        DataMgr.getPlayerData(player).setIsUsingSS(true);
        SPWeaponMgr.setSPCoolTimeAnimation(player, 160);

        BukkitRunnable it = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                player.getInventory().clear();
                player.updateInventory();

                ItemStack item = new ItemStack(Material.WHEAT);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("右クリックで斬撃、シフトで防御");
                item.setItemMeta(meta);
                for (int count = 0; count < 9; count++){
                    player.getInventory().setItem(count, item);
                    if(count % 2 != 0)
                        player.getInventory().setItem(count, new ItemStack(Material.AIR));
                }
                player.updateInventory();
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 161, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 161, 0));
                //SwordPaintRunnable(p);
                SwordGurdRunnable(p);
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
        task.runTaskLater(Main.getPlugin(), 160);
    }
    public static void AttackSword(Player player){
        if(player.hasPotionEffect(PotionEffectType.LUCK)) {
            if(!player.isSneaking()) {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4F, 1.5F);
                Location vec = player.getLocation().add(player.getEyeLocation().getDirection().normalize().multiply(3.0));
                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if(DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb())
                        if(target.getWorld() == player.getWorld())
                            if(target.getLocation().distance(vec) < Main.PARTICLE_RENDER_DISTANCE)
                                if(target.equals(player)) {
                                    target.spawnParticle(Particle.SWEEP_ATTACK, vec.add(0, 1.5, 0), 0, 10, 7, 10);
                                }else{
                                    target.spawnParticle(Particle.SWEEP_ATTACK, vec, 0, 8, 5, 8);
                                }
                }
                int maxDist = 3;
                for(int i = 0; i <= maxDist - 1; i++){
                    List<Location> p_locs = Sphere.getSphere(vec, i, 20);
                    for(Location loc : p_locs){
                        PaintMgr.Paint(loc, player, false);
                        PaintMgr.PaintHightestBlock(loc, player, false, false);
                    }
                }

                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                    if (!DataMgr.getPlayerData(target).isInMatch())
                        continue;
                    if (target.getLocation().distance(vec) <= maxDist + 1) {
                        double damage = 15.1;
                        if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                            Sclat.giveDamage(player, target, damage, "spWeapon");

                            //AntiNoDamageTime
                            BukkitRunnable task = new BukkitRunnable() {
                                Player p = target;

                                @Override
                                public void run() {
                                    target.setNoDamageTicks(0);
                                }
                            };
                            task.runTaskLater(Main.getPlugin(), 1);


                        }
                    }
                }


                for (Entity as : player.getWorld().getEntities()) {
                    if (as instanceof ArmorStand) {
                        if (as.getLocation().distanceSquared(vec) <= (maxDist + 1) * (maxDist + 1)) {
                            double damage = 15.1;
                            ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
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
            task2.runTaskLater(Main.getPlugin(), 5);
        }
    }
    public static void SwordPaintRunnable(Player player){
        BukkitRunnable task = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                try{
                    PlayerData data = DataMgr.getPlayerData(p);
                    if(!data.isInMatch() || !p.isOnline() || !DataMgr.getPlayerData(player).getIsUsingSP())
                        cancel();

                    if(p.hasPotionEffect(PotionEffectType.LUCK) && p.getGameMode() != GameMode.SPECTATOR && !p.getInventory().getItemInMainHand().getType().equals(Material.AIR)){
                        Vector locvec = p.getEyeLocation().getDirection();
                        Location eloc = p.getEyeLocation();
                        Vector vec = new Vector(locvec.getX(), 0, locvec.getZ()).normalize();
                        Location front = eloc.add(vec.getX() * 0.5, -0.9, vec.getZ() * 0.5);
                        PaintMgr.PaintHightestBlock(front, p, false, true);
                    }

                }catch(Exception e){cancel();}
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static void SwordGurdRunnable(Player player){
        BukkitRunnable task = new BukkitRunnable(){
            KasaData kdata = new KasaData(player);
            ArmorStand as1;
            ArmorStand as2;
            ArmorStand as3;
            ArmorStand as4;
            int c = 0;
            boolean gurd =false;
            Player p = player;
            Location eloc = p.getEyeLocation();
            Vector pv = p.getEyeLocation().getDirection().normalize();
            Vector vec3 = new Vector(pv.getX(), 0, pv.getZ()).normalize();
            Vector vec1 = new Vector(vec3.getZ() * -1, 0, vec3.getX());
            Vector vec2 = new Vector(vec3.getZ(), 0, vec3.getX() * -1);
            Location l1 = eloc.clone().add(vec1.clone().multiply(0.4)).add(vec3.clone().multiply(0.7));
            Location r1 = eloc.clone().add(vec2.clone().multiply(0.4)).add(vec3.clone().multiply(0.7));
            Location m1 = eloc.clone().add(vec3.clone().multiply(0.8));
            @Override
            public void run(){
                try{
                    c++;
                    if(c%2==0) {
                        PlayerData data = DataMgr.getPlayerData(p);
                        if (!data.isInMatch() || !p.isOnline() || !DataMgr.getPlayerData(player).getIsUsingSP()) {
                            as1.remove();
                            as2.remove();
                            as3.remove();
                            as4.remove();
                            cancel();
                        }
                        //防具立て召喚
                        if (p.hasPotionEffect(PotionEffectType.LUCK) && p.getGameMode() != GameMode.SPECTATOR && p.isSneaking()) {
                            eloc = p.getEyeLocation();
                            pv = p.getEyeLocation().getDirection().normalize();
                            vec3 = new Vector(pv.getX(), 0, pv.getZ()).normalize();
                            vec1 = new Vector(vec3.getZ() * -1, 0, vec3.getX());
                            vec2 = new Vector(vec3.getZ(), 0, vec3.getX() * -1);
                            l1 = eloc.clone().add(vec1.clone().multiply(0.4)).add(vec3.clone().multiply(0.7));
                            r1 = eloc.clone().add(vec2.clone().multiply(0.4)).add(vec3.clone().multiply(0.7));
                            m1 = eloc.clone().add(vec3.clone().multiply(0.8));
                            if (!gurd) {
                                kdata = new KasaData(player);
                                DataMgr.setKasaDataWithPlayer(player, kdata);
                                List<ArmorStand> list = new ArrayList<ArmorStand>();
                                as1 = player.getWorld().spawn(m1.clone().add(0, -1.8, 0), ArmorStand.class, armorStand -> {
                                    armorStand.setGravity(false);
                                    armorStand.setVisible(false);
                                });
                                as2 = player.getWorld().spawn(m1.clone().add(0, -0.8, 0), ArmorStand.class, armorStand -> {
                                    armorStand.setGravity(false);
                                    armorStand.setVisible(false);
                                });
                                as3 = player.getWorld().spawn(r1.clone().add(0, -1.2, 0), ArmorStand.class, armorStand -> {
                                    armorStand.setGravity(false);
                                    armorStand.setVisible(false);
                                });
                                as4 = player.getWorld().spawn(l1.clone().add(0, -1.2, 0), ArmorStand.class, armorStand -> {
                                    armorStand.setGravity(false);
                                    armorStand.setVisible(false);
                                });
                                gurd = true;
                                list.add(as1);
                                list.add(as2);
                                list.add(as3);
                                list.add(as4);
                                List<ArmorStand> aslist = new ArrayList<ArmorStand>();
                                aslist.addAll(list);
                                kdata.setArmorStandList(aslist);
                                for (ArmorStand as : list) {
                                    //as.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
                                    as.setBasePlate(false);
                                    //as.setVisible(false);
                                    //as.setGravity(false);
                                    as.setCustomName("Kasa");
                                    DataMgr.setKasaDataWithARmorStand(as, kdata);
                                }
                            } else {
                                as1.teleport(m1.clone().add(0, -1.8, 0));
                                as2.teleport(m1.clone().add(0, -0.8, 0));
                                as3.teleport(r1.clone().add(0, -1.2, 0));
                                as4.teleport(l1.clone().add(0, -1.2, 0));
                            }
                        } else if (gurd) {
                            as1.remove();
                            as2.remove();
                            as3.remove();
                            as4.remove();
                            gurd = false;
                        }
                    }
                    if (p.getGameMode() != GameMode.SPECTATOR && p.isSneaking() && gurd && kdata.getDamage() != 0) {
                        ShootCounter(player);
                        kdata.setDamage(0);
                    }

                }catch(Exception e){cancel();}
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static void ShootCounter(Player player){

        double QuadroShootSpeed = 5.9;
        if(player.getGameMode() == GameMode.SPECTATOR) return;

//        PlayerData data = DataMgr.getPlayerData(player);
//        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
//        ArrayList<Vector> positions = rayTrace.traverse(QuadroShootSpeed * QuadroDisTick,0.7);
//        boolean isLockOnPlayer = false;
//        check:
//        for (int i = 0; i < positions.size(); i++) {
//            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
//            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
//                if (player != target && player.getWorld() == target.getWorld()) {
//                    if (target.getLocation().distance(position) < 2) {
//                        isLockOnPlayer = true;
//                        break check;
//                    }
//                }
//            }
//            for (Entity as : player.getWorld().getEntities()) {
//                if (as instanceof ArmorStand) {
//                    if (as.getCustomName() != null) {
//                        if (as.getLocation().distanceSquared(position) <= 4 /* 2*2 */) {
//                            isLockOnPlayer = true;
//                            break check;
//                        }
//                    }
//                }
//            }
//        }
        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true);

        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
        Vector vec = player.getLocation().getDirection().multiply(QuadroShootSpeed);
        double random = 0.1;
        vec.add(new Vector(Math.random() * random - random/2, 0, Math.random() * random - random/2));
        ball.setVelocity(vec);
        ball.setShooter(player);
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        buf.append("#QuadroArmsShotgunCounterShot");
        String name = buf.toString();
        DataMgr.mws.add(name);//
        ball.setCustomName(name);
        DataMgr.getMainSnowballNameMap().put(name, ball);
        DataMgr.setSnowballHitCount(name, 0);
        BukkitRunnable SpinnerTask = new BukkitRunnable(){
            int i = 0;
            int tick = 1;
            //Vector fallvec;
            Vector origvec = vec;
            Snowball inkball = ball;
            boolean addedFallVec = false;
            Player p = player;
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(QuadroShootSpeed/35);

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
        SpinnerTask.runTaskTimer(Main.getPlugin(), 0,1);
    }
}
