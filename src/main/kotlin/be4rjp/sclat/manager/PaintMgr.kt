package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPaintDataFromBlock
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSpongeFromBlock
import be4rjp.sclat.data.DataMgr.setPaintDataFromBlock
import be4rjp.sclat.data.DataMgr.setSpongeWithBlock
import be4rjp.sclat.data.DataMgr.spongeMap
import be4rjp.sclat.data.Match
import be4rjp.sclat.data.PaintData
import be4rjp.sclat.data.Sponge
import be4rjp.sclat.weapon.Gear
import be4rjp.sclat.weapon.Gear.getGearInfluence
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import java.util.Random
import kotlin.math.abs

/**
 *
 * @author Be4rJP
 */
object PaintMgr {
    fun paint(
        location: Location,
        player: Player?,
        sphere: Boolean,
    ) {
        if (Sclat.type == ServerType.LOBBY) return

        val mw = getPlayerData(player)!!.weaponClass!!.mainWeapon
        var blocks: MutableList<Block> = ArrayList<Block>()
        blocks.add(location.getBlock())
        if (sphere) blocks = generateSphere(location, mw!!.maxPaintDis, 1.0, false, true, 0.0, mw.paintRandom)

        // List<Block> blocks = getTargetBlocks(location, mw.getPaintRandom(), true, 0,
        // mw.getMaxPaintDis());
        for (block in blocks) {
            if (block.getType() == getPlayerData(player)!!.team!!.teamColor!!.wool) continue

            try {
                if (block.getY() <= getPlayerData(player)!!.match!!.mapData!!.voidY) {
                    continue
                }
            } catch (e: Exception) {
            }

            if (block.getType() == Material.WET_SPONGE || block.getType().toString().contains("POWDER")) {
                if (spongeMap.containsKey(block)) {
                    val sponge = getSpongeFromBlock(block)
                    val pdata = getPlayerData(player)
                    if (pdata!!.weaponClass!!.mainWeapon!!.weaponType == "Charger") {
                        sponge!!.giveDamage(
                            15.0,
                            pdata.team,
                        )
                    } else {
                        sponge!!.giveDamage(pdata.weaponClass!!.mainWeapon!!.damage, pdata.team)
                    }
                } else if (block.getType() == Material.WET_SPONGE) {
                    val sponge = Sponge(block)
                    val pdata = getPlayerData(player)
                    sponge.match = (pdata!!.match)
                    sponge.team = (pdata.team)
                    setSpongeWithBlock(block, sponge)
                    sponge.giveDamage(pdata.weaponClass!!.mainWeapon!!.damage, pdata.team)
                }
            }

            if (block.getType() == Material.WET_SPONGE || block.getType().toString().contains("POWDER")) return

            if (canPaint(block)) {
                if (Sclat.Companion.conf!!
                        .config!!
                        .getString("WorkMode") !=
                    "Trial"
                ) {
                    if (!getPlayerData(player)!!.match!!.mapData!!.canPaintBBlock() &&
                        block.getType() == Material.BARRIER
                    ) {
                        return
                    }
                }
                if (blockDataMap.containsKey(block)) {
                    val data = getPaintDataFromBlock(block)
                    val bTeam = data!!.team
                    val aTeam = getPlayerData(player)!!.team
                    if (bTeam != aTeam) {
                        data.team = (aTeam)
                        bTeam!!.subtractPaintCount()
                        aTeam!!.addPaintCount()
                        // Sclat.setBlockByNMS(block, ATeam.getTeamColor().wool, false);
                        getPlayerData(player)!!.match!!.blockUpdater!!.setBlock(
                            block,
                            aTeam.teamColor!!.wool!!,
                        )
                        getPlayerData(player)!!.addPaintCount()
                        if (Random().nextInt(
                                (
                                    (
                                        12 / getGearInfluence(player, Gear.Type.SPECIAL_UP) /
                                            getPlayerData(player)!!.weaponClass!!.mainWeapon!!.sPRate
                                    )
                                ).toInt(),
                            ) == 0 &&
                            !getPlayerData(player)!!.isUsingSP
                        ) {
                            SPWeaponMgr.addSPCharge(player)
                        }
                    }
                } else {
                    val team = getPlayerData(player)!!.team
                    team!!.addPaintCount()
                    val data = PaintData(block)
                    data.match = (getPlayerData(player)!!.match)
                    data.team = (team)
                    data.setOrigianlType(block.getType())
                    data.blockData = (block.getBlockData())
                    // data.setOriginalState(block.getState());
                    setPaintDataFromBlock(block, data)
                    // Sclat.setBlockByNMS(block, team.getTeamColor().wool, false);
                    getPlayerData(player)!!.match!!.blockUpdater!!.setBlock(block, team.teamColor!!.wool!!)
                    getPlayerData(player)!!.addPaintCount()
                    if (Random().nextInt(
                            (
                                (
                                    13 / getGearInfluence(player, Gear.Type.SPECIAL_UP) /
                                        getPlayerData(player)!!.weaponClass!!.mainWeapon!!.sPRate
                                )
                            ).toInt(),
                        ) == 0 &&
                        !getPlayerData(player)!!.isUsingSP
                    ) {
                        SPWeaponMgr.addSPCharge(player)
                    }
                }
            }
        }
    }

