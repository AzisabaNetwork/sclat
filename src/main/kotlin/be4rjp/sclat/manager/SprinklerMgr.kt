package be4rjp.sclat.manager

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object SprinklerMgr {
    fun sprinklerShoot(
        player: Player,
        `as`: ArmorStand,
        vec: Vector,
    ) {
        getPlayerData(player)
        val ball = player.world.spawnEntity(`as`.location.add(0.0, 0.5, 0.0), EntityType.SNOWBALL) as Snowball
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        player.world.playSound(`as`.location, Sound.ENTITY_PIG_STEP, 0.1f, 1f)
        val random = 1.2
        vec.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
            ),
        )
        ball.velocity = vec
        ball.shooter = player
        ball.customName = "Sprinkler"
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                val tick: Int = 2

                // Vector fallvec;
                val origvec: Vector = vec
                val inkball: Snowball = ball
                val p: Player = player

                override fun run() {
                    val bd =
                        getPlayerData(p)!!
                            .team!!
                            .teamColor!!
                            .wool!!
                            .createBlockData()
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings!!.showEffectMainWeaponInk()) {
                            o_player.spawnParticle<BlockData?>(
                                Particle.BLOCK_DUST,
                                inkball.location,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                1.0,
                                bd,
                            )
                        }
                    }
                    if (i >= tick) inkball.velocity = inkball.velocity.add(Vector(0.0, -0.1, 0.0))
                    if (i != tick) PaintMgr.paintHightestBlock(inkball.location, p, true, true)
                    if (inkball.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
