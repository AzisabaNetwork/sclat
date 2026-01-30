package be4rjp.sclat.api.utils

import org.bukkit.scoreboard.Objective

object ObjectiveUtil {
    @JvmStatic
    fun setLine(
        objective: Objective,
        list: MutableList<String>,
    ) {
        var index = list.size

        for (line in list) {
            index -= 1
            val score = objective.getScore(line)
            score.setScore(index)
        }
    }
}