    fun canPaint(block: Block): Boolean =
        !(
            block.getType() == Material.AIR ||
                block.getType() == Material.SHULKER_BOX ||
                block.getType() == Material.IRON_BARS ||
                block.getType() == Material.VINE ||
                block
                    .getType()
                    .toString()
                    .contains("SIGN") ||
                block.getType().toString().contains("GLASS") ||
                block.getType().toString().contains("CARPET") ||
                block.getType().toString().contains("POWDER") ||
                block.getType().toString().contains("FENCE") ||
                block.getType().toString().contains("STAIR") ||
                block
                    .getType()
                    .toString()
                    .contains("PLATE") ||
                block.getType() == Material.WATER ||
                block.getType() == Material.OBSIDIAN ||
                block
                    .getType()
                    .toString()
                    .contains("SLAB") ||
                block.getType().toString().contains("DOOR")
        )

    fun paintByTeam(
        block: Block,
        team: Team,
        match: Match,
    ) {
        var team = team
        var match = match
        if (block.getType() == team.teamColor!!.wool) return

        if (!(
                block.getType() == Material.AIR ||
                    block.getType() == Material.SHULKER_BOX ||
                    block.getType() == Material.IRON_BARS ||
                    block.getType() == Material.VINE ||
                    block
                        .getType()
                        .toString()
                        .contains("SIGN") ||
                    block.getType().toString().contains("GLASS") ||
                    block.getType().toString().contains("CARPET") ||
                    block.getType().toString().contains("POWDER") ||
                    block.getType().toString().contains("FENCE") ||
                    block.getType().toString().contains("STAIR") ||
                    block
                        .getType()
                        .toString()
                        .contains("PLATE") ||
                    block.getType() == Material.WATER ||
                    block.getType() == Material.OBSIDIAN ||
                    block
                        .getType()
                        .toString()
                        .contains("SLAB")
            )
        ) {
            if (blockDataMap.containsKey(block)) {
                val data = getPaintDataFromBlock(block)
                val bTeam = data!!.team
                val aTeam = team
                if (bTeam != aTeam) {
                    data.team = (aTeam)
                    bTeam!!.subtractPaintCount()
                    aTeam.addPaintCount()
                    match.blockUpdater!!.setBlock(block, aTeam.teamColor!!.wool!!)
                }
            } else {
                team.addPaintCount()
                val data = PaintData(block)
                data.match = (match)
                data.team = (team)
                data.setOrigianlType(block.getType())
                data.blockData = (block.getBlockData())
                setPaintDataFromBlock(block, data)
                match.blockUpdater!!.setBlock(block, team.teamColor!!.wool!!)
            }
        }
    }

