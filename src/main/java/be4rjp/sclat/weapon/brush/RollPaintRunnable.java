package be4rjp.sclat.weapon.brush;

import be4rjp.dadadachecker.ClickType;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.PaintMgr;
import be4rjp.sclat.weapon.Gear;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RollPaintRunnable extends BukkitRunnable {
	private final Player player;
	protected RollPaintRunnable(Player player) {
		this.player = player;
	}
	@Override
	public void run() {
		try {
			PlayerData data = DataMgr.getPlayerData(player);
			if (!data.isInMatch() || !player.isOnline())
				cancel();

			if (data.getIsHolding() && data.getCanPaint() && data.isInMatch()
					&& Sclat.dadadaCheckerAPI.getPlayerClickType(player) != ClickType.RENDA
					&& player.getGameMode() != GameMode.SPECTATOR) {
				if (player.getExp() <= (float) (data.getWeaponClass().getMainWeapon().getRollerNeedInk()
						* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
						/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP))) {
					player.sendTitle("", ChatColor.RED + "インクが足りません", 0, 13, 2);
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
					return;
				}
				player.setExp(player.getExp() - (float) (data.getWeaponClass().getMainWeapon().getRollerNeedInk()
						* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
						/ Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
				Vector locvec = player.getEyeLocation().getDirection();
				Location eloc = player.getEyeLocation();
				Vector vec = new Vector(locvec.getX(), 0, locvec.getZ()).normalize();
				// RayTrace rayTrace1 = new RayTrace(front.toVector(), vec1);
				// ArrayList<Vector> positions1 =
				// rayTrace1.traverse(data.getWeaponClass().getMainWeapon().getRollerWidth(),
				// 0.5);
				Location front = eloc.add(vec.getX() * 2, -0.9, vec.getZ() * 2);
				if (data.getWeaponClass().getMainWeapon().getIsHude())
					front = eloc.add(vec.getX() * 1.5, -0.9, vec.getZ() * 1.5);
				org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(player).getTeam().getTeamColor().getWool()
						.createBlockData();
				for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
					if (DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
						if (target.getWorld() == player.getWorld())
							if (target.getLocation().distanceSquared(front) < Sclat.PARTICLE_RENDER_DISTANCE_SQUARED)
								target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, front, 2, 0, 0, 0, 1, bd);
				}
				Vector vec1 = new Vector(vec.getZ() * -1, 0, vec.getX());
				Vector vec2 = new Vector(vec.getZ(), 0, vec.getX() * -1);

				// 筆系武器
				if (data.getWeaponClass().getMainWeapon().getIsHude()) {
					Location position = player.getLocation();
					PaintMgr.PaintHightestBlock(front, player, false, true);
					player.getLocation().getWorld().spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 2, 0, 0, 0,
							1, bd);

					for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
						if (DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
							if (target.getWorld() == player.getWorld())
								if (target.getLocation()
										.distanceSquared(position) < Sclat.PARTICLE_RENDER_DISTANCE_SQUARED)
									target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 2, 0, 0, 0, 1, bd);
					}

					double maxDistSquad = 4 /* 2*2 */;
					for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
						if (!DataMgr.getPlayerData(target).isInMatch())
							continue;
						if (DataMgr.getPlayerData(player).getTeam() != DataMgr.getPlayerData(target).getTeam()
								&& target.getGameMode().equals(GameMode.ADVENTURE)) {
							if (target.getLocation().distanceSquared(position) <= maxDistSquad) {

								double damage = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon()
										.getRollerDamage();

								SclatUtil.giveDamage(player, target, damage, "killed");
							}
						}
					}

					for (Entity as : player.getWorld().getEntities()) {
						if (as instanceof ArmorStand) {
							if (as.getCustomName() != null) {
								if (as.getLocation().distanceSquared(position) <= maxDistSquad) {
									double damage = DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon()
											.getRollerDamage();
									ArmorStandMgr.giveDamageArmorStand((ArmorStand) as, damage, player);
								}
							}
						}
					}
					player.setWalkSpeed((float) (data.getWeaponClass().getMainWeapon().getUsingWalkSpeed()
							* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)));
					return;
				}
				PaintMgr.PaintHightestBlock(eloc, player, false, true);
				player.setWalkSpeed((float) (data.getWeaponClass().getMainWeapon().getUsingWalkSpeed()
						* Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)));
			}

		} catch (Exception e) {
			cancel();
		}
	}
}
