package be4rjp.sclat.weapon.bucket;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BucketHealRunnable extends BukkitRunnable {
	private final Player p;
	private final int level;
	private int Ctime = 200;
	private boolean bh_recharge = true;
	public BucketHealRunnable(Player player, int level) {
		this.p = player;
		this.level = level;
	}

	@Override
	public void run() {
		PlayerData data = DataMgr.getPlayerData(p);
		if (level >= 1) {
			Ctime = 100;
		}
		if (!data.isInMatch() || !p.isOnline()) {
			cancel();
			return;
		}
		if (data.getIsSneaking() && bh_recharge && p.getGameMode().equals(GameMode.ADVENTURE)) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Ctime, level));
			p.getWorld().playSound(p.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.4F, 1.5F);
			bh_recharge = false;
			BukkitRunnable healtask = new BukkitRunnable() {// クールタイムを管理しています
				@Override
				public void run() {
					bh_recharge = true;
				}
			};
			healtask.runTaskLater(Sclat.getPlugin(), Ctime);
		}
	}
}
