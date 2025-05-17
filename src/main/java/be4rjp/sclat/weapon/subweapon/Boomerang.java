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
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Boomerang {
    public static void BoomerangRunnable(Player player){
        Vector pVector = player.getEyeLocation().getDirection();
        Vector vec = new Vector(pVector.getX(), pVector.getY(), pVector.getZ()).normalize().multiply(0.6);
        BukkitRunnable task = new BukkitRunnable() {
            Vector aVec = vec.clone();
            Location bloc;
            int i = 0;
            ArmorStand as1;
            ArmorStand as2;
            ArmorStand as3;
            FallingBlock fb;
            boolean cumbackBoomeran = false;
            int cumbacktime = 90;
            boolean explode = false;
            @Override
            public void run() {
                try {
                    if(i == 0){
                        cumbackBoomeran = false;
                        if(!DataMgr.getPlayerData(player).getIsBombRush())
                            player.setExp(player.getExp() - 0.59F);

                        as1 = player.getWorld().spawn(player.getLocation().add(0, 1.6, 0), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setSmall(true);
                        });
                        as2 = player.getWorld().spawn(player.getLocation().add(0, 1.6, 0), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setGravity(false);
                            armorStand.setMarker(true);

                            armorStand.setSmall(true);
                        });
                        Location loc = player.getLocation().add(0, 0.8, 0);
                        loc.setYaw(90);
                        as3 = player.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setGravity(false);
                            armorStand.setSmall(true);
                        });

                        fb = player.getWorld().spawnFallingBlock(player.getLocation(), Material.WHITE_CARPET.createBlockData());
                        fb.setGravity(false);
                        fb.setDropItem(false);
                        fb.setHurtEntities(false);

                        as2.addPassenger(fb);
                    }

                    Location aloc = as1.getLocation().add(0, -0.8, 0);
                    aloc.setYaw(90);
                    Location as1l = as1.getLocation();
                    ((CraftArmorStand)as2).getHandle().setPositionRotation(as1l.getX(), as1l.getY(), as1l.getZ(), 0, 0);
                    as3.teleport(aloc);
                    fb.setTicksLived(1);

                    if(i >= 5 && !cumbackBoomeran){
                        if(bloc.getX() == as1l.getX() && bloc.getZ() != as1l.getZ() || bloc.getZ() == as1l.getZ() && bloc.getX() != as1l.getX()) {
                            aVec = new Vector(player.getLocation().getX()-bloc.getX(), (player.getLocation().getY()+1.6)-bloc.getY(), player.getLocation().getZ()-bloc.getZ()).normalize().multiply(1.0);
                            cumbackBoomeran = true;
                            cumbacktime = i;
                            for(int painti = 0; painti <= 2; painti++){
                                List<Location> p_locs = Sphere.getSphere(as1l, painti, 20);
                                for(Location loc : p_locs){
                                    PaintMgr.Paint(loc, player, false);
                                }
                            }
                        }else if(as1.isOnGround()){
                            aVec = new Vector(player.getLocation().getX()-bloc.getX(), (player.getLocation().getY()+1.6)-bloc.getY(), player.getLocation().getZ()-bloc.getZ()).normalize().multiply(1.0);
                            cumbackBoomeran = true;
                            cumbacktime = i;
                        }
                        else if(i == 30 ) {
                            aVec = new Vector(player.getLocation().getX()-bloc.getX(), (player.getLocation().getY()+1.6)-bloc.getY(), player.getLocation().getZ()-bloc.getZ()).normalize().multiply(1.0);
                            cumbackBoomeran = true;
                            cumbacktime = 35;
                        }
                    }
                    if(i >= cumbacktime + 5){
                        if(bloc.getX() == as1l.getX() && bloc.getZ() != as1l.getZ() || bloc.getZ() == as1l.getZ() && bloc.getX() != as1l.getX()) {
                            explode = true;
                        }
                    }
                    if(i >= cumbacktime + 15){
                        explode = true;
                    }
                    as1.setVelocity(aVec);

                    PaintMgr.PaintHightestBlock(as1l, player, false, true);

                    bloc = as1l.clone();

                    if(i % 10 == 0){
                        for (Player o_player : Main.getPlugin().getServer().getOnlinePlayers())
                            ((CraftPlayer)o_player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityEquipment(as3.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool()))));
                    }

                    if(i >= cumbacktime+3 && i <= cumbacktime+13){
                        if(i % 2 == 0)
                            player.getWorld().playSound(as1l, Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.6F);
                    }

                    //エフェクト
                    if(i % 2 == 0){
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb())
                                if(target.getWorld() == player.getWorld())
                                    if(target.getLocation().distanceSquared(as1l) < Main.PARTICLE_RENDER_DISTANCE_SQUARED)
                                        target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, as1l, 2, 0, 0, 0, 1, bd);
                        }
                        //攻撃判定
                        for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            if(DataMgr.getPlayerData(target).getSettings().ShowEffect_Bomb()){
                                if(target.getWorld() == player.getWorld()){
                                    if(target.getLocation().distance(as1l) <= 1.2){
                                        double damage = 0.2;
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
                            }
                        }

                        for(Entity as : player.getWorld().getEntities()){
                            if (as.getLocation().distance(as1l) <= 1.2){
                                if(as instanceof ArmorStand){
                                    double damage = 0.2;
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, player);
                                    if(as.getCustomName() != null){
                                        if(as.getCustomName().equals("SplashShield") || as.getCustomName().equals("Kasa"))
                                            break;
                                    }
                                }
                            }
                        }
                    }

                    if(i == 90 || !player.isOnline() || !DataMgr.getPlayerData(player).isInMatch() || explode){
                        //半径
                        double maxDist = 3;

                        //爆発音
                        player.getWorld().playSound(as1l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

                        //爆発エフェクト
                        Sclat.createInkExplosionEffect(as1l, maxDist, 15, player);

                        //バリアをはじく
                        Sclat.repelBarrier(as1l, maxDist, player);

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
                                                if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                                    as1.remove();
                                                    as2.remove();
                                                    as3.remove();
                                                    fb.remove();
                                                    cancel();
                                                }
                                            } else if (as.getCustomName().equals("SplashShield")) {
                                                SplashShieldData splashShieldData = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
                                                if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                                    as1.remove();
                                                    as2.remove();
                                                    as3.remove();
                                                    fb.remove();
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
                                double damage = (maxDist - target.getLocation().distance(as1l)) * 1 * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP);
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
                            if (as.getLocation().distance(as1l) <= maxDist){
                                if(as instanceof ArmorStand){
                                    double damage = (maxDist - as.getLocation().distance(as1l)) * 1 * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP);
                                    ArmorStandMgr.giveDamageArmorStand((ArmorStand)as, damage, player);
                                    if(as.getCustomName() != null){
                                        if(as.getCustomName().equals("SplashShield") || as.getCustomName().equals("Kasa"))
                                            break;
                                    }
                                }
                            }
                        }

                        as1.remove();
                        as2.remove();
                        as3.remove();
                        fb.remove();
                        cancel();
                    }

                    i++;
                }catch(Exception e){
                    as1.remove();
                    as2.remove();
                    as3.remove();
                    fb.remove();
                    cancel();
                }
            }
        };
        if(player.getExp() > 0.6 || DataMgr.getPlayerData(player).getIsBombRush())
            task.runTaskTimer(Main.getPlugin(), 0, 1);
        else{
            player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
        }

        BukkitRunnable cooltime = new BukkitRunnable(){
            @Override
            public void run(){
                DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
            }
        };
        cooltime.runTaskLater(Main.getPlugin(), 10);
    }
}
