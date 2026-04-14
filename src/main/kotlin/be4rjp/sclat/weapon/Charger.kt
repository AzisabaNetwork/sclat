package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GaugeAPI.toGauge
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.setPlayerFOV
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.plugin
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 *
 * @author Be4rJP
 */
object Charger {
    @JvmStatic
    fun chargerRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var charge: Int = 0
                var keeping: Int = 0
                var max: Int = getPlayerData(p)!!.weaponClass?.mainWeapon!!.maxCharge
                var min: Int = max * 2 / 3 // インク消費軽減チャージ

                override fun run() {
                    val data = getPlayerData(p)

                    data!!.tick = data.tick + 1

                    if (data.isUsingMM ||
                        data.isUsingJetPack ||
                        data.isUsingTyakuti ||
                        data.isUsingSS
                    ) {
                        charge = 0
                        data.tick = 8
                        return
                    }

                    if (keeping == data.weaponClass?.mainWeapon!!.chargeKeepingTime &&
                        data.weaponClass?.mainWeapon!!.canChargeKeep &&
                        data.settings?.doChargeKeep()!!
                    ) {
                        charge =
                            0
                    }

                    if (data.tick <= 6 && data.isInMatch) {
                        val w =
                            data.weaponClass
                                ?.mainWeapon!!
                                .weaponIteamStack!!
                                .clone()
                        val wm = w.itemMeta

                        if (data.weaponClass?.mainWeapon!!.scope) {
                            data.isCharging = true
                        }

                        // data.setTick(data.getTick() + 1);
                        if (charge < max) charge++

                        if (data.weaponClass?.mainWeapon!!.scope) {
                        /*
                         * if(charge != max) p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                         * 1, (int)charge / 3)); else p.addPotionEffect(new
                         * PotionEffect(PotionEffectType.SLOW, 40000, (int)charge / 3));
                         */
                            if (charge < max) {
                                setPlayerFOV(p, -0.1f / (charge.toFloat() / 19.0f))
                            }
                        }

                        wm!!.setDisplayName(
                            (
                                wm.displayName + "§7[" +
                                    toGauge(charge, max, data.team?.teamColor!!.colorCode, "§7") + "]"
                            ),
                        )
                        w.itemMeta = wm
                        p.inventory.setItem(0, w)
                        val rayTrace = RayTrace(p.eyeLocation.toVector(), p.eyeLocation.direction)
                        val positions =
                            rayTrace.traverse(
                                (
                                    charge.toDouble() * data.weaponClass?.mainWeapon!!.chargeRatio *
                                        data.weaponClass
                                            ?.mainWeapon!!
                                            .distanceTick
                                            .toDouble()
                                ).toInt()
                                    .toDouble(),
                                0.7,
                            )
                        check@ for (vector in positions) {
                            val position = vector.toLocation(p.location.world!!)
                            player.world.getBlockAt(position)
                            if (position.block.type != Material.AIR) {
                                // if(rayTrace.intersects(new BoundingBox(block), (int)(charge / 2 *
                                // data.getWeaponClass().getMainWeapon().getDistanceTick()), 0.1))
                                break
                            }
                            // if(i % 2 == 0){
                            // for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
                            // if (target.equals(p) ||
                            // !DataMgr.getPlayerData(target).getSettings().ShowEffect_ChargerLine())
                            // continue;
                            // if (target.getWorld() == p.getWorld()) {
                            // if (target.getLocation().distanceSquared(position) <
                            // Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                            // Particle.DustOptions dustOptions = new
                            // Particle.DustOptions(data.getTeam().getTeamColor().getBukkitColor(), 1);
                            // target.spawnParticle(Particle.REDSTONE, position, 1, 0, 0, 0, 50,
                            // dustOptions);
                            // }
                            // }
                            // }
                            // }
                        }
                    }

                    if (charge == max || data.weaponClass?.mainWeapon!!.hanbunCharge) {
                        if (p.inventory
                                .itemInMainHand.type == Material.AIR
                        ) {
                            if (data.weaponClass?.mainWeapon!!.canChargeKeep) {
                                if (data.settings?.doChargeKeep()!!) {
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
                    /*
                     * if(player.hasPotionEffect(PotionEffectType.SLOW))
                     * player.removePotionEffect(PotionEffectType.SLOW);
                     */
                        if (data.weaponClass?.mainWeapon!!.scope) {
                            data.isCharging = false
                            setPlayerFOV(player, 0.06f)
                        }
                        if (charge <= min) {
                            if (p.exp > (
                                    data.weaponClass?.mainWeapon!!.needInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) * charge
                                ) / 2
                            ) {
                                p.exp -=
                                    (
                                        (data.weaponClass?.mainWeapon!!.needInk / 2) *
                                            Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                            Gear.getGearInfluence(
                                                player,
                                                Gear.Type.MAIN_INK_EFFICIENCY_UP,
                                            ) * charge
                                    ).toFloat()
                                shoot(
                                    p,
                                    (
                                        charge.toDouble() * data.weaponClass?.mainWeapon!!.chargeRatio *
                                            data.weaponClass
                                                ?.mainWeapon!!
                                                .distanceTick
                                                .toDouble()
                                    ).toInt(),
                                    data.weaponClass?.mainWeapon!!.damage * charge,
                                    data.weaponClass?.mainWeapon!!.decreaseRate,
                                )
                            } else {
                                val reach = (p.exp / data.weaponClass?.mainWeapon!!.needInk).toInt()
                                if (reach >= 2) {
                                    charge = 0
                                    // p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                                    // p.setExp(p.getExp() - (float)
                                    // ((data.getWeaponClass().getMainWeapon().getNeedInk() * reach/2) *
                                    // Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                    // Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                                    p.exp = 0.0f
                                    shoot(
                                        p,
                                        (
                                            reach.toDouble() * data.weaponClass?.mainWeapon!!.chargeRatio *
                                                data.weaponClass
                                                    ?.mainWeapon!!
                                                    .distanceTick
                                                    .toDouble()
                                        ).toInt(),
                                        data.weaponClass?.mainWeapon!!.damage * reach,
                                        data.weaponClass?.mainWeapon!!.decreaseRate,
                                    )
                                } else {
                                    p.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 10, 2)
                                    p.playSound(p.location, Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                                }
                            }
                        } else if (p.exp > (
                                data.weaponClass?.mainWeapon!!.needInk * charge
                                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
                            )
                        ) {
                            p.exp -=
                                (
                                    data.weaponClass?.mainWeapon!!.needInk
                                        * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(
                                            player,
                                            Gear.Type.MAIN_INK_EFFICIENCY_UP,
                                        ) * charge
                                ).toFloat()
                            shoot(
                                p,
                                (
                                    charge.toDouble() * data.weaponClass?.mainWeapon!!.chargeRatio *
                                        data.weaponClass
                                            ?.mainWeapon!!
                                            .distanceTick
                                            .toDouble()
                                ).toInt(),
                                data.weaponClass?.mainWeapon!!.damage * charge,
                                data.weaponClass?.mainWeapon!!.decreaseRate,
                            )
                        } else {
                            val reach = (p.exp / data.weaponClass?.mainWeapon!!.needInk).toInt()
                            if (reach >= 2) {
                                charge = 0
                                // p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                                // p.setExp(p.getExp() -
                                // (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * reach *
                                // Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                // Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                                p.exp = 0.0f
                                shoot(
                                    p,
                                    (
                                        reach.toDouble() * data.weaponClass?.mainWeapon!!.chargeRatio *
                                            data.weaponClass
                                                ?.mainWeapon!!
                                                .distanceTick
                                                .toDouble()
                                    ).toInt(),
                                    data.weaponClass?.mainWeapon!!.damage * reach,
                                    data.weaponClass?.mainWeapon!!.decreaseRate,
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

    fun shoot(
        player: Player,
        reach: Int,
        damage: Double,
        decRate: Double,
    ) {
        if (player.gameMode == GameMode.SPECTATOR) return
        // player.sendMessage(String.valueOf(reach));
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions =
            rayTrace
                .traverse((reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(), 0.2)

        val entityLocation = player.eyeLocation
        val distance = reach.toDouble() // レイの長さ
        val world = player.world
        var raydistance = distance
        val rayresult =
            world.rayTraceBlocks(
                entityLocation,
                player.eyeLocation.direction,
                distance,
            )
        // if (result != null && result.getHitBlock() != null) {
        if (rayresult != null && rayresult.hitBlock != null) {
            val hitlocation = rayresult.hitPosition.toLocation(world)
            raydistance = entityLocation.distance(hitlocation)
        }
        var loopsize = positions.size.toFloat()
        var i = 0
        loop@ while (i < loopsize) {
            val position = positions[i].toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                if (block.type == Material.WHITE_STAINED_GLASS_PANE ||
                    block.type == Material.GLASS_PANE ||
                    block.type == Material.ORANGE_STAINED_GLASS_PANE ||
                    block.type == Material.LIGHT_BLUE_STAINED_GLASS_PANE ||
                    block.type == Material.RED_STAINED_GLASS_PANE ||
                    block.type == Material.LIME_STAINED_GLASS_PANE ||
                    block.type == Material.BLACK_STAINED_GLASS_PANE ||
                    block.type == Material.GRAY_STAINED_GLASS_PANE ||
                    block.type == Material.CYAN_STAINED_GLASS_PANE ||
                    block.type == Material.BLUE_STAINED_GLASS_PANE ||
                    block.type == Material.IRON_BARS
                ) {
                    val raydis = (raydistance / 0.195).toFloat()
                    if (loopsize > raydis) {
                        loopsize = raydis
                    }
                } else {
                    // if(rayTrace.intersects(new BoundingBox(block), reach, 0.01)){
                    PaintMgr.paint(position, player, true)
                    break
                    // }
                }
            }
            PaintMgr.paintHightestBlock(position, player, false, true)
            // for (Player target : Main.getPlugin().getServer().getOnlinePlayers()) {
            // if (!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
            // continue;
            // if (target.getWorld() == position.getWorld()) {
            // if (target.getLocation().distanceSquared(position) <
            // Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
            // org.bukkit.block.data.BlockData bd =
            // DataMgr.getPlayerData(player).getTeam().getTeamColor().wool.createBlockData();
            // target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, position, 1, 0, 0, 0, 1,
            // bd);
            // }
            // }
            // }
            if (getPlayerData(player)!!.settings?.showEffectMainWeaponInk()!!) {
                if (player.world === position.world) {
                    if (player.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                        val bd =
                            getPlayerData(player)!!
                                .team
                                ?.teamColor!!
                                .wool!!
                                .createBlockData()
                        player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                    }
                }
            }

            val maxDistSquad = 4.0 // 2*2
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.isInMatch) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.gameMode == GameMode.ADVENTURE
                ) {
                    if (target.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(
                                BoundingBox(target as Entity),
                                (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(),
                                0.05,
                            )
                        ) {
                            val death: Boolean
                            var hitDamage = damage
                            if (isbackstab(player, target)) {
                                hitDamage = damage * decRate
                            }
                            death = giveDamage(player, target, hitDamage, "killed")
                            if (death) {
                                player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.2f, 1.3f)
                            } else {
                                player.playSound(player.location, Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)
                            }

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var p: Player = target

                                    override fun run() {
                                        target.noDamageTicks = 0
                                    }
                                }
                            task.runTaskLater(plugin, 1)
                            break@loop
                        }
                    }
                }
            }

            for (`as` in player.world.entities) {
                if (`as` is ArmorStand) {
                    if (`as`.location.distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(
                                BoundingBox(`as` as Entity),
                                (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(),
                                0.05,
                            )
                        ) {
                            if (`as`.customName != null) {
                                if (`as`.customName == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(player)!!.team) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`.world
                                            .playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.customName == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team != getPlayerData(player)!!.team) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`.world
                                            .playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.customName!!)) {
                                        if (`as`.customName != "21" &&
                                            `as`.customName != "100"
                                        ) {
                                            if (`as`.isVisible) {
                                                player.playSound(
                                                    player.location,
                                                    Sound.ENTITY_ARROW_HIT_PLAYER,
                                                    1.2f,
                                                    1.3f,
                                                )
                                            }
                                        }
                                    }
                                    if (isbackstabStand(player, `as`)) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage * decRate, player)
                                    } else {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    }
                                    break@loop
                                }
                            }
                            if (isbackstabStand(player, `as`)) {
                                ArmorStandMgr.giveDamageArmorStand(`as`, damage * decRate, player)
                            } else {
                                ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                            }
                        }
                    }
                }
            }

            i++
        }
    }

    fun isbackstab(
        p: Player,
        target: Player,
    ): Boolean {
        var pyaw = 0.0
        var tyaw = 0.0
        if (p.eyeLocation.yaw < 0) {
            pyaw = (p.eyeLocation.yaw + 360).toDouble()
        } else {
            pyaw = p.eyeLocation.yaw.toDouble()
        }
        if (target.eyeLocation.yaw < 0) {
            tyaw = (target.eyeLocation.yaw + 360).toDouble()
        } else {
            tyaw = target.eyeLocation.yaw.toDouble()
        }
        return (pyaw - tyaw < 147 && pyaw - tyaw > -147) || pyaw - tyaw > 213 || pyaw - tyaw < -213
    }

    fun isbackstabStand(
        p: Player,
        target: ArmorStand,
    ): Boolean {
        var pyaw = 0.0
        var tyaw = 0.0
        if (p.eyeLocation.yaw < 0) {
            pyaw = (p.eyeLocation.yaw + 360).toDouble()
        } else {
            pyaw = p.eyeLocation.yaw.toDouble()
        }
        if (target.eyeLocation.yaw < 0) {
            tyaw = (target.eyeLocation.yaw + 360).toDouble()
        } else {
            tyaw = target.eyeLocation.yaw.toDouble()
        }
        return (pyaw - tyaw < 147 && pyaw - tyaw > -147) || pyaw - tyaw > 213 || pyaw - tyaw < -213
    }
}
