
package be4rjp.sclat.weapon.bucket;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.SimpleRunnable;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class Bucket {
	public static void ShootBucket(Player player) {
		PlayerData data = DataMgr.getPlayerData(player);
		if (data.getCanRollerShoot()) {
			SimpleRunnable.runTaskLater(cancel -> {
				PlayerData playerData = DataMgr.getPlayerData(player);
				playerData.setCanRollerShoot(true);
			}, data.getWeaponClass().getMainWeapon().getCoolTime());

//			BukkitRunnable delay = new BukkitRunnable() {
//				@Override
//				public void run() {
//					boolean sound = false;
//					for (int i = 0; i < data.getWeaponClass().getMainWeapon().getRollerShootQuantity(); i++) {
//						boolean is = Shoot(player, null);
//						if (is)
//							sound = true;
//					}
//					if (sound)
//						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
//				}
//			};
//			 delay.runTaskLater(Main.getPlugin(),
//			 data.getWeaponClass().getMainWeapon().getDelay());

			new BucketShootRunnable(player, () -> Shoot(player, null)).runTaskTimer(Sclat.getPlugin(), 0, data.getWeaponClass().getMainWeapon().getDelay());
			data.setCanRollerShoot(false);
		}
	}

	public static boolean Shoot(Player player, Vector v) {

		if (player.getGameMode() == GameMode.SPECTATOR)
			return false;

		PlayerData data = DataMgr.getPlayerData(player);
		if (player.getExp() <= (float) (data.getWeaponClass().getMainWeapon().getNeedInk()
				* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
				/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP))) {
			player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 13, 2);
			return true;
		}
		player.setExp(player.getExp() - (float) (data.getWeaponClass().getMainWeapon().getNeedInk()
				* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
				/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
		Snowball ball = player.launchProjectile(Snowball.class);
		((CraftSnowball) ball).getHandle().setItem(CraftItemStack
				.asNMSCopy(new ItemStack(DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool())));
		Vector vec = player.getLocation().getDirection()
				.multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootSpeed());
		if (v != null)
			vec = v;
		double random = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getRandom();
		int distick = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getDistanceTick();
		vec.add(new Vector(Math.random() * random - random / 2, Math.random() * random / 1.5 - random / 3,
				Math.random() * random - random / 2));
		ball.setVelocity(vec);
		ball.setShooter(player);
		String name = String.valueOf(Sclat.getNotDuplicateNumber());
		DataMgr.mws.add(name);
		ball.setCustomName(name);
		DataMgr.getMainSnowballNameMap().put(name, ball);
		DataMgr.setSnowballHitCount(name, 0);
		new BucketTickRunnable(name, player, ball, distick, () -> ball).runTaskTimer(Sclat.getPlugin(), 0, 1);

		return false;
	}

	public static void BucketHealRunnable(Player player, int level) {
		new BucketHealRunnable(player, level).runTaskTimer(Sclat.getPlugin(), 0, 1);
	}
}
