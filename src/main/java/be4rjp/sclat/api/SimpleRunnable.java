package be4rjp.sclat.api;

import be4rjp.sclat.Sclat;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SimpleRunnable {
	public static BukkitRunnable from(Runnable action) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				action.run();
			}
		};
	}

    public static BukkitTask runTaskLater(Runnable action, long delay) {
        return from(action).runTaskLater(Sclat.getPlugin(), delay);
    }

    public static BukkitTask runTaskTimer(Runnable action, long delay, long period) {
        return from(action).runTaskTimer(Sclat.getPlugin(), delay, period);
    }
}
