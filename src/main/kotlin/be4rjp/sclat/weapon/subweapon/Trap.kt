package be4rjp.sclat.weapon.subweapon

import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.TrapData
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object Trap {
    @JvmStatic
    fun useTrap(player: Player) {
        val data = getPlayerData(player)
        val block = player.location.add(0.0, -1.0, 0.0).block
        if (data!!.team != null && PaintMgr.canPaint(block)) {
            PaintMgr.paint(block.location, player, true)
            if (player.isOnGround &&
                getPlayerData(player)!!.isInMatch &&
                player.exp >= 0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)
            ) {
                TrapData(player.location.add(0.0, -1.0, 0.0), player, data.team, data.trapCount)
                data.addTrapCount()
                player.exp = player.exp - (0.39 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()
                player.playSound(player.location, Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON, 1f, 1.2f)
            } else if (player.exp < (0.4 / Gear.getGearInfluence(player, Gear.Type.SUB_SPEC_UP)).toFloat()) {
                player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            } else if (!player.isOnGround) {
                player.sendTitle("", ChatColor.RED.toString() + "空中では使用できません", 0, 5, 2)
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            }
        } else if (!PaintMgr.canPaint(block)) {
            player.sendTitle("", ChatColor.RED.toString() + "ここでは使用できません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
        }

        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    val data = getPlayerData(player)
                    data!!.canUseSubWeapon = true
                }
            }
        delay.runTaskLater(plugin, 20)
    }
}
