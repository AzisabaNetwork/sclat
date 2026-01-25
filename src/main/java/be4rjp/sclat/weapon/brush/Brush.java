package be4rjp.sclat.weapon.brush;

import be4rjp.dadadachecker.ClickType;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.SimpleRunnable;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class Brush {
	public static void HoldRunnable(Player player) {
		BukkitRunnable task = new BukkitRunnable() {
			Player p = player;
			@Override
			public void run() {
				PlayerData data = DataMgr.getPlayerData(p);

				data.setTick(data.getTick() + 1);

				if (!data.isInMatch() || !p.isOnline()) {
					cancel();
					return;
				}

				ClickType clickType = Sclat.dadadaCheckerAPI.getPlayerClickType(player);

				if (/* data.getTick() >= 6 */clickType == ClickType.NO_CLICK && data.isInMatch()) {
					data.setTick(7);
					data.setIsHolding(false);
					data.setCanPaint(false);
					data.setCanShoot(true);
				}
			}
		};
		task.runTaskTimer(Sclat.getPlugin(), 0, 1);
	}

	public static void RollPaintRunnable(Player player) {
		BukkitRunnable task = new RollPaintRunnable(player);
		if (DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getIsHude())
			task.runTaskTimer(Sclat.getPlugin(), 0, 1);
		else
			task.runTaskTimer(Sclat.getPlugin(), 0, 5);
	}

	public static void ShootPaintRunnable(Player player) {
		PlayerData pdata = DataMgr.getPlayerData(player);
		if (pdata.getCanRollerShoot()) {
			BukkitRunnable task = SimpleRunnable.from(cancel -> {
				if (!DataMgr.getPlayerData(player).isInMatch() || !player.isOnline()) {
					cancel.run();
					return;
				}
				pdata.setCanRollerShoot(true);
				if (!player.getGameMode().equals(GameMode.ADVENTURE)
						|| player.getInventory().getItemInMainHand().getItemMeta().equals(Material.AIR))
					return;
				if (player.getExp() >= pdata.getWeaponClass().getMainWeapon().getNeedInk())
					player.getWorld().playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1F, 1F);
				else
					return;
				Vector vec = player.getLocation().getDirection()
						.multiply(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getShootSpeed());
				final double random = pdata.getWeaponClass().getMainWeapon().getHudeRandom();
				vec.add(new Vector(Math.random() * random - random / 2, Math.random() * random / 4 - random / 8,
						Math.random() * random - random / 2));
				for (int i = 0; i < pdata.getWeaponClass().getMainWeapon().getRollerShootQuantity(); i++) {
					if (pdata.getWeaponClass().getMainWeapon().getIsHude())
						Brush.Shoot(player, vec);
					else
						Brush.Shoot(player, null);
				}
				// ShootRunnable(p);
				pdata.setCanPaint(true);

			});
			task.runTaskLater(Sclat.getPlugin(), pdata.getWeaponClass().getMainWeapon().getShootTick());
			pdata.setCanRollerShoot(false);
		}
	}

	public static void Shoot(Player player, Vector v) {

		if (player.getGameMode() == GameMode.SPECTATOR)
			return;

		PlayerData data = DataMgr.getPlayerData(player);
		if (player.getExp() <= (float) (data.getWeaponClass().getMainWeapon().getNeedInk()
				* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
				/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP))) {
			player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 13, 2);
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
			return;
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
		if (!data.getWeaponClass().getMainWeapon().getIsHude()) {
			if (player.isOnGround())
				vec.add(new Vector(Math.random() * random - random / 2, Math.random() * random / 4 - random / 8,
						Math.random() * random - random / 2));
			if (!player.isOnGround()) {
				if (data.getWeaponClass().getMainWeapon().getCanTatehuri())
					vec.add(new Vector(Math.random() * random / 4 - random / 8, Math.random() * random,
							Math.random() * random / 4 - random / 8));
				if (!data.getWeaponClass().getMainWeapon().getCanTatehuri())
					vec.add(new Vector(Math.random() * random - random / 2, Math.random() * random / 4 - random / 8,
							Math.random() * random - random / 2));
				// player.sendMessage(String.valueOf(player.isOnGround()));
			}
		} else {
			vec.add(new Vector(Math.random() * random - random / 2, Math.random() * random / 4 - random / 8,
					Math.random() * random - random / 2));
		}
		ball.setVelocity(vec);
		ball.setShooter(player);
		String name = String.valueOf(Sclat.getNotDuplicateNumber());
		DataMgr.mws.add(name);
		ball.setCustomName(name);
		DataMgr.getMainSnowballNameMap().put(name, ball);
		DataMgr.setSnowballHitCount(name, 0);

		new BrushTickRunnable(name, player, ball, distick, () -> ball).runTaskTimer(Sclat.getPlugin(), 0, 1);
	}

}
