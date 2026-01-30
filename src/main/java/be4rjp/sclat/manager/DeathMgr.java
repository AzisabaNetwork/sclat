
package be4rjp.sclat.manager;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.Sphere;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.weapon.Gear;
import be4rjp.sclat.weapon.spweapon.SuperArmor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 *
 * @author Be4rJP
 */
public class DeathMgr {
	public static void PlayerDeathRunnable(Player target, Player shooter, String type) {

		DataMgr.getPlayerData(target).setIsDead(true);
		target.setGameMode(GameMode.SPECTATOR);

		DataMgr.getPlayerData(target).setPoison(false);

		Item drop1 = target.getWorld().dropItem(target.getEyeLocation(),
				DataMgr.getPlayerData(target).getWeaponClass().getMainWeapon().getWeaponIteamStack());
		Item drop2 = target.getWorld().dropItem(target.getEyeLocation(),
				DataMgr.getPlayerData(target).team.getTeamColor().getBougu());
		final double random = 0.4;
		drop1.setVelocity(
				new Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2));
		drop2.setVelocity(
				new Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2));

		SclatUtil.createInkExplosionEffect(target.getEyeLocation().add(0, -1, 0), 3, 30, shooter);

		if (Gear.getGearInfluence(target, Gear.Type.PENA_DOWN) == 1.0) {
			DataMgr.getPlayerData(target).setSPGauge((int) (DataMgr.getPlayerData(target).getSPGauge() * 0.7));
		}

		// 半径
		double maxDist = 3;

		// 塗る
		for (int i = 0; i <= maxDist; i++) {
			List<Location> p_locs = Sphere.getSphere(target.getLocation(), i, 20);
			for (Location loc : p_locs) {
				PaintMgr.Paint(loc, shooter, false);
			}
		}

		BukkitRunnable clear = new BukkitRunnable() {
			@Override
			public void run() {
				drop1.remove();
				drop2.remove();
			}
		};
		clear.runTaskLater(Sclat.getPlugin(), 50);

		if (target.hasPotionEffect(PotionEffectType.GLOWING))
			target.removePotionEffect(PotionEffectType.GLOWING);
		if (target.hasPotionEffect(PotionEffectType.SLOW))
			target.removePotionEffect(PotionEffectType.SLOW);

		if (type.equals("killed") || type.equals("subWeapon") || type.equals("spWeapon")) {
			DataMgr.getPlayerData(shooter).addKillCount();
			DataMgr.getPlayerData(shooter).team.addKillCount();
			if (!DataMgr.getPlayerData(shooter).getIsUsingSP())
				for (int i = 0; i < 10; i++)
					SPWeaponMgr.addSPCharge(shooter);
		} else if (DataMgr.getPlayerData(target).lastAttack != target) {
			Player Lastattacker = DataMgr.getPlayerData(target).lastAttack;
			DataMgr.getPlayerData(Lastattacker).addKillCount();
			DataMgr.getPlayerData(Lastattacker).team.addKillCount();
			if (!DataMgr.getPlayerData(Lastattacker).getIsUsingSP())
				for (int i = 0; i < 10; i++)
					SPWeaponMgr.addSPCharge(Lastattacker);
		}

		BukkitRunnable task = new BukkitRunnable() {
			Player t = target;
			Player s = shooter;
			Location loc;
			int i = 0;

			@Override
			public void run() {
				try {
					if (!DataMgr.getPlayerData(t).isInMatch()) {
						cancel();
						return;
					}
					if (type.equals("killed")) {
						t.setGameMode(GameMode.SPECTATOR);
						t.getInventory().clear();
						DataMgr.getPlayerData(t).tick = 10;
						if (s.getGameMode().equals(GameMode.ADVENTURE)) {
							t.setSpectatorTarget(s);
						} else {
							loc = DataMgr.getPlayerData(t).match.getMapData().getIntro();
							t.teleport(loc);
						}

						PlayerData sdata = DataMgr.getPlayerData(s);
						String msg = sdata.team.getTeamColor().getColorCode()
								+ s.getDisplayName() + ChatColor.RESET + "に" + ChatColor.BOLD + sdata.getWeaponClass()
										.getMainWeapon().getWeaponIteamStack().getItemMeta().getDisplayName()
								+ ChatColor.RESET + "でやられた！";
						if (i == 0) {
							t.sendTitle(ChatColor.GREEN + "復活まであと: 5秒", msg, 0, 21, 0);
							for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
								player.sendMessage(
										sdata.team.getTeamColor().getColorCode() + s.getDisplayName() + ChatColor.RESET
												+ "が" + DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
												+ t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD
												+ sdata.getWeaponClass().getMainWeapon().getWeaponIteamStack()
														.getItemMeta().getDisplayName()
												+ ChatColor.RESET + "で倒した！");
								s.spigot().sendMessage(ChatMessageType.ACTION_BAR,
										TextComponent.fromLegacyText(
												DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
														+ t.getDisplayName() + ChatColor.RESET + "を倒した！"));
								s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 10);

							}
						}
						if (i == 20)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 4秒", msg, 0, 21, 0);
						DataMgr.getPlayerData(t).lastAttack = t;
						if (i == 40)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 3秒", msg, 0, 21, 0);
						if (i == 60)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 2秒", msg, 0, 21, 0);
						if (i == 80)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 1秒", msg, 0, 18, 2);
						// t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
						// s.getDisplayName() + ChatColor.RESET + "に" + ChatColor.BOLD +
						// sdata.getWeaponClass().getMainWeapon().getWeaponIteamStack().getItemMeta().getDisplayName()
						// + ChatColor.RESET + "でやられた！", 0, 5, 2);
						if (i == 100) {
							DataMgr.getPlayerData(target).setIsDead(false);
							Location loc = DataMgr.getPlayerData(t).getMatchLocation();
							t.teleport(loc);
							t.setGameMode(GameMode.ADVENTURE);
							t.getInventory().setItem(0,
									DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
							t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(),
									Sound.ENTITY_PLAYER_SWIM, 1, 1);
							t.setExp(0.99F);
							t.setHealth(20);
							// DataMgr.getPlayerData(t).setLastAttack(t);
							WeaponClassMgr.setWeaponClass(t);
							BarrierEffectRunnable(t, 120);
							SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
							if (DataMgr.getPlayerData(t).getSPGauge() == 100)
								SPWeaponMgr.setSPWeapon(t);
							cancel();
						}
					}

					if (type.equals("subWeapon")) {
						t.setGameMode(GameMode.SPECTATOR);
						t.getInventory().clear();
						DataMgr.getPlayerData(t).tick = 10;
						if (s.getGameMode().equals(GameMode.ADVENTURE)) {
							t.setSpectatorTarget(s);
						} else {
							loc = DataMgr.getPlayerData(t).match.getMapData().getIntro();
							t.teleport(loc);
						}

						PlayerData sdata = DataMgr.getPlayerData(s);
						String msg = sdata.team.getTeamColor().getColorCode() + s.getDisplayName() + ChatColor.RESET
								+ "に" + ChatColor.BOLD + sdata.getWeaponClass().getSubWeaponName() + ChatColor.RESET
								+ "でやられた！";
						if (i == 0) {
							t.sendTitle(ChatColor.GREEN + "復活まであと: 5秒", msg, 0, 21, 0);
							for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
								player.sendMessage(sdata.team.getTeamColor().getColorCode() + s.getDisplayName()
										+ ChatColor.RESET + "が"
										+ DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
										+ t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD
										+ sdata.getWeaponClass().getSubWeaponName() + ChatColor.RESET + "で倒した！");
								s.spigot().sendMessage(ChatMessageType.ACTION_BAR,
										TextComponent.fromLegacyText(
												DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
														+ t.getDisplayName() + ChatColor.RESET + "を倒した！"));
								s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 10);

							}
						}
						if (i == 20)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 4秒", msg, 0, 21, 0);
						DataMgr.getPlayerData(t).lastAttack = t;
						if (i == 40)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 3秒", msg, 0, 21, 0);
						if (i == 60)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 2秒", msg, 0, 21, 0);
						if (i == 80)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 1秒", msg, 0, 18, 2);
						// t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
						// s.getDisplayName() + ChatColor.RESET + "に" + ChatColor.BOLD +
						// sdata.getWeaponClass().getMainWeapon().getWeaponIteamStack().getItemMeta().getDisplayName()
						// + ChatColor.RESET + "でやられた！", 0, 5, 2);
						if (i == 100) {
							DataMgr.getPlayerData(target).setIsDead(false);
							Location loc = DataMgr.getPlayerData(t).getMatchLocation();
							t.teleport(loc);
							t.setGameMode(GameMode.ADVENTURE);
							t.getInventory().setItem(0,
									DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
							t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(),
									Sound.ENTITY_PLAYER_SWIM, 1, 1);
							t.setExp(0.99F);
							t.setHealth(20);
							// DataMgr.getPlayerData(t).setLastAttack(t);
							WeaponClassMgr.setWeaponClass(t);
							BarrierEffectRunnable(t, 120);
							SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
							if (DataMgr.getPlayerData(t).getSPGauge() == 100)
								SPWeaponMgr.setSPWeapon(t);
							cancel();
						}
					}

					if (type.equals("spWeapon")) {
						t.setGameMode(GameMode.SPECTATOR);
						t.getInventory().clear();
						DataMgr.getPlayerData(t).tick = 10;
						if (s.getGameMode().equals(GameMode.ADVENTURE)) {
							t.setSpectatorTarget(s);
						} else {
							loc = DataMgr.getPlayerData(t).match.getMapData().getIntro();
							t.teleport(loc);
						}

						PlayerData sdata = DataMgr.getPlayerData(s);
						String msg = sdata.team.getTeamColor().getColorCode() + s.getDisplayName() + ChatColor.RESET
								+ "に" + ChatColor.BOLD + sdata.getWeaponClass().getSPWeaponName() + ChatColor.RESET
								+ "でやられた！";
						if (i == 0) {
							t.sendTitle(ChatColor.GREEN + "復活まであと: 5秒", msg, 0, 21, 0);
							for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
								player.sendMessage(
										sdata.team.getTeamColor().getColorCode() + s.getDisplayName() + ChatColor.RESET
												+ "が" + DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
												+ t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD
												+ sdata.getWeaponClass().getSPWeaponName() + ChatColor.RESET + "で倒した！");
								s.spigot().sendMessage(ChatMessageType.ACTION_BAR,
										TextComponent.fromLegacyText(
												DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
														+ t.getDisplayName() + ChatColor.RESET + "を倒した！"));
								s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 10);

							}
						}
						if (i == 20)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 4秒", msg, 0, 21, 0);
						DataMgr.getPlayerData(t).lastAttack = t;
						if (i == 40)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 3秒", msg, 0, 21, 0);
						if (i == 60)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 2秒", msg, 0, 21, 0);
						if (i == 80)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 1秒", msg, 0, 18, 2);
						// t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
						// s.getDisplayName() + ChatColor.RESET + "に" + ChatColor.BOLD +
						// sdata.getWeaponClass().getMainWeapon().getWeaponIteamStack().getItemMeta().getDisplayName()
						// + ChatColor.RESET + "でやられた！", 0, 5, 2);
						if (i == 100) {
							DataMgr.getPlayerData(target).setIsDead(false);
							Location loc = DataMgr.getPlayerData(t).getMatchLocation();
							t.teleport(loc);
							t.setGameMode(GameMode.ADVENTURE);
							t.getInventory().setItem(0,
									DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
							t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(),
									Sound.ENTITY_PLAYER_SWIM, 1, 1);
							t.setExp(0.99F);
							t.setHealth(20);
							// DataMgr.getPlayerData(t).setLastAttack(t);
							WeaponClassMgr.setWeaponClass(t);
							BarrierEffectRunnable(t, 120);
							SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
							if (DataMgr.getPlayerData(t).getSPGauge() == 100)
								SPWeaponMgr.setSPWeapon(t);
							cancel();
						}
					}

					if (type.equals("water")) {
						t.setGameMode(GameMode.SPECTATOR);
						DataMgr.getPlayerData(t).tick = 10;
						t.getInventory().clear();
						if (i == 0) {
							loc = t.getLocation();
							if (DataMgr.getPlayerData(t).lastAttack == t) {
								for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
									player.sendMessage(DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
											+ t.getDisplayName() + ChatColor.RESET + "は溺れてしまった！");
								}
							} else {
								Player splayer = DataMgr.getPlayerData(t).lastAttack;
								PlayerData sdata = DataMgr.getPlayerData(splayer);
								for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
									// player.sendMessage(
									// DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode() +
									// t.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET
									// +sdata.getTeam().getTeamColor().getColorCode() + splayer.getDisplayName() +
									// ChatColor.RESET+ "に突き落とされてしまった！");
									player.sendMessage(sdata.team.getTeamColor().getColorCode()
											+ splayer.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET
											+ DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
											+ t.getDisplayName() + ChatColor.RESET + "を水中に落とした！");
								}
							}
						}
						t.teleport(loc);

						if (i == 0)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 5秒", "溺れてしまった！", 0, 21, 0);
						if (i == 20)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 4秒", "溺れてしまった！", 0, 21, 0);
						DataMgr.getPlayerData(t).lastAttack = t;
						if (i == 40)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 3秒", "溺れてしまった！", 0, 21, 0);
						if (i == 60)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 2秒", "溺れてしまった！", 0, 21, 0);
						if (i == 80)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 1秒", "溺れてしまった！", 0, 18, 2);
						if (i == 100) {
							DataMgr.getPlayerData(target).setIsDead(false);
							Location loc1 = DataMgr.getPlayerData(t).getMatchLocation();
							t.teleport(loc1);
							t.setGameMode(GameMode.ADVENTURE);
							t.getInventory().setItem(0,
									DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
							t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(),
									Sound.ENTITY_PLAYER_SWIM, 1, 1);
							t.setExp(0.99F);
							t.setHealth(20);
							// DataMgr.getPlayerData(t).setLastAttack(t);
							WeaponClassMgr.setWeaponClass(t);
							BarrierEffectRunnable(t, 120);
							SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
							if (DataMgr.getPlayerData(t).getSPGauge() == 100)
								SPWeaponMgr.setSPWeapon(t);
							cancel();
						}
					}
					if (type.equals("fall")) {
						t.setGameMode(GameMode.SPECTATOR);
						DataMgr.getPlayerData(t).tick = 10;
						t.getInventory().clear();
						if (i == 0) {
							loc = DataMgr.getPlayerData(t).match.getMapData().getIntro();
							if (DataMgr.getPlayerData(t).lastAttack == t) {
								for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
									player.sendMessage(DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
											+ t.getDisplayName() + ChatColor.RESET + "は奈落に落ちてしまった！");
								}
							} else {
								Player splayer = DataMgr.getPlayerData(t).lastAttack;
								PlayerData sdata = DataMgr.getPlayerData(splayer);
								for (Player player : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
									// player.sendMessage(DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode()
									// + t.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET +
									// sdata.getTeam().getTeamColor().getColorCode() + splayer.getDisplayName() +
									// ChatColor.RESET + "に突き落とされてしまった！");
									player.sendMessage(sdata.team.getTeamColor().getColorCode()
											+ splayer.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET
											+ DataMgr.getPlayerData(t).team.getTeamColor().getColorCode()
											+ t.getDisplayName() + ChatColor.RESET + "を奈落に落とした！");
								}
							}
						}
						t.teleport(loc);

						if (i == 0)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 5秒", "マップの外に落ちてしまった！", 0, 21, 0);
						if (i == 20)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 4秒", "マップの外に落ちてしまった！", 0, 21, 0);
						DataMgr.getPlayerData(t).lastAttack = t;
						if (i == 40)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 3秒", "マップの外に落ちてしまった！", 0, 21, 0);
						if (i == 60)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 2秒", "マップの外に落ちてしまった！", 0, 21, 0);
						if (i == 80)
							t.sendTitle(ChatColor.GREEN + "復活まであと: 1秒", "マップの外に落ちてしまった！", 0, 18, 2);
						if (i == 100) {
							DataMgr.getPlayerData(target).setIsDead(false);
							Location loc1 = DataMgr.getPlayerData(t).getMatchLocation();
							t.teleport(loc1);
							t.setGameMode(GameMode.ADVENTURE);
							t.getInventory().setItem(0,
									DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
							t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(),
									Sound.ENTITY_PLAYER_SWIM, 1, 1);
							t.setExp(0.99F);
							t.setHealth(20);
							// DataMgr.getPlayerData(t).setLastAttack(t);
							WeaponClassMgr.setWeaponClass(t);
							BarrierEffectRunnable(t, 120);
							SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
							if (DataMgr.getPlayerData(t).getSPGauge() == 100)
								SPWeaponMgr.setSPWeapon(t);
							cancel();
						}
					}

					if (i == 100)
						target.getInventory().setHeldItemSlot(1);

					i++;
				} catch (Exception e) {
					DataMgr.getPlayerData(target).setIsDead(false);
					Location loc1 = DataMgr.getPlayerData(t).getMatchLocation();
					t.teleport(loc1);
					t.setGameMode(GameMode.ADVENTURE);
					t.getInventory().setItem(0,
							DataMgr.getPlayerData(t).getWeaponClass().getMainWeapon().getWeaponIteamStack());
					t.getWorld().playSound(DataMgr.getPlayerData(t).getMatchLocation(), Sound.ENTITY_PLAYER_SWIM, 1, 1);
					t.setExp(0.99F);
					t.setHealth(20);
					WeaponClassMgr.setWeaponClass(t);
					SuperArmor.setArmor(t, Double.MAX_VALUE, 120, false);
					if (DataMgr.getPlayerData(t).getSPGauge() == 100)
						SPWeaponMgr.setSPWeapon(t);
					cancel();
					Sclat.getPlugin().getLogger().warning(e.getMessage());
				}
			}
		};
		task.runTaskTimer(Sclat.getPlugin(), 0, 1);
	}
	public static void BarrierEffectRunnable(Player player, int time) {
		int resettime = time / 4;
		PlayerData data = DataMgr.getPlayerData(player);

		// エフェクト
		BukkitRunnable task = new BukkitRunnable() {
			Player p = player;
			int c = 0;
			@Override
			public void run() {
				if (!data.isInMatch() || !player.getGameMode().equals(GameMode.ADVENTURE) || !p.isOnline()) {
					cancel();
				}
				Location loc = p.getLocation().add(0, 0.5, 0);

				List<Location> s_locs = Sphere.getSphere(loc, 2, 23);
				for (Player o_player : Sclat.getPlugin().getServer().getOnlinePlayers()) {
					if (DataMgr.getPlayerData(o_player).settings.ShowEffect_SPWeapon() && !o_player.equals(player)) {
						Particle.DustOptions dustOptions = new Particle.DustOptions(
								data.team.getTeamColor().getBukkitColor(), 1);
						org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).team.getTeamColor().getWool()
								.createBlockData();
						for (Location e_loc : s_locs)
							if (o_player.getWorld() == e_loc.getWorld())
								if (o_player.getLocation()
										.distanceSquared(e_loc) < Sclat.PARTICLE_RENDER_DISTANCE_SQUARED)
									o_player.spawnParticle(Particle.REDSTONE, e_loc, 0, 0, 0, 0, 70, dustOptions);
					}
				}
				if (c >= resettime) {
					cancel();
				}
				if (data.armor < 9999) {
					cancel();
				}
				c++;
			}
		};
		task.runTaskTimer(Sclat.getPlugin(), 0, 4);
	}
}
