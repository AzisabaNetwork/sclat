package be4rjp.sclat.manager;

import be4rjp.sclat.VariablesKt;
import be4rjp.sclat.api.player.PlayerReturn;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerReturnManager {

	public static List<PlayerReturn> list = new ArrayList<>();

	public static boolean isReturned(String uuid) {
		for (PlayerReturn pr : list) {
			if (pr.getUUID().equals(uuid)) {
				list.remove(pr);
				return true;
			}
		}
		return false;
	}

	public static void runRemoveTask() {
		BukkitRunnable task = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					list.removeIf(pr -> !pr.getFlag());
				} catch (Exception e) {
				}
			}
		};
		task.runTaskTimer(VariablesKt.getPlugin(), 0, 200);
	}

	public static void addPlayerReturn(String uuid) {
		PlayerReturn pr = new PlayerReturn(uuid);
		list.add(pr);
	}
}
