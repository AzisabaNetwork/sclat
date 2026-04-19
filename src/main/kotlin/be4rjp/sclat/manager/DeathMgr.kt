package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger
import be4rjp.sclat.weapon.Gear
import be4rjp.sclat.weapon.Gear.getGearInfluence
import be4rjp.sclat.weapon.spweapon.SuperArmor.setArmor
import net.azisaba.sclat.core.shape.Sphere.getSphere
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
        getPlayerData(target)!!.isDead = true
        target.gameMode = GameMode.SPECTATOR

        getPlayerData(target)!!.poison = false

        val drop1 =
            target.world.dropItem(
                target.eyeLocation,
                getPlayerData(target)!!.weaponClass!!.mainWeapon!!.weaponIteamStack!!,
            )
        val drop2 =
            target.world.dropItem(
                target.eyeLocation,
                getPlayerData(target)!!.team!!.teamColor!!.bougu!!,
            )
        val random = 0.4
        drop1.velocity = Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2)
        drop2.velocity = Vector(Math.random() * random - random / 2, random * 2 / 3, Math.random() * random - random / 2)

        createInkExplosionEffect(target.eyeLocation.add(0.0, -1.0, 0.0), 3.0, 30, shooter)

        if (getGearInfluence(target, Gear.Type.PENA_DOWN) == 1.0) {
            getPlayerData(target)!!.sPGauge = ((getPlayerData(target)!!.sPGauge * 0.7).toInt())
        }

        // 半径
        val maxDist = 3.0

        // 塗る
        for (i in 0..maxDist.toInt()) {
            val pLocs = getSphere(target.location, i.toDouble(), 20)
            for (loc in pLocs) {
                PaintMgr.paint(loc, shooter, false)
            }
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
                lateinit var loc: Location
                var i: Int = 0

                override fun run() {
                    try {
                        if (!getPlayerData(t)!!.isInMatch) {
                            cancel()
                            return
                        }
                        sclatLogger.debug("Handling player kill!!")
                        if (type == "killed") {
                            t.gameMode = GameMode.SPECTATOR
                            t.inventory.clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.gameMode == GameMode.ADVENTURE) {
                                t.spectatorTarget = s
                            } else {
                                loc = getPlayerData(t)?.match?.mapData?.intro!!
                                t.teleport(loc)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                    "に" + ChatColor.BOLD +
                                    sdata.weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .itemMeta!!
                                        .displayName +
                                    ChatColor.RESET + "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.server.onlinePlayers) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.displayName + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!
                                                    .mainWeapon!!
                                                    .weaponIteamStack!!
                                                    .itemMeta!!
                                                    .displayName +
                                                ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.displayName + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
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
                                t.gameMode = GameMode.ADVENTURE
                                t.inventory.setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.world.playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.exp = 0.99f
                                t.health = 20.0
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "subWeapon") {
                            t.gameMode = GameMode.SPECTATOR
                            t.inventory.clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.gameMode == GameMode.ADVENTURE) {
                                t.spectatorTarget = s
                            } else {
                                loc = getPlayerData(t)!!.match!!.mapData!!.intro
                                    ?: throw RuntimeException("mapdata intro is null on subWeapon")
                                t.teleport(loc)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                    "に" + ChatColor.BOLD + sdata.weaponClass!!.subWeaponName + ChatColor.RESET +
                                    "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.server.onlinePlayers) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.displayName + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!.subWeaponName + ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.displayName + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
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
                                t.gameMode = GameMode.ADVENTURE
                                t.inventory.setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.world.playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.exp = 0.99f
                                t.health = 20.0
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "spWeapon") {
                            t.gameMode = GameMode.SPECTATOR
                            t.inventory.clear()
                            getPlayerData(t)!!.tick = 10
                            if (s.gameMode == GameMode.ADVENTURE) {
                                t.spectatorTarget = s
                            } else {
                                loc =
                                    getPlayerData(t)!!.match!!.mapData!!.intro
                                        ?: throw RuntimeException("intro mapdata is null on wpWeapon")
                                t.teleport(loc)
                            }

                            val sdata = getPlayerData(s)
                            val msg = (
                                sdata!!.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                    "に" + ChatColor.BOLD + sdata.weaponClass!!.sPWeaponName + ChatColor.RESET +
                                    "でやられた！"
                            )
                            if (i == 0) {
                                t.sendTitle(ChatColor.GREEN.toString() + "復活まであと: 5秒", msg, 0, 21, 0)
                                for (player in plugin.server.onlinePlayers) {
                                    player.sendMessage(
                                        (
                                            sdata.team!!.teamColor!!.colorCode + s.displayName + ChatColor.RESET +
                                                "が" + getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                t.displayName + ChatColor.RESET + "を" + ChatColor.BOLD +
                                                sdata.weaponClass!!.sPWeaponName + ChatColor.RESET + "で倒した！"
                                        ),
                                    )
                                    s.spigot().sendMessage(
                                        ChatMessageType.ACTION_BAR,
                                        *TextComponent.fromLegacyText(
                                            (
                                                getPlayerData(t)!!.team!!.teamColor!!.colorCode +
                                                    t.displayName + ChatColor.RESET + "を倒した！"
                                            ),
                                        ),
                                    )
                                    s.playSound(s.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 10f)
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
                                t.gameMode = GameMode.ADVENTURE
                                t.inventory.setItem(
                                    0,
                                    getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.world.playSound(
                                    getPlayerData(t)!!.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.exp = 0.99f
                                t.health = 20.0
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.MAX_VALUE, 120, false)
                                if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (type == "water") {
                            t.gameMode = GameMode.SPECTATOR
                            val playerData =
                                getPlayerData(t) ?: run {
                                    sclatLogger.debug("Player Data is null on water death")
                                    return
                                }
                            playerData.tick = 10
                            t.inventory.clear()
                            sclatLogger.debug("Water tick if / current tick: {}", i)
                            if (i == 0) {
                                loc = t.location
                                if (playerData.lastAttack === t) {
                                    for (player in plugin
                                        .server
                                        .onlinePlayers) {
                                        player.sendMessage(
                                            (
                                                playerData.team?.teamColor?.colorCode +
                                                    t.displayName + ChatColor.RESET + "は溺れてしまった！"
                                            ),
                                        )
                                    }
                                } else {
                                    val splayer = playerData.lastAttack
                                    val sdata = getPlayerData(splayer)
                                    for (player in plugin
                                        .server
                                        .onlinePlayers) {
                                        // player.sendMessage(
                                        // DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode() +
                                        // t.displayName + ChatColor.RESET + "は" + ChatColor.RESET
                                        // +sdata.getTeam().getTeamColor().getColorCode() + splayer.displayName +
                                        // ChatColor.RESET+ "に突き落とされてしまった！");
                                        player.sendMessage(
                                            (
                                                sdata?.team?.teamColor?.colorCode +
                                                    splayer?.displayName + ChatColor.RESET + "は" + ChatColor.RESET +
                                                    playerData.team?.teamColor?.colorCode +
                                                    t.displayName + ChatColor.RESET + "を水中に落とした！"
                                            ),
                                        )
                                    }
                                }
                            }
                            t.teleport(loc)

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
                            playerData.lastAttack = t
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
                                val loc1 = playerData.matchLocation
                                t.teleport(loc1!!)
                                t.gameMode = GameMode.ADVENTURE
                                t.inventory.setItem(
                                    0,
                                    playerData.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.world.playSound(
                                    playerData.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.exp = 0.99f
                                t.health = 20.0
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.MAX_VALUE, 120, false)
                                if (playerData.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }
                        if (type == "fall") {
                            t.gameMode = GameMode.SPECTATOR
                            val playerData = getPlayerData(t)
                            if (playerData == null) {
                                sclatLogger.debug("Player data is null on fall handling")
                                return
                            }
                            playerData.tick = 10
                            t.inventory.clear()
                            sclatLogger.debug("on fall handling")
                            if (i == 0) {
                                loc = playerData.match!!.mapData!!.intro ?: throw RuntimeException("Intro map data is null!")
                                if (playerData.lastAttack === t) {
                                    for (player in plugin
                                        .server
                                        .onlinePlayers) {
                                        player.sendMessage(
                                            (
                                                playerData.team!!.teamColor!!.colorCode +
                                                    t.displayName + ChatColor.RESET + "は奈落に落ちてしまった！"
                                            ),
                                        )
                                    }
                                } else {
                                    val splayer = playerData.lastAttack
                                    val sdata = getPlayerData(splayer)
                                    for (player in plugin
                                        .server
                                        .onlinePlayers) {
                                        // player.sendMessage(DataMgr.getPlayerData(t).getTeam().getTeamColor().getColorCode()
                                        // + t.displayName + ChatColor.RESET + "は" + ChatColor.RESET +
                                        // sdata.getTeam().getTeamColor().getColorCode() + splayer.displayName +
                                        // ChatColor.RESET + "に突き落とされてしまった！");
                                        player.sendMessage(
                                            (
                                                sdata!!.team!!.teamColor!!.colorCode +
                                                    splayer!!.displayName + ChatColor.RESET + "は" + ChatColor.RESET +
                                                    playerData.team!!.teamColor!!.colorCode +
                                                    t.displayName + ChatColor.RESET + "を奈落に落とした！"
                                            ),
                                        )
                                    }
                                }
                            }
                            t.teleport(loc)

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
                            playerData.lastAttack = t
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
                                val loc1 = playerData.matchLocation
                                t.teleport(loc1!!)
                                t.gameMode = GameMode.ADVENTURE
                                t.inventory.setItem(
                                    0,
                                    playerData.weaponClass!!.mainWeapon!!.weaponIteamStack,
                                )
                                t.world.playSound(
                                    playerData.matchLocation!!,
                                    Sound.ENTITY_PLAYER_SWIM,
                                    1f,
                                    1f,
                                )
                                t.exp = 0.99f
                                t.health = 20.0
                                // DataMgr.getPlayerData(t).setLastAttack(t);
                                WeaponClassMgr.setWeaponClass(t)
                                barrierEffectRunnable(t, 120)
                                setArmor(t, Double.MAX_VALUE, 120, false)
                                if (playerData.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                                cancel()
                            }
                        }

                        if (i == 100) target.inventory.heldItemSlot = 1

                        i++
                    } catch (e: Exception) {
                        getPlayerData(target)!!.isDead = (false)
                        val loc1 = getPlayerData(t)!!.matchLocation
                        t.teleport(loc1!!)
                        t.gameMode = GameMode.ADVENTURE
                        t.inventory.setItem(0, getPlayerData(t)!!.weaponClass!!.mainWeapon!!.weaponIteamStack)
                        t.world.playSound(getPlayerData(t)!!.matchLocation!!, Sound.ENTITY_PLAYER_SWIM, 1f, 1f)
                        t.exp = 0.99f
                        t.health = 20.0
                        WeaponClassMgr.setWeaponClass(t)
                        setArmor(t, Double.MAX_VALUE, 120, false)
                        if (getPlayerData(t)!!.sPGauge == 100) SPWeaponMgr.setSPWeapon(t)
                        cancel()
                        sclatLogger.error("Failed to process death", e)
                        e.printStackTrace()
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
                    if (!data!!.isInMatch || (player.gameMode != GameMode.ADVENTURE) || !p.isOnline) {
                        cancel()
                    }
                    val loc = p.location.add(0.0, 0.5, 0.0)

                    val sLocs = getSphere(loc, 2.0, 23)
                    for (o_player in plugin.server.onlinePlayers) {
                        if (getPlayerData(o_player)!!.settings!!.showEffectSPWeapon() && o_player != player) {
                            val dustOptions =
                                Particle.DustOptions(
                                    data.team!!.teamColor!!.bukkitColor!!,
                                    1f,
                                )
                            getPlayerData(p)!!
                                .team!!
                                .teamColor!!
                                .wool!!
                                .createBlockData()
                            for (e_loc in sLocs) {
                                if (o_player.world === e_loc.world) {
                                    if (o_player
                                            .location
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
