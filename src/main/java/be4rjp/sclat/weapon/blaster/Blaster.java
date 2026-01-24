
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
import be4rjp.sclat.weapon.Gear;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 *
 * @author Be4rJP
 */
public class Blaster {
	public static void ShootBlaster(Player player) {
		PlayerData data = DataMgr.getPlayerData(player);
		if (data.getCanRollerShoot()) {
			SimpleRunnable.runTaskLater(cancel -> {
				PlayerData playerData = DataMgr.getPlayerData(player);
				playerData.setCanRollerShoot(true);
			}, data.getWeaponClass().getMainWeapon().getCoolTime());

			SimpleRunnable.runTaskLater(cancel -> {
				Shoot(player);
			}, data.getWeaponClass().getMainWeapon().getDelay());
			data.setCanRollerShoot(false);
		}
	}

	public static void Shoot(Player player) {

		if (player.getGameMode() == GameMode.SPECTATOR)
			return;

		PlayerData data = DataMgr.getPlayerData(player);
		data.setCanRollerShoot(false);
		if (player.getExp() <= (float) (data.getWeaponClass().getMainWeapon().getNeedInk()
				* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
				/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP))) {
			player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 5, 2);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
			return;
		}
		player.setExp(player.getExp() - (float) (data.getWeaponClass().getMainWeapon().getNeedInk()
				* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
				/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
		Snowball ball = player.launchProjectile(Snowball.class);
		((CraftSnowball) ball).getHandle().setItem(CraftItemStack
				.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3F, 1F);
		Vector vec = player.getLocation().getDirection()
				.multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootSpeed());
		double random = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getRandom();
		int distick = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getDistanceTick();
		if (!player.isOnGround())
			vec.add(new Vector(Math.random() * random - random / 2, 0, Math.random() * random - random / 2));
		ball.setVelocity(vec);
		ball.setShooter(player);
		ball.setGravity(false);
		String name = String.valueOf(Sclat.getNotDuplicateNumber());
		DataMgr.mws.add(name);
		DataMgr.tsl.add(name);
		ball.setCustomName(name);
		DataMgr.getMainSnowballNameMap().put(name, ball);
		DataMgr.setSnowballHitCount(name, 0);
		BukkitRunnable task = new BlasterTickRunnable(name, player, ball, vec, distick, () -> ball);
		task.runTaskTimer(Sclat.getPlugin(), 0, 1);
	}

	public static void Explode(Player player, Location blastcenter) {
		PlayerData data = DataMgr.getPlayerData(player);
		// 半径
		double maxDist = data.getWeaponClass().getMainWeapon().getBlasterExHankei();

		// 爆発音
		player.getWorld().playSound(blastcenter, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);

		// 爆発エフェクト
		SclatUtil.createInkExplosionEffect(blastcenter, maxDist, 25, player);

		// バリアをはじく
		SclatUtil.repelBarrier(blastcenter, maxDist, player);

		// 塗る
		for (int i = 0; i <= maxDist - 1; i++) {
			List<Location> p_locs = Sphere.getSphere(blastcenter, i, 20);
			for (Location loc : p_locs) {
				PaintMgr.Paint(loc, player, false);
				PaintMgr.PaintHightestBlock(loc, player, false, false);
			}
		}

		// 攻撃判定の処理
		// for (Entity as : player.getWorld().getEntities()) {
		// if (as instanceof ArmorStand) {
		// if (as.getCustomName() != null) {
		// if (as.getLocation().distanceSquared(blastcenter) <= (maxDist + 1)*(maxDist +
		// 1)) {
		// try {
		// if (as.getCustomName().equals("Kasa")) {
		// KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
		// if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() !=
		// DataMgr.getPlayerData(player).getTeam()) {
		// cancel();
		// }
		// } else if (as.getCustomName().equals("SplashShield")) {
		// SplashShieldData splashShieldData =
		// DataMgr.getSplashShieldDataFromArmorStand((ArmorStand) as);
		// if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() !=
		// DataMgr.getPlayerData(player).getTeam()) {
		// cancel();
		// }
		// }
		// }catch (Exception e){}
		// }
		// }
		// }
		// }

		for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
			if (!DataMgr.getPlayerData(target).isInMatch())
				continue;
			if (target.getLocation().distance(blastcenter) <= maxDist + 1) {
				double damage = 10;
				if (data.getWeaponClass().getMainWeapon().getIsManeuver())
					damage = data.getWeaponClass().getMainWeapon().getBlasterExDamage();
				else
					damage = (maxDist - target.getLocation().distance(blastcenter))
							* data.getWeaponClass().getMainWeapon().getBlasterExDamage() * 0.4;
				if (damage > data.getWeaponClass().getMainWeapon().getDamage()) {
					damage = data.getWeaponClass().getMainWeapon().getDamage();
				}
				if (damage < 0.1) {
					damage = 0.1;
				}
				if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam()
						&& target.getGameMode().equals(GameMode.ADVENTURE)) {
					SclatUtil.giveDamage(player, target, damage, "killed");

					// AntiNoDamageTime
					SimpleRunnable.runTaskLater(cancel -> {
						target.setNoDamageTicks(0);
					}, 1);

				}
			}
		}

		for (Entity as : player.getWorld().getEntities()) {
			if (as instanceof ArmorStand) {
				if (as.getLocation().distanceSquared(blastcenter) <= (maxDist + 1) * (maxDist + 1)) {
					try {
						double damage = (maxDist + 1 - as.getLocation().distance(blastcenter))
								* data.getWeaponClass().getMainWeapon().getBlasterExDamage();
						if (damage > data.getWeaponClass().getMainWeapon().getDamage()) {
							damage = data.getWeaponClass().getMainWeapon().getDamage();
						}
						if (as.getCustomName().equals("Kasa")) {
							KasaData kasaData = DataMgr.getKasaDataFromArmorStand((ArmorStand) as);
							if (DataMgr.getPlayerData(kasaData.getPlayer()).getTeam() != DataMgr.getPlayerData(player)
									.getTeam()) {
								ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
							}
						} else if (as.getCustomName().equals("SplashShield")) {
							SplashShieldData splashShieldData = DataMgr
									.getSplashShieldDataFromArmorStand((ArmorStand) as);
							if (DataMgr.getPlayerData(splashShieldData.getPlayer()).getTeam() != DataMgr
									.getPlayerData(player).getTeam()) {
								ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}

	}

}
