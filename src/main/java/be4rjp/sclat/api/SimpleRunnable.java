package be4rjp.sclat.api;

import be4rjp.sclat.Sclat;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class SimpleRunnable {
	public static BukkitRunnable from(Consumer<Runnable> action) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				action.accept(this::cancel);
			}
		};
	}

	public static BukkitTask runTaskLater(Consumer<Runnable> action, long delay) {
		return from(action).runTaskLater(Sclat.getPlugin(), delay);
	}

	public static BukkitTask runTaskTimer(Consumer<Runnable> action, long delay, long period) {
		return from(action).runTaskTimer(Sclat.getPlugin(), delay, period);
	}
}
