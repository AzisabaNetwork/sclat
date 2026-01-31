package be4rjp.sclat.manager

import be4rjp.sclat.Sclat
import be4rjp.sclat.api.ServerType
import be4rjp.sclat.data.DataMgr.blockDataMap
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.plugin
import be4rjp.sclat.weapon.Gear
import be4rjp.sclat.weapon.Gear.getGearInfluence
import net.minecraft.server.v1_14_R1.EntitySquid
import net.minecraft.server.v1_14_R1.EntityTypes
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityTeleport
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving
import net.minecraft.server.v1_14_R1.World
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import org.bukkit.util.Vector

/**
 *
 * @author Be4rJP
 */
object SquidMgr {
    fun SquidRunnable(player: Player) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var `is`: Boolean = false
                var is2: Boolean = true
                var i: Int = 0

                // LivingEntity squid;
                override fun run() {
                    val data = getPlayerData(p)

                    if (!p.isOnline()) {
                        cancel()
                    }

                    if (!data!!.isInMatch) {
                        if (p.hasPotionEffect(PotionEffectType.REGENERATION)) p.removePotionEffect(PotionEffectType.REGENERATION)
                        if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) p.removePotionEffect(PotionEffectType.INVISIBILITY)
                        p.setWalkSpeed(0.2f)
                        @Suppress("DEPRECATION")
                        p.setMaxHealth(20.0)

                        if (Sclat.type == ServerType.MATCH) {
                            p.setFoodLevel(20)
                            p.setExp(0f)
                        }

                        if (data.canFly) {
                            p.setAllowFlight(true)
                            p.setFlying(true)
                        } else {
                            if (p.hasPermission("sclat.lobbyfly") || Sclat.flyList.contains(p.getName())) {
                                p.setAllowFlight(true)
                            } else {
                                p.setAllowFlight(false)
                                p.setFlying(false)
                            }
                        }
                        return
                    }

                    // Sponge
                    val pl = p.getLocation().add(0.0, 0.5, 0.0)
                    val b1 = pl.getBlock()
                    val b2 = pl.clone().add(0.0, 1.0, 0.0).getBlock()
                    if (b1.getType().toString().contains("POWDER") || b2.getType().toString().contains("POWDER")) {
                        p.teleport(pl.add(0.0, 0.5, 0.0))
                        p.setVelocity(Vector(0.0, 0.5, 0.0))
                    }

                    if (data.weaponClass!!.mainWeapon!!.isManeuver) {
                        if (p.getInventory().getItemInMainHand().getType()
                            ==
                            data.weaponClass!!
                                .mainWeapon!!
                                .weaponIteamStack!!
                                .getType()
                        ) {
                            if (p.getInventory().getItemInOffHand().getType()
                                !=
                                data.weaponClass!!
                                    .mainWeapon!!
                                    .weaponIteamStack!!
                                    .getType()
                            ) {
                                p.getInventory().setItem(
                                    40,
                                    data.weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .clone(),
                                )
                            }
                        } else {
                            p.getInventory().setItem(40, ItemStack(Material.AIR))
                        }
                    }

                    val down = p.getLocation().getBlock().getRelative(BlockFace.DOWN)
                    if (blockDataMap.containsKey(down) && p.getGameMode() == GameMode.ADVENTURE) {
                        if (blockDataMap.get(down)!!.team != data.team) {
                            if (data.armor <= 0 && !data.isPoisonCoolTime) {
                                p.addPotionEffect(PotionEffect(PotionEffectType.POISON, 200, 3))
                            }
                        } else {
                            if (p.hasPotionEffect(PotionEffectType.POISON)) p.removePotionEffect(PotionEffectType.POISON)
                        }
                    } else {
                        if (Sclat.tutorial && down.getType().toString().contains("WOOL")) {
                            if (down.getType() != data.team!!.teamColor!!.wool) {
                                if (data.armor <= 0 && !data.isPoisonCoolTime) {
                                    p.addPotionEffect(PotionEffect(PotionEffectType.POISON, 200, 3))
                                }
                            }
                        } else if (p.hasPotionEffect(PotionEffectType.POISON)) {
                            p.removePotionEffect(PotionEffectType.POISON)
                        }
                    }

