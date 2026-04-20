package be4rjp.sclat.api

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.packet.WorldPackets
import be4rjp.sclat.api.packet.WorldPackets.broadcastBlockChange
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.manager.BungeeCordMgr
import be4rjp.sclat.manager.DeathMgr
import be4rjp.sclat.manager.MatchMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.server.StatusClient
import net.azisaba.sclat.core.DelegatedLogger
import net.azisaba.sclat.core.shape.Sphere.getSphere
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Instrument
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 *
 * 全体的に使いそうなメソッドをここに置いておく
 */
object SclatUtil {
    private val logger by DelegatedLogger()

    @JvmStatic
    fun setBlockByNMS(
        b: Block,
        material: Material,
        applyPhysics: Boolean,
    ) {
        b.setType(material, applyPhysics)
    }

    @JvmStatic
    fun setBlockByNMSChunk(
        b: Block,
        material: Material,
        applyPhysics: Boolean,
    ) {
        b.setBlockData(material.createBlockData(), applyPhysics)
    }

    fun sendBlockChangeForAllPlayer(
        b: Block,
        material: Material?,
    ) {
        broadcastBlockChange(b.location, material)
    }

    @JvmStatic
    fun sendWorldBorderWarningPacket(player: Player) {
        WorldPackets.sendWorldBorderWarningPacket(player)
    }

    @JvmStatic
    fun sendWorldBorderWarningClearPacket(player: Player) {
        WorldPackets.sendWorldBorderWarningClearPacket(player)
    }

    @JvmStatic
    fun setPlayerFOV(
        player: Player,
        fov: Float,
    ) {
        WorldPackets.setPlayerFOV(player, fov)
    }

