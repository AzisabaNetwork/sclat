package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.GaugeAPI.toGauge
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
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
    fun SpinnerRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var charge: Int = 0
                var keeping: Int = 0
                var max: Int = getPlayerData(p)!!.getWeaponClass().mainWeapon!!.maxCharge

                override fun run() {
                    val data = getPlayerData(p)

                    data!!.tick = data.tick + 1

                    if (keeping == data.getWeaponClass().mainWeapon!!.chargeKeepingTime && data.getWeaponClass().mainWeapon!!.canChargeKeep &&
                        data.settings.doChargeKeep()
                    ) {
                        charge =
                            0
                    }

                    if (data.getIsUsingMM() || data.getIsUsingJetPack() || data.getIsUsingTyakuti() ||
                        data.getIsUsingSS()
                    ) {
                        charge = 0
                        data.tick = 8
                        return
                    }

                    if (data.tick <= 6 && data.isInMatch()) {
                        val w =
                            data
                                .getWeaponClass()
                                .mainWeapon!!
                                .weaponIteamStack!!
                                .clone()
                        val wm = w.getItemMeta()

                        // data.setTick(data.getTick() + 1);
                        if (charge < max) charge++

                        wm!!.setDisplayName(
                            (
                                wm.getDisplayName() + "§7[" +
                                    toGauge(charge, max, data.team.teamColor!!.colorCode, "§7") + "]"
                                ),
                        )
                        w.setItemMeta(wm)
                        p.getInventory().setItem(0, w)
                    }

                    if (charge == max || data.getWeaponClass().mainWeapon!!.hanbunCharge) {
                        if (p
                                .getInventory()
                                .getItemInMainHand()
                                .getType() == Material.AIR
                        ) {
                            if (data.getWeaponClass().mainWeapon!!.canChargeKeep) {
                                if (data.settings.doChargeKeep()) {
                                    data.tick =
                                        11
                                }
                            }
                        }
                    }

                    if (p.getGameMode() == GameMode.SPECTATOR) charge = 0

                    if (data.tick >= 11 && (charge == max || data.getWeaponClass().mainWeapon!!.hanbunCharge)) {
                        keeping++
                    } else {
                        keeping = 0
                    }

                    if (data.tick == 7 && data.isInMatch()) {
                        if (p.getExp() > data.getWeaponClass().mainWeapon!!.needInk * charge) {
                            SpinnerShootRunnable(
                                (charge * data.getWeaponClass().mainWeapon!!.chargeRatio).toInt(),
                                p,
                            )
                        } else {
                            val reach = (p.getExp() / data.getWeaponClass().mainWeapon!!.needInk).toInt()
                            if (reach >= 2) {
                                SpinnerShootRunnable(
                                    (reach * data.getWeaponClass().mainWeapon!!.chargeRatio).toInt(),
                                    p,
                                )
                            } else {
                                p.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 10, 2)
                                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                            }
                        }
                        charge = 0
                        p.getInventory().setItem(0, data.getWeaponClass().mainWeapon!!.weaponIteamStack)
                        data.tick = 8
                        data.setIsHolding(false)
                    }

                    if (!data.isInMatch() || !p.isOnline()) cancel()
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun SpinnerShootRunnable(
        charge: Int,
        player: Player,
    ) {
        getPlayerData(player)!!.setCanCharge(false)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    if (c == charge || !p.isOnline() || getPlayerData(p)!!.getIsSquid()) {
                        getPlayerData(p)!!.setCanCharge(true)
                        cancel()
                    }
                    val data = getPlayerData(p)
                    if (data!!.getIsUsingMM() || data.getIsUsingJetPack() || data.getIsUsingTyakuti() ||
                        data.getIsUsingSS()
                    ) {
                        cancel()
                    }
                    Shoot(
                        p,
                        (charge / getPlayerData(p)!!.getWeaponClass().mainWeapon!!.chargeRatio).toInt(),
                    )
                    c++
                }
            }
        task.runTaskTimer(
            plugin,
            2,
            getPlayerData(player)!!
                .getWeaponClass()
                .mainWeapon!!
                .shootTick
                .toLong(),
        )
    }

    fun Shoot(
        player: Player,
        charge: Int,
    ) {
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        if (player.getExp() <=
            (
                data!!.getWeaponClass().mainWeapon!!.needInk
                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                    Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                ).toFloat()
        ) {
            player.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 5, 2)
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
            return
        }
        player.setExp(
            player.getExp() -
                (
                    data.getWeaponClass().mainWeapon!!.needInk
                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                        Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)
                    ).toFloat(),
        )
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3f, 1.1f)
        val vec =
            player
                .getLocation()
                .getDirection()
                .multiply(getPlayerData(player)!!.getWeaponClass().mainWeapon!!.shootSpeed * charge)
        val random = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.random
        val distick = getPlayerData(player)!!.getWeaponClass().mainWeapon!!.distanceTick
        vec.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
                Math.random() * random - random / 2,
            ),
        )
        ball.setVelocity(vec)
        ball.setShooter(player)
        val name = notDuplicateNumber.toString()
        DataMgr.mws.add(name)
        ball.setCustomName(name)
        mainSnowballNameMap.put(name, ball)
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
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(getPlayerData(p)!!.getWeaponClass().mainWeapon!!.shootSpeed / 17)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        val bd =
                            getPlayerData(p)!!
                                .team.teamColor!!
                                .wool!!
                                .createBlockData()
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_MainWeaponInk()) {
                                if (o_player.getWorld() ===
                                    inkball!!.getWorld()
                                ) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<BlockData?>(
                                            Particle.BLOCK_DUST,
                                            inkball!!.getLocation(),
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
                        inkball!!.setVelocity(fallvec)
                        addedFallVec = true
                    }
                    if (i >= tick && i <= tick + 15) {
                        inkball!!.setVelocity(
                            inkball!!.getVelocity().add(Vector(0.0, -0.1, 0.0)),
                        )
                    }
                    if (i != tick) PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, true, true)
                    if (inkball!!.isDead()) cancel()

                    i++
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
