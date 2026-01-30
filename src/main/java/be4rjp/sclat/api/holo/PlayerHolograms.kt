package be4rjp.sclat.api.holo

import be4rjp.sclat.manager.PlayerStatusMgr
import org.bukkit.entity.Player
import org.jspecify.annotations.NullMarked
import java.util.UUID
import java.util.function.Consumer

@NullMarked
class PlayerHolograms {
    protected val rankingHoloMap: HashMap<UUID, RankingHolograms> = HashMap<UUID, RankingHolograms>()

    fun add(player: Player) {
        val playerHolo = RankingHolograms(player)
        rankingHoloMap.put(player.getUniqueId(), playerHolo)
        PlayerStatusMgr.HologramUpdateRunnable(player)
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

    fun get(player: Player): RankingHolograms? = get(player.getUniqueId())

    fun get(playerUuid: UUID): RankingHolograms? = rankingHoloMap.get(playerUuid)

    fun remove(player: Player) {
        rankingHoloMap.remove(player.getUniqueId())
    }

    val keys: MutableSet<UUID>
        get() = rankingHoloMap.keys
}