    @JvmStatic
    fun restartServer() {
        val commands: MutableList<String> =
            mutableListOf(
                "restart " + Sclat.conf?.servers!!.getString("ServerName"),
                "stop",
            )
        val sc =
            StatusClient(
                Sclat.conf?.config!!.getString("StatusShare.Host"),
                Sclat.conf?.config!!.getInt("StatusShare.Port"),
                commands,
            )
        sc.startClient()

        for (player in plugin.server.onlinePlayers) {
            BungeeCordMgr.playerSendServer(player, "sclat")
            DataMgr.getPlayerData(player)?.setServerName("Sclat")
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart")
                }
            }
        task.runTaskLater(plugin, 100)
    }

    @JvmStatic
    fun sendRestartedServerInfo() {
        val commands: MutableList<String> =
            mutableListOf(
                "restarted " + Sclat.conf?.servers!!.getString("ServerName"),
                "map " + Sclat.conf?.servers!!.getString("ServerName") + " " +
                    DataMgr.getMapRandom(if (MatchMgr.mapcount == 0) 0 else MatchMgr.mapcount - 1).mapName!!,
                "stop",
            )
        val sc =
            StatusClient(
                Sclat.conf?.config!!.getString("StatusShare.Host"),
                Sclat.conf?.config!!.getInt("StatusShare.Port"),
                commands,
            )
        sc.startClient()
    }

    @JvmStatic
    fun sendMessage(
        message: String?,
        type: MessageType,
    ) {
        val sclat = "[§bSclat§r] "
        val buff = StringBuilder()
        buff.append(sclat)
        buff.append(message)
        when (type) {
            MessageType.ALL_PLAYER -> {
                for (player in plugin.server.onlinePlayers) {
                    player.sendMessage(buff.toString())
                }
            }

            MessageType.CONSOLE -> {
                logger.info(buff.toString())
            }

            MessageType.BROADCAST -> {
                plugin.server.broadcastMessage(buff.toString())
            }

            else -> {}
        }
    }

    fun sendMessage(
        message: String?,
        type: MessageType?,
        team: Team?,
    ) {
        val sclat = "[§6Sclat§r] "
        val buff = StringBuilder()
        buff.append(sclat)
        buff.append(message)
        if (type == MessageType.TEAM) {
            for (player in plugin.server.onlinePlayers) {
                val playerTeam = DataMgr.getPlayerData(player)?.team ?: continue
                if (team == null) continue
                if (playerTeam != team) continue
                player.sendMessage(buff.toString())
            }
        }
    }

    @JvmStatic
    fun sendMessage(
        message: String?,
        type: MessageType?,
        player: Player,
    ) {
        val sclat = "[§6Sclat§r] "
        val buff = StringBuilder()
        buff.append(sclat)
        buff.append(message)
        if (type == MessageType.PLAYER) player.sendMessage(buff.toString())
    }

    @JvmStatic
    fun playGameSound(
        player: Player,
        type: SoundType,
    ) {
        when (type) {
            SoundType.ERROR -> {
                player.playNote(player.location, Instrument.BASS_GUITAR, Note.flat(0, Note.Tone.G))
                player.playNote(player.location, Instrument.BASS_GUITAR, Note.flat(0, Note.Tone.G))
            }

            SoundType.SUCCESS -> {
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
            }

            SoundType.CONGRATULATIONS -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            }
        }
    }

    fun sendSclatLobby(player: Player) {
        val send: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        BungeeCordMgr.playerSendServer(player, "sclat")
                        DataMgr.getPlayerData(player)?.setServerName("Sclat")
                    } catch (e: Exception) {
                    }
                }
            }
        send.runTaskLater(plugin, 20)
    }

    @JvmStatic
    fun createInkExplosionEffect(
        center: Location,
        radius: Double,
        accuracy: Int,
        player: Player?,
    ) {
        val sLocs: MutableList<Location> = getSphere(center, radius - 0.5, accuracy)
        val bd =
            DataMgr
                .getPlayerData(player)
                ?.team
                ?.teamColor!!
                .wool!!
                .createBlockData()
        for (oPlayer in plugin.server.onlinePlayers) {
            if (DataMgr.getPlayerData(oPlayer)?.settings?.showEffectBombEx()!!) {
                for (loc in sLocs) {
                    if (oPlayer.world === loc.world) {
                        if (oPlayer.location.distanceSquared(loc) < Sclat.particleRenderDistanceSquared) {
                            oPlayer.spawnParticle<BlockData?>(
                                Particle.BLOCK_DUST,
                                loc,
                                0,
                                loc.x - center.x,
                                loc.y - center.y,
                                loc.z - center.z,
                                1.0,
                                bd,
                            )
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun repelBarrier(
        center: Location,
        radius: Double,
        shooter: Player?,
    ) {
        for (player in plugin.server.onlinePlayers) {
            val playerData = DataMgr.getPlayerData(player)

            if (player.world !== center.world) continue
            if (playerData?.armor!! < 10000.0) continue
            if (player.gameMode == GameMode.SPECTATOR) continue
            if (playerData.team!! == DataMgr.getPlayerData(shooter)?.team!!) continue

            val distance = player.location.distance(center)

            if (distance > radius) continue

            val loc = player.location
            val vector = Vector(loc.x - center.x, 0.0, loc.z - center.z)

            if (vector.lengthSquared() == 0.0) continue

            val nomVec = vector.normalize()
            val rate = ((radius - distance) / radius) * 2.5

            player.velocity = nomVec.multiply(rate)
            player.world.playSound(player.location, Sound.ENTITY_SPLASH_POTION_BREAK, 1f, 1.5f)
        }
    }

    @JvmStatic
    fun giveDamage(
        player: Player?,
        target: Player,
        damage: Double,
        damageType: String?,
    ): Boolean {
        var damage = damage
        val targetData = DataMgr.getPlayerData(target)!!
        DataMgr.getPlayerData(player)
        if (target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            damage *= 0.6
        }
        val armorHealth = targetData.armor
        // if((target.getHealth()*2 + armorHealth*2 + target.getAbsorptionAmount() >
        // damage && armorHealth>=0.01 )||(target.getHealth() + armorHealth +
        // target.getAbsorptionAmount() > damage) && armorHealth<0.01){
        if ((target.health + target.absorptionAmount > (damage - armorHealth) / 2 && armorHealth > 0.01) ||
            ((target.health + target.absorptionAmount > damage) && armorHealth <= 0.01)
        ) {
            targetData.lastAttack = player
            if (armorHealth > damage) {
                targetData.armor = armorHealth - damage
            } else {
                if (armorHealth > 0.01) {
                    target.damage((damage - armorHealth) / 2)
                } else {
                    target.damage(damage - armorHealth)
                }
                targetData.armor = 0.0
            }
        } else {
            target.gameMode = GameMode.SPECTATOR
            DeathMgr.playerDeathRunnable(target, player!!, damageType!!)
            targetData.armor = 0.0
            return true
        }
        return false
    }

    @JvmStatic
    fun isNumber(s: String): Boolean {
        try {
            s.toDouble()
            return true
        } catch (e: NumberFormatException) {
            return false
        }
    }
}
