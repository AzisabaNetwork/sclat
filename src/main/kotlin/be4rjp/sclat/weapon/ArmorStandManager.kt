package be4rjp.sclat.weapon

import be4rjp.sclat.api.GlowingAPI
import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.data.DataMgr.setKasaDataWithARmorStand
import be4rjp.sclat.data.KasaData
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.EnumItemSlot
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityEquipment
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle

object ArmorStandManager {
    val hashPlayer: HashMap<ArmorStand?, Player?> = HashMap()
    val hashArmorstand: HashMap<ArmorStand?, ArmorStand?> = HashMap()

    /**
     * Spawns and configures a group of 3 armor stands (main + 2 wings) for a funnel.
     * All stands are set up as small, invisible, gravity-less "Kasa" entities.
     *
     * @param player   The owning player (used for glowing setup and world context).
     * @param data     The player's data (main stand is registered here).
     * @param kdata    The kasa data associated with this group.
     * @param mainLoc  Spawn location for the main (central) stand.
     * @param wingsLoc Spawn location for the two wing stands (defaults to [mainLoc]).
     * @param applyGlowing Whether to explicitly disable glowing on the main stand.
     */
    fun spawnFunnelGroup(
        player: Player,
        data: PlayerData,
        kdata: KasaData,
        mainLoc: Location,
        wingsLoc: Location = mainLoc,
        applyGlowing: Boolean = false,
    ): MutableList<ArmorStand> {
        val main = player.world.spawnEntity(mainLoc.clone(), EntityType.ARMOR_STAND) as ArmorStand
        val wing1 = player.world.spawnEntity(wingsLoc.clone(), EntityType.ARMOR_STAND) as ArmorStand
        val wing2 = player.world.spawnEntity(wingsLoc.clone(), EntityType.ARMOR_STAND) as ArmorStand

        wing1.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(-40.0))
        wing2.headPose = EulerAngle(Math.toRadians(-45.0), 0.0, Math.toRadians(40.0))

        val list = mutableListOf(main, wing1, wing2)
        data.setArmorlist(main)
        if (applyGlowing) GlowingAPI.setGlowing(main, player, false)

        for (armorStand in list) {
            armorStand.isSmall = true
            armorStand.setBasePlate(false)
            armorStand.isVisible = false
            armorStand.setGravity(false)
            armorStand.customName = "Kasa"
            setKasaDataWithARmorStand(armorStand, kdata)
        }

        return list
    }

    /**
     * Sends equipment packets for a funnel group to all online players.
     * list[0] (main) wears the team wool; list[1] and list[2] (wings) wear the team glass pane.
     */
    fun sendEquipmentPackets(
        list: List<ArmorStand>,
        team: Team,
    ) {
        val glassPaneMaterial =
            Material.getMaterial(team.teamColor!!.glass.toString() + "_PANE")!!
        val woolItem = ItemStack(team.teamColor!!.wool!!)
        val glassPaneItem = ItemStack(glassPaneMaterial)

        for (onlinePlayer in plugin.server.onlinePlayers) {
            (onlinePlayer as CraftPlayer).handle.playerConnection.sendPacket(
                PacketPlayOutEntityEquipment(
                    list[1].entityId,
                    EnumItemSlot.HEAD,
                    CraftItemStack.asNMSCopy(glassPaneItem),
                ),
            )
            onlinePlayer.handle.playerConnection.sendPacket(
                PacketPlayOutEntityEquipment(
                    list[2].entityId,
                    EnumItemSlot.HEAD,
                    CraftItemStack.asNMSCopy(glassPaneItem),
                ),
            )
            onlinePlayer.handle.playerConnection.sendPacket(
                PacketPlayOutEntityEquipment(
                    list[0].entityId,
                    EnumItemSlot.HEAD,
                    CraftItemStack.asNMSCopy(woolItem),
                ),
            )
        }
    }

    /**
     * Removes a funnel group: cleans up map entries, unregisters the main stand from player data,
     * removes all entities, and clears the list.
     */
    fun cleanupFunnelGroup(
        list: MutableList<ArmorStand>,
        data: PlayerData,
    ) {
        if (list.isEmpty()) return
        val kasaStand = list[0]
        hashPlayer.remove(kasaStand)
        hashArmorstand.remove(kasaStand)
        data.subArmorlist(kasaStand)
        for (armorStand in list) {
            armorStand.remove()
        }
        list.clear()
    }
}
