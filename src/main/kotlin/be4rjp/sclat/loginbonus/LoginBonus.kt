package be4rjp.sclat.loginbonus

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.utils.DayRefreshSet
import be4rjp.sclat.manager.PlayerStatusMgr
import org.bukkit.entity.Player
import java.util.UUID

object LoginBonus {
    val refreshSet: DayRefreshSet = DayRefreshSet()

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
        PlayerStatusMgr.addMoney(player, Sclat.conf!!.loginBonusConfig.money)
        PlayerStatusMgr.addTicket(player, Sclat.conf!!.loginBonusConfig.ticket)
        markClaimed(player.uniqueId)
        return true
    }
}
