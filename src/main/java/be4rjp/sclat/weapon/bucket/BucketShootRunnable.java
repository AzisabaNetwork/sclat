package be4rjp.sclat.weapon.bucket;

import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Supplier;

public class BucketShootRunnable extends BukkitRunnable {
	private final Player player;
	private final Supplier<Boolean> shootSupplier;
	int c = 0;
	boolean sound = false;
	public BucketShootRunnable(Player player, Supplier<Boolean> shootSupplier) {
		this.player = player;
		this.shootSupplier = shootSupplier;
	}
	@Override
	public void run() {
		PlayerData data = DataMgr.getPlayerData(player);
		c++;
		int q = 2;
		for (int i = 0; i < data.getWeaponClass().getMainWeapon().getRollerShootQuantity(); i++) {
			boolean is = shootSupplier.get();
			if (is)
				sound = true;
		}
		if (sound)
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
		if (c == q)
			cancel();
	}
}
