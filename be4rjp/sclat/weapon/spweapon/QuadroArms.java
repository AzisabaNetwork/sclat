
package be4rjp.sclat.weapon.spweapon;

import be4rjp.sclat.Main;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.Sphere;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.manager.SPWeaponMgr;
import be4rjp.sclat.manager.WeaponClassMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import be4rjp.sclat.raytrace.RayTrace;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class QuadroArms {
    private static HashMap<Player, Integer> Hash_Quadro_overheat = new HashMap<>();
    public static void setQuadroArms(Player player){
        DataMgr.getPlayerData(player).setIsUsingSP(true);
        DataMgr.getPlayerData(player).setIsUsingSS(true);
        SPWeaponMgr.setSPCoolTimeAnimation(player, 120);
        if(Hash_Quadro_overheat.containsKey(player)) {
            Hash_Quadro_overheat.replace(player,0);
        }else{
            Hash_Quadro_overheat.put(player,0);
        }
        BukkitRunnable it = new BukkitRunnable() {
            Player p = player;
            @Override
            public void run() {
                player.getInventory().clear();
                player.updateInventory();
                ItemStack item = new ItemStack(Material.SUGAR);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Quadro-BLUE");
                item.setItemMeta(meta);
                ItemStack item2 = new ItemStack(Material.SUGAR);
                ItemMeta meta2 = item2.getItemMeta();
                meta2.setDisplayName("Quadro-GREEN");
                item2.setItemMeta(meta2);
                ItemStack item3 = new ItemStack(Material.SUGAR);
                ItemMeta meta3 = item3.getItemMeta();
                meta3.setDisplayName("Quadro-RED");
                item3.setItemMeta(meta3);
                ItemStack item4 = new ItemStack(Material.SUGAR);
                ItemMeta meta4 = item4.getItemMeta();
                meta4.setDisplayName("Quadro-WHITE");
                item4.setItemMeta(meta4);
                for (int count = 0; count < 9; count++){
                    if(count % 2 != 0)
                        player.getInventory().setItem(count, new ItemStack(Material.AIR));
                }
                player.getInventory().setItem(0, item);
                player.getInventory().setItem(2, item2);
                player.getInventory().setItem(4, item3);
                player.getInventory().setItem(6, item4);
                player.updateInventory();
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 121, 1));
                overheat_bar(player);
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
        task.runTaskLater(Main.getPlugin(), 120);
    }

    public static void overheat_bar(Player player){
        BossBar bar = Main.getPlugin().getServer().createBossBar(DataMgr.getPlayerData(player).getTeam().getTeamColor().getColorCode() + "§Quadro_overheat", BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG);
        bar.setProgress(0);
        bar.addPlayer(player);

        BukkitRunnable overheat_anime = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);
                if( Hash_Quadro_overheat.get(p)< 47) {
                    bar.setProgress((double)Hash_Quadro_overheat.get(p) / 47);
                    if (!bar.getPlayers().contains(p))
                        bar.addPlayer(p);
                }else {
                    bar.setProgress(1);
                    if (!bar.getPlayers().contains(p))
                        bar.addPlayer(p);
                }
                if(!DataMgr.getPlayerData(p).isInMatch() || !p.isOnline()){
                    bar.removeAll();
                    cancel();
                }
                if(!DataMgr.getPlayerData(p).getIsUsingSS()){
                    bar.removeAll();
                    cancel();
                }
            }
        };
        overheat_anime.runTaskTimer(Main.getPlugin(), 0, 2);
    }
    public static void QuadroCooltime(Player player,int i){
        PlayerData data = DataMgr.getPlayerData(player);
        BukkitRunnable delay1 = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(player);
                data.setCanUseSubWeapon(true);
            }
        };
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                Burstshoot(player, false);
            }
        };
        BukkitRunnable delaySG = new BukkitRunnable(){
            final Player p = player;
            @Override
            public void run(){
                boolean sound = false;
                Burstshoot(player, true);
                int overheatgage = Hash_Quadro_overheat.get(p);
                if(overheatgage>47){
                    Hash_Quadro_overheat.replace(p,overheatgage-13);
                }else if(overheatgage>10){
                    Hash_Quadro_overheat.replace(p,overheatgage-10);
                }else if(overheatgage<=10){
                    Hash_Quadro_overheat.replace(p,0);
                }
                player.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9F, 1.3F);
                if(sound){
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                }
            }
        };

        BukkitRunnable delaySL = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                ShootQuadroSlosher(player);
                int overheatgage = Hash_Quadro_overheat.get(p);
                if(overheatgage<= 13){
                    Hash_Quadro_overheat.replace(p,0);
                }else{
                    Hash_Quadro_overheat.replace(p,overheatgage-13);
                }
            }
        };
        BukkitRunnable delaySE = new BukkitRunnable(){
            Player p = player;
            @Override
            public void run(){
                ShootSensor(player);
                int overheatgage = Hash_Quadro_overheat.get(p);
                if(overheatgage<= 10){
                    Hash_Quadro_overheat.replace(p,0);
                }else{
                    Hash_Quadro_overheat.replace(p,overheatgage-10);
                }
            }
        };
        switch(i) {
            case 1:
                delay.runTaskLater(Main.getPlugin(), 1);
                delay1.runTaskLater(Main.getPlugin(), 6);
                break;
            case 2:
                delaySG.runTaskLater(Main.getPlugin(), 1);
                delay1.runTaskLater(Main.getPlugin(), 10);
                break;
            case 3:
                delaySL.runTaskLater(Main.getPlugin(), 1);
                delay1.runTaskLater(Main.getPlugin(), 17);
                break;
            case 4:
                delaySE.runTaskLater(Main.getPlugin(), 1);
                delay1.runTaskLater(Main.getPlugin(), 15);
        }
    }
    public static void Burstshoot(Player player ,boolean IsSG ){
        BukkitRunnable Bursttask = new BukkitRunnable(){
            Player p = player;
            int c = 0;
            @Override
            public void run(){
                c++;
                int q = 7;
                int overheatgage =Hash_Quadro_overheat.get(p);
                if (overheatgage>47){
                    player.sendTitle("", ChatColor.RED + "オーバーヒート!!!", 0, 5, 2);
                    cancel();
                }else{
                    Hash_Quadro_overheat.replace(p,overheatgage+1);
                }
                ShootSpinner(p);
                if(c == q)
                    cancel();
            }
        };
        BukkitRunnable BursttaskSG = new BukkitRunnable(){
            Player p = player;
            int c = 0;
            @Override
            public void run(){
                c++;
                int q = 3;
                for (int i = 0; i < 5; i++) {
                    ShootSG(p);
                }
                if(c == q) {
                    cancel();
                }
            }
        };
        if(player.hasPotionEffect(PotionEffectType.LUCK)) {
            if (!IsSG) {
                Bursttask.runTaskTimer(Main.getPlugin(), 0, 1);
            } else {
                BursttaskSG.runTaskTimer(Main.getPlugin(), 0, 1);
            }
        }
    }
    public static void ShootSpinner(Player player){

        double QuadroShootSpeed = 4.3;
        int QuadroDisTick = 2;
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = DataMgr.getPlayerData(player);
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(QuadroShootSpeed * QuadroDisTick,0.7);
        boolean isLockOnPlayer = false;
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
        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true);

        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
        Vector vec = player.getLocation().getDirection().multiply(QuadroShootSpeed);
        double random = 0.32;
        int distick = QuadroDisTick;
        vec.add(new Vector(Math.random() * random - random/2, 0, Math.random() * random - random/2));
        ball.setVelocity(vec);
        ball.setShooter(player);
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        buf.append("#QuadroArmsSpinner");
        String name = buf.toString();
        DataMgr.mws.add(name);//
        ball.setCustomName(name);
        DataMgr.getMainSnowballNameMap().put(name, ball);
        DataMgr.setSnowballHitCount(name, 0);
        BukkitRunnable SpinnerTask = new BukkitRunnable(){
            int i = 0;
            int tick = 3;
            //Vector fallvec;
            Vector origvec = vec;
            Snowball inkball = ball;
            boolean addedFallVec = false;
            Player p = player;
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(QuadroShootSpeed/14);

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
    public static  boolean ShootSG(Player player) {

        if(player.getGameMode() == GameMode.SPECTATOR) return false;
        double ShootSpeed = 4.5;
        PlayerData data = DataMgr.getPlayerData(player);
        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        Vector vec = player.getLocation().getDirection().multiply(ShootSpeed);
        double random = 1.2;
        int distick = 2;
        vec.add(new Vector(Math.random() * random - random/2, Math.random() * random/1.5 - random/3, Math.random() * random - random/2));
        ball.setVelocity(vec);
        ball.setShooter(player);
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        buf.append("#QuadroArmsShotgun");
        String name = buf.toString();
        DataMgr.mws.add(name);
        ball.setCustomName(name);
        DataMgr.getMainSnowballNameMap().put(name, ball);
        DataMgr.setSnowballHitCount(name, 0);
        BukkitRunnable task = new BukkitRunnable(){
            int i = 0;
            int tick = distick;
            Snowball inkball = ball;
            Player p = player;
            boolean addedFallVec = false;
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(ShootSpeed/150);
            @Override
            public void run(){
                inkball = DataMgr.getMainSnowballNameMap().get(name);

                if(!inkball.equals(ball)){
                    i+=DataMgr.getSnowballHitCount(name) - 1;
                    DataMgr.setSnowballHitCount(name, 0);
                }

                if(i != 0) {
                    for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                        if (!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
                            continue;
                        if (target.getWorld() == inkball.getWorld()) {
                            if (target.getLocation().distanceSquared(inkball.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                                target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 1, 0, 0, 0, 1, bd);
                            }
                        }
                    }
                }

                if(i >= tick && !addedFallVec){
                    inkball.setVelocity(fallvec);
                    addedFallVec = true;
                }
                if(i >= tick && i <= tick + 15)
                    inkball.setVelocity(inkball.getVelocity().add(new Vector(0, -0.1, 0)));
                if(i != tick)
                    PaintMgr.PaintHightestBlock(inkball.getLocation(), p, true, true);
                if(inkball.isDead())
                    cancel();

                i++;
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);

        return false;
    }
    public static void ShootQuadroSlosher(Player player){

        if(player.getGameMode() == GameMode.SPECTATOR) return;

        if(!player.hasPotionEffect(PotionEffectType.LUCK)) {return;}
        double ShootSpeed = 3.9;
        PlayerData data = DataMgr.getPlayerData(player);
        Snowball ball = player.launchProjectile(Snowball.class);
        ((CraftSnowball)ball).getHandle().setItem(CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
        Vector vec = player.getLocation().getDirection().multiply(ShootSpeed);
        int distick = 2;
        ball.setVelocity(vec);
        ball.setShooter(player);
        String originName = String.valueOf(Main.getNotDuplicateNumber());
        StringBuilder buf = new StringBuilder();
        buf.append(originName);
        buf.append("#QuadroArmsSpinner");
        String name = buf.toString();
        ball.setCustomName(name);
        DataMgr.mws.add(name);
        DataMgr.getMainSnowballNameMap().put(name, ball);
        DataMgr.setSnowballHitCount(name, 0);
        BukkitRunnable task = new BukkitRunnable(){
            int i = 0;
            int tick = distick;
            Snowball inkball = ball;
            Player p = player;
            boolean addedFallVec = false;
            double BlasterExDamage =3.1;
            double BlasterExHankei=4;
            Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY()  , inkball.getVelocity().getZ()).multiply(ShootSpeed/17);
            @Override
            public void run(){
                try{
                    inkball = DataMgr.getMainSnowballNameMap().get(name);

                    if(!inkball.equals(ball)){
                        i+=DataMgr.getSnowballHitCount(name) - 1;
                        DataMgr.setSnowballHitCount(name, 0);
                    }
                    for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                        if(!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
                            continue;
                        if(target.getWorld() == inkball.getWorld()){
                            if(target.getLocation().distanceSquared(inkball.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED){
                                org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool().createBlockData();
                                target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 3, 0, 0, 0, 1, bd);
                            }
                        }
                    }


                    PaintMgr.PaintHightestBlock(inkball.getLocation(), p, false, true);

                    if(i >= tick && !addedFallVec){
                        inkball.setVelocity(fallvec);
                        addedFallVec = true;
                    }
                    if(i >= tick && i <= tick + 15)
                        inkball.setVelocity(inkball.getVelocity().add(new Vector(0, -0.1, 0)));
                    if(inkball.isDead()){
                        //半径
                        double maxDist = BlasterExHankei;

                        //爆発音
                        player.getWorld().playSound(inkball.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7F, 1);

                        //爆発エフェクト
                        Sclat.createInkExplosionEffect(inkball.getLocation(), maxDist, 25, player);

                        //塗る
                        for(int i = 0; i <= maxDist; i++){
                            List<Location> p_locs = Sphere.getSphere(inkball.getLocation(), i, 20);
                            for(Location loc : p_locs){
                                PaintMgr.Paint(loc, p, false);
                                PaintMgr.PaintHightestBlock(loc, p, false, false);
                            }
                        }

                        //攻撃判定の処理
                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(!DataMgr.getPlayerData(target).isInMatch())
                                continue;
                            if (target.getLocation().distanceSquared(inkball.getLocation()) <= maxDist*maxDist) {
                                double damage = (1 + maxDist - target.getLocation().distance(inkball.getLocation())) * BlasterExDamage;
                                if(DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)){
                                    Sclat.giveDamage(player, target, damage, "spWeapon");

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
                            if(as instanceof ArmorStand){
                                if (as.getLocation().distanceSquared(inkball.getLocation()) <= maxDist*maxDist) {
                                    double damage = ( 1 + maxDist - as.getLocation().distance(inkball.getLocation())) * BlasterExDamage;
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, p);
                                }
                            }
                        }
                        cancel();
                    }

                    i++;
                }catch(Exception e){
                    cancel();
                }
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
    public static void ShootSensor(Player player){
        if(!player.hasPotionEffect(PotionEffectType.LUCK)) {return;}
        BukkitRunnable task = new BukkitRunnable(){
            Player p = player;
            Vector p_vec;
            double x = 0;
            double z = 0;
            boolean collision = false;
            boolean block_check = false;
            int c = 0;
            Item drop;
            Snowball ball;
            @Override
            public void run(){
                try{
                    if(c == 0){
                        p_vec = p.getEyeLocation().getDirection().multiply(1.35);
                        ItemStack bom = new ItemStack(Material.DISPENSER).clone();
                        ItemMeta bom_m = bom.getItemMeta();
                        bom_m.setLocalizedName(String.valueOf(Main.getNotDuplicateNumber()));
                        bom.setItemMeta(bom_m);
                        drop = p.getWorld().dropItem(p.getEyeLocation(), bom);
                        drop.setVelocity(p_vec);
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

                    if(DataMgr.getSnowballIsHit(ball) || drop.isOnGround()){

                        //半径
                        double maxDist = 9.0;

                        //爆発音
                        player.getWorld().playSound(drop.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 2);

                        //爆発エフェクト
                        List<Location> s_locs = Sphere.getSphere(drop.getLocation(), maxDist, 15);
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(o_player).getSettings().ShowEffect_BombEx()){
                                for(Location loc : s_locs){
                                    if(o_player.getWorld() == loc.getWorld()){
                                        if(o_player.getLocation().distanceSquared(loc) < Main.PARTICLE_RENDER_DISTANCE_SQUARED){
                                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLACK, 1);
                                            o_player.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 1, dustOptions);
                                        }
                                    }
                                }
                            }
                        }

                        //あたり判定の処理

                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(!DataMgr.getPlayerData(target).isInMatch() || target.getWorld() != p.getWorld())
                                continue;
                            if (target.getLocation().distance(drop.getLocation()) <= maxDist) {
                                if(DataMgr.getPlayerData(player).getTeam().getID() != DataMgr.getPlayerData(target).getTeam().getID()){
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 240, 1));
                                }

                            }
                        }

                        for(Entity as : player.getWorld().getEntities()){
                            if (as.getLocation().distance(drop.getLocation()) <= maxDist){
                                if(as.getCustomName() != null){
                                    if(as.getCustomName() == null) continue;
                                    if(as instanceof ArmorStand && !as.getCustomName().equals("Path") && !as.getCustomName().equals("21") && !as.getCustomName().equals("100")&& !as.getCustomName().equals("SplashShield") && !as.getCustomName().equals("Kasa")){
                                        ((ArmorStand)as).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 240, 1));
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


                    if(c > 1000){
                        drop.remove();
                        cancel();
                        return;
                    }
                }catch(Exception e){
                    cancel();
                    drop.remove();
                    Main.getPlugin().getLogger().warning(e.getMessage());
                }
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}

