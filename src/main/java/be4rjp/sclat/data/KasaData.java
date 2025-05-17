package be4rjp.sclat.data;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Be4rJP
 */
public class KasaData {

    double damage = 0;
    private List<ArmorStand> list = new ArrayList<ArmorStand>();
    private final Player player;

    public KasaData(Player player) {
        this.player = player;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
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
}
