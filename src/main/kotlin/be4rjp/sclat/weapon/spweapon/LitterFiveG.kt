package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr.getKasaDataFromArmorStand
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object LitterFiveG {
    private val Hash_charge = HashMap<Player?, Int?>()
    private val Hash_cps = HashMap<Player?, Int?>()
    private const val MAX_CHARGE = 44

    @JvmStatic
    fun setLitterFiveG(player: Player) {
        getPlayerData(player)!!.isUsingSP = true
        getPlayerData(player)!!.isUsingSS = false
        SPWeaponMgr.setSPCoolTimeAnimation(player, 280)
        if (Hash_charge.containsKey(player)) {
            Hash_charge.replace(player, 0)
            Hash_cps.replace(player, 1)
        } else {
            Hash_charge.put(player, 0)
            Hash_cps.put(player, 1)
        }
        val it: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    player.inventory.clear()
                    player.updateInventory()
                    val item = ItemStack(Material.NAUTILUS_SHELL)
                    val meta = item.itemMeta
                    meta!!.setDisplayName("右クリックで射撃!")
                    item.itemMeta = meta
                    for (count in 0..8) {
                        player.inventory.setItem(count, item)
                    }
                    player.updateInventory()
                    chargeBar(player)
                }
            }
        it.runTaskLater(plugin, 2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    if (getPlayerData(p)!!.isInMatch) {
                        getPlayerData(p)!!.isUsingSP = false
                        getPlayerData(p)!!.isUsingSS = false
                        player.inventory.clear()
                        WeaponClassMgr.setWeaponClass(p)
                    }
                }
            }
        task.runTaskLater(plugin, 281)
    }

    fun chargeBar(player: Player) {
        val bar =
            plugin.server.createBossBar(
                getPlayerData(player)!!.team!!.teamColor!!.colorCode + "§cCharge",
                BarColor.RED,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        bar.progress = 0.0
        bar.addPlayer(player)

        val overheatAnime: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var bell: Boolean = false
                var visible: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (p.gameMode == GameMode.SPECTATOR) {
                        visible = false
                    }
                    if (Hash_charge.containsKey(p)) {
                        if (Hash_charge[p]!! < MAX_CHARGE) {
                            bar.progress = Hash_charge[p]!!.toDouble() / MAX_CHARGE
                            if (!bar.players.contains(p)) {
                                bar.addPlayer(p)
                            }
                            if (bell) {
                                bell = false
                            }
                        } else {
                            bar.progress = 1.0
                            if (!bar.players.contains(p)) {
                                bar.addPlayer(p)
                            }
                            if (!bell) {
                                p.playSound(p.location, Sound.ITEM_TRIDENT_RETURN, 2.2f, 1.3f)
                                bell = true
                            }
                        }
                        if (!data!!.isInMatch || !p.isOnline) {
                            bar.removeAll()
                            cancel()
                        }
                        if (!data.isUsingSP) {
                            bar.removeAll()
                            cancel()
                        }
                        if (data.settings!!.showEffectChargerLine()) {
                            if (Hash_charge[p]!! < MAX_CHARGE) {
                                Hash_charge.replace(p, Hash_charge[p]!! + Hash_cps[p]!!)
                            }
                        } else {
                            if (Hash_charge[p]!! < MAX_CHARGE && data.isUsingSS) {
                                Hash_charge.replace(p, Hash_charge[p]!! + Hash_cps[p]!!)
                            }
                            if (Hash_charge[p] != 0 && !data.isUsingSS) {
                                shootLitterFiveG(p)
                            }
                        }
                        if (player.inventory.itemInMainHand.itemMeta == null) {
                            Hash_charge.replace(p, 0)
                            Hash_cps.replace(p, 1)
                        }
                        val rayTrace = RayTrace(p.eyeLocation.toVector(), p.eyeLocation.direction)
                        var range = Hash_charge[p]!!.toDouble()
                        if (range > MAX_CHARGE) {
                            range = MAX_CHARGE.toDouble()
                        }
                        val positions = rayTrace.traverse((range * 1.8).toInt().toDouble(), 0.7)
                        if (visible) {
                            check@ for (i in positions.indices) {
                                val position = positions[i].toLocation(p.location.world!!)
                                if (position.block.type != Material.AIR) {
                                    break
                                }
                                if (i % 5 == 0) {
                                    for (target in plugin.server.onlinePlayers) {
                                        if (target == p) continue
                                        if (target.world === p.world) {
                                            if (target
                                                    .location
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data.team!!.teamColor!!.bukkitColor!!,
                                                        1f,
                                                    )
                                                target.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    position,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    10.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        overheatAnime.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun shootLitterFiveG(player: Player) {
        if (player.gameMode == GameMode.SPECTATOR || !getPlayerData(player)!!.isUsingSP) return
        var range: Int = Hash_charge[player]!!
        var damage = (Hash_charge[player]!! / 5).toDouble()
        // 半径
        var maxDist = 2.0
        if (range >= MAX_CHARGE) {
            range = MAX_CHARGE
            damage = 22.1
            maxDist = 4.0
            Hash_cps.replace(player, Hash_cps[player]!! + 1)
        } else {
            Hash_cps.replace(player, 1)
        }
        val reach = (range * 1.8).toInt()
        player.world.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 5f)
        val rayTrace = RayTrace(player.eyeLocation.toVector(), player.eyeLocation.direction)
        val positions = rayTrace.traverse((reach).toDouble(), 0.2)
        Hash_charge.replace(player, 0)

        loop@ for (vector in positions) {
            val position = vector.toLocation(player.location.world!!)
            val block = player.location.world!!.getBlockAt(position)

            if (block.type != Material.AIR) {
                for (o_player in plugin.server.onlinePlayers) {
                    if (getPlayerData(o_player)!!.settings!!.showEffectMainWeaponInk()) {
                        // 爆発音
                        player.world.playSound(position, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(position, maxDist, 25, player)

                        // 塗る
                        var `in` = 0
                        while (`in` <= maxDist - 1) {
                            val pLocs = getSphere(position, `in`.toDouble(), 20)
                            for (loc in pLocs) {
                                PaintMgr.paint(loc, player, false)
                                PaintMgr.paintHightestBlock(loc, player, false, false)
                            }
                            `in`++
                        }
                    }
                }
                break
            }
            // PaintMgr.PaintHightestBlock(position, player, false, true);
            for (target in plugin.server.onlinePlayers) {
                if (!getPlayerData(target)!!.settings!!.showEffectMainWeaponInk()) continue
                if (target.world === position.world) {
                    if (target.location.distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                        val bd =
                            getPlayerData(player)!!
                                .team
                                ?.let { it.teamColor!! }!!
                                .wool!!
                                .createBlockData()
                        target.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
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
                        if (rayTrace.intersects(BoundingBox(target as Entity), (reach).toDouble(), 0.05)) {
                            val death: Boolean
                            death = giveDamage(player, target, damage, "spWeapon")
                            if (death) {
                                player.playSound(player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1.2f, 1.3f)
                                player.world.playSound(target.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
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
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .world
                                            .playSound(`as`.location, Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.customName == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .world
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
                                    ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                    break@loop
                                }
                            }
                            ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                        }
                    }
                }
            }
        }

        val task2: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    getPlayerData(p)!!.canUseSubWeapon = true
                }
            }
        if (getPlayerData(player)!!.settings!!.showEffectChargerLine()) {
            task2.runTaskLater(plugin, 8)
        }
    }

    @JvmStatic
    fun chargeLitterFiveG(player: Player?) {
        getPlayerData(player)!!.isUsingSS = true

        val task3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    getPlayerData(p)!!.isUsingSS = false
                }
            }
        task3.runTaskLater(plugin, 8)
        getPlayerData(player)!!.isUsingSS = true
    }
}
