package be4rjp.sclat.entity

import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class KasaEntity(
    private val player: Player,
    private val teamColor: String,
) {
    var damage: Double = 0.0
    private val armorStandList: MutableList<ArmorStand> = mutableListOf()

    fun initialize(
        location: Location,
        offset: Vector,
    ) {
        val spawnLocation = location.clone().add(offset)

        val as1 = spawnArmorStand(spawnLocation.clone().add(0.0, 2.8, 0.0), EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0)))
        val as2 = spawnArmorStand(spawnLocation.clone().add(0.0, 2.8, 0.0), EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0)))
        val as3 = spawnArmorStand(spawnLocation.clone().add(0.0, 2.5, 0.0), EulerAngle(0.0, 0.0, 0.0))

        armorStandList.addAll(listOf(as1, as2, as3))

        for (armorStand in armorStandList) {
            armorStand.customName = "Kasa"
            setKasaDataWithARmorStand(armorStand, null)
            GlowingAPI.setGlowing(armorStand, player, false)
        }

        equipArmorStands()
    }

    private fun spawnArmorStand(
        location: Location,
        headPose: EulerAngle,
    ): ArmorStand {
        val armorStand = player.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isSmall = true
        armorStand.setBasePlate(false)
        armorStand.isVisible = false
        armorStand.setGravity(false)
        armorStand.headPose = headPose
        return armorStand
    }

    private fun equipArmorStands() {
        for (armorStand in armorStandList) {
            val item = ItemStack(Material.getMaterial("${teamColor}_PANE")!!)
            armorStand.equipment?.helmet = item
        }
    }

    fun cleanup() {
        for (armorStand in armorStandList) {
            armorStand.remove()
        }
        armorStandList.clear()
    }
}
