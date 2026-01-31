package be4rjp.sclat.emblem

import org.bukkit.entity.Player
import java.util.function.Function

class EmblemData(
    val itemName: String,
    val condition: Function<Player, Boolean>,
)
