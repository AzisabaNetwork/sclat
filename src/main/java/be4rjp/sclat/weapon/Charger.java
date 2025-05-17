package be4rjp.sclat.weapon;

import be4rjp.sclat.GaugeAPI;
import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.raytrace.BoundingBox;
import be4rjp.sclat.raytrace.RayTrace;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * @author Be4rJP
 */
public class Charger {
    public static void ChargerRunnable(Player player) {
        BukkitRunnable task = new BukkitRunnable() {
            final Player p = player;
            int charge = 0;
            int keeping = 0;
            final int max = DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getMaxCharge();
            final int min = max * 2 / 3;//インク消費軽減チャージ

            @Override
            public void run() {
                PlayerData data = DataMgr.getPlayerData(p);

                data.setTick(data.getTick() + 1);

                if (data.getIsUsingMM() || data.getIsUsingJetPack() || data.getIsUsingTyakuti() || data.getIsUsingSS()) {
                    charge = 0;
                    data.setTick(8);
                    return;
                }

                if (keeping == data.getWeaponClass().getMainWeapon().getChargeKeepingTime() && data.getWeaponClass().getMainWeapon().getCanChargeKeep() && data.getSettings().doChargeKeep())
                    charge = 0;

                if (data.getTick() <= 6 && data.isInMatch()) {
                    ItemStack w = data.getWeaponClass().getMainWeapon().getWeaponIteamStack().clone();
                    ItemMeta wm = w.getItemMeta();

                    if (data.getWeaponClass().getMainWeapon().getScope()) {
                        data.setIsCharging(true);
                    }

                    //data.setTick(data.getTick() + 1);
                    if (charge < max)
                        charge++;

                    if (data.getWeaponClass().getMainWeapon().getScope()) {
                        /*
                        if(charge != max)
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1, (int)charge / 3));
                        else
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40000, (int)charge / 3));*/
                        if (charge < max) {
                            Sclat.setPlayerFOV(p, -0.1F / ((float) charge / 19.0F));
                        }

                    }

                    wm.setDisplayName(wm.getDisplayName() + "§7[" + GaugeAPI.toGauge(charge, max, data.getTeam().getTeamColor().getColorCode(), "§7") + "]");
                    w.setItemMeta(wm);
                    p.getInventory().setItem(0, w);
                    RayTrace rayTrace = new RayTrace(p.getEyeLocation().toVector(), p.getEyeLocation().getDirection());
                    ArrayList<Vector> positions = rayTrace.traverse((int) ((double) charge * data.getWeaponClass().getMainWeapon().getChargeRatio() * (double) data.getWeaponClass().getMainWeapon().getDistanceTick()), 0.7);
                    check:
                    for (int i = 0; i < positions.size(); i++) {
                        Location position = positions.get(i).toLocation(p.getLocation().getWorld());
                        Block block = player.getWorld().getBlockAt(position);
                        if (!position.getBlock().getType().equals(Material.AIR)) {
                            //if(rayTrace.intersects(new BoundingBox(block), (int)(charge / 2 * data.getWeaponClass().getMainWeapon().getDistanceTick()), 0.1))
                            break;
                        }
//                        if(i % 2 == 0){
//                                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
//                                    if (target.equals(p) || !DataMgr.getPlayerData(target).getSettings().ShowEffect_ChargerLine())
//                                        continue;
//                                    if (target.getWorld() == p.getWorld()) {
//                                        if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
//                                            Particle.DustOptions dustOptions = new Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
//                                            target.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 50, dustOptions);
//                                        }
//                                    }
//                                }
//                        }
                    }
                }


                if (charge == max || data.getWeaponClass().getMainWeapon().getHanbunCharge())
                    if (p.getInventory().getItemInMainHand().getType().equals(Material.AIR))
                        if (data.getWeaponClass().getMainWeapon().getCanChargeKeep())
                            if (data.getSettings().doChargeKeep())
                                data.setTick(11);

                if (p.getGameMode().equals(GameMode.SPECTATOR))
                    charge = 0;

                if (data.getTick() >= 11 && (charge == max || data.getWeaponClass().getMainWeapon().getHanbunCharge()))
                    keeping++;
                else
                    keeping = 0;


                if (data.getTick() == 7 && data.isInMatch()) {
                    /*
                    if(player.hasPotionEffect(PotionEffectType.SLOW))
                        player.removePotionEffect(PotionEffectType.SLOW);*/
                    if (data.getWeaponClass().getMainWeapon().getScope()) {
                        data.setIsCharging(false);
                        Sclat.setPlayerFOV(player, 0.06F);
                    }
                    if (charge <= min) {
                        if (p.getExp() > data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) * charge / 2) {
                            p.setExp(p.getExp() - (float) ((data.getWeaponClass().getMainWeapon().getNeedInk() / 2) * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP) * charge));
                            Charger.Shoot(p, (int) ((double) charge * data.getWeaponClass().getMainWeapon().getChargeRatio() * (double) data.getWeaponClass().getMainWeapon().getDistanceTick()), data.getWeaponClass().getMainWeapon().getDamage() * charge, data.getWeaponClass().getMainWeapon().getDecreaseRate());
                        } else {
                            int reach = (int) (p.getExp() / data.getWeaponClass().getMainWeapon().getNeedInk());
                            if (reach >= 2) {
                                charge = 0;
                                //p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                                //p.setExp(p.getExp() - (float) ((data.getWeaponClass().getMainWeapon().getNeedInk() * reach/2) * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                                p.setExp(0.0F);
                                Charger.Shoot(p, (int) ((double) reach * data.getWeaponClass().getMainWeapon().getChargeRatio() * (double) data.getWeaponClass().getMainWeapon().getDistanceTick()), data.getWeaponClass().getMainWeapon().getDamage() * reach, data.getWeaponClass().getMainWeapon().getDecreaseRate());
                            } else {
                                p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                            }
                        }
                    } else if (p.getExp() > data.getWeaponClass().getMainWeapon().getNeedInk() * charge * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)) {
                        p.setExp(p.getExp() - (float) (data.getWeaponClass().getMainWeapon().getNeedInk() * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP) * charge));
                        Charger.Shoot(p, (int) ((double) charge * data.getWeaponClass().getMainWeapon().getChargeRatio() * (double) data.getWeaponClass().getMainWeapon().getDistanceTick()), data.getWeaponClass().getMainWeapon().getDamage() * charge, data.getWeaponClass().getMainWeapon().getDecreaseRate());
                    } else {
                        int reach = (int) (p.getExp() / data.getWeaponClass().getMainWeapon().getNeedInk());
                        if (reach >= 2) {
                            charge = 0;
                            //p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                            //p.setExp(p.getExp() - (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) / Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                            p.setExp(0.0F);
                            Charger.Shoot(p, (int) ((double) reach * data.getWeaponClass().getMainWeapon().getChargeRatio() * (double) data.getWeaponClass().getMainWeapon().getDistanceTick()), data.getWeaponClass().getMainWeapon().getDamage() * reach, data.getWeaponClass().getMainWeapon().getDecreaseRate());
                        } else {
                            p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                        }
                    }
                    charge = 0;
                    p.getInventory().setItem(0, data.getWeaponClass().getMainWeapon().getWeaponIteamStack());
                    data.setTick(8);
                    data.setIsHolding(false);
                }

                if (!data.isInMatch() || !p.isOnline())
                    cancel();

            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void Shoot(Player player, int reach, double damage, double decRate) {

        if (player.getGameMode() == GameMode.SPECTATOR) return;
        //player.sendMessage(String.valueOf(reach));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5);
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse((int) (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)), 0.2);


        Location entityLocation = player.getEyeLocation();
        double distance = reach; // レイの長さ
        World world = player.getWorld();
        double raydistance = distance;
        RayTraceResult rayresult = world.rayTraceBlocks(entityLocation, player.getEyeLocation().getDirection(), distance);
        //if (result != null && result.getHitBlock() != null) {
        if (rayresult != null && rayresult.getHitBlock() != null) {
            Location hitlocation = rayresult.getHitPosition().toLocation(world);
            raydistance = entityLocation.distance(hitlocation);
        }
        float loopsize = positions.size();
        loop:
        for (int i = 0; i < loopsize; i++) {

            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if (!block.getType().equals(Material.AIR)) {
                if (block.getType().equals(Material.WHITE_STAINED_GLASS_PANE) || block.getType().equals(Material.GLASS_PANE) || block.getType().equals(Material.ORANGE_STAINED_GLASS_PANE) || block.getType().equals(Material.LIGHT_BLUE_STAINED_GLASS_PANE) || block.getType().equals(Material.RED_STAINED_GLASS_PANE) || block.getType().equals(Material.LIME_STAINED_GLASS_PANE) || block.getType().equals(Material.BLACK_STAINED_GLASS_PANE) || block.getType().equals(Material.GRAY_STAINED_GLASS_PANE) || block.getType().equals(Material.CYAN_STAINED_GLASS_PANE) || block.getType().equals(Material.BLUE_STAINED_GLASS_PANE) || block.getType().equals(Material.IRON_BARS)) {
                    float raydis = (float) (raydistance / 0.195);
                    if (loopsize > raydis) {
                        loopsize = raydis;
                    }
                } else {
                    //if(rayTrace.intersects(new BoundingBox(block), reach, 0.01)){
                    PaintMgr.Paint(position, player, true);
                    break;
                    //}
                }
            }
            PaintMgr.PaintHightestBlock(position, player, false, true);
//                for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
//                    if (!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
//                        continue;
//                    if (target.getWorld() == position.getWorld()) {
//                        if (target.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
//                            org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
//                            target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
//                        }
//                    }
//                }
            if (DataMgr.getPlayerData(player).getSettings().ShowEffect_MainWeaponInk()) {
                if (player.getWorld() == position.getWorld()) {
                    if (player.getLocation().distanceSquared(position) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool().createBlockData();
                        player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1, bd);
                    }
                }
            }


            double maxDistSquad = 4 /* 2*2 */;
            for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                if (!DataMgr.getPlayerData(target).isInMatch())
                    continue;
                if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam() && target.getGameMode().equals(GameMode.ADVENTURE)) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(new BoundingBox(target), (int) (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)), 0.05)) {
                            boolean death;
                            double hitDamage = damage;
                            if (Isbackstab(player, target)) {
                                hitDamage = damage * decRate;
                            }
                            death = Sclat.giveDamage(player, target, hitDamage, "killed");
                            if (death)
                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2F, 1.3F);
                            else
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2F, 1.3F);

                            //AntiNoDamageTime
                            BukkitRunnable task = new BukkitRunnable() {
                                final Player p = target;

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
                        if (rayTrace.intersects(new BoundingBox(as), (int) (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)), 0.05)) {
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
                                    if (IsbackstabStand(player, (ArmorStand) as)) {
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage * decRate, player);
                                    } else {
                                        ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                                    }
                                    break loop;
                                }
                            }
                            if (IsbackstabStand(player, (ArmorStand) as)) {
                                ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage * decRate, player);
                            } else {
                                ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
                            }
                        }
                    }
                }
            }


        }


    }

    public static boolean Isbackstab(Player p, Player target) {
        double pyaw = 0.0;
        double tyaw = 0.0;
        if (p.getEyeLocation().getYaw() < 0) {
            pyaw = p.getEyeLocation().getYaw() + 360;
        } else {
            pyaw = p.getEyeLocation().getYaw();
        }
        if (target.getEyeLocation().getYaw() < 0) {
            tyaw = target.getEyeLocation().getYaw() + 360;
        } else {
            tyaw = target.getEyeLocation().getYaw();
        }
        return (pyaw - tyaw < 130 && pyaw - tyaw > -130) || pyaw - tyaw > 230 || pyaw - tyaw < -230;
    }

    public static boolean IsbackstabStand(Player p, ArmorStand target) {
        double pyaw = 0.0;
        double tyaw = 0.0;
        if (p.getEyeLocation().getYaw() < 0) {
            pyaw = p.getEyeLocation().getYaw() + 360;
        } else {
            pyaw = p.getEyeLocation().getYaw();
        }
        if (target.getEyeLocation().getYaw() < 0) {
            tyaw = target.getEyeLocation().getYaw() + 360;
        } else {
            tyaw = target.getEyeLocation().getYaw();
        }
        return (pyaw - tyaw < 130 && pyaw - tyaw > -130) || pyaw - tyaw > 230 || pyaw - tyaw < -230;
    }

}
