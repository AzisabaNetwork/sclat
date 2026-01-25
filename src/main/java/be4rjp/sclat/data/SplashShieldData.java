
package be4rjp.sclat.data;

import be4rjp.sclat.api.IOwnable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Be4rJP
 */
public class SplashShieldData implements IOwnable {
	private BukkitRunnable task;
	private List<ArmorStand> list = new ArrayList<>();
	private Player player;
	private double damage;
	private boolean IsDeploy = false;

	public SplashShieldData(Player player) {
		this.player = player;
	}

	public BukkitRunnable getTask() {
		return this.task;
	}

	public List<ArmorStand> getArmorStandList() {
		return this.list;
	}

	public Player getPlayer() {
		return this.player;
	}

	public double getDamage() {
		return this.damage;
	}

	public boolean getIsDeploy() {
		return this.IsDeploy;
	}

	public void setTask(BukkitRunnable task) {
		this.task = task;
	}

	public void setArmorStandList(List<ArmorStand> list) {
		this.list = list;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public void setIsDeploy(boolean isdep) {
		this.IsDeploy = isdep;
	}

	@Override
	public Player getOwner() {
		return player;
	}
}