    fun getCubeBlocks(
        start: Block,
        radius: Int,
    ): ArrayList<Block> {
        val blocks = ArrayList<Block>()
        var x = start.getLocation().getX() - radius
        while (x <= start.getLocation().getX() + radius) {
            var y = start.getLocation().getY() - radius
            while (y <= start.getLocation().getY() + radius) {
                var z = start.getLocation().getZ() - radius
                while (z <= start.getLocation().getZ() + radius) {
                    val loc = Location(start.getWorld(), x, y, z)
                    blocks.add(loc.getBlock())
                    z++
                }
                y++
            }
            x++
        }
        return blocks
    }

    @Synchronized
    fun getTargetBlocks(
        loc: Location,
        r: Int,
        loop: Boolean,
        loopc: Int,
        max: Int,
    ): MutableList<Block?> {
        val b0 = loc.getBlock()
        val b1 = loc.add(1.0, 0.0, 0.0).getBlock()
        val b2 = loc.add(0.0, 0.0, 1.0).getBlock()
        val b3 = loc.add(-1.0, 0.0, 0.0).getBlock()
        val b4 = loc.add(0.0, 0.0, -1.0).getBlock()
        val b5 = loc.add(0.0, 1.0, 0.0).getBlock()
        val b6 = loc.add(0.0, -1.0, 0.0).getBlock()

        val tempList: MutableList<Block?> = ArrayList<Block?>()

        if (loopc == 0) tempList.add(b0)
        tempList.add(b1)
        tempList.add(b2)
        tempList.add(b3)
        tempList.add(b4)

        if (loop) {
            val random = Random()
            val c = random.nextInt(r)
            val b = false
            var loopc2 = loopc
            if (c == 0) tempList.addAll(getTargetBlocks(b1.getLocation(), r, false, loopc2, max))
            if (c == 1) tempList.addAll(getTargetBlocks(b2.getLocation(), r, false, loopc2, max))
            if (c == 2) tempList.addAll(getTargetBlocks(b3.getLocation(), r, false, loopc2, max))
            if (c == 3) tempList.addAll(getTargetBlocks(b4.getLocation(), r, false, loopc2, max))

            loopc2++

            tempList.addAll(getTargetBlocks(b5.getLocation(), r, false, loopc2, max))
            tempList.addAll(getTargetBlocks(b6.getLocation(), r, false, loopc2, max))
        }
        return tempList
    }

