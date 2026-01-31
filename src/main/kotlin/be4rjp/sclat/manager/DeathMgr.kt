package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import be4rjp.sclat.weapon.Gear.getGearInfluence
import be4rjp.sclat.weapon.spweapon.SuperArmor.setArmor
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object DeathMgr {
    fun playerDeathRunnable(
        target: Player,
        shooter: Player,
        type: String,
    ) {
        getPlayerData(target)!!.isDead = (true)
        target.setGameMode(GameMode.SPECTATOR)

        getPlayerData(target)!!.poison = (false)

        val drop1 =
            target.getWorld().dropItem(
                target.getEyeLocation(),
                getPlayerData(target)!!.weaponClass!!.mainWeapon!!.weaponIteamStack!!,
            )
        val drop2 =
            target.getWorld().dropItem(
                target.getEyeLocation(),
                getPlayerData(target)!!.team!!.teamColor!!.bougu!!,
            )
        val random = 0.4
        drop1.setVelocity(
            Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2),
        )
        drop2.setVelocity(
            Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2),
        )

        createInkExplosionEffect(target.getEyeLocation().add(0.0, -1.0, 0.0), 3.0, 30, shooter)

        if (getGearInfluence(target, Gear.Type.PENA_DOWN) == 1.0) {
            getPlayerData(target)!!.sPGauge = ((getPlayerData(target)!!.sPGauge * 0.7).toInt())
        }

        // 半径
        val maxDist = 3.0

        // 塗る
        var i = 0
        while (i <= maxDist) {
            val pLocs = getSphere(target.getLocation(), i.toDouble(), 20)
            for (loc in pLocs) {
                PaintMgr.paint(loc, shooter, false)
            }
            i++
        }

        val clear: BukkitRunnable =
            object : BukkitRunnable() {
                override fun run() {
                    drop1.remove()
                    drop2.remove()
                }
            }
        clear.runTaskLater(plugin, 50)

        if (target.hasPotionEffect(PotionEffectType.GLOWING)) target.removePotionEffect(PotionEffectType.GLOWING)
        if (target.hasPotionEffect(PotionEffectType.SLOW)) target.removePotionEffect(PotionEffectType.SLOW)

        if (type == "killed" || type == "subWeapon" || type == "spWeapon") {
            getPlayerData(shooter)!!.addKillCount()
            getPlayerData(shooter)!!.team!!.addKillCount()
            if (!getPlayerData(shooter)!!.isUsingSP) for (i in 0..9) SPWeaponMgr.addSPCharge(shooter)
        } else if (getPlayerData(target)!!.lastAttack !== target) {
            val lastAttacker = getPlayerData(target)!!.lastAttack
            getPlayerData(lastAttacker)!!.addKillCount()
            getPlayerData(lastAttacker)!!.team!!.addKillCount()
            if (!getPlayerData(lastAttacker)!!.isUsingSP) for (i in 0..9) SPWeaponMgr.addSPCharge(lastAttacker)
        }

        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val t: Player = target
                val s: Player = shooter
                var loc: Location? = null
                var i: Int = 0

                override fun run() {
                    try {
                        if (!getPlayerData(t)!!.isInMatch) {
                            cancel()
                            return
                        }
                        if (type == "killed") {
                            t.setGameMode(GameMode.SPECTATOR)
                            t.getInventory().clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.getGameMode() == GameMode.ADVENTURE) {
                                t.setSpectatorTarget(s)
                            } else {
                                loc = getPlayerData(t)!!.match!!.mapData!!.intro
                                t.teleport(loc!!)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                    "に" + ChatColor.BOLD +
                                    sdata.weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .getItemMeta()!!
                                        .getDisplayName() +
                                    ChatColor.RESET + "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.getServer().getOnlinePlayers()) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!
                                                    .mainWeapon!!
                                                    .weaponIteamStack!!
                                                    .getItemMeta()!!
                                                    .getDisplayName() +
                                                ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
                                }
                            }
                            if (i == 20) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 4秒", msg, 0, 21, 0)
                            getPlayerData(t)!!.lastAttack = t
                            if (i == 40) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 3秒", msg, 0, 21, 0)
                            if (i == 60) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 2秒", msg, 0, 21, 0)
                            if (i == 80) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 1秒", msg, 0, 18, 2)
                            // t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
                            // s.displayName + ChatColor.RESET + "に" + ChatColor.BOLD +
                            // sdata.weaponClass.mainWeapon.getWeaponIteamStack().getItemMeta().displayName
                            // + ChatColor.RESET + "でやられた！", 0, 5, 2);
                            if (i == 100) {
                                getPlayerData(target)!!.isDead = (false)
                                val loc = getPlayerData(t)!!.matchLocation
                                t.teleport(loc!!)
                                t.setGameMode(GameMode.ADVENTURE)
                                t.getInventory().setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.getWorld().playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.setExp(0.99f)
                                t.setHealth(20.0)
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "subWeapon") {
                            t.setGameMode(GameMode.SPECTATOR)
                            t.getInventory().clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.getGameMode() == GameMode.ADVENTURE) {
                                t.setSpectatorTarget(s)
                            } else {
                                loc = getPlayerData(t)!!.match!!.mapData!!.intro
                                t.teleport(loc!!)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                    "に" + ChatColor.BOLD + sdata.weaponClass!!.subWeaponName + ChatColor.RESET +
                                    "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.getServer().getOnlinePlayers()) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!.subWeaponName + ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
                                }
                            }
                            if (i == 20) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 4秒", msg, 0, 21, 0)
                            getPlayerData(t)!!.lastAttack = t
                            if (i == 40) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 3秒", msg, 0, 21, 0)
                            if (i == 60) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 2秒", msg, 0, 21, 0)
                            if (i == 80) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 1秒", msg, 0, 18, 2)
                            // t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
                            // s.displayName + ChatColor.RESET + "に" + ChatColor.BOLD +
                            // sdata.weaponClass.mainWeapon.getWeaponIteamStack().getItemMeta().displayName
                            // + ChatColor.RESET + "でやられた！", 0, 5, 2);
                            if (i == 100) {
                                getPlayerData(target)!!.isDead = (false)
                                val loc = getPlayerData(t)!!.matchLocation
                                t.teleport(loc!!)
                                t.setGameMode(GameMode.ADVENTURE)
                                t.getInventory().setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.getWorld().playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.setExp(0.99f)
                                t.setHealth(20.0)
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "spWeapon") {
                            t.setGameMode(GameMode.SPECTATOR)
                            t.getInventory().clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.getGameMode() == GameMode.ADVENTURE) {
                                t.setSpectatorTarget(s)
                            } else {
                                loc = getPlayerData(t)!!.match!!.mapData!!.intro
                                t.teleport(loc!!)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                    "に" + ChatColor.BOLD + sdata.weaponClass!!.sPWeaponName + ChatColor.RESET +
                                    "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.getServer().getOnlinePlayers()) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.getDisplayName() + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.getDisplayName() + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!.sPWeaponName + ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
                                }
                            }
                            if (i == 20) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 4秒", msg, 0, 21, 0)
                            getPlayerData(t)!!.lastAttack = t
                            if (i == 40) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 3秒", msg, 0, 21, 0)
                            if (i == 60) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 2秒", msg, 0, 21, 0)
                            if (i == 80) t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 1秒", msg, 0, 18, 2)
                            // t.sendTitle("", sdata.getTeam().getTeamColor().getColorCode() +
                            // s.displayName + ChatColor.RESET + "に" + ChatColor.BOLD +
                            // sdata.weaponClass.mainWeapon.getWeaponIteamStack().getItemMeta().displayName
                            // + ChatColor.RESET + "でやられた！", 0, 5, 2);
                            if (i == 100) {
                                getPlayerData(target)!!.isDead = (false)
                                val loc = getPlayerData(t)!!.matchLocation
                                t.teleport(loc!!)
                                t.setGameMode(GameMode.ADVENTURE)
                                t.getInventory().setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.getWorld().playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.setExp(0.99f)
                                t.setHealth(20.0)
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "water") {
                            t.setGameMode(GameMode.SPECTATOR)
                            getPlayerData(t)!!.tick = 10
                            t.getInventory().clear()
                            if (i == 0) {
                                loc = t.getLocation()
                                if (getPlayerData(t)!!.lastAttack === t) {
                                    for (player in plugin
                                        .getServer()
                                        .getOnlinePlayers()) {
                                        player.sendMessage(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "は溺れてしまった！"
                                            ),
                                        )
                                    }
                                } else {
                                    val splayer = getPlayerData(t)!!.lastAttack
                                    val sdata = getPlayerData(splayer)
                                    for (player in plugin
                                        .getServer()
                                        .getOnlinePlayers()) {
                                        // player.sendMessage(
                                        // DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode() +
                                        // t.displayName + ChatColor.RESET + "は" + ChatColor.RESET
                                        // +sdata.getTeam().getTeamColor().getColorCode() + splayer.displayName +
                                        // ChatColor.RESET+ "に突き落とされてしまった！");
                                        player.sendMessage(
                                            (
                                                sdata!!.team!!.teamColor!!.colorCode +
                                                    splayer!!.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET +
                                                    getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "を水中に落とした！"
                                            ),
                                        )
                                    }
                                }
                            }
                            t.teleport(loc!!)

                            if (i == 0) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 5秒",
                                    "溺れてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 20) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 4秒",
                                    "溺れてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            getPlayerData(t)!!.lastAttack = t
                            if (i == 40) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 3秒",
                                    "溺れてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 60) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 2秒",
                                    "溺れてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 80) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 1秒",
                                    "溺れてしまった！",
                                    0,
                                    18,
                                    2,
                                )
                            }
                            if (i == 100) {
                                getPlayerData(target)!!.isDead = (false)
                                val loc1 = getPlayerData(t)!!.matchLocation
                                t.teleport(loc1!!)
                                t.setGameMode(GameMode.ADVENTURE)
                                t.getInventory().setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.getWorld().playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.setExp(0.99f)
                                t.setHealth(20.0)
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }
                        if (type == "fall") {
                            t.setGameMode(GameMode.SPECTATOR)
                            getPlayerData(t)!!.tick = 10
                            t.getInventory().clear()
                            if (i == 0) {
                                loc = getPlayerData(t)!!.match!!.mapData!!.intro
                                if (getPlayerData(t)!!.lastAttack === t) {
                                    for (player in plugin
                                        .getServer()
                                        .getOnlinePlayers()) {
                                        player.sendMessage(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "は奈落に落ちてしまった！"
                                            ),
                                        )
                                    }
                                } else {
                                    val splayer = getPlayerData(t)!!.lastAttack
                                    val sdata = getPlayerData(splayer)
                                    for (player in plugin
                                        .getServer()
                                        .getOnlinePlayers()) {
                                        // player.sendMessage(DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode()
                                        // + t.displayName + ChatColor.RESET + "は" + ChatColor.RESET +
                                        // sdata.getTeam().getTeamColor().getColorCode() + splayer.displayName +
                                        // ChatColor.RESET + "に突き落とされてしまった！");
                                        player.sendMessage(
                                            (
                                                sdata!!.team!!.teamColor!!.colorCode +
                                                    splayer!!.getDisplayName() + ChatColor.RESET + "は" + ChatColor.RESET +
                                                    getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.getDisplayName() + ChatColor.RESET + "を奈落に落とした！"
                                            ),
                                        )
                                    }
                                }
                            }
                            t.teleport(loc!!)

                            if (i == 0) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 5秒",
                                    "マップの外に落ちてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 20) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 4秒",
                                    "マップの外に落ちてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            getPlayerData(t)!!.lastAttack = t
                            if (i == 40) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 3秒",
                                    "マップの外に落ちてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 60) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 2秒",
                                    "マップの外に落ちてしまった！",
                                    0,
                                    21,
                                    0,
                                )
                            }
                            if (i == 80) {
                                t.sendTitle(
                                    ChatColor.GREEN.toString() + "復活まであと: 1秒",
                                    "マップの外に落ちてしまった！",
                                    0,
                                    18,
                                    2,
                                )
                            }
                            if (i == 100) {
                                getPlayerData(target)!!.isDead = (false)
                                val loc1 = getPlayerData(t)!!.matchLocation
                                t.teleport(loc1!!)
                                t.setGameMode(GameMode.ADVENTURE)
                                t.getInventory().setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.getWorld().playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.setExp(0.99f)
                                t.setHealth(20.0)
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (i == 100) target.getInventory().setHeldItemSlot(1)

                        i++
                    } catch (e: Exception) {
                        getPlayerData(target)!!.isDead = (false)
                        val loc1 = getPlayerData(t)!!.matchLocation
                        t.teleport(loc1!!)
                        t.setGameMode(GameMode.ADVENTURE)
                        t.getInventory().setItem(0, getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack)
                        t.getWorld().playSound(getPlayerData(t)!!.matchLocation!!, Sound.ENTITY_PLAYER_SWIM, 1f, 1f)
                        t.setExp(0.99f)
                        t.setHealth(20.0)
                        WeaponClassMgr.setWeaponClass(t)
                        setArmor(t, Double.Companion.MAX_VALUE, 120, false)
                        if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                        cancel()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun barrierEffectRunnable(
        player: Player,
        time: Int,
    ) {
        val resettime = time / 4
        val data = getPlayerData(player)

        // エフェクト
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var c: Int = 0

                override fun run() {
                    if (!data!!.isInMatch || (player.getGameMode() != GameMode.ADVENTURE) || !p.isOnline()) {
                        cancel()
                    }
                    val loc = p.getLocation().add(0.0, 0.5, 0.0)

                    val sLocs = getSphere(loc, 2.0, 23)
                    for (o_player in plugin.getServer().getOnlinePlayers()) {
                        if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon() && o_player != player) {
                            val dustOptions =
                                Particle.DustOptions(
                                    data.team!!.teamColor!!.bukkitColor!!,
                                    1f,
                                )
                            val bd =
                                getPlayerData(p)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            for (e_loc in sLocs) {
                                if (o_player.getWorld() === e_loc.getWorld()) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(e_loc) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            e_loc,
                                            0,
                                            0.0,
                                            0.0,
                                            0.0,
                                            70.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (c >= resettime) {
                        cancel()
                    }
                    if (data.armor < 9999) {
                        cancel()
                    }
                    c++
                }
            }
        task.runTaskTimer(plugin, 0, 4)
    }
}
