package be4rjp.sclat.api.holo;

import be4rjp.sclat.manager.PlayerStatusMgr;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@NullMarked
public class PlayerHolograms {
	protected final HashMap<UUID, RankingHolograms> rankingHoloMap = new HashMap<>();

	public void add(Player player) {
		RankingHolograms playerHolo = new RankingHolograms(player);
		rankingHoloMap.put(player.getUniqueId(), playerHolo);
		PlayerStatusMgr.HologramUpdateRunnable(player);
	}

	public void ifPresent(Player player, Consumer<RankingHolograms> holoConsumer) {
		RankingHolograms holo = get(player);
		if(holo != null) {
			holoConsumer.accept(holo);
		}
	}

	@Nullable
	public RankingHolograms get(Player player) {
		return get(player.getUniqueId());
	}

	@Nullable
	public RankingHolograms get(UUID playerUuid) {
		return rankingHoloMap.get(playerUuid);
	}

	public void remove(Player player) {
		rankingHoloMap.remove(player.getUniqueId());
	}

	public Set<UUID> getKeys() {
		return rankingHoloMap.keySet();
	}
}