    @Synchronized
    fun generateSphere(
        loc: Location,
        r: Double,
        h: Double,
        hollow: Boolean,
        sphere: Boolean,
        plus_y: Double,
        random: Int,
    ): MutableList<Block> {
        val circleblocks: MutableList<Block> = ArrayList<Block>()
        val cx = loc.getX()
        val cy = loc.getY()
        val cz = loc.getZ()

        var i = 0

        var x = cx - r
        while (x <= cx + r) {
            var z = cz - r
            while (z <= cz + r) {
                var y = (if (sphere) cy - r else cy)
                while (y < (if (sphere) cy + r else cy + h)) {
                    val dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (if (sphere) (cy - y) * (cy - y) else 0.0)
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        val l = Location(loc.getWorld(), x, y + plus_y, z)
                        circleblocks.add(l.getWorld()!!.getBlockAt(l))
                        if (random < i) {
                            val ran = Random()
                            val rani = ran.nextInt(2)
                            if (rani == 0) return circleblocks
                        }
                        i++
                    }
                    y++
                }
                z++
            }
            x++
        }
        return circleblocks
    }

    fun paintInLine(
        l1: Location,
        l2: Location,
        player: Player,
    ) {
        val xSlope = (l1.getBlockX() - l2.getBlockX()).toDouble()
        val ySlope = (l1.getBlockY() - l2.getBlockY()) / xSlope
        val zSlope = (l1.getBlockZ() - l2.getBlockZ()) / xSlope
        var y = l1.getBlockY().toDouble()
        var z = l1.getBlockZ().toDouble()
        val interval = 1 / (if (abs(ySlope) > abs(zSlope)) ySlope else zSlope)
        var x = l1.getBlockX().toDouble()
        while (x - l1.getBlockX() < abs(xSlope)) {
            var i = y.toInt()
            while (i > 0) {
                if (Location(player.getWorld(), x, i.toDouble(), z).getBlock().getType() != Material.AIR) {
                    val random = Random()
                    val r = random.nextInt(10)
                    if (r == 0) {
                        paint(Location(player.getWorld(), x, i.toDouble(), z), player, true)
                    }
                    break
                }
                i--
            }

            x += interval
            y += ySlope
            z += zSlope
        }
    }

    fun paintHightestBlock(
        loc: Location,
        player: Player,
        randomb: Boolean,
        inkrandom: Boolean,
    ) {
        var i = loc.getBlockY()
        val x = loc.getBlockX()
        val z = loc.getBlockZ()
        while (i > 0) {
            if (Location(player.getLocation().getWorld(), x.toDouble(), i.toDouble(), z.toDouble())
                    .getBlock()
                    .getType() != Material.AIR
            ) {
                val random = Random()
                val r = random.nextInt(8)
                if (r == 0 && randomb) {
                    paint(
                        Location(player.getLocation().getWorld(), x.toDouble(), i.toDouble(), z.toDouble()),
                        player,
                        inkrandom,
                    )
                }
                if (!randomb) {
                    paint(
                        Location(player.getLocation().getWorld(), x.toDouble(), i.toDouble(), z.toDouble()),
                        player,
                        inkrandom,
                    )
                }
                break
            }
            i--
        }
    }

    fun paintGlass(match: Match) {
        // team0
        var match = match
        val blocks: MutableList<Block> = ArrayList<Block>()
        val b0 =
            match.mapData!!
                .team0Loc!!
                .getBlock()
                .getRelative(BlockFace.DOWN)
        blocks.add(b0)
        blocks.add(b0.getRelative(BlockFace.EAST))
        blocks.add(b0.getRelative(BlockFace.NORTH))
        blocks.add(b0.getRelative(BlockFace.SOUTH))
        blocks.add(b0.getRelative(BlockFace.WEST))
        blocks.add(b0.getRelative(BlockFace.NORTH_EAST))
        blocks.add(b0.getRelative(BlockFace.NORTH_WEST))
        blocks.add(b0.getRelative(BlockFace.SOUTH_EAST))
        blocks.add(b0.getRelative(BlockFace.SOUTH_WEST))
        for (block in blocks) {
            if (block.getType() == Material.WHITE_STAINED_GLASS) {
                val data = PaintData(block)
                data.match = (match)
                data.team = (match.team0)
                data.setOrigianlType(block.getType())
                setPaintDataFromBlock(block, data)
                SclatUtil.setBlockByNMS(block, match.team0!!.teamColor!!.glass!!, false)
                // block.setType(match.team0.getTeamColor().glass);
                match.team0!!.addPaintCount()
            }
        }

        // team1
        val blocks1: MutableList<Block> = ArrayList<Block>()
        val b1 =
            match.mapData!!
                .team1Loc!!
                .getBlock()
                .getRelative(BlockFace.DOWN)
        blocks1.add(b1)
        blocks1.add(b1.getRelative(BlockFace.EAST))
        blocks1.add(b1.getRelative(BlockFace.NORTH))
        blocks1.add(b1.getRelative(BlockFace.SOUTH))
        blocks1.add(b1.getRelative(BlockFace.WEST))
        blocks1.add(b1.getRelative(BlockFace.NORTH_EAST))
        blocks1.add(b1.getRelative(BlockFace.NORTH_WEST))
        blocks1.add(b1.getRelative(BlockFace.SOUTH_EAST))
        blocks1.add(b1.getRelative(BlockFace.SOUTH_WEST))
        for (block in blocks1) {
            if (block.getType() == Material.WHITE_STAINED_GLASS) {
                val data = PaintData(block)
                data.match = (match)
                data.team = (match.team1)
                data.setOrigianlType(block.getType())
                setPaintDataFromBlock(block, data)
                SclatUtil.setBlockByNMS(block, match.team1!!.teamColor!!.glass!!, false)
                // block.setType(match.team1.getTeamColor().glass);
                match.team1!!.addPaintCount()
            }
        }
    }
}
