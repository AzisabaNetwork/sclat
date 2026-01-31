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
        val data = getPlayerData(player)
        val ball = player.getWorld().spawnEntity(`as`.getLocation().add(0.0, 0.5, 0.0), EntityType.SNOWBALL) as Snowball
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(`as`.getLocation(), Sound.ENTITY_PIG_STEP, 0.1f, 1f)
        val random = 1.2
        vec.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
            ),
        )
        ball.setVelocity(vec)
        ball.setShooter(player)
        ball.setCustomName("Sprinkler")
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
                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        if (getPlayerData(o_player)!!.settings!!.ShowEffect_MainWeaponInk()) {
                            o_player.spawnParticle<BlockData?>(
                                Particle.BLOCK_DUST,
                                inkball.getLocation(),
                                1,
                                0.0,
                                0.0,
                                0.0,
                                1.0,
                                bd,
                            )
                        }
                    }
                    if (i >= tick) inkball.setVelocity(inkball.getVelocity().add(Vector(0.0, -0.1, 0.0)))
                    if (i != tick) PaintMgr.paintHightestBlock(inkball.getLocation(), p, true, true)
                    if (inkball.isDead()) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
