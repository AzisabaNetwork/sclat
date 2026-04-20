package be4rjp.sclat.data

import be4rjp.sclat.api.SclatUtil.setBlockByNMS
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.team.Team
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
class Sponge(
    var block: Block,
) {
    @JvmField
    var team: Team? = null
    var health: Double = 1.0

    @JvmField
    var match: Match? = null
    private var level = 0
    private var canGiveDamage = true

    fun giveDamage(
        damage: Double,
        team: Team?,
    ) {
        if (!canGiveDamage) return
        canGiveDamage = false
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    canGiveDamage = true
                }
            }
        task.runTaskLater(plugin, 10)

        if (this.team != team) {
            if (this.health > damage) {
                this.health -= damage
            } else {
                this.team = team
                this.health = 1.0
            }
        } else {
            if (this.health < 30) {
                this.health += damage
            }
        }

        if (this.health <= 10) {
            if (this.level != 0) {
                this.block
                    .location
                    .world!!
                    .playSound(this.block.location, Sound.ITEM_BUCKET_FILL, 1f, 1f)
            }
            this.level = 0
        }

        if (this.health > 10 && this.health < 25) {
            if (this.level != 1) {
                this.block
                    .location
                    .world!!
                    .playSound(this.block.location, Sound.ITEM_BUCKET_FILL, 1f, 1f)
            }
            this.level = 1
        }
        if (this.health >= 25) {
            if (this.level != 2) {
                this.block
                    .location
                    .world!!
                    .playSound(this.block.location, Sound.ITEM_BUCKET_FILL, 1f, 1f)
            }
            this.level = 2
        }

        // プレイヤーが近くにいないかチェック
//        /*
//         * for (Player player : Main.getPlugin().getServer().getOnlinePlayers()) {
//         * if(player.getWorld() == this.block.getWorld())
//         * if(player.getLocation().distance(this.block.getLocation()) < 3) return; }
//         */

        // Block reset
        val rb: MutableList<Block> = PaintMgr.getCubeBlocks(block, 2)
        for (b in rb) {
            if (b.type == Material.AIR || b.type.toString().contains("POWDER")) {
                if (DataMgr.blockDataMap.containsKey(b)) {
                    val data = DataMgr.getPaintDataFromBlock(b)
                    data?.team = this.team
                    // match.getBlockUpdater().setBlock(b, Material.AIR);
                    setBlockByNMS(b, Material.AIR, true)
                    // b.setType(Material.AIR);
                } else {
                    val data = PaintData(b)
                    data.match = match
                    data.setOrigianlType(b.type)
                    data.team = this.team
                    // match.getBlockUpdater().setBlock(b, Material.AIR);
                    setBlockByNMS(b, Material.AIR, true)
                    // b.setType(Material.AIR);
                    DataMgr.setPaintDataFromBlock(b, data)
                    DataMgr.setSpongeWithBlock(b, this)
                }
            }
        }

        val blocks: MutableList<Block> = PaintMgr.getCubeBlocks(block, level)
        for (b in blocks) {
            if (b.type == Material.AIR || b.type.toString().contains("POWDER")) {
                val data = DataMgr.getPaintDataFromBlock(b)
                data?.team = this.team
                // match.getBlockUpdater().setBlock(b,
                // Material.getMaterial(this.team.getTeamColor().getConcrete().toString() +
                // "_POWDER"));
                setBlockByNMS(
                    b,
                    Material.getMaterial(
                        this.team!!
                            .teamColor!!
                            .concrete!!
                            .toString() + "_POWDER",
                    )!!,
                    false,
                )
                // b.setType(Material.getMaterial(this.team.getTeamColor().getConcrete().toString()
                // + "_POWDER"));
            }
        }
    }
}
