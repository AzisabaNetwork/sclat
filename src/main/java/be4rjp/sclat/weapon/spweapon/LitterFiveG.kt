package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.raytrace.BoundingBox
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
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
    private const val max_charge = 44

    @JvmStatic
    fun setLitterFiveG(player: Player) {
        getPlayerData(player)!!.setIsUsingSP(true)
        getPlayerData(player)!!.setIsUsingSS(false)
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
                    player.getInventory().clear()
                    player.updateInventory()
                    val item = ItemStack(Material.NAUTILUS_SHELL)
                    val meta = item.getItemMeta()
                    meta!!.setDisplayName("右クリックで射撃!")
                    item.setItemMeta(meta)
                    for (count in 0..8) {
                        player.getInventory().setItem(count, item)
                    }
                    player.updateInventory()
                    charge_bar(player)
                }
            }
        it.runTaskLater(plugin, 2)

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    if (getPlayerData(p)!!.isInMatch()) {
                        getPlayerData(p)!!.setIsUsingSP(false)
                        getPlayerData(p)!!.setIsUsingSS(false)
                        player.getInventory().clear()
                        WeaponClassMgr.setWeaponClass(p)
                    }
                }
            }
        task.runTaskLater(plugin, 281)
    }

    fun charge_bar(player: Player) {
        val bar =
            plugin.getServer().createBossBar(
                getPlayerData(player)!!.team.teamColor!!.colorCode + "§cCharge",
                BarColor.RED,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        bar.setProgress(0.0)
        bar.addPlayer(player)

        val overheat_anime: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var bell: Boolean = false
                var visible: Boolean = true

                override fun run() {
                    val data = getPlayerData(p)
                    if (p.getGameMode() == GameMode.SPECTATOR) {
                        visible = false
                    }
                    if (Hash_charge.containsKey(p)) {
                        if (Hash_charge.get(p)!! < max_charge) {
                            bar.setProgress(Hash_charge.get(p)!!.toDouble() / max_charge)
                            if (!bar.getPlayers().contains(p)) {
                                bar.addPlayer(p)
                            }
                            if (bell) {
                                bell = false
                            }
                        } else {
                            bar.setProgress(1.0)
                            if (!bar.getPlayers().contains(p)) {
                                bar.addPlayer(p)
                            }
                            if (!bell) {
                                p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RETURN, 2.2f, 1.3f)
                                bell = true
                            }
                        }
                        if (!data!!.isInMatch() || !p.isOnline()) {
                            bar.removeAll()
                            cancel()
                        }
                        if (!data.getIsUsingSP()) {
                            bar.removeAll()
                            cancel()
                        }
                        if (data.settings.ShowEffect_ChargerLine()) {
                            if (Hash_charge.get(p)!! < max_charge) {
                                Hash_charge.replace(p, Hash_charge.get(p)!! + Hash_cps.get(p)!!)
                            }
                        } else {
                            if (Hash_charge.get(p)!! < max_charge && data.getIsUsingSS()) {
                                Hash_charge.replace(p, Hash_charge.get(p)!! + Hash_cps.get(p)!!)
                            }
                            if (Hash_charge.get(p) != 0 && !data.getIsUsingSS()) {
                                Shoot_LitterFiveG(p)
                            }
                        }
                        if (player.getInventory().getItemInMainHand().getItemMeta() == null) {
                            Hash_charge.replace(p, 0)
                            Hash_cps.replace(p, 1)
                        }
                        val rayTrace = RayTrace(p.getEyeLocation().toVector(), p.getEyeLocation().getDirection())
                        var range = Hash_charge.get(p)!!.toDouble()
                        if (range > max_charge) {
                            range = max_charge.toDouble()
                        }
                        val positions = rayTrace.traverse((range * 1.8).toInt().toDouble(), 0.7)
                        if (visible) {
                            check@ for (i in positions.indices) {
                                val position = positions.get(i).toLocation(p.getLocation().getWorld()!!)
                                if (position.getBlock().getType() != Material.AIR) {
                                    break
                                }
                                if (i % 5 == 0) {
                                    for (target in plugin.getServer().getOnlinePlayers()) {
                                        if (target == p) continue
                                        if (target.getWorld() === p.getWorld()) {
                                            if (target
                                                    .getLocation()
                                                    .distanceSquared(position) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions =
                                                    Particle.DustOptions(
                                                        data.team.teamColor!!.bukkitColor!!,
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
        overheat_anime.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun Shoot_LitterFiveG(player: Player) {
        if (player.getGameMode() == GameMode.SPECTATOR || !getPlayerData(player)!!.getIsUsingSP()) return
        var range: Int = Hash_charge.get(player)!!
        var damage = (Hash_charge.get(player)!! / 5).toDouble()
        // 半径
        var maxDist = 2.0
        if (range >= max_charge) {
            range = max_charge
            damage = 22.1
            maxDist = 4.0
            Hash_cps.replace(player, Hash_cps.get(player)!! + 1)
        } else {
            Hash_cps.replace(player, 1)
        }
        val reach = (range * 1.8).toInt()
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 5f)
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace.traverse((reach).toDouble(), 0.2)
        Hash_charge.replace(player, 0)

        loop@ for (vector in positions) {
            val position = vector.toLocation(player.getLocation().getWorld()!!)
            val block = player.getLocation().getWorld()!!.getBlockAt(position)

            if (block.getType() != Material.AIR) {
                for (o_player in plugin.getServer().getOnlinePlayers()) {
                    if (getPlayerData(o_player)!!.settings.ShowEffect_MainWeaponInk()) {
                        // 爆発音
                        player.getWorld().playSound(position, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

                        // 爆発エフェクト
                        createInkExplosionEffect(position, maxDist, 25, player)

                        // 塗る
                        var `in` = 0
                        while (`in` <= maxDist - 1) {
                            val p_locs = getSphere(position, `in`.toDouble(), 20)
                            for (loc in p_locs) {
                                PaintMgr.Paint(loc, player, false)
                                PaintMgr.PaintHightestBlock(loc, player, false, false)
                            }
                            `in`++
                        }
                    }
                }
                break
            }
            // PaintMgr.PaintHightestBlock(position, player, false, true);
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (!getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) continue
                if (target.getWorld() === position.getWorld()) {
                    if (target.getLocation().distanceSquared(position) < Sclat.particleRenderDistanceSquared) {
                        val bd =
                            getPlayerData(player)!!
                                .team.teamColor!!
                                .wool!!
                                .createBlockData()
                        target.spawnParticle<BlockData?>(Particle.BLOCK_DUST, position, 1, 0.0, 0.0, 0.0, 1.0, bd)
                    }
                }
            }

            val maxDistSquad = 4.0 // 2*2
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (!getPlayerData(target)!!.isInMatch()) continue
                if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                    target.getGameMode() == GameMode.ADVENTURE
                ) {
                    if (target.getLocation().distanceSquared(position) <= maxDistSquad) {
                        if (rayTrace.intersects(BoundingBox(target as Entity), (reach).toDouble(), 0.05)) {
                            val death: Boolean
                            death = giveDamage(player, target, damage, "spWeapon")
                            if (death) {
                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.2f, 1.3f)
                                player.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2f, 1.3f)
                            }

                            // AntiNoDamageTime
                            val task: BukkitRunnable =
                                object : BukkitRunnable() {
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
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else if (`as`.getCustomName() == "Kasa") {
                                    val ssdata = getKasaDataFromArmorStand(`as`)
                                    if (getPlayerData(ssdata!!.player)!!.team !=
                                        DataMgr
                                            .getPlayerData(player)!!
                                            .team
                                    ) {
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, player)
                                        `as`
                                            .getWorld()
                                            .playSound(`as`.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.2f)
                                        break@loop
                                    }
                                } else {
                                    if (SclatUtil.isNumber(`as`.getCustomName()!!)) {
                                        if (`as`.getCustomName() != "21" &&
                                            `as`.getCustomName() != "100"
                                        ) {
                                            if (`as`.isVisible()) {
                                                player.playSound(
                                                    player.getLocation(),
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
                    getPlayerData(p)!!.setCanUseSubWeapon(true)
                }
            }
        if (getPlayerData(player)!!.settings.ShowEffect_ChargerLine()) {
            task2.runTaskLater(plugin, 8)
        }
    }

    @JvmStatic
    fun Charge_LitterFiveG(player: Player?) {
        getPlayerData(player)!!.setIsUsingSS(true)

        val task3: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    getPlayerData(p)!!.setIsUsingSS(false)
                }
            }
        task3.runTaskLater(plugin, 8)
        getPlayerData(player)!!.setIsUsingSS(true)
    }
}
