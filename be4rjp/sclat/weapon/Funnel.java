package be4rjp.sclat.weapon;

import be4rjp.sclat.GlowingAPI;
import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.*;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.raytrace.BoundingBox;
import be4rjp.sclat.raytrace.RayTrace;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Funnel {

    static HashMap<ArmorStand,Player> HashPlayer = new HashMap<>();
    static HashMap<ArmorStand,ArmorStand> HashArmorstand = new HashMap<>();
    static int FunnelMaxHP = 10;
    static int FunnelMaxHP2 = 3;
    static double FunnelSpeed = 1.0;
    public static void FunnelShot(Player player, ArmorStand funnel, Location taegetloc) {
        double damage = 3.0;
        Location funloc = funnel.getEyeLocation();
        if(player.getGameMode() == GameMode.SPECTATOR) return;
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5);
        RayTrace rayTrace = new RayTrace(funloc.toVector(),new Vector(taegetloc.getX()-funloc.getX(),taegetloc.getY()-funloc.getY(),taegetloc.getZ()-funloc.getZ()).normalize());
        ArrayList<Vector> positions = rayTrace.traverse(4, 0.2);


        loop : for(int i = 0; i < positions.size();i++) {

            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if (!block.getType().equals(Material.AIR)) {
                break loop;
            }
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
                if (!DataMgr.getPlayerData(target).isInMatch())
                    continue;
                if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(new BoundingBox((Entity) target), 4, 0.05)) {
                            Sclat.giveDamage(player, target, damage, "killed");
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2F, 1.3F);

                            //AntiNoDamageTime
                            BukkitRunnable task = new BukkitRunnable() {
                                Player p = target;

                                @Override
                                public void run() {
                                    target.setNoDamageTicks(0);
                                }
                            };
                            task.runTaskLater(Main.getPlugin(), 1);
                            break loop;
                        }
                    }
                }
            }

            for (Entity as : player.getWorld().getEntities()) {
                if (as instanceof ArmorStand) {
                    if (as.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(new BoundingBox((Entity) as), 4, 0.05)) {
                            if (as.getCustomName() != null) {
                                if (as.getCustomName().equals("SplashShield")) {
                                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
                                    if (DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                } else if (as.getCustomName().equals("Kasa")) {
                                    KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
                                    if (DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                } else {
                                    if (Sclat.isNumber(as.getCustomName()))
                                        if (!as.getCustomName().equals("21") && !as.getCustomName().equals("100"))
                                            if (((ArmorStand) as).isVisible())
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
    }

    public static void FunnelFloat(Player player) {
        KasaData kdata = new KasaData(player);
        KasaData kdata1 = new KasaData(player);
        KasaData kdata2 = new KasaData(player);
        DataMgr.setKasaDataWithPlayer(player, kdata);
        DataMgr.setKasaDataWithPlayer(player, kdata1);
        DataMgr.setKasaDataWithPlayer(player, kdata2);

        BukkitRunnable task = new BukkitRunnable() {
            Player p = player;

            PlayerData data = DataMgr.getPlayerData(p);
            int i = 0;

            List<ArmorStand> list = new ArrayList<ArmorStand>();
            List<ArmorStand> list1 = new ArrayList<ArmorStand>();
            List<ArmorStand> list2 = new ArrayList<ArmorStand>();
            List<List<ArmorStand>> list5 = new ArrayList<List<ArmorStand>>();
            List<ArmorStand> list6 = new ArrayList<ArmorStand>();

            ArmorStand as1;
            ArmorStand as2;
            ArmorStand as3;

            ArmorStand as11;
            ArmorStand as12;
            ArmorStand as13;

            ArmorStand as21;
            ArmorStand as22;
            ArmorStand as23;
            ArmorStand las;
            Boolean check = false;
            int kdataReset = -1;
            int kdataReset1 = -1;
            int kdataReset2 = -1;

            @Override
            public void run() {
                try{
                    //Location loc = p.getLocation().add(0, -1.7, 0);
                    Location locp = p.getLocation();
                    Vector pv = p.getEyeLocation().getDirection().normalize();
                    Vector vec = new Vector(pv.getX(), 0, pv.getZ()).normalize();
                    Vector vec1;
                    Vector vec2;
                    Vector l1 = new Vector(vec.clone().getZ() * -1, 0, vec.clone().getX());
                    Vector r1 = new Vector(vec.clone().getZ(), 0, vec.clone().getX() * -1);
                    BukkitRunnable taskcheck = new BukkitRunnable() {
                        @Override
                        public void run() {
                            check = true;
                        }
                    };
                    BukkitRunnable listremove = new BukkitRunnable() {
                        @Override
                        public void run() {
                            try{
                                if(HashPlayer.containsKey(list.get(0))){
                                    HashPlayer.remove(list.get(0));
                                }
                                if(HashArmorstand.containsKey(list.get(0))){
                                    HashArmorstand.remove(list.get(0));
                                }
                                data.subArmorlist(list.get(0));
                                for(ArmorStand as :list) {
                                    as.remove();
                                }
                            }catch(Exception e){
                            }
                            list.clear();
                            as3 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.5, 0), EntityType.ARMOR_STAND);
                            as1 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.8, 0), EntityType.ARMOR_STAND);
                            as2 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.8, 0), EntityType.ARMOR_STAND);
                            as1.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-40)));
                            as2.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(40)));
                            list.add(as3);
                            list.add(as1);
                            list.add(as2);
                            data.setArmorlist(as3);
                            GlowingAPI.setGlowing(as3, player, false);
                            for (ArmorStand as : list) {
                                as.setSmall(true);
                                as.setBasePlate(false);
                                as.setVisible(false);
                                as.setGravity(false);
                                as.setCustomName("Kasa");
                                DataMgr.setKasaDataWithARmorStand(as, kdata);
                            }
                            Team team = data.getTeam();
                            for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                            }
                            list6.add(as3);
                            kdata.setDamage(0);
                            kdata.setArmorStandList(list);
                            cancel();
                        }
                    };BukkitRunnable listremove1 = new BukkitRunnable() {
                        @Override
                        public void run() {
                            try{
                                if(HashPlayer.containsKey(list1.get(0))){
                                    HashPlayer.remove(list1.get(0));
                                }
                                if(HashArmorstand.containsKey(list1.get(0))){
                                    HashArmorstand.remove(list1.get(0));
                                }
                                data.subArmorlist(list1.get(0));
                                for(ArmorStand as :list1) {
                                    as.remove();
                                }
                            }catch(Exception e){
                            }
                            list1.clear();
                            as13 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as11 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as12 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as11.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-40)));
                            as12.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(40)));
                            list1.add(as13);
                            list1.add(as11);
                            list1.add(as12);
                            data.setArmorlist(as13);
                            GlowingAPI.setGlowing(as13, player, false);
                            for (ArmorStand as : list1) {
                                as.setSmall(true);
                                as.setBasePlate(false);
                                as.setVisible(false);
                                as.setGravity(false);
                                as.setCustomName("Kasa");
                                DataMgr.setKasaDataWithARmorStand(as, kdata1);
                            }
                            Team team = data.getTeam();
                            for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list1.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list1.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list1.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                            }
                            list6.add(as13);
                            kdata1.setDamage(0);
                            kdata1.setArmorStandList(list1);
                            cancel();
                        }
                    };BukkitRunnable listremove2 = new BukkitRunnable() {
                        @Override
                        public void run() {
                            try{
                                if(HashPlayer.containsKey(list2.get(0))){
                                    HashPlayer.remove(list2.get(0));
                                }
                                if(HashArmorstand.containsKey(list2.get(0))){
                                    HashArmorstand.remove(list2.get(0));
                                }
                                data.subArmorlist(list2.get(0));
                                for(ArmorStand as :list2) {
                                    as.remove();
                                }
                            }catch(Exception e){
                            }
                            list2.clear();
                            as23 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as21 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as22 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                            as21.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-40)));
                            as22.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(40)));
                            list2.add(as23);
                            list2.add(as21);
                            list2.add(as22);
                            data.setArmorlist(as23);
                            GlowingAPI.setGlowing(as23, player, false);
                            for (ArmorStand as : list2) {
                                as.setSmall(true);
                                as.setBasePlate(false);
                                as.setVisible(false);
                                as.setGravity(false);
                                as.setCustomName("Kasa");
                                DataMgr.setKasaDataWithARmorStand(as, kdata2);
                            }
                            Team team = data.getTeam();
                            for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list2.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list2.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(list2.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                            }
                            list6.add(as23);
                            kdata2.setDamage(0);
                            kdata2.setArmorStandList(list2);
                            cancel();
                        }
                    };
                    if(i == 0){
                        as3 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.5, 0), EntityType.ARMOR_STAND);
                        pv = as3.getEyeLocation().getDirection().normalize();
                        vec1 = new Vector(pv.clone().getX()*0.707 - pv.clone().getZ()*0.707, 0, pv.clone().getX()*0.707 + pv.clone().getZ()*0.707).normalize();
                        vec2 = new Vector(pv.clone().getX()*0.707 + pv.clone().getZ()*0.707, 0, -pv.clone().getX()*0.707 + pv.clone().getZ()*0.707).normalize();
                        as1 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.8, 0).add(vec1.clone().multiply(0.3)), EntityType.ARMOR_STAND);
                        as2 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 2.8, 0).add(vec2.clone().multiply(0.3)), EntityType.ARMOR_STAND);
                        list.add(as3);
                        list.add(as1);
                        list.add(as2);
                        as13 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        as11 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1.3, 0).add(vec1.clone().multiply(0.3)).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        as12 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1.3, 0).add(vec2.clone().multiply(0.3)).add(l1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        list1.add(as13);
                        list1.add(as11);
                        list1.add(as12);
                        as23 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1, 0).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        as21 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1.3, 0).add(vec1.clone().multiply(0.3)).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        as22 = (ArmorStand)p.getWorld().spawnEntity(locp.clone().add(0, 1.3, 0).add(vec2.clone().multiply(0.3)).add(r1.clone().multiply(1.5)), EntityType.ARMOR_STAND);
                        list2.add(as23);
                        list2.add(as21);
                        list2.add(as22);

                        list5.add(list);
                        list5.add(list1);
                        list5.add(list2);
                        data.setArmorlist(as3);
                        data.setArmorlist(as13);
                        data.setArmorlist(as23);
                        list6.add(as3);
                        list6.add(as13);
                        list6.add(as23);
                        kdata.setArmorStandList(list);
                        kdata1.setArmorStandList(list1);
                        kdata2.setArmorStandList(list2);
                        kdata.setDamage(0);
                        kdata1.setDamage(0);
                        kdata2.setDamage(0);
                        for (ArmorStand as : list) {
                            DataMgr.setKasaDataWithARmorStand(as, kdata);
                        }
                        for (ArmorStand as : list1) {
                            DataMgr.setKasaDataWithARmorStand(as, kdata1);
                        }
                        for (ArmorStand as : list2) {
                            DataMgr.setKasaDataWithARmorStand(as, kdata2);
                        }
                        for(List<ArmorStand> aslist :list5) {
                            aslist.get(1).setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(-40)));
                            aslist.get(2).setHeadPose(new EulerAngle(Math.toRadians(-45), 0, Math.toRadians(40)));
                            for (ArmorStand as : aslist) {
                                as.setSmall(true);
                                as.setBasePlate(false);
                                as.setVisible(false);
                                as.setGravity(false);
                                as.setCustomName("Kasa");
                            }
                        }
                        Team team = data.getTeam();
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                            for(List<ArmorStand> aslist :list5) {
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                            }
                        }
                        taskcheck.runTaskLater(Main.getPlugin(), 20);
                    }
                    if(i >= 0){
                        //ファンネル破壊時の復活処理
                        if(p.getGameMode() == GameMode.SPECTATOR){
                            if(kdata.getDamage()<=FunnelMaxHP){
                                kdata.setDamage(1024);
                            }
                            if(kdata1.getDamage()<=FunnelMaxHP){
                                kdata1.setDamage(1024);
                            }
                            if(kdata2.getDamage()<=FunnelMaxHP){
                                kdata2.setDamage(1024);
                            }
                        }
                        if(kdata.getDamage()>FunnelMaxHP && kdata.getDamage()<9999){
                            ArmorStand kasaStand = kdata.getArmorStandList().get(0);
                            data.subArmorlist(kasaStand);
                            if(HashPlayer.containsKey(kasaStand)){
                                if(HashPlayer.get(kasaStand).getGameMode() != GameMode.SPECTATOR){
                                    kdataReset+=60;
                                }
                                HashPlayer.remove(kasaStand);
                            }else
                            if(HashArmorstand.containsKey(kasaStand)) {
                                kdataReset+=60;
                                HashArmorstand.remove(kasaStand);
                            }else{
                                list6.remove(kasaStand);
                                if(kdata.getDamage()==1024){
                                    listremove.runTaskLater(Main.getPlugin(), 110);
                                }else {
                                    listremove.runTaskLater(Main.getPlugin(), 160);
                                }
                            }
                            kdata.setDamage(10000);
                            for (ArmorStand as : kdata.getArmorStandList()) {
                                as.remove();
                            }
                        }
                        if(kdata1.getDamage()>FunnelMaxHP && kdata1.getDamage()<9999){
                            ArmorStand kasaStand1 = kdata1.getArmorStandList().get(0);
                            data.subArmorlist(kasaStand1);
                            if(HashPlayer.containsKey(kasaStand1)){
                                if(HashPlayer.get(kasaStand1).getGameMode() != GameMode.SPECTATOR){
                                    kdataReset1+=60;
                                }
                                HashPlayer.remove(kasaStand1);
                            }else
                            if(HashArmorstand.containsKey(kasaStand1)) {
                                kdataReset1+=60;
                                HashArmorstand.remove(kasaStand1);
                            }else{
                                list6.remove(kasaStand1);
                                if(kdata1.getDamage()==1024){
                                    listremove1.runTaskLater(Main.getPlugin(), 110);
                                }else {
                                    listremove1.runTaskLater(Main.getPlugin(), 160);
                                }
                            }
                            kdata1.setDamage(10000);
                            for (ArmorStand as : kdata1.getArmorStandList()) {
                                as.remove();
                            }
                        }
                        if(kdata2.getDamage()>FunnelMaxHP && kdata2.getDamage()<9999){
                            ArmorStand kasaStand2 = kdata2.getArmorStandList().get(0);
                            data.subArmorlist(kasaStand2);
                            if(HashPlayer.containsKey(kasaStand2)){
                                if(HashPlayer.get(kasaStand2).getGameMode() != GameMode.SPECTATOR){
                                    kdataReset2+=60;
                                }
                                HashPlayer.remove(kasaStand2);
                            }else if(HashArmorstand.containsKey(kasaStand2)) {
                                kdataReset2+=60;
                                HashArmorstand.remove(kasaStand2);
                            }else{
                                list6.remove(kasaStand2);
                                if(kdata2.getDamage()==1024){
                                    listremove2.runTaskLater(Main.getPlugin(), 110);
                                }else {
                                    listremove2.runTaskLater(Main.getPlugin(), 160);
                                }
                            }
                            kdata2.setDamage(10000);
                            for (ArmorStand as : kdata2.getArmorStandList()) {
                                as.remove();
                            }
                        }
                        if(i==kdataReset){
                            listremove.runTaskLater(Main.getPlugin(), 1);
                            kdataReset=-1;
                        }
                        if(i==kdataReset1){
                            listremove1.runTaskLater(Main.getPlugin(), 1);
                            kdataReset1=-1;
                        }
                        if(i==kdataReset2){
                            listremove2.runTaskLater(Main.getPlugin(), 1);
                            kdataReset2=-1;
                        }
                        //ファンネル破壊時の復活処理了
                        pv = new Vector(p.getEyeLocation().getDirection().normalize().getX(),0,p.getEyeLocation().getDirection().normalize().getZ());
                        int io = 0;
                        for(List<ArmorStand> aslist :list5) {
                            ArmorStand aslistget0 = aslist.get(0);
                            if(io==0) {
                                if (as3 != null) {
                                    if (!HashPlayer.containsKey(aslistget0) && !HashArmorstand.containsKey(aslistget0)) {
                                        aslistget0.teleport(locp.clone().add(0, 2.5, 0));
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashPlayer.get(aslistget0).getLocation().add(pv.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==0) {
                                            if(!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).getIsUsingSP()){
                                                Funnel.FunnelShot(p, aslistget0, HashPlayer.get(aslistget0).getEyeLocation());
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if((HashPlayer.get(aslistget0).getGameMode() == GameMode.SPECTATOR ||!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).isInMatch() || !HashPlayer.get(aslistget0).isOnline()) && kdata.getDamage()<FunnelMaxHP){
                                            kdata.setDamage(FunnelMaxHP+1);
                                            kdataReset=i+3;
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashArmorstand.get(aslistget0).getLocation().add(pv.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==0) {
                                            Funnel.FunnelShot(p, aslistget0, HashArmorstand.get(aslistget0).getEyeLocation());
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if(!HashArmorstand.get(aslistget0).isVisible()){
                                            kdataReset=i+3;
                                            kdata.setDamage(FunnelMaxHP+1);
                                        }
                                    }
                                    if(i%20==0) {
                                        Team team = data.getTeam();
                                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                                        }
                                    }
                                }else{
                                    p.sendMessage("as3isNULL");
                                }
                            }
                            if(io==1){
                                if (as13 != null) {
                                    if (!HashPlayer.containsKey(aslistget0) && !HashArmorstand.containsKey(aslistget0)) {
                                        aslistget0.teleport(locp.clone().add(0, 1, 0).add(l1.clone().multiply(1.5)));
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashPlayer.get(aslistget0).getLocation().add(l1.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==16) {
                                            if(!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).getIsUsingSP()) {
                                                Funnel.FunnelShot(p, aslistget0, HashPlayer.get(aslistget0).getEyeLocation());
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if((HashPlayer.get(aslistget0).getGameMode() == GameMode.SPECTATOR ||!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).isInMatch() || !HashPlayer.get(aslistget0).isOnline()) && kdata1.getDamage()<FunnelMaxHP){
                                            kdataReset1=i+3;
                                            kdata1.setDamage(FunnelMaxHP+1);
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashArmorstand.get(aslistget0).getLocation().add(l1.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==16) {
                                            Funnel.FunnelShot(p, aslistget0, HashArmorstand.get(aslist.get(0)).getEyeLocation());
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if(!HashArmorstand.get(aslistget0).isVisible()){
                                            kdataReset1=i+3;
                                            kdata1.setDamage(FunnelMaxHP+1);
                                        }
                                    }
                                    if(i%20==0) {
                                        Team team = data.getTeam();
                                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                                        }
                                    }
                                }else{
                                    p.sendMessage("as13isNull");
                                }
                            }
                            if(io==2){
                                if (as23 != null) {
                                    if (!HashPlayer.containsKey(aslistget0) && !HashArmorstand.containsKey(aslist.get(0))) {
                                        aslistget0.teleport(locp.clone().add(0, 1, 0).add(r1.clone().multiply(1.5)));
                                    } else if (HashPlayer.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashPlayer.get(aslistget0).getLocation().add(r1.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==32) {
                                            if(!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).getIsUsingSP()) {
                                                Funnel.FunnelShot(p, aslistget0, HashPlayer.get(aslistget0).getEyeLocation());
                                            }
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if((HashPlayer.get(aslistget0).getGameMode() == GameMode.SPECTATOR ||!DataMgr.getPlayerData(HashPlayer.get(aslistget0)).isInMatch() || !HashPlayer.get(aslistget0).isOnline()) && kdata2.getDamage()<FunnelMaxHP){
                                            kdataReset2=i+3;
                                            kdata2.setDamage(FunnelMaxHP+1);
                                        }
                                    } else if (HashArmorstand.containsKey(aslistget0)) {
                                        Location las = aslistget0.getLocation();
                                        Location lpl = HashArmorstand.get(aslistget0).getLocation().add(r1.clone().multiply(2).add(new Vector(0, 1.4, 0)));
                                        pv = new Vector(lpl.getX() - las.getX(), lpl.getY() - las.getY(), lpl.getZ() - las.getZ());
                                        if(i%48==32) {
                                            Funnel.FunnelShot(p, aslistget0, HashArmorstand.get(aslistget0).getEyeLocation());
                                        }
                                        if (pv.length() > 1) {
                                            if (!aslistget0.hasGravity()) {
                                                aslistget0.setGravity(true);
                                            }
                                            aslistget0.setVelocity(pv.normalize().multiply(FunnelSpeed));
                                        } else {
                                            if (aslistget0.hasGravity()) {
                                                aslistget0.setGravity(false);
                                            }
                                            aslistget0.teleport(lpl);
                                        }
                                        if(!HashArmorstand.get(aslistget0).isVisible()){
                                            kdataReset2=i+3;
                                            kdata2.setDamage(FunnelMaxHP+1);
                                        }
                                    }
                                    //残数表記
                                    if(i%20==0) {
                                        if(p.getGameMode() != GameMode.SPECTATOR) {
                                            int funnelamo = Funnelamount(player);
                                            ItemStack nuget;
                                            if (funnelamo > 0) {
                                                nuget = new ItemStack(Material.GOLD_NUGGET, funnelamo);
                                            } else {
                                                nuget = new ItemStack(Material.AIR);
                                            }
                                            player.getInventory().setItem(8, nuget);
                                        }
                                        //残数表記了
                                        Team team = data.getTeam();
                                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers()) {
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(2).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(1).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial(team.getTeamColor().getGlass().toString() + "_PANE")))));
                                            ((CraftPlayer) o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(aslist.get(0).getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(team.getTeamColor().getWool()))));
                                        }
                                    }
                                }else{
                                    p.sendMessage("as23isNull");
                                }
                            }
                            io++;
                        }
                        for(List<ArmorStand> aslist :list5) {
                            pv = aslist.get(0).getEyeLocation().getDirection().normalize();
                            vec1 = new Vector(pv.clone().getX()*0.707 - pv.clone().getZ()*0.707, 0, pv.clone().getX()*0.707 + pv.clone().getZ()*0.707).normalize();
                            vec2 = new Vector(pv.clone().getX()*0.707 + pv.clone().getZ()*0.707, 0, -pv.clone().getX()*0.707 + pv.clone().getZ()*0.707).normalize();
                            Location floc2 = aslist.get(0).getLocation().clone();
                            aslist.get(1).teleport(floc2.clone().add(0, 0.3, 0).add(vec1.clone().multiply(0.3)));
                            aslist.get(2).teleport(floc2.clone().add(0, 0.3, 0).add(vec2.clone().multiply(0.3)));
                        }
                    }
                    if(check && p.isSneaking() && p.getGameMode() != GameMode.SPECTATOR){
                        check=false;
                        taskcheck.runTaskLater(Main.getPlugin(), 18);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5);
                        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(),player.getEyeLocation().getDirection());
                        ArrayList<Vector> positions = rayTrace.traverse(55, 0.3);

                        loop : for(int it = 0; it < positions.size();it++){

                            Location position = positions.get(it).toLocation(player.getLocation().getWorld());
                            Block block = player.getLocation().getWorld().getBlockAt(position);

                            if(!block.getType().equals(Material.AIR)){
                                break loop;
                            }
                            if (!DataMgr.getPlayerData(player).getSettings().ShowEffect_MainWeaponInk())
                                continue;
                            if(it<10) {
                                if (player.getWorld() == position.getWorld()) {
                                    if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                                        player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
                                    }
                                }
                            }


                            double maxDistSquad = 20 /* 2*2 */;
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if(!DataMgr.getPlayerData(target).isInMatch())
                                    continue;
                                if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                                    if(target.getLocation().distanceSquared(position) <= maxDistSquad){
                                        //if(rayTrace.intersects(new BoundingBox((Entity)target), (30), 0.2)){
                                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 5);
                                            if(list6.size()>0) {
                                                if (list6.get(list6.size() - 1).equals(as3) && FunAmoP(target)) {
                                                    player.getWorld().playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);
                                                    HashPlayer.put(as3, target);
                                                    GlowingAPI.setGlowing(as3, player, true);
                                                    GlowingAPI.setGlowing(as3, target, true);
                                                    if(kdata.getDamage()<FunnelMaxHP2){
                                                        kdata.setDamage(FunnelMaxHP2);
                                                    }
                                                    as3.setGravity(true);
                                                    kdataReset=i+210;
                                                    //listremove.runTaskLater(Main.getPlugin(), 140);
                                                    list6.remove(list6.size() - 1);
                                                } else if (list6.get(list6.size() - 1).equals(as13) && FunAmoP(target)) {
                                                    player.getWorld().playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);
                                                    HashPlayer.put(as13, target);
                                                    GlowingAPI.setGlowing(as13, player, true);
                                                    GlowingAPI.setGlowing(as13, target, true);
                                                    if(kdata1.getDamage()<FunnelMaxHP2){
                                                        kdata1.setDamage(FunnelMaxHP2);
                                                    }
                                                    as13.setGravity(true);
                                                    kdataReset1=i+210;
                                                    //listremove1.runTaskLater(Main.getPlugin(), 140);
                                                    list6.remove(list6.size() - 1);
                                                } else if (list6.get(list6.size() - 1).equals(as23) && FunAmoP(target)) {
                                                    player.getWorld().playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);
                                                    HashPlayer.put(as23, target);
                                                    GlowingAPI.setGlowing(as23, player, true);
                                                    GlowingAPI.setGlowing(as23, target, true);
                                                    if(kdata2.getDamage()<FunnelMaxHP2){
                                                        kdata2.setDamage(FunnelMaxHP2);
                                                    }
                                                    as23.setGravity(true);
                                                    kdataReset2=i+210;
                                                    //listremove2.runTaskLater(Main.getPlugin(), 140);
                                                    list6.remove(list6.size() - 1);
                                                }
                                            }
                                            break loop;
                                        //}
                                    }
                                }
                            }

                            for(Entity as : player.getWorld().getEntities()){
                                if (as instanceof ArmorStand){
                                    if(as.getLocation().distanceSquared(position) <= maxDistSquad){
                                        //if(rayTrace.intersects(new BoundingBox((Entity)as), (int)(30), 0.2)){
                                            if(as.getCustomName() != null){
                                                if(as.getCustomName().equals("SplashShield")){
//                                                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand)as);
//                                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
//                                                        break loop;
//                                                    }
                                                }else if(as.getCustomName().equals("Kasa")){
//                                                    KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand)as);
//                                                    if(DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()){
//                                                        break loop;
//                                                    }
                                                }else{
                                                    if(Sclat.isNumber(as.getCustomName()))
                                                        if(!as.getCustomName().equals("21") && !as.getCustomName().equals("100"))
                                                            if(((ArmorStand) as).isVisible())
                                                                //player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                                                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 5);
                                                        if(list6.size()>0) {
                                                            if (list6.get(list6.size() - 1).equals(as3) && FunAmoA((ArmorStand) as)) {
                                                                player.getWorld().playSound(as.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);                                                                HashArmorstand.put(as3, (ArmorStand) as);
                                                                GlowingAPI.setGlowing(as3, player, true);
                                                                if(kdata.getDamage()<FunnelMaxHP2){
                                                                    kdata.setDamage(FunnelMaxHP2);
                                                                }
                                                                as3.setGravity(true);
                                                                kdataReset=i+210;
                                                                //listremove.runTaskLater(Main.getPlugin(), 140);
                                                                list6.remove(list6.size() - 1);
                                                            } else if (list6.get(list6.size() - 1).equals(as13) && FunAmoA((ArmorStand) as)) {
                                                                player.getWorld().playSound(as.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);
                                                                HashArmorstand.put(as13, (ArmorStand) as);
                                                                GlowingAPI.setGlowing(as13, player, true);
                                                                if(kdata1.getDamage()<FunnelMaxHP2){
                                                                    kdata1.setDamage(FunnelMaxHP2);
                                                                }
                                                                as13.setGravity(true);
                                                                kdataReset1=i+210;
                                                                //listremove1.runTaskLater(Main.getPlugin(), 140);
                                                                list6.remove(list6.size() - 1);
                                                            } else if (list6.get(list6.size() - 1).equals(as23) && FunAmoA((ArmorStand) as)) {
                                                                player.getWorld().playSound(as.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 2);
                                                                HashArmorstand.put(as23, (ArmorStand) as);
                                                                GlowingAPI.setGlowing(as23, player, true);
                                                                if(kdata2.getDamage()<FunnelMaxHP2){
                                                                    kdata2.setDamage(FunnelMaxHP2);
                                                                }
                                                                as23.setGravity(true);
                                                                kdataReset2=i+210;
                                                                //listremove2.runTaskLater(Main.getPlugin(), 140);
                                                                list6.remove(list6.size() - 1);
                                                            }
                                                        }
                                                    break loop;
                                                }
                                            }
                                            //ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                        //}
                                    }
                                }
                            }


                        }
                    }
                    if(!p.isOnline() || !data.isInMatch()){
                        if( DataMgr.getPlayerData(p).isInMatch())
                            as1.getWorld().playSound(as1.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8F, 0.8F);

                        for(List<ArmorStand> aslist :list5) {
                            for (ArmorStand as : aslist) {
                                as.remove();
                            }
                        }
                        las.remove();
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


    public static double FunnelPursuit(Player player,ArmorStand target) {
        double rate = 0;
        for(int ai = 0; ai<3; ai++) {
            try {
                if(HashArmorstand.containsKey(DataMgr.getPlayerData(player).getArmorlist(ai))){
                    if(HashArmorstand.get(DataMgr.getPlayerData(player).getArmorlist(ai)).equals(target)){
                        rate = rate + 1.5;
                    }
                }
            }catch (Exception e){
                rate = rate - 0.7;
            }
        }
        BukkitRunnable task = new BukkitRunnable() {
            Player p=player;
            PlayerData data = DataMgr.getPlayerData(player);
            Location loct = target.getLocation();
            Location locd ;

            @Override
            public void run(){
                for(int ai = 0; ai<3; ai++) {
                    try {
                        locd = data.getArmorlist(ai).getEyeLocation();
                        Vector vec = new Vector(loct.getX() - locd.getX(), loct.getY() - locd.getY() + 1.5, loct.getZ() - locd.getZ());
                        RayTrace rayTrace = new RayTrace(locd.toVector(), vec);
                        ArrayList<Vector> positions = rayTrace.traverse(vec.length(), 0.4);
                        double veclength = vec.length()/2;
                        if(veclength>12){
                            veclength=12;
                        }
                        for (int i = 0; i < veclength; i++) {
                            Location position = positions.get(i).toLocation(p.getLocation().getWorld());
                            if (player.getWorld() == position.getWorld()) {
                                if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                    Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                    player.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 3, dustOptions);
                                }
                            }
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if (target.equals(p) || !DataMgr.getPlayerData(target).getSettings().ShowEffect_ChargerLine())
                                    continue;
                                if (target.getWorld() == p.getWorld()) {
                                    if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                        Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                        target.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 3, dustOptions);
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                    }
                }
            }
        };
        task.runTaskLater(Main.getPlugin(), 1);
        return rate;
    }
    public static double FunnelPursuitPlayer(Player player,Player target) {
        double rate = 0;
        for(int ai = 0; ai<3; ai++) {
            try {
                if(HashPlayer.containsKey(DataMgr.getPlayerData(player).getArmorlist(ai))){
                    if(HashPlayer.get(DataMgr.getPlayerData(player).getArmorlist(ai)).equals(target)){
                        rate = rate + 1.5;
                    }
                }
            }catch (Exception e){
                rate = rate - 0.7;
            }
        }
        BukkitRunnable task = new BukkitRunnable() {
            Player p=player;
            PlayerData data = DataMgr.getPlayerData(player);
            Location loct = target.getLocation();
            Location locd ;

            @Override
            public void run(){
                for(int ai = 0; ai<3; ai++) {
                    try {
                        locd = data.getArmorlist(ai).getEyeLocation();
                        Vector vec = new Vector(loct.getX() - locd.getX(), loct.getY() - locd.getY() + 1.5, loct.getZ() - locd.getZ());
                        RayTrace rayTrace = new RayTrace(locd.toVector(), vec);
                        ArrayList<Vector> positions = rayTrace.traverse((int)vec.length(), 0.4);
                        double veclength = vec.length()/2;
                        if(veclength>12){
                            veclength=12;
                        }
                        for (int i = 0; i < veclength; i++) {
                            Location position = positions.get(i).toLocation(p.getLocation().getWorld());
                            if (player.getWorld() == position.getWorld()) {
                                if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                    Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                    player.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 3, dustOptions);
                                }
                            }
                            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                                if (target.equals(p) || !DataMgr.getPlayerData(target).getSettings().ShowEffect_ChargerLine())
                                    continue;
                                if (target.getWorld() == p.getWorld()) {
                                    if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                        Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                                        target.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 3, dustOptions);
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                    }
                }
            }
        };
        task.runTaskLater(Main.getPlugin(), 1);
        return rate;
    }

    public static int Funnelamount(Player player) {
        int rate = 3;
        for (int ai = 0; ai < 3; ai++) {
            try {
                if(HashPlayer.containsKey(DataMgr.getPlayerData(player).getArmorlist(ai))){
                    rate = rate - 1;
                }
                if(HashArmorstand.containsKey(DataMgr.getPlayerData(player).getArmorlist(ai))){
                    rate = rate - 1;
                }
            } catch (Exception e) {
                rate = rate - 1;
            }
        }
        return rate;
    }
    private static boolean FunAmoP(Player player){
        int count = 0;
        for(Map.Entry<ArmorStand,Player> entry: HashPlayer.entrySet()){
            if(entry.getValue() == player){
                count++;
            }
        }
        if(count >=3){
            return false;
        }else {
            return true;
        }
    }
    private static boolean FunAmoA(ArmorStand stand){
        int count = 0;
        for(Map.Entry<ArmorStand,ArmorStand> entry: HashArmorstand.entrySet()){
            if(entry.getValue() == stand){
                count++;
            }
        }
        if(count >=3){
            return false;
        }else {
            return true;
        }
    }
}
