package be4rjp.sclat.emblem;

import org.bukkit.entity.Player;

import java.util.function.Function;

public class EmblemData {
    public final String itemName;
    public final Function<Player, Boolean> condition;

    public EmblemData(String itemName, Function<Player, Boolean> condition) {
        this.itemName = itemName;
        this.condition = condition;
    }
}
