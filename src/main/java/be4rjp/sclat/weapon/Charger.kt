package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.GaugeAPI.toGauge
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.SclatUtil.setPlayerFOV
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
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
    fun ChargerRunnable(player: Player) {
        val task: BukkitRunnable = object : BukkitRunnable() {
            var p: Player = player
            var charge: Int = 0
            var keeping: Int = 0
            var max: Int = getPlayerData(p)!!.getWeaponClass().mainWeapon!!.maxCharge
            var min: Int = max * 2 / 3 // インク消費軽減チャージ
            override fun run() {
                val data = getPlayerData(p)

                data!!.tick = data.tick + 1

                if (data.getIsUsingMM() || data.getIsUsingJetPack() || data.getIsUsingTyakuti() ||
                    data.getIsUsingSS()
                ) {
                    charge = 0
                    data.tick = 8
                    return
                }

                if (keeping == data.getWeaponClass().mainWeapon!!.chargeKeepingTime && data.getWeaponClass().mainWeapon!!.canChargeKeep && data.settings.doChargeKeep()) {
                    charge =
                        0
                }

                if (data.tick <= 6 && data.isInMatch()) {
                    val w = data.getWeaponClass().mainWeapon!!.weaponIteamStack!!.clone()
                    val wm = w.getItemMeta()

                    if (data.getWeaponClass().mainWeapon!!.scope) {
                        data.setIsCharging(true)
                    }

                    // data.setTick(data.getTick() + 1);
                    if (charge < max) charge++

                    if (data.getWeaponClass().mainWeapon!!.scope) {
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
                            wm.getDisplayName() + "§7[" +
                                toGauge(charge, max, data.team.teamColor!!.colorCode, "§7") + "]"
                            ),
                    )
                    w.setItemMeta(wm)
                    p.getInventory().setItem(0, w)
                    val rayTrace = RayTrace(p.getEyeLocation().toVector(), p.getEyeLocation().getDirection())
                    val positions = rayTrace.traverse(
                        (charge.toDouble() * data.getWeaponClass().mainWeapon!!.chargeRatio * data.getWeaponClass().mainWeapon!!.distanceTick.toDouble()).toInt()
                            .toDouble(),
                        0.7,
                    )
                    check@ for (vector in positions) {
                        val position = vector.toLocation(p.getLocation().getWorld()!!)
                        val block = player.getWorld().getBlockAt(position)
                        if (position.getBlock().getType() != Material.AIR) {
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

                if (charge == max || data.getWeaponClass().mainWeapon!!.hanbunCharge) if (p.getInventory()
                        .getItemInMainHand().getType() == Material.AIR
                ) if (data.getWeaponClass().mainWeapon!!.canChargeKeep) if (data.settings.doChargeKeep()) {
                    data.tick =
                        11
                }

                if (p.getGameMode() == GameMode.SPECTATOR) charge = 0

                if (data.tick >= 11 && (charge == max || data.getWeaponClass().mainWeapon!!.hanbunCharge)) {
                    keeping++
                } else {
                    keeping = 0
                }

                if (data.tick == 7 && data.isInMatch()) {
                    /*
                     * if(player.hasPotionEffect(PotionEffectType.SLOW))
                     * player.removePotionEffect(PotionEffectType.SLOW);
                     */
                    if (data.getWeaponClass().mainWeapon!!.scope) {
                        data.setIsCharging(false)
                        setPlayerFOV(player, 0.06f)
                    }
                    if (charge <= min) {
                        if (p.getExp() > (
                                data.getWeaponClass().mainWeapon!!.needInk
                                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) * charge
                                ) / 2
                        ) {
                            p.setExp(
                                p.getExp() - (
                                    (data.getWeaponClass().mainWeapon!!.needInk / 2) *
                                        Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                        Gear.getGearInfluence(
                                            player,
                                            Gear.Type.MAIN_INK_EFFICIENCY_UP,
                                        ) * charge
                                    ).toFloat(),
                            )
                            Shoot(
                                p,
                                (charge.toDouble() * data.getWeaponClass().mainWeapon!!.chargeRatio * data.getWeaponClass().mainWeapon!!.distanceTick.toDouble()).toInt(),
                                data.getWeaponClass().mainWeapon!!.damage * charge,
                                data.getWeaponClass().mainWeapon!!.decreaseRate,
                            )
                        } else {
                            val reach = (p.getExp() / data.getWeaponClass().mainWeapon!!.needInk).toInt()
                            if (reach >= 2) {
                                charge = 0
                                // p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                                // p.setExp(p.getExp() - (float)
                                // ((data.getWeaponClass().getMainWeapon().getNeedInk() * reach/2) *
                                // Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                // Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                                p.setExp(0.0f)
                                Shoot(
                                    p,
                                    (reach.toDouble() * data.getWeaponClass().mainWeapon!!.chargeRatio * data.getWeaponClass().mainWeapon!!.distanceTick.toDouble()).toInt(),
                                    data.getWeaponClass().mainWeapon!!.damage * reach,
                                    data.getWeaponClass().mainWeapon!!.decreaseRate,
                                )
                            } else {
                                p.sendTitle("", ChatColor.RED.toString() + "インクが足りません", 0, 10, 2)
                                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                            }
                        }
                    } else if (p.getExp() > (
                            data.getWeaponClass().mainWeapon!!.needInk * charge
                                * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)
                            )
                    ) {
                        p.setExp(
                            p.getExp() - (
                                data.getWeaponClass().mainWeapon!!.needInk
                                    * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                                    Gear.getGearInfluence(
                                        player,
                                        Gear.Type.MAIN_INK_EFFICIENCY_UP,
                                    ) * charge
                                ).toFloat(),
                        )
                        Shoot(
                            p,
                            (charge.toDouble() * data.getWeaponClass().mainWeapon!!.chargeRatio * data.getWeaponClass().mainWeapon!!.distanceTick.toDouble()).toInt(),
                            data.getWeaponClass().mainWeapon!!.damage * charge,
                            data.getWeaponClass().mainWeapon!!.decreaseRate,
                        )
                    } else {
                        val reach = (p.getExp() / data.getWeaponClass().mainWeapon!!.needInk).toInt()
                        if (reach >= 2) {
                            charge = 0
                            // p.sendMessage(String.valueOf(data.getWeaponClass().getMainWeapon().getChargeRatio()));
                            // p.setExp(p.getExp() -
                            // (float)(data.getWeaponClass().getMainWeapon().getNeedInk() * reach *
                            // Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP) /
                            // Gear.getGearInfluence(player, Gear.Type.MAIN_INK_EFFICIENCY_UP)));
                            p.setExp(0.0f)
                            Shoot(
                                p,
                                (reach.toDouble() * data.getWeaponClass().mainWeapon!!.chargeRatio * data.getWeaponClass().mainWeapon!!.distanceTick.toDouble()).toInt(),
                                data.getWeaponClass().mainWeapon!!.damage * reach,
                                data.getWeaponClass().mainWeapon!!.decreaseRate,
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

    fun Shoot(player: Player, reach: Int, damage: Double, decRate: Double) {
        if (player.getGameMode() == GameMode.SPECTATOR) return
        // player.sendMessage(String.valueOf(reach));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.4f, 5f)
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace
            .traverse((reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(), 0.2)

        val entityLocation = player.getEyeLocation()
        val distance = reach.toDouble() // レイの長さ
        val world = player.getWorld()
        var raydistance = distance
        val rayresult = world.rayTraceBlocks(
            entityLocation,
            player.getEyeLocation().getDirection(),
            distance,
        )
        // if (result != null && result.getHitBlock() != null) {
        if (rayresult != null && rayresult.getHitBlock() != null) {
            val hitlocation = rayresult.getHitPosition().toLocation(world)
            raydistance = entityLocation.distance(hitlocation)
        }
        var loopsize = positions.size.toFloat()
        var i = 0
        loop@ while (i < loopsize) {
            val position = positions.get(i).toLocation(player.getLocation().getWorld()!!)
            val block = player.getLocation().getWorld()!!.getBlockAt(position)

            if (block.getType() != Material.AIR) {
                if (block.getType() == Material.WHITE_STAINED_GLASS_PANE ||
                    block.getType() == Material.GLASS_PANE ||
                    block.getType() == Material.ORANGE_STAINED_GLASS_PANE ||
                    block.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ||
                    block.getType() == Material.RED_STAINED_GLASS_PANE ||
                    block.getType() == Material.LIME_STAINED_GLASS_PANE ||
                    block.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                    block.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    block.getType() == Material.CYAN_STAINED_GLASS_PANE ||
                    block.getType() == Material.BLUE_STAINED_GLASS_PANE ||
                    block.getType() == Material.IRON_BARS
                ) {
                    val raydis = (raydistance / 0.195).toFloat()
                    if (loopsize > raydis) {
                        loopsize = raydis
                    }
                } else {
                    // if(rayTrace.intersects(new BoundingBox(block), reach, 0.01)){
                    PaintMgr.Paint(position, player, true)
                    break
                    // }
                }
            }
            PaintMgr.PaintHightestBlock(position, player, false, true)
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
            if (getPlayerData(player)!!.settings.ShowEffect_MainWeaponInk()) {
                if (player.getWorld() === position.getWorld()) {
                    if (player.getLocation().distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                        val bd = getPlayerData(player)!!.team.teamColor!!.wool!!
                            .createBlockData()
                        player.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                    }
                }
            }

            val maxDistSquad = 4.0 /* 2*2 */
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (!getPlayerData(target)!!.isInMatch()) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.getGameMode() == GameMode.ADVENTURE
                ) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(
                                BoundingBox(target as Entity),
                                (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(),
                                0.05,
                            )
                        ) {
                            val death: Boolean
                            var hitDamage = damage
                            if (Isbackstab(player, target)) {
                                hitDamage = damage * decRate
                            }
                            death = giveDamage(player, target, hitDamage, "killed")
                            if (death) {
                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2f, 1.3f)
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)
                            }

                            // AntiNoDamageTime
                            val task: BukkitRunnable = object : BukkitRunnable() {
                                var p: Player = target
                                override fun run() {
                                    target.setNoDamageTicks(0)
                                }
                            }
                            task.runTaskLater(plugin, 1)
                            break@loop
                        }
                    }
                }
            }

            for (`as` in player.getWorld().getEntities()) {
                if (`as` is ArmorStand) {
                    if (`as`.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(
                                BoundingBox(`as` as Entity),
                                (reach * Gear.getGearInfluence(player, Gear.Type.MAIN_SPEC_UP)).toInt().toDouble(),
                                0.05,
                            )
                        ) {
                            if (`as`.getCustomName() != null) {
                                if (`as`.getCustomName() == "SplashShield") {
                                    val ssdata = getSplashShieldDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team != DataMgr
                                            .getPlayerData(player)!!.team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`.getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.getCustomName() == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team != DataMgr
                                            .getPlayerData(player)!!.team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`.getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.getCustomName()!!)) if (`as`.getCustomName() != "21" && `as`.getCustomName() != "100") if (`as`.isVisible()) {
                                        player.playSound(
                                            player.getLocation(),
                                            Sound.ENTITY_ARROW_HIT_PLAYER,
                                            1.2f,
                                            1.3f,
                                        )
                                    }
                                    if (IsbackstabStand(player, `as`)) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage * decRate, player)
                                    } else {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    }
                                    break@loop
                                }
                            }
                            if (IsbackstabStand(player, `as`)) {
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

    fun Isbackstab(p: Player, target: Player): Boolean {
        var pyaw = 0.0
        var tyaw = 0.0
        if (p.getEyeLocation().getYaw() < 0) {
            pyaw = (p.getEyeLocation().getYaw() + 360).toDouble()
        } else {
            pyaw = p.getEyeLocation().getYaw().toDouble()
        }
        if (target.getEyeLocation().getYaw() < 0) {
            tyaw = (target.getEyeLocation().getYaw() + 360).toDouble()
        } else {
            tyaw = target.getEyeLocation().getYaw().toDouble()
        }
        if ((pyaw - tyaw < 130 && pyaw - tyaw > -130) || pyaw - tyaw > 230 || pyaw - tyaw < -230) {
            return true
        } else {
            return false
        }
    }

    fun IsbackstabStand(p: Player, target: ArmorStand): Boolean {
        var pyaw = 0.0
        var tyaw = 0.0
        if (p.getEyeLocation().getYaw() < 0) {
            pyaw = (p.getEyeLocation().getYaw() + 360).toDouble()
        } else {
            pyaw = p.getEyeLocation().getYaw().toDouble()
        }
        if (target.getEyeLocation().getYaw() < 0) {
            tyaw = (target.getEyeLocation().getYaw() + 360).toDouble()
        } else {
            tyaw = target.getEyeLocation().getYaw().toDouble()
        }
        return (pyaw - tyaw < 130 && pyaw - tyaw > -130) || pyaw - tyaw > 230 || pyaw - tyaw < -230
    }
}
