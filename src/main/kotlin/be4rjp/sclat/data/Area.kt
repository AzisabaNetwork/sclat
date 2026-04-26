package be4rjp.sclat.data

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.manager.PaintMgr
import net.azisaba.sclat.core.enums.MessageType
import net.azisaba.sclat.core.team.SclatTeam
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Shulker
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import java.util.function.Consumer

/**
 *
 * @author Be4rJP
 */
class Area(
    private val plugin: JavaPlugin,
    private val from: Location,
    private val to: Location,
) {
    private var match: Match? = null
    var team: SclatTeam? = null
        private set
    private var colorTeam0: Team? = null
    private var colorTeam1: Team? = null
    private var task: BukkitRunnable? = null
    var shulkerBoxes: MutableList<Shulker> = ArrayList()
        private set
    private var blist: MutableList<Block> = ArrayList()

    fun setupAreaTeam() {
        colorTeam0 =
            match!!
                .team0!!
                .team!!
                .scoreboard!!
                .registerNewTeam("ColorTeam0" + Sclat.notDuplicateNumber)
        colorTeam0!!.setCanSeeFriendlyInvisibles(false)
        colorTeam0!!.color = match!!.team0!!.teamColor!!.chatColor!!

        colorTeam1 =
            match!!
                .team1!! // Todo: もともとteam0を使っていたため、不都合が発生しないか確認する
                .team!!
                .scoreboard!!
                .registerNewTeam("ColorTeam1" + Sclat.notDuplicateNumber)
        colorTeam1!!.setCanSeeFriendlyInvisibles(false)
        colorTeam1!!.color = match!!.team1!!.teamColor!!.chatColor!!
    }

    fun setup(match: Match) {
        this.match = match
        this.team = null
        this.blist = ArrayList()
        this.shulkerBoxes = ArrayList()

        for (x in this.from.blockX..this.to.blockX) {
            for (z in this.from.blockZ..this.to.blockZ) {
                val loc = Location(this.from.world, x.toDouble(), this.from.blockY.toDouble(), z.toDouble())
                if (loc.block.type != Material.AIR) this.blist.add(loc.block)
                if (x == this.from.blockX || x == this.to.blockX || z == this.from.blockZ || z == this.to.blockZ) {
                    val sl =
                        this.from.world!!.spawnEntity(
                            loc.clone().add(0.0, 0.0, 0.0),
                            EntityType.SHULKER,
                        ) as Shulker
                    sl.setAI(false)
                    sl.setGravity(false)
                    sl.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 4000, 1, false, false))
                    sl.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 4000, 1, false, false))
                    // EntityShulker esl = ((CraftShulker)sl).getHandle();
                    // esl.setFlag(5, true);
                    this.shulkerBoxes.add(sl)
                    /*
                     * BukkitRunnable task = new BukkitRunnable() {
                     *
                     * @Override public void run() { for(Player oplayer :
                     * Main.getPlugin(Main.class).getServer().getOnlinePlayers()){
                     * if(!DataMgr.getPlayerData(oplayer).getSettings().ShowAreaRegion()) {
                     * ((CraftPlayer) oplayer).getHandle().playerConnection.sendPacket(new
                     * PacketPlayOutEntityDestroy(sl.getEntityId())); } } } };
                     * task.runTaskLater(Main.getPlugin(), 40);
                     *
                     */
                }
                /*
                 * for(Shulker sl : this.slist){ Block b =
                 * sl.getLocation().getBlock().getRelative(BlockFace.UP);
                 * if(b.getType().equals(Material.AIR) ||
                 * b.getType().toString().contains("CARPET")){
                 * match.getBlockUpdater().setBlock(b, Material.WHITE_CARPET);
                 * DataMgr.rblist.add(b); } //sl.remove(); }
                 */
            }
        }

        task =
            object : BukkitRunnable() {
                override fun run() {
                    // エリアの発光表示
//                /*
//                 * for(Shulker sl : slist) { for (Player oplayer :
//                 * Main.getPlugin(Main.class).getServer().getOnlinePlayers()) { if
//                 * (DataMgr.getPlayerData(oplayer).getSettings().ShowAreaRegion()) {
//                 * GlowingAPI.setGlowing(sl, oplayer, true); } } }
//                 */

                    // エリア処理

                    var t0c = 0
                    var t1c = 0
                    for (block in blist) {
                        if (DataMgr.blockDataMap.containsKey(block)) {
                            if (match.team0!! == DataMgr.blockDataMap[block]!!.team!!) {
                                t0c++
                            } else {
                                t1c++
                            }
                        }
                    }
                    if (team != null) {
                        if (team == match.team0!!) {
                            if ((blist.size * 0.5) < t1c.toDouble()) {
                                sendMessage("§3§lカウントストップ!", MessageType.ALL_PLAYER)
                                for (oplayer in plugin
                                    .server
                                    .onlinePlayers) {
                                    if (DataMgr.getPlayerData(oplayer)?.isInMatch!!) {
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 2f)
                                    }
                                }
                                removeColor()
                                team = null
                            }
                        }
                        if (team == match.team1!!) {
                            if ((blist.size * 0.5) < t0c.toDouble()) {
                                sendMessage("§3§lカウントストップ!", MessageType.ALL_PLAYER)
                                for (oplayer in plugin
                                    .server
                                    .onlinePlayers) {
                                    if (DataMgr.getPlayerData(oplayer)?.isInMatch!!) {
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 2f)
                                    }
                                }
                                removeColor()
                                team = null
                            }
                        }
                    } else {
                        if ((blist.size * 0.6) < t0c.toDouble()) {
                            team = match.team0!!
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (DataMgr.getPlayerData(oplayer)?.isInMatch!!) {
                                    if (team == DataMgr.getPlayerData(oplayer)?.team!!) {
                                        sendMessage("§fエリアを確保した!", MessageType.PLAYER, oplayer)
                                        oplayer.sendTitle("", "§fエリアを確保した!", 10, 20, 10)
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 3f)
                                    } else {
                                        sendMessage("§4エリアが確保された!", MessageType.PLAYER, oplayer)
                                        oplayer.sendTitle("", "§4エリアが確保された!", 10, 20, 10)
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 3f)
                                    }
                                }
                            }
                            updateBlocks()
                        } else if ((blist.size * 0.6) < t1c.toDouble()) {
                            team = match.team1!!
                            for (oplayer in plugin.server.onlinePlayers) {
                                if (DataMgr.getPlayerData(oplayer)?.isInMatch!!) {
                                    if (team == DataMgr.getPlayerData(oplayer)?.team!!) {
                                        sendMessage("§fエリアを確保した!", MessageType.PLAYER, oplayer)
                                        oplayer.sendTitle("", "§fエリアを確保した!", 10, 20, 10)
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 3f)
                                    } else {
                                        sendMessage("§4エリアが確保された!", MessageType.PLAYER, oplayer)
                                        oplayer.sendTitle("", "§4エリアが確保された!", 10, 20, 10)
                                        oplayer.playSound(oplayer.location, Sound.BLOCK_ANVIL_PLACE, 1f, 3f)
                                    }
                                }
                            }
                            updateBlocks()
                        }
                    }
                }
            }
    }

    fun start() {
        this.task!!.runTaskTimer(plugin, 0, 20)
    }

    fun stop() {
        this.task!!.cancel()
        this.shulkerBoxes.forEach(Consumer { obj: Shulker? -> obj!!.remove() })
        this.shulkerBoxes.clear()
    }

    private fun updateBlocks() {
        for (block in this.blist) {
            // Block b =
            // block.getLocation().getWorld().getHighestBlockAt(block.getLocation());
            PaintMgr.paintByTeam(block, team!!, match!!)
        }

        for (sl in this.shulkerBoxes) {
            if (match!!.team0!! == team) {
                colorTeam0!!.addEntry(sl.uniqueId.toString())
            } else {
                colorTeam1!!.addEntry(sl.uniqueId.toString())
            }
            // this.team?.team!!.addEntry(sl.getUniqueId().toString());
        }
    }

    fun removeColor() {
        for (sl in this.shulkerBoxes) {
            if (match!!.team0!! == team) {
                colorTeam0!!.removeEntry(sl.uniqueId.toString())
            } else {
                colorTeam1!!.removeEntry(sl.uniqueId.toString())
            }
            // this.team?.team!!.removeEntry(sl.getUniqueId().toString());
        }
    }
}
