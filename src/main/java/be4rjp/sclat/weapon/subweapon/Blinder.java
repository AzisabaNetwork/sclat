package be4rjp.sclat.weapon.subweapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.raytrace.BoundingBox;
import be4rjp.sclat.raytrace.RayTrace;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Blinder {
    public static void BlinderRunnable(Player player) {
        Player p = player;
        int reach = 35;


        BukkitRunnable cooltime = new BukkitRunnable() {
            @Override
            public void run() {
                DataMgr.getPlayerData(player).setCanUseSubWeapon(true);
            }
        };
        cooltime.runTaskLater(Main.getPlugin(), 8);
        if (p.getExp() > 0.36f || DataMgr.getPlayerData(player).getIsBombRush()) {
            if (!DataMgr.getPlayerData(player).getIsBombRush()) {
                p.setExp(player.getExp() - 0.35f);
            }
            Shootblind(p, reach);
        } else {
            p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
            p.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
        }
    }

    public static void Shootblind(Player player, int reach) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        RayTrace rayTrace = new RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
        ArrayList<Vector> positions = rayTrace.traverse(reach, 0.15);
        loop:
        for (int i = 0; i < positions.size(); i++) {

            Location position = positions.get(i).toLocation(player.getLocation().getWorld());
            Block block = player.getLocation().getWorld().getBlockAt(position);

            if (!block.getType().equals(Material.AIR)) {
                break;
            }
//            if(i<8) {
//                PaintMgr.PaintHightestBlock(position, player, false, true);
//            }
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
                        if (rayTrace.intersects(new BoundingBox(target), reach, 0.2)) {
                            String Weapontype = DataMgr.getPlayerData(target).getWeaponClass().getMainWeapon().getWeaponType();
                            int effecttime = 40;
                            if (Weapontype.equals("Charger") || Weapontype.equals("Spinner") || DataMgr.getPlayerData(target).getWeaponClass().getMainWeapon().getIsManeuver()) {
                                effecttime += 25;
                            } else if (Weapontype.equals("Blaster") || Weapontype.equals("Hound")) {
                                effecttime += 10;
                            } else if (Weapontype.equals("Roller") || Weapontype.equals("Slosher") || Weapontype.equals("Bucket")) {
                                effecttime -= 10;
                            }
                            if (DataMgr.getPlayerData(target).getIsUsingSP() || DataMgr.getPlayerData(target).getArmor() > 0) {
                                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 120, 1));
                                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.2F, 1.3F);
                            } else if (i > 85 && !target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (effecttime * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)), 1));
                                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 70 + effecttime, 1));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.2F, 1.3F);
                            } else if (target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.2F, 1.3F);
                            } else {
                                DataMgr.getPlayerData(target).setPoison(true);
                                PoisonRunnable3(target, (int) (effecttime * Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP) - 10));
                                target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2F, 1.3F);
                            }
                            break loop;
                        }
                    }
                }
            }

            for (Entity as : player.getWorld().getEntities()) {
                if (as instanceof ArmorStand) {
                    if (as.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(new BoundingBox(as), reach, 0.2)) {
                            if (as.getCustomName() != null) {
                                if (as.getCustomName().equals("SplashShield")) {
                                    SplashShieldData ssdata = DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
                                    if (DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                } else if (as.getCustomName().equals("Kasa")) {
                                    KasaData ssdata = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
                                    if (DataMgr.getPlayerData(ssdata.getPlayer()).getTeam() != DataMgr.getPlayerData(player).getTeam()) {
                                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.8F, 1.2F);
                                        break loop;
                                    }
                                } else {
                                    if (Sclat.isNumber(as.getCustomName()))
                                        if (!as.getCustomName().equals("21") && !as.getCustomName().equals("100"))
                                            if (((ArmorStand) as).isVisible())
                                                if (i > 85) {
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.2F, 1.3F);
                                                } else {
                                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2F, 1.3F);
                                                }
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }


        }
    }

    public static void PoisonRunnable3(Player player, int delay) {
        BukkitRunnable cooltime = new BukkitRunnable() {
            @Override
            public void run() {
                DataMgr.getPlayerData(player).setPoison(false);
            }
        };
        cooltime.runTaskLater(Main.getPlugin(), delay);
    }
}
