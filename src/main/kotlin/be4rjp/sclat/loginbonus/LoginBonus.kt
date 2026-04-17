package be4rjp.sclat.loginbonus

import be4rjp.sclat.config.NewConfig
import be4rjp.sclat.manager.PlayerStatusMgr
import net.azisaba.sclat.core.utils.DailyRefreshSet
import org.bukkit.entity.Player
import java.util.UUID

object LoginBonus {
    var refreshSet: DailyRefreshSet = DailyRefreshSet()
        internal set

    fun isClaimable(playerUUID: UUID) = playerUUID !in refreshSet

    fun markClaimed(playerUUID: UUID) = refreshSet.plus(playerUUID)

    fun unmarkClaimed(playerUUID: UUID) = refreshSet.minus(playerUUID)

    /**
     * Try to claim daily login bonus
     *
     * @param player target player
     * @return is succeeded
     */
    fun tryClaim(player: Player): Boolean {
        if (!isClaimable(player.uniqueId)) return false
        PlayerStatusMgr.addMoney(player, NewConfig.loginBonusReward.money)
        PlayerStatusMgr.addTicket(player, NewConfig.loginBonusReward.ticket)
        markClaimed(player.uniqueId)
        return true
    }
}