                    if (i > 2) {
                        i = 0
                        data.isSquid = (player.getInventory().getItemInMainHand().getType() == Material.AIR)
                    }
                    i++

                /*
                 * if(Main.tutorial && down.getType().toString().contains("WOOL")){
                 * if(down.getType() != data.getTeam().getTeamColor().wool){ if(data.getArmor()
                 * <= 0 && !data.isPoisonCoolTime){ p.addPotionEffect(new
                 * PotionEffect(PotionEffectType.POISON, 200, 3)); } } }
                 */
                    if (data.isPoisonCoolTime) {
                        if (p.hasPotionEffect(PotionEffectType.POISON)) {
                            p.removePotionEffect(
                                PotionEffectType.POISON,
                            )
                        }
                    }

                    if ((data.isOnInk && data.isSquid) || data.isOnPath) {
                        is2 = false
                        if (!`is`) {
                            p.playSound(p.getLocation(), Sound.ITEM_BUCKET_FILL, 0.5f, 1f)
                            `is` = true
                            p.setFoodLevel(20)
                        }
                        if (data.isUsingJetPack) p.setFlySpeed(0.1f)

                        if (p.getExp() <= (
                                0.99f -
                                    (
                                        Sclat.Companion.conf!!
                                            .config!!
                                            .getDouble("SquidRecovery") *
                                            getGearInfluence(p, Gear.Type.INK_RECOVERY_UP)
                                        ).toFloat()
                                )
                        ) {
                            if (data.canUseSubWeapon) {
                                p.setExp(
                                    p.getExp() +
                                        (
                                            Sclat.Companion.conf!!
                                                .config!!
                                                .getDouble("SquidRecovery") *
                                                getGearInfluence(p, Gear.Type.INK_RECOVERY_UP)
                                            ).toFloat(),
                                )
                            }
                        }
                        p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 200, 3))

                        // p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));
                        val loc = p.getLocation()
                        var gro = data.playerGroundLocation
                        if (gro == null) gro = loc
                        if (loc.getX() != gro.getX() || loc.getX() != gro.getX() || loc.getX() != gro.getX()) {
                            p.setSprinting(true)
                            val bd =
                                getPlayerData(p)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            p.getLocation().getWorld()!!.spawnParticle<BlockData?>(
                                Particle.BLOCK_DUST,
                                p.getLocation(),
                                2,
                                0.1,
                                0.1,
                                0.1,
                                1.0,
                                bd,
                            )
                        } else {
                            p.setSprinting(false)
                        }

                        var speed = (
                            Sclat.Companion.conf!!
                                .config!!
                                .getDouble("SquidSpeed") *
                                getGearInfluence(p, Gear.Type.IKA_SPEED_UP)
                            )

                        if (data.speed != 0.0) speed = data.speed

                        if (!getPlayerData(p)!!.poison) {
                            p.setWalkSpeed(speed.toFloat())
                        } else {
                            p.setWalkSpeed(((speed - speed / 3) * getGearInfluence(p, Gear.Type.PENA_DOWN)).toFloat())
                        }
                    } else {
                        if (!is2) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SWIM, 0.3f, 5f)
                            is2 = true
                            p.setSprinting(false)
                            p.setFoodLevel(4)
                        }
                        `is` = false
                        if (p.hasPotionEffect(PotionEffectType.REGENERATION)) p.removePotionEffect(PotionEffectType.REGENERATION)

                        // if(p.hasPotionEffect(PotionEffectType.INVISIBILITY))
                        // p.removePotionEffect(PotionEffectType.INVISIBILITY);
                        var speed = 0.2

                        if (p.getInventory().getItemInMainHand().getType()
                            ==
                            data.weaponClass!!
                                .mainWeapon!!
                                .weaponIteamStack!!
                                .getType()
                        ) {
                            speed = data.weaponClass!!
                                .mainWeapon!!
                                .inHoldSpeed
                                .toDouble() *
                                getGearInfluence(
                                    p,
                                    Gear.Type.HITO_SPEED_UP,
                                )
                        } else {
                            speed = (
                                Sclat.Companion.conf!!
                                    .config!!
                                    .getDouble("PlayerWalkSpeed") *
                                    getGearInfluence(p, Gear.Type.HITO_SPEED_UP)
                                )
                        }

                        if (data.speed != 0.0) speed = data.speed

                        if (p.getExp() <= (
                                0.99f -
                                    Sclat.Companion.conf!!
                                        .config!!
                                        .getDouble("NormalRecovery")
                                        .toFloat()
                                )
                        ) {
                            p.setExp(
                                p.getExp() +
                                    Sclat.Companion.conf!!
                                        .config!!
                                        .getDouble("NormalRecovery")
                                        .toFloat(),
                            )
                        }

                        if (data.isHolding && data.canPaint && p.getExp() >= data.weaponClass!!.mainWeapon!!.needInk) {
                            p.setSprinting(true)
                        } else {
                            if (!getPlayerData(p)!!.poison) p.setWalkSpeed(speed.toFloat())
                            if (getPlayerData(p)!!.poison) {
                                p.setWalkSpeed(
                                    (
                                        speed *
                                            getGearInfluence(
                                                p,
                                                Gear.Type.PENA_DOWN,
                                            ) - speed / 3
                                        ).toFloat(),
                                )
                            }
                        }

                        if (p.getGameMode() != GameMode.CREATIVE && !data.isUsingJetPack) {
                            p.setAllowFlight(false)
                            p.setFlying(false)
                        }
                    }

                    // プレイヤーが最後に立っていた地面を記録する
                    if (p.isOnGround()) data.playerGroundLocation = p.getLocation()
                }
            }
        task.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun SquidShowRunnable(player: Player) {
        val data = getPlayerData(player)

        /*
         * Squid squid = (Squid)player.getWorld().spawnEntity(player.getLocation(),
         * EntityType.SQUID); squid.setAI(false); squid.setSilent(true);
         * squid.setRemainingAir(Integer.MAX_VALUE);
         * squid.setMaximumAir(Integer.MAX_VALUE);
         * ((LivingEntity)squid).setCollidable(false);
         * ((LivingEntity)player).setCollidable(false);
         *
         * if(conf.getConfig().getString("WorkMode").equals("Trial")){ ScoreboardManager
         * manager = Bukkit.getScoreboardManager(); Scoreboard scoreboard =
         * manager.getNewScoreboard();
         *
         * org.bukkit.scoreboard.Team bteam0 =
         * scoreboard.registerNewTeam(data.getTeam().getTeamColor().getColorName());
         * bteam0.setColor(data.getTeam().getTeamColor().getChatColor());
         * //bteam0.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
         * bteam0.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
         * org.bukkit.scoreboard.Team.OptionStatus.NEVER);
         *
         * player.setScoreboard(scoreboard); bteam0.addEntry(player.getName());
         *
         * bteam0.addEntry(squid.getUniqueId().toString()); }
         *
         * if(data.getTeam() != null){ squid.setCustomName(player.getName());
         * squid.setCustomNameVisible(true);
         * if(!conf.getConfig().getString("WorkMode").equals("Trial"))
         * data.getTeam().getTeam().addEntry(squid.getUniqueId().toString()); }
         */
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player
                var `is`: Boolean = false
                var is2: Boolean = true
                var is3: Boolean = false
                var is4: Boolean = true
                var set: Boolean = false
                val death: Boolean = false
                val nmsWorld: World? = (p.getWorld() as CraftWorld).getHandle()
                val es: EntitySquid = EntitySquid(EntityTypes.SQUID, nmsWorld)

                override fun run() {
                    if (!set) {
                        set = true
                        es.setNoAI(true)
                        es.setNoGravity(true)
                        es.setCustomName(CraftChatMessage.fromStringOrNull(player.getName()))
                        es.setCustomNameVisible(true)
                        (es.getBukkitEntity() as LivingEntity).setCollidable(false)

                        // data.getTeam().getTeam().addEntry(es.getBukkitEntity().getUniqueId().toString());
                        if (Sclat.Companion.conf!!
                                .config!!
                                .getString("WorkMode") == "Trial"
                        ) {
                            val manager = Bukkit.getScoreboardManager()
                            val scoreboard = manager!!.getNewScoreboard()

                            val bteam0 =
                                scoreboard
                                    .registerNewTeam(data!!.team!!.teamColor!!.colorName!!)
                            bteam0.setColor(data.team!!.teamColor!!.chatColor!!)
                            // bteam0.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
                            bteam0.setPrefix(data.team!!.teamColor!!.colorCode!!)
                            bteam0.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)

                            @Suppress("DEPRECATION")
                            bteam0.addPlayer(player)
                            player.setScoreboard(scoreboard)

                            bteam0.addEntry(es.getBukkitEntity().getUniqueId().toString())

                            // player.setScoreboard(data.getMatch().getScoreboard());
                            // data.getTeam().getTeam().addEntry(player.getName());

                            // data.getTeam().getTeam().addEntry(es.getBukkitEntity().getUniqueId().toString());
                        } else {
                            data!!.team!!.team!!.addEntry(es.getBukkitEntity().getUniqueId().toString())
                        }
                    }

                    try {
                        val loc = player.getLocation()
                        es.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), 0f)

                        if (!data!!.isOnInk && data.isSquid && !data.isOnPath) {
                            is2 = false
                            if (!`is`) {
                                `is` = true
                                val packet = PacketPlayOutSpawnEntityLiving(es)
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (p.getWorld() === target.getWorld()) {
                                        (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                                    }
                                }
                            }

                            // squid.teleport(p);
                            val packet = PacketPlayOutEntityTeleport(es)
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (p.getWorld() === target.getWorld()) {
                                    (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                                }
                            }
                        } else {
                            `is` = false
                            if (!is2) {
                                is2 = true
                                val packet =
                                    PacketPlayOutEntityDestroy(
                                        es.getBukkitEntity().getEntityId(),
                                    )
                                for (target in plugin.getServer().getOnlinePlayers()) {
                                    if (p.getWorld() === target.getWorld()) {
                                        (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }

                    if (data!!.isSquid) {
                        is4 = false
                        if (!is3) {
                            is3 = true
                            p.getEquipment()!!.setHelmet(ItemStack(Material.AIR))
                            if (data.weaponClass!!.mainWeapon!!.weaponType == "Buckler") {
                                p.getInventory().setItem(40, ItemStack(Material.AIR))
                            }
                            if (data.weaponClass!!.mainWeapon!!.isManeuver) {
                                p.getInventory().setItem(
                                    40,
                                    ItemStack(
                                        Material.AIR,
                                    ),
                                )
                            }
                        }
                        p.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 200, 1))
                    } else {
                        is3 = false
                        if (!is4) {
                            is4 = true
                            p.getEquipment()!!.setHelmet(getPlayerData(p)!!.team!!.teamColor!!.bougu)
                            if (data.weaponClass!!.mainWeapon!!.weaponType == "Buckler") {
                                p.getInventory().setItem(40, ItemStack(Material.SLIME_BALL))
                            }
                            if (data.weaponClass!!.mainWeapon!!.isManeuver) {
                                p.getInventory().setItem(
                                    40,
                                    getPlayerData(p)!!
                                        .weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .clone(),
                                )
                            }
                        }
                        if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) p.removePotionEffect(PotionEffectType.INVISIBILITY)
                    }

                    if (p.getGameMode() == GameMode.SPECTATOR && !data.isJumping) {
                        try {
                            val packet =
                                PacketPlayOutEntityDestroy(
                                    es.getBukkitEntity().getEntityId(),
                                )
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (p.getWorld() === target.getWorld()) {
                                    (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }

                    if (!data.isInMatch || !p.isOnline()) {
                        try {
                            val packet =
                                PacketPlayOutEntityDestroy(
                                    es.getBukkitEntity().getEntityId(),
                                )
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (p.getWorld() === target.getWorld()) {
                                    (target as CraftPlayer).getHandle().playerConnection.sendPacket(packet)
                                }
                            }
                        } catch (e: Exception) {
                        }
                        // squid.remove();
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 30, 3)
    }

    fun PoisonCoolTime(player: Player?) {
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player? = player

                override fun run() {
                    getPlayerData(p)!!.isPoisonCoolTime = false
                }
            }
        task.runTaskLater(plugin, 10)
    }
}
