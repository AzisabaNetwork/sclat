package be4rjp.sclat.manager

import be4rjp.sclat.data.DataMgr.getPlayerData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapCursor
import org.bukkit.map.MapCursorCollection
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object MapKitMgr {
    fun setMapKit(player: Player) {
        val view = Bukkit.getServer().createMap(player.getWorld())
        view.setCenterX(player.getLocation().getBlockX())
        view.setCenterZ(player.getLocation().getBlockZ())
        view.setScale(MapView.Scale.CLOSEST)

        // renderer
        view.addRenderer(MyRenderer())

        val item = ItemStack(Material.FILLED_MAP)
        val meta = item.getItemMeta() as MapMeta?
        @Suppress("DEPRECATION")
        meta!!.setMapId(view.getId())
        meta.setDisplayName("カーソルを合わせて右クリックで発射")
        item.setItemMeta(meta)

        for (count in 1..8) {
            player.getInventory().setItem(count, item)
        }
        meta.setDisplayName("カーソルを合わせて右クリックで発射!")
        item.setItemMeta(meta)
        player.getInventory().setItem(0, item)

        player.updateInventory()

        val loc = player.getLocation()
        loc.setPitch(65f)
        player.teleport(loc)

        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100000, 10))

        getPlayerData(player)!!.playerMapLoc = loc
    }

    fun getMapLocationVector(player: Player): Vector {
        val loc = getPlayerData(player)!!.playerMapLoc!!.clone()
        val ploc = player.getLocation().clone()
        var x = (((ploc.getYaw() + 180) - (loc.getYaw() + 180)) * 3 / 2).toInt()
        var y = ((ploc.getPitch() - loc.getPitch()) * 3 / 2).toInt()
        if (x > 128) x = 128
        if (x < -128) x = -128
        if (y > 128) y = 128
        if (y < -128) y = -128
        return Vector(x, 0, y)
    }

    internal class MyRenderer : MapRenderer() {
        override fun render(
            view: MapView,
            canvas: MapCanvas,
            player: Player,
        ) {
            val cursors = MapCursorCollection()
            val loc = getPlayerData(player)!!.playerMapLoc!!.clone()
            val ploc = player.getLocation().clone()
            var x = (((ploc.getYaw() + 180) - (loc.getYaw() + 180)) * 3).toInt()
            var y = ((ploc.getPitch() - loc.getPitch()) * 3).toInt()
            if (x > 128) x = 128
            if (x < -128) x = -128
            if (y > 128) y = 128
            if (y < -128) y = -128
            if (x == 128 || x == -128) {
                ploc.setYaw(loc.getYaw())
                player.teleport(ploc)
            }

            cursors.addCursor(MapCursor(x.toByte(), y.toByte(), 6.toByte(), MapCursor.Type.WHITE_CROSS, true))
            canvas.setCursors(cursors)
        }
    }
}
