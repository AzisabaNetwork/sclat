package be4rjp.sclat.api.team

import be4rjp.sclat.data.MapData
import org.bukkit.Location

/**
 *
 * @author Be4rJP
 */
class TeamLoc(
    val map: MapData,
) {
    private val list0: MutableList<Location?>? = null

    private val list1: MutableList<Location?>? = null

    fun setupTeam0Loc() {
        val l = map.team0Loc
        // l.setX(l.getBlockX() + 0.5D);
        // l.setZ(l.getBlockZ() + 0.5D);
        // Location l1 = new Location(l.getWorld(), l.getBlockX() + 1D, l.getBlockY(),
        // l.getBlockZ() + 1D);
        // Location l2 = new Location(l.getWorld(), l.getBlockX() - 1D, l.getBlockY(),
        // l.getBlockZ() + 1D);
        // Location l3 = new Location(l.getWorld(), l.getBlockX() + 1D, l.getBlockY(),
        // l.getBlockZ() - 1D);
        // Location l4 = new Location(l.getWorld(), l.getBlockX() - 1D, l.getBlockY(),
        // l.getBlockZ() - 1D);
        // list0.add(l1);
        // list0.add(l2);
        // list0.add(l3);
        list0!!.add(l)
        list0.shuffle()
    }

    fun setupTeam1Loc() {
        val l = map.team1Loc!!
        l.x = l.blockX + 0.5
        l.z = l.blockZ + 0.5
        val l1 = Location(l.world, l.blockX + 1.0, l.blockY.toDouble(), l.blockZ + 1.0)
        val l2 = Location(l.world, l.blockX - 1.0, l.blockY.toDouble(), l.blockZ + 1.0)
        val l3 = Location(l.world, l.blockX + 1.0, l.blockY.toDouble(), l.blockZ - 1.0)
        val l4 = Location(l.world, l.blockX - 1.0, l.blockY.toDouble(), l.blockZ - 1.0)
        list1!!.add(l1)
        list1.add(l2)
        list1.add(l3)
        list1.add(l4)
        list1.shuffle()
    }

    fun getTeam0Loc(i: Int): Location? = list0!![i]

    fun getTeam1Loc(i: Int): Location? = list1!![i]
}
