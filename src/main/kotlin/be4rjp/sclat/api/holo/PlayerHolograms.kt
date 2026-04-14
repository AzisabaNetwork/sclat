package be4rjp.sclat.api.holo

import be4rjp.sclat.manager.PlayerStatusMgr
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked
import java.util.UUID
import java.util.function.Consumer

@NullMarked
class PlayerHolograms {
    private val rankingHoloMap: HashMap<UUID, RankingHolograms> = HashMap()

    fun add(player: Player) {
        val playerHolo = RankingHolograms(player)
        rankingHoloMap[player.uniqueId] = playerHolo
        PlayerStatusMgr.hologramUpdateRunnable(player)
    }

    fun ifPresent(
        player: Player,
        holoConsumer: Consumer<RankingHolograms>,
    ) {
        val holo = get(player)
        if (holo != null) {
            holoConsumer.accept(holo)
        }
    }

    fun get(player: Player): RankingHolograms? = get(player.uniqueId)

    fun get(playerUuid: UUID): RankingHolograms? = rankingHoloMap[playerUuid]

    fun remove(player: Player) {
        rankingHoloMap.remove(player.uniqueId)
    }

    val keys: MutableSet<UUID>
        get() = rankingHoloMap.keys
}
