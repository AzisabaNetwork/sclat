package be4rjp.sclat.weapon.blaster;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.SimpleRunnable;
import be4rjp.sclat.api.Sphere;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.KasaData;
import be4rjp.sclat.data.SplashShieldData;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Supplier;

public class BlasterTickRunnable extends BukkitRunnable {
	int i = 0;
	String name;
	int tick;
	Vector origvec;
	Snowball inkball;
	Player p;
	Supplier<Snowball> snowballSupplier;
	public BlasterTickRunnable(String name, Player player, Snowball inkball, Vector origvec, int tick,
			Supplier<Snowball> snowballSupplier) {
		this.name = name;
		this.p = player;
		this.inkball = inkball;
		this.origvec = origvec;
		this.tick = tick;
		this.snowballSupplier = snowballSupplier;
	}
	@Override
	public void run() {
		PlayerData data = DataMgr.getPlayerData(p);
		inkball = DataMgr.getMainSnowballNameMap().get(name);

		if (!inkball.equals(snowballSupplier.get())) {
			i += DataMgr.getSnowballHitCount(name) - 1;
			DataMgr.setSnowballHitCount(name, 0);
		}

		org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor().getWool()
				.createBlockData();
		for (Player o_player : Sclat.getPlugin().getServer().getOnlinePlayers()) {
			if (DataMgr.getPlayerData(o_player).getSettings().ShowEffect_MainWeaponInk())
				if (o_player.getWorld() == inkball.getWorld())
					if (o_player.getLocation()
							.distanceSquared(inkball.getLocation()) < Sclat.PARTICLE_RENDER_DISTANCE_SQUARED)
						o_player.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 1, 0, 0, 0, 1,
								bd);
		}

		if (i >= tick && !inkball.isDead()) {
			// 半径
			double maxDist = data.getWeaponClass().getMainWeapon().getBlasterExHankei();

			// 爆発音
			p.getWorld().playSound(inkball.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

			// 爆発エフェクト
			SclatUtil.createInkExplosionEffect(inkball.getLocation(), maxDist, 25, p);

			// バリアをはじく
			SclatUtil.repelBarrier(inkball.getLocation(), maxDist, p);

			// 塗る
			for (int i = 0; i <= maxDist - 1; i++) {
				List<Location> p_locs = Sphere.getSphere(inkball.getLocation(), i, 20);
				for (Location loc : p_locs) {
					PaintMgr.Paint(loc, p, false);
					PaintMgr.PaintHightestBlock(loc, p, false, false);
				}
			}

			// 攻撃判定の処理
			for (Entity as : p.getWorld().getEntities()) {
				if (as instanceof ArmorStand) {
					if (as.getCustomName() != null) {
						if (as.getLocation().distanceSquared(inkball.getLocation()) <= maxDist * maxDist) {
							try {
								if (as.getCustomName().equals("Kasa")) {
									KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
									if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() != DataMgr
											.getPlayerData(p).getTeam()) {
										inkball.remove();
										cancel();
									}
								} else if (as.getCustomName().equals("SplashShield")) {
									SplashShieldData splashShieldData = DataMgr
											.getSplashShieldDataFromArmorStand((ArmorStand) as);
									if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() != DataMgr
											.getPlayerData(p).getTeam()) {
										inkball.remove();
										cancel();
									}
								}
							} catch (Exception e) {
							}
						}
					}
				}
			}

			for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
				if (!DataMgr.getPlayerData(target).isInMatch())
					continue;
				if (target.getLocation().distance(inkball.getLocation()) <= maxDist + 1) {
					double damage = 10;
					if (data.getWeaponClass().getMainWeapon().getIsManeuver())
						damage = data.getWeaponClass().getMainWeapon().getBlasterExDamage();
					else
						damage = (maxDist + 1 - target.getLocation().distance(inkball.getLocation()))
								* data.getWeaponClass().getMainWeapon().getBlasterExDamage();
					if (damage > data.getWeaponClass().getMainWeapon().getDamage()) {
						damage = data.getWeaponClass().getMainWeapon().getDamage();
					}
					if (DataMgr.getPlayerData(p).getTeam() != DataMgr.getPlayerData(target).getTeam()
							&& target.getGameMode().equals(GameMode.ADVENTURE)) {
						SclatUtil.giveDamage(p, target, damage, "killed");

						// AntiNoDamageTime
						SimpleRunnable.runTaskLater(cancel -> {
							target.setNoDamageTicks(0);
						}, 1);

					}
				}
			}

			for (Entity as : p.getWorld().getEntities()) {
				if (as instanceof ArmorStand) {
					if (as.getLocation().distanceSquared(inkball.getLocation()) <= (maxDist + 1) * (maxDist + 1)) {
						double damage = (maxDist + 1 - as.getLocation().distance(inkball.getLocation()))
								* data.getWeaponClass().getMainWeapon().getBlasterExDamage();
						if (damage > data.getWeaponClass().getMainWeapon().getDamage()) {
							damage = data.getWeaponClass().getMainWeapon().getDamage();
						}
						ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, p);
					}
				}
			}

			inkball.remove();
		}
		if (i != tick)
			PaintMgr.PaintHightestBlock(inkball.getLocation(), p, false, true);
		if (inkball.isDead())
			cancel();
		i++;
	}
}
