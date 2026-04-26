package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import net.azisaba.sclat.core.gauge.GaugeAPI.toGauge
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object Spinner {
    @JvmStatic
    fun spinnerRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var charge: Int = 0
                var keeping: Int = 0
                var max: Int = getPlayerData(p)!!.weaponClass!!.mainWeapon!!.maxCharge

                override fun run() {
                    val data = getPlayerData(p)

                    data!!.tick = data.tick + 1

                    if (keeping == data.weaponClass?.mainWeapon!!.chargeKeepingTime &&
                        data.weaponClass?.mainWeapon!!.canChargeKeep &&
                        data.settings!!.doChargeKeep()
                    ) {
                        charge =
                            0
                    }
                    if (data.isUsingMM || data.isUsingJetPack || data.isUsingTyakuti || data.isUsingSS) {
                        charge = 0
                        data.tick = 8
                        return
                    }

                    if (data.tick <= 6 && data.isInMatch) {
                        val w =
                            data
                                .weaponClass!!
                                .mainWeapon!!
                                .weaponIteamStack!!
                                .clone()
                        val wm = w.itemMeta

                        // data.setTick(data.getTick() + 1);
                        if (charge < max) charge++

                        wm!!.setDisplayName(
                            (
                                wm.displayName + "§7[" +
                                    toGauge(charge, max, data.team!!.teamColor!!.colorCode, "§7") + "]"
                            ),
                        )
                        w.itemMeta = wm
                        p.inventory.setItem(0, w)
                    }

                    if (charge == max || data.weaponClass?.mainWeapon!!.hanbunCharge) {
                        if (p
                                .inventory
                                .itemInMainHand
                                .type == Material.AIR
                        ) {
                            if (data.weaponClass?.mainWeapon!!.canChargeKeep) {
                                if (data.settings!!.doChargeKeep()) {
                                    data.tick =
                                        11
                                }
                            }
                        }
                    }

                    if (p.gameMode == GameMode.SPECTATOR) charge = 0

                    if (data.tick >= 11 && (charge == max || data.weaponClass?.mainWeapon!!.hanbunCharge)) {
                        keeping++
                    } else {
                        keeping = 0
                    }

                    if (data.tick == 7 && data.isInMatch) {
                        if (p.exp > data.weaponClass?.mainWeapon!!.needInk * charge) {
                            spinnerShootRunnable(
                                (charge * data.weaponClass?.mainWeapon!!.chargeRatio).toInt(),
                                p,
                            )
                        } else {
                            val reach = (p.exp / data.weaponClass?.mainWeapon!!.needInk).toInt()
                            if (reach >= 2) {
                                spinnerShootRunnable(
                                    (reach * data.weaponClass?.mainWeapon!!.chargeRatio).toInt(),
                                    p,
                                )
                            } else {
                                p.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 10, 2)
                                p.playSound(p.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                            }
                        }
                        charge = 0
                        p.inventory.setItem(0, data.weaponClass?.mainWeapon!!.weaponIteamStack)
                        data.tick = 8
                        data.isHolding = false
                    }

                    if (!data.isInMatch || !p.isOnline) cancel()
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun spinnerShootRunnable(
        charge: Int,
        player: Player,
    ) {
        getPlayerData(player)!!.canCharge = false
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    if (c == charge || !p.isOnline || getPlayerData(p)!!.isSquid) {
                        getPlayerData(p)!!.canCharge = true
                        cancel()
                    }
                    val data = getPlayerData(p)
                    if (data!!.isUsingMM ||
                        data.isUsingJetPack ||
                        data.isUsingTyakuti ||
                        data.isUsingSS
                    ) {
                        cancel()
                    }
                    shoot(
                        p,
                        (charge / getPlayerData(p)!!.weaponClass!!.mainWeapon!!.chargeRatio).toInt(),
                    )
                    c++
                }
            }
        task.runTaskTimer(
            plugin,
            2,
            getPlayerData(player)!!
                .weaponClass!!
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun shoot(
        player: Player,
        charge: Int,
    ) {
        if (player.gameMode == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.exp <=
            (
                data!!.weaponClass!!.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.exp -=
            (
                data.weaponClass?.mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
            ).toFloat()
        val ball = player.launchProjectile(Snowball::class.java)
        (ball as CraftSnowball).handle.setItem(CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team!!.teamColor!!.wool!!)))
        player.world.playSound(player.location, Sound.ENTITY_PIG_STEP, 0.3f, 1.1f)
        val vec =
            player
                .location
                .direction
                .multiply(getPlayerData(player)!!.weaponClass!!.mainWeapon!!.shootSpeed * charge)
        val random = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.random
        val distick = getPlayerData(player)!!.weaponClass!!.mainWeapon!!.distanceTick
        vec.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
            ),
        )
        ball.velocity = vec
        ball.shooter = player
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        ball.customName = name
        mainSnowballNameMap[name] = ball
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick

                // Vector fallvec;
                var origvec: Vector = vec
                var inkball: Snowball? = ball
                var addedFallVec: Boolean = false
                var p: Player = player
                var fallvec: Vector =
                    Vector(
                        inkball!!.velocity.getX(),
                        inkball!!.velocity.getY(),
                        inkball!!.velocity.getZ(),
                    ).multiply(getPlayerData(p)!!.weaponClass!!.mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap[name]

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.server.onlinePlayers) {
                            if (getPlayerData(o_player)!!.settings!!.showEffectMainWeaponInk()) {
                                if (o_player.world ===
                                    inkball!!.world
                                ) {
                                    if (o_player
                                            .location
                                            .distanceSquared(inkball!!.location) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball!!.location,
                                            0,
                                            0.0,
                                            -1.0,
                                            0.0,
                                            1.0,
                                            bd,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (i >= tick && !addedFallVec) {
                        inkball!!.velocity = fallvec
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.velocity = inkball!!.velocity.add(Vector(0.0, -0.1, 0.0))
                    }
                    if (i != tick) PaintMgr.paintHightestBlock(inkball!!.location, p, true, true)
                    if (inkball!!.isDead) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
