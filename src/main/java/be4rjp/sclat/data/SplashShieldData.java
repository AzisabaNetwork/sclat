package be4rjp.sclat.data;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Be4rJP
 */
public class SplashShieldData {
    private final Player player;
    private BukkitRunnable task;
    private List<ArmorStand> list = new ArrayList<ArmorStand>();
    private double damage;
    private boolean IsDeploy = false;

    public SplashShieldData(Player player) {
        this.player = player;
    }

    public BukkitRunnable getTask() {
        return this.task;
    }

    public void setTask(BukkitRunnable task) {
        this.task = task;
    }

    public List<ArmorStand> getArmorStandList() {
        return this.list;
    }

    public void setArmorStandList(List<ArmorStand> list) {
        this.list = list;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public boolean getIsDeploy() {
        return this.IsDeploy;
    }

    public void setIsDeploy(boolean isdep) {
        this.IsDeploy = isdep;
    }

}
