package be4rjp.sclat.weapon.spweapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.Sclat.Companion.notDuplicateNumber
import be4rjp.sclat.api.SclatUtil.createInkExplosionEffect
import be4rjp.sclat.api.SclatUtil.giveDamage
import be4rjp.sclat.api.Sphere.getSphere
import be4rjp.sclat.api.raytrace.RayTrace
import be4rjp.sclat.data.DataMgr
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSnowballHitCount
import be4rjp.sclat.data.DataMgr.getSnowballIsHit
import be4rjp.sclat.data.DataMgr.mainSnowballNameMap
import be4rjp.sclat.data.DataMgr.setSnowballHitCount
import be4rjp.sclat.data.DataMgr.setSnowballIsHit
import be4rjp.sclat.manager.ArmorStandMgr
import be4rjp.sclat.manager.PaintMgr
import be4rjp.sclat.manager.SPWeaponMgr
import be4rjp.sclat.manager.WeaponClassMgr
import be4rjp.sclat.plugin
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.BlockData
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftSnowball
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.Random

/**
 *
 * @author Be4rJP
 */
object QuadroArms {
    private val Hash_Quadro_overheat = HashMap<Player?, Int?>()

    @JvmStatic
    fun setQuadroArms(player: Player) {
        getPlayerData(player)!!.setIsUsingSP(true)
        getPlayerData(player)!!.setIsUsingSS(true)
        SPWeaponMgr.setSPCoolTimeAnimation(player, 120)
        if (Hash_Quadro_overheat.containsKey(player)) {
            Hash_Quadro_overheat.replace(player, 0)
        } else {
            Hash_Quadro_overheat.put(player, 0)
        }
        val it: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    player.getInventory().clear()
                    player.updateInventory()
                    val item = ItemStack(Material.MUSIC_DISC_13)
                    val meta = item.getItemMeta()
                    meta!!.setDisplayName("Quadro-BLUE")
                    item.setItemMeta(meta)
                    val item2 = ItemStack(Material.MUSIC_DISC_CAT)
                    val meta2 = item2.getItemMeta()
                    meta2!!.setDisplayName("Quadro-GREEN")
                    item2.setItemMeta(meta2)
                    val item3 = ItemStack(Material.MUSIC_DISC_BLOCKS)
                    val meta3 = item3.getItemMeta()
                    meta3!!.setDisplayName("Quadro-RED")
                    item3.setItemMeta(meta3)
                    val item4 = ItemStack(Material.MUSIC_DISC_CHIRP)
                    val meta4 = item4.getItemMeta()
                    meta4!!.setDisplayName("Quadro-WHITE")
                    item4.setItemMeta(meta4)
                    for (count in 0..8) {
                        if (count % 2 != 0) player.getInventory().setItem(count, ItemStack(Material.AIR))
                    }
                    player.getInventory().setItem(0, item)
                    player.getInventory().setItem(2, item2)
                    player.getInventory().setItem(4, item3)
                    player.getInventory().setItem(6, item4)
                    player.updateInventory()
                    player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 121, 1))
                    overheat_bar(player)
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
        task.runTaskLater(plugin, 120)
    }

    fun overheat_bar(player: Player) {
        val bar =
            plugin.getServer().createBossBar(
                getPlayerData(player)!!.team.teamColor!!.colorCode + "§Quadro_overheat",
                BarColor.RED,
                BarStyle.SOLID,
                BarFlag.CREATE_FOG,
            )
        bar.setProgress(0.0)
        bar.addPlayer(player)

        val overheat_anime: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player

                override fun run() {
                    val data = getPlayerData(p)
                    if (Hash_Quadro_overheat.get(p)!! < 47) {
                        bar.setProgress(Hash_Quadro_overheat.get(p)!!.toDouble() / 47)
                        if (!bar.getPlayers().contains(p)) bar.addPlayer(p)
                    } else {
                        bar.setProgress(1.0)
                        if (!bar.getPlayers().contains(p)) bar.addPlayer(p)
                    }
                    if (!getPlayerData(p)!!.isInMatch() || !p.isOnline()) {
                        bar.removeAll()
                        cancel()
                    }
                    if (!getPlayerData(p)!!.getIsUsingSS()) {
                        bar.removeAll()
                        cancel()
                    }
                }
            }
        overheat_anime.runTaskTimer(plugin, 0, 2)
    }

    @JvmStatic
    fun QuadroCooltime(
        player: Player,
        i: Int,
    ) {
        val data = getPlayerData(player)
        val delay1: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    val data = getPlayerData(player)
                    data!!.setCanUseSubWeapon(true)
                }
            }
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    Burstshoot(player, false)
                }
            }
        val delaySG: BukkitRunnable =
            object : BukkitRunnable() {
                val p: Player = player

                override fun run() {
                    val sound = false
                    Burstshoot(player, true)
                    val overheatgage: Int = Hash_Quadro_overheat.get(p)!!
                    if (overheatgage > 47) {
                        Hash_Quadro_overheat.replace(p, overheatgage - 13)
                    } else if (overheatgage > 10) {
                        Hash_Quadro_overheat.replace(p, overheatgage - 10)
                    } else if (overheatgage <= 10) {
                        Hash_Quadro_overheat.replace(p, 0)
                    }
                    player.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.9f, 1.3f)
                    if (sound) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.63f)
                    }
                }
            }

        val delaySL: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    ShootQuadroSlosher(player)
                    val overheatgage: Int = Hash_Quadro_overheat.get(p)!!
                    if (overheatgage <= 13) {
                        Hash_Quadro_overheat.replace(p, 0)
                    } else {
                        Hash_Quadro_overheat.replace(p, overheatgage - 13)
                    }
                }
            }
        val delaySE: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player? = player

                override fun run() {
                    ShootSensor(player)
                    val overheatgage: Int = Hash_Quadro_overheat.get(p)!!
                    if (overheatgage <= 10) {
                        Hash_Quadro_overheat.replace(p, 0)
                    } else {
                        Hash_Quadro_overheat.replace(p, overheatgage - 10)
                    }
                }
            }
        when (i) {
            1 -> {
                delay.runTaskLater(plugin, 1)
                delay1.runTaskLater(plugin, 6)
            }

            2 -> {
                delaySG.runTaskLater(plugin, 1)
                delay1.runTaskLater(plugin, 10)
            }

            3 -> {
                delaySL.runTaskLater(plugin, 1)
                delay1.runTaskLater(plugin, 17)
            }

            4 -> {
                delaySE.runTaskLater(plugin, 1)
                delay1.runTaskLater(plugin, 15)
            }
        }
    }

    fun Burstshoot(
        player: Player,
        IsSG: Boolean,
    ) {
        val Bursttask: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    c++
                    val q = 7
                    val overheatgage: Int = Hash_Quadro_overheat.get(p)!!
                    if (overheatgage > 47) {
                        player.sendTitle("", ChatColor.RED.toString() + "オーバーヒート!!!", 0, 5, 2)
                        cancel()
                    } else {
                        Hash_Quadro_overheat.replace(p, overheatgage + 1)
                    }
                    ShootSpinner(p)
                    if (c == q) cancel()
                }
            }
        val BursttaskSG: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var c: Int = 0

                override fun run() {
                    c++
                    val q = 3
                    for (i in 0..4) {
                        ShootSG(p)
                    }
                    if (c == q) {
                        cancel()
                    }
                }
            }
        if (player.hasPotionEffect(PotionEffectType.LUCK)) {
            if (!IsSG) {
                Bursttask.runTaskTimer(plugin, 0, 1)
            } else {
                BursttaskSG.runTaskTimer(plugin, 0, 1)
            }
        }
    }

    fun ShootSpinner(player: Player) {
        val QuadroShootSpeed = 4.3
        val QuadroDisTick = 2
        if (player.getGameMode() == GameMode.SPECTATOR) return

        val data = getPlayerData(player)
        val rayTrace = RayTrace(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection())
        val positions = rayTrace.traverse(QuadroShootSpeed * QuadroDisTick, 0.7)
        var isLockOnPlayer = false
        check@ for (vector in positions) {
            val position = vector.toLocation(player.getLocation().getWorld()!!)
            for (target in plugin.getServer().getOnlinePlayers()) {
                if (player !== target && player.getWorld() === target.getWorld()) {
                    if (target.getLocation().distance(position) < 2) {
                        isLockOnPlayer = true
                        break@check
                    }
                }
            }
            for (`as` in player.getWorld().getEntities()) {
                if (`as` is ArmorStand) {
                    if (`as`.getCustomName() != null) {
                        if (`as`.getLocation().distanceSquared(position) <= 4 /* 2*2 */) {
                            isLockOnPlayer = true
                            break@check
                        }
                    }
                }
            }
        }
        PaintMgr.PaintHightestBlock(player.getLocation(), player, true, true)

        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_STEP, 0.3f, 1f)
        val vec = player.getLocation().getDirection().multiply(QuadroShootSpeed)
        val random = 0.32
        val distick = QuadroDisTick
        vec.add(Vector(Math.random() * random - random / 2, 0.0, Math.random() * random - random / 2))
        ball.setVelocity(vec)
        ball.setShooter(player)
        val originName = notDuplicateNumber.toString()
        val name = originName + "#QuadroArmsSpinner"
        DataMgr.mws.add(name) //
        ball.setCustomName(name)
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val SpinnerTask: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = 3

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
                    ).multiply(QuadroShootSpeed / 14)

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
                    // if(i != tick)
                    if ((Random().nextInt(7)) == 0) PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, false, true)
                    if (inkball!!.isDead()) cancel()

                    i++
                }
            }
        SpinnerTask.runTaskTimer(plugin, 0, 1)
    }

    fun ShootSG(player: Player): Boolean {
        if (player.getGameMode() == GameMode.SPECTATOR) return false
        val ShootSpeed = 4.5
        val data = getPlayerData(player)
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        val vec = player.getLocation().getDirection().multiply(ShootSpeed)
        val random = 1.2
        val distick = 2
        vec.add(
            Vector(
                Math.random() * random - random / 2,
                Math.random() * random / 1.5 - random / 3,
                Math.random() * random - random / 2,
            ),
        )
        ball.setVelocity(vec)
        ball.setShooter(player)
        val originName = notDuplicateNumber.toString()
        val name = originName + "#QuadroArmsShotgun"
        DataMgr.mws.add(name)
        ball.setCustomName(name)
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick
                var inkball: Snowball? = ball
                var p: Player = player
                var addedFallVec: Boolean = false
                var fallvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(ShootSpeed / 150)

                override fun run() {
                    inkball = mainSnowballNameMap.get(name)

                    if (inkball != ball) {
                        i += getSnowballHitCount(name) - 1
                        setSnowballHitCount(name, 0)
                    }

                    if (i != 0) {
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) continue
                            if (target.getWorld() === inkball!!.getWorld()) {
                                if (target
                                        .getLocation()
                                        .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team.teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        inkball!!.getLocation(),
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0,
                                        bd,
                                    )
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

        return false
    }

    fun ShootQuadroSlosher(player: Player) {
        if (player.getGameMode() == GameMode.SPECTATOR) return

        if (!player.hasPotionEffect(PotionEffectType.LUCK)) {
            return
        }
        val ShootSpeed = 3.9
        val data = getPlayerData(player)
        val ball = player.launchProjectile<Snowball>(Snowball::class.java)
        (ball as CraftSnowball).getHandle().setItem(
            CraftItemStack.asNMSCopy(ItemStack(getPlayerData(player)!!.team.teamColor!!.wool!!)),
        )
        val vec = player.getLocation().getDirection().multiply(ShootSpeed)
        val distick = 2
        ball.setVelocity(vec)
        ball.setShooter(player)
        val originName = notDuplicateNumber.toString()
        val name = originName + "#QuadroArmsSpinner"
        ball.setCustomName(name)
        DataMgr.mws.add(name)
        mainSnowballNameMap.put(name, ball)
        setSnowballHitCount(name, 0)
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var i: Int = 0
                var tick: Int = distick
                var inkball: Snowball? = ball
                var p: Player = player
                var addedFallVec: Boolean = false
                var BlasterExDamage: Double = 3.1
                var BlasterExHankei: Double = 4.0
                var fallvec: Vector =
                    Vector(
                        inkball!!.getVelocity().getX(),
                        inkball!!.getVelocity().getY(),
                        inkball!!.getVelocity().getZ(),
                    ).multiply(ShootSpeed / 17)

                override fun run() {
                    try {
                        inkball = mainSnowballNameMap.get(name)

                        if (inkball != ball) {
                            i += getSnowballHitCount(name) - 1
                            setSnowballHitCount(name, 0)
                        }
                        for (target in plugin.getServer().getOnlinePlayers()) {
                            if (!getPlayerData(target)!!.settings.ShowEffect_MainWeaponInk()) continue
                            if (target.getWorld() === inkball!!.getWorld()) {
                                if (target
                                        .getLocation()
                                        .distanceSquared(inkball!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                ) {
                                    val bd =
                                        getPlayerData(p)!!
                                            .team.teamColor!!
                                            .wool!!
                                            .createBlockData()
                                    target.spawnParticle<BlockData?>(
                                        Particle.BLOCK_DUST,
                                        inkball!!.getLocation(),
                                        3,
                                        0.0,
                                        0.0,
                                        0.0,
                                        1.0,
                                        bd,
                                    )
                                }
                            }
                        }

                        PaintMgr.PaintHightestBlock(inkball!!.getLocation(), p, false, true)

                        if (i >= tick && !addedFallVec) {
                            inkball!!.setVelocity(fallvec)
                            addedFallVec = true
                        }
                        if (i >= tick && i <= tick + 15) {
                            inkball!!.setVelocity(
                                inkball!!.getVelocity().add(Vector(0.0, -0.1, 0.0)),
                            )
                        }
                        if (inkball!!.isDead()) {
                            // 半径
                            val maxDist = BlasterExHankei

                            // 爆発音
                            player
                                .getWorld()
                                .playSound(inkball!!.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 1f)

                            // 爆発エフェクト
                            createInkExplosionEffect(inkball!!.getLocation(), maxDist, 25, player)

                            // 塗る
                            run {
                                var i = 0
                                while (i <= maxDist) {
                                    val p_locs = getSphere(inkball!!.getLocation(), i.toDouble(), 20)
                                    for (loc in p_locs) {
                                        PaintMgr.Paint(loc, p, false)
                                        PaintMgr.PaintHightestBlock(loc, p, false, false)
                                    }
                                    i++
                                }
                            }

                            // 攻撃判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch()) continue
                                if (target.getLocation().distanceSquared(inkball!!.getLocation()) <= maxDist * maxDist) {
                                    val damage = (
                                        (1 + maxDist - target.getLocation().distance(inkball!!.getLocation())) *
                                            BlasterExDamage
                                        )
                                    if (getPlayerData(player)!!.team != getPlayerData(target)!!.team &&
                                        target.getGameMode() == GameMode.ADVENTURE
                                    ) {
                                        giveDamage(player, target, damage, "spWeapon")

                                        // AntiNoDamageTime
                                        val task: BukkitRunnable =
                                            object : BukkitRunnable() {
                                                var p: Player = target

                                                override fun run() {
                                                    target.setNoDamageTicks(0)
                                                }
                                            }
                                        task.runTaskLater(plugin, 1)
                                    }
                                }
                            }

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as` is ArmorStand) {
                                    if (`as`.getLocation().distanceSquared(inkball!!.getLocation()) <= maxDist * maxDist) {
                                        val damage = (
                                            (1 + maxDist - `as`.getLocation().distance(inkball!!.getLocation())) *
                                                BlasterExDamage
                                            )
                                        ArmorStandMgr.giveDamageArmorStand(`as`, damage, p)
                                    }
                                }
                            }
                            cancel()
                        }

                        i++
                    } catch (e: Exception) {
                        cancel()
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }

    fun ShootSensor(player: Player) {
        if (!player.hasPotionEffect(PotionEffectType.LUCK)) {
            return
        }
        val task: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var p_vec: Vector? = null
                var x: Double = 0.0
                var z: Double = 0.0
                var collision: Boolean = false
                var block_check: Boolean = false
                var c: Int = 0
                var drop: Item? = null
                var ball: Snowball? = null

                override fun run() {
                    try {
                        if (c == 0) {
                            p_vec = p.getEyeLocation().getDirection().multiply(1.35)
                            val bom = ItemStack(Material.DISPENSER).clone()
                            val bom_m = bom.getItemMeta()
                            bom_m!!.setLocalizedName(notDuplicateNumber.toString())
                            bom.setItemMeta(bom_m)
                            drop = p.getWorld().dropItem(p.getEyeLocation(), bom)
                            drop!!.setVelocity(p_vec!!)
                            // 雪玉をスポーンさせた瞬間にプレイヤーに雪玉がデスポーンした偽のパケットを送信する
                            ball = player.launchProjectile<Snowball>(Snowball::class.java)
                            ball!!.setVelocity(Vector(0, 0, 0))
                            setSnowballIsHit(ball, false)

                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                val connection = (o_player as CraftPlayer).getHandle().playerConnection
                                connection.sendPacket(PacketPlayOutEntityDestroy(ball!!.getEntityId()))
                            }
                            p_vec = p.getEyeLocation().getDirection()
                        }

                        if (!drop!!.isOnGround() &&
                            !(
                                drop!!.getVelocity().getX() == 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() != 0.0
                                ) &&
                            !(
                                drop!!.getVelocity().getX() != 0.0 && drop!!
                                    .getVelocity()
                                    .getZ() == 0.0
                                )
                        ) {
                            ball!!.setVelocity(drop!!.getVelocity())
                        }

                        if (getSnowballIsHit(ball) || drop!!.isOnGround()) {
                            // 半径

                            val maxDist = 9.0

                            // 爆発音
                            player.getWorld().playSound(drop!!.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 2f)

                            // 爆発エフェクト
                            val s_locs = getSphere(drop!!.getLocation(), maxDist, 15)
                            for (o_player in plugin.getServer().getOnlinePlayers()) {
                                if (getPlayerData(o_player)!!.settings.ShowEffect_BombEx()) {
                                    for (loc in s_locs) {
                                        if (o_player.getWorld() === loc.getWorld()) {
                                            if (o_player
                                                    .getLocation()
                                                    .distanceSquared(loc) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                val dustOptions = Particle.DustOptions(Color.BLACK, 1f)
                                                o_player.spawnParticle<Particle.DustOptions?>(
                                                    Particle.REDSTONE,
                                                    loc,
                                                    1,
                                                    0.0,
                                                    0.0,
                                                    0.0,
                                                    1.0,
                                                    dustOptions,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // あたり判定の処理
                            for (target in plugin.getServer().getOnlinePlayers()) {
                                if (!getPlayerData(target)!!.isInMatch() || target.getWorld() !== p.getWorld()) continue
                                if (target.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (getPlayerData(player)!!.team.iD !=
                                        getPlayerData(target)!!
                                            .team
                                            .iD
                                    ) {
                                        target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 240, 1))
                                    }
                                }
                            }

                            for (`as` in player.getWorld().getEntities()) {
                                if (`as`.getLocation().distance(drop!!.getLocation()) <= maxDist) {
                                    if (`as`.getCustomName() != null) {
                                        if (`as`.getCustomName() == null) continue
                                        if (`as` is ArmorStand && (`as`.getCustomName() != "Path") && (`as`.getCustomName() != "21") &&
                                            (`as`.getCustomName() != "100") &&
                                            (`as`.getCustomName() != "SplashShield") &&
                                            (`as`.getCustomName() != "Kasa")
                                        ) {
                                            `as`
                                                .addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 240, 1))
                                        }
                                    }
                                }
                            }

                            drop!!.remove()
                            cancel()
                            return
                        }

                        // ボムの視認用エフェクト
                        for (o_player in plugin.getServer().getOnlinePlayers()) {
                            if (getPlayerData(o_player)!!.settings.ShowEffect_Bomb()) {
                                if (o_player.getWorld() === drop!!.getLocation().getWorld()) {
                                    if (o_player
                                            .getLocation()
                                            .distanceSquared(drop!!.getLocation()) < Sclat.particleRenderDistanceSquared
                                    ) {
                                        val dustOptions =
                                            Particle.DustOptions(
                                                getPlayerData(p)!!.team.teamColor!!.bukkitColor!!,
                                                1f,
                                            )
                                        o_player.spawnParticle<Particle.DustOptions?>(
                                            Particle.REDSTONE,
                                            drop!!.getLocation(),
                                            1,
                                            0.0,
                                            0.0,
                                            0.0,
                                            50.0,
                                            dustOptions,
                                        )
                                    }
                                }
                            }
                        }

                        c++
                        x = drop!!.getLocation().getX()
                        z = drop!!.getLocation().getZ()

                        if (c > 1000) {
                            drop!!.remove()
                            cancel()
                            return
                        }
                    } catch (e: Exception) {
                        cancel()
                        drop!!.remove()
                        plugin.getLogger().warning(e.message)
                    }
                }
            }
        task.runTaskTimer(plugin, 0, 1)
    }
}
