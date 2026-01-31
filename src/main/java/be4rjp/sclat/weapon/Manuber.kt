package be4rjp.sclat.weapon

import be4rjp.sclat.Sclat
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getSplashShieldDataFromArmorStand
import be4rjp.sclat.plugin
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.function.Predicate

object Manuber {
    @JvmStatic
    fun ManeuverRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.getLocation()
                var loc2: Location = player.getLocation()
                var before: Location = player.getLocation()
                var before_2: Location = player.getLocation()

                // int sl = 0;
                // スライドの仕様改変
                var sl_recharge_1: Boolean = true
                var sl_recharge_2: Boolean = true

                // スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
                var distcheck: Double = 0.0
                var check: Boolean = false // スライドの解除後判定用

                override fun run() {
                    val data = getPlayerData(p)
                    val ploc = p.getLocation()

                    if (!data!!.isInMatch() || !p.isOnline()) {
                        cancel()
                        return
                    }

                    val location = p.getLocation()

                    var x = location.getX() - before.getX()
                    var z = location.getZ() - before.getZ()
                    var vec = p.getEyeLocation().getDirection()
                    if (x != 0.0 || z != 0.0) {
                        vec = Vector(x, 0.0, z)
                    } else {
                        x = location.getX() - before_2.getX()
                        z = location.getZ() - before_2.getZ()
                        if (x != 0.0 || z != 0.0) {
                            vec = Vector(x, 0.0, z)
                        }
                    }
                    before_2 = before.clone()
                    before = location.clone()

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                    // マニューバー系
                    if (data.getWeaponClass().mainWeapon!!.isManeuver) {
                        // if(p.getExp() >= ink) {
                        if (data.getIsSneaking() && sl_recharge_2 && !data.getIsSliding() && (
                                p
                                    .getInventory()
                                    .getItemInMainHand()
                                    .getType()
                                    ==
                                    data
                                        .getWeaponClass()
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .getType()
                                )
                        ) { // slをsl_recharge_2に変更することで優先順位が低い方のスライドが残っている時のみ使えるようにしました
                            val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize()
                            val ev = jvec.clone().normalize().multiply(-2)
                            check = true

                            // p.setExp(p.getExp() - ink);

                            // エフェクト
                            val bd =
                                getPlayerData(player)!!
                                    .team.teamColor!!
                                    .wool!!
                                    .createBlockData()
                            val random = 1.0

                            if (getPlayerData(player)!!.armor > 9999) {
                                getPlayerData(player)!!.armor = 0.0
                            }
                            p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                            distcheck = EntityWallHit(p, jvec.clone())
                            while (distcheck > 0 &&
                                !isSafeLocation(p, location.clone().add(jvec.clone().multiply(distcheck)))
                            ) {
                                distcheck = distcheck - 0.2
                                // p.sendMessage("テレポート位置に障害物があります");
                                if (distcheck <= 0) {
                                    // p.sendMessage("テレポート距離が０になりました");
                                    distcheck = 0.0
                                }
                            }
                            // p.sendMessage("X "+verif.getX()+"Y "+verif.getY()+"Z "+verif.getZ());
                            p.teleport(
                                location
                                    .clone()
                                    .add(jvec.clone().multiply(EntityArmorstandHit(p, jvec.clone(), distcheck))),
                            )

                            // effect
                            for (i in 0..7) {
                                val randomVector =
                                    Vector(
                                        Math.random() * random - random / 2,
                                        Math.random() * random - random / 2,
                                        Math.random() * random - random / 2,
                                    )
                                val erv = ev.clone().add(randomVector)
                                for (o_player in plugin.getServer().getOnlinePlayers()) {
                                    if (getPlayerData(o_player)!!.settings.ShowEffect_BombEx()) {
                                        if (o_player.getWorld() === location.getWorld()) {
                                            if (o_player
                                                    .getLocation()
                                                    .distanceSquared(location) < Sclat.particleRenderDistanceSquared
                                            ) {
                                                for (i2 in 0..3) {
                                                    o_player.spawnParticle<BlockData?>(
                                                        Particle.BLOCK_DUST,
                                                        location
                                                            .clone()
                                                            .add(jvec.clone().multiply(i * distcheck / 8.0f))
                                                            .add(0.0, 1.1, 0.0)
                                                            .add(
                                                                randomVector.getX(),
                                                                randomVector.getY(),
                                                                randomVector.getZ(),
                                                            ),
                                                        0,
                                                        erv.getX(),
                                                        erv.getY(),
                                                        erv.getZ(),
                                                        1.0,
                                                        bd,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            data.setIsSneaking(false)
                            data.setIsSliding(true)
                            data.setCanShoot(false)
                            // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                            if (!sl_recharge_1) {
                                sl_recharge_2 = false
                            } else {
                                sl_recharge_1 = false
                            }
                            // sl++;
                            val task0_1: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.setVelocity(Vector(0, 0, 0))
                                            data.setIsUsingManeuver(true)
                                            data.setCanShoot(true)
                                        }

                                        if (i == 9) {
                                            loc = p.getLocation()
                                            data.setIsUsingManeuver(true)
                                        }

                                        if (i == 10) {
                                            // data.setIsUsingManeuver(false);
                                            // if(sl_recharge_2) {
                                            data.setIsSliding(false)
                                            // }
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            val task0_0: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.setVelocity(Vector(0, 0, 0))
                                            data.setIsUsingManeuver(true)
                                            data.setCanShoot(true)
                                        }

                                        if (i == 9) {
                                            loc2 = p.getLocation()
                                            data.setIsUsingManeuver(true)
                                        }

                                        if (i == 10) {
                                            // data.setIsUsingManeuver(false);
                                            data.setIsSliding(false)
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            if (sl_recharge_2) {
                                // task0_1.runTaskTimer(Main.getPlugin(), 0, 1);
                                task0_1.runTaskTimer(plugin, 0, 1)
                            } else {
                                task0_0.runTaskTimer(plugin, 0, 1)
                            }
                            // BukkitRunnable task2 = new BukkitRunnable() {
                            // @Override
                            // public void run() {
                            // sl = 0;
                            // check = true;
                            // }
                            // };
                            // booleam型の変数で二つのスライドをそれぞれ表現している、優先順位が低い方がTrueのときは高い方が使われた後のため高い方のリチャージをする（優先順位が高い方は2秒、低い方は2.2秒）
                            // check = false;
                        }
                        // }else{
                        // p.sendTitle("", ChatColor.RED + "インクが足りません", 0, 10, 2);
                        // player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
                        // }
                    }

                    if (!data.getIsSliding()) {
                        if ((loc.getX() == ploc.getX() && loc.getZ() == ploc.getZ()) ||
                            (loc2.getX() == ploc.getX() && loc2.getZ() == ploc.getZ())
                        ) {
                            data.setIsUsingManeuver(true)
                        } else {
                            if (check) {
                                // p.sendMessage("現在地はX＝"+p.getLocation().getX()+" Y="+p.getLocation().getY()+"
                                // Z="+p.getLocation().getZ());
                                // p.sendMessage("LOC1はX＝"+loc.getX()+" Y="+loc.getY()+" Z="+loc.getZ());
                                // p.sendMessage("LOC2はX＝"+loc2.getX()+" Y="+loc2.getY()+" Z="+loc2.getZ());
                                val task4: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            sl_recharge_1 = true
                                            // p.sendMessage("スライド１リチャージ完了です");
                                            // check = true;
                                        }
                                    }

                                val task5: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            sl_recharge_1 = true
                                            sl_recharge_2 = true
                                            // p.sendMessage("スライド2リチャージ完了です");
                                            // check = true;
                                        }
                                    }
                                if (sl_recharge_2) {
                                    task4.runTaskLater(plugin, 64)
                                } else {
                                    task5.runTaskLater(plugin, 64)
                                }
                                check = false
                            }
                            data.setIsUsingManeuver(false)
                        }
                    }

                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    private fun EntityWallHit(
        p: Player,
        direction: Vector,
    ): Double {
        val entityLocation = p.getLocation().clone()
        val distance = 5.2 // レイの長さ
        val world = p.getWorld()
        val rayresult = world.rayTraceBlocks(entityLocation, direction, distance)
        // if (result != null && result.getHitBlock() != null) {
        if (rayresult != null && rayresult.getHitBlock() != null) {
            val hitlocation = rayresult.getHitPosition().toLocation(world)
            val raydistance = entityLocation.distance(hitlocation)
            if (raydistance - 0.4 > 0) {
                return raydistance - 0.4
            } else {
                return 0.0
            }
        } else {
            return 4.9
        }
    }

    private fun EntityArmorstandHit(
        p: Player,
        direction: Vector,
        dist: Double,
    ): Double {
        val entityLocation = p.getLocation().clone()
        val distance = dist // レイの長さ
        var distance2 = dist
        val world = p.getWorld()
        // if (result != null && result.getHitBlock() != null) {
        val isArmorStand = Predicate { entity: Entity? -> entity!!.getType() == EntityType.ARMOR_STAND }

        // rayTraceEntitiesでエンティティとの衝突を判定
        val result = world.rayTraceEntities(entityLocation, direction, distance, 0.5, isArmorStand)

        if (result != null && result.getHitEntity() != null) {
            // 衝突までの距離を計算
            val hitEntity = result.getHitEntity()
            val armorStand = hitEntity as ArmorStand
            if (armorStand.getCustomName() != null) {
                if (armorStand.getCustomName() == "SplashShield") {
                    val ssdata = getSplashShieldDataFromArmorStand(armorStand)
                    if (getPlayerData(p)!!.team != getPlayerData(ssdata!!.player)!!.team && ssdata.isDeploy) {
                        val hitLocation = result.getHitPosition().toLocation(world)
                        distance2 = entityLocation.distance(hitLocation)
                        if (dist - distance2 > 0.7) {
                            distance2 = distance2 + 0.4
                        }
                        return distance2
                    }
                }
            }
        }
        return dist
    }

    fun isSafeLocation(
        player: Player?,
        location: Location,
    ): Boolean {
        val world = location.getWorld()
        var contact = true
        if (world == null) return false

        // player.sendMessage("テレポートの位置はX"+LX+"Y"+LY+"Z"+LZ);

        // プレイヤーの体が占める範囲 (高さ2ブロック、幅1ブロック)
        val playerBoundingBox =
            BoundingBox(
                location.getX() - 0.4,
                location.getY(),
                location.getZ() - 0.4,
                location.getX() + 0.4,
                location.getY() + 1.9,
                location.getZ() + 0.4, // location.clone().getX() - 0.4, location.clone().getY(),
                // location.clone().getZ() - 0.4,
                // location.clone().getX() + 0.4, location.clone().getY() + 1.9,
                // location.clone().getZ() + 0.4
            )

        // プレイヤーの足元と頭の位置のブロックを取得
        // Block blockAtFeet = location.getBlock();
        // Block blockAtHead = location.clone().add(0, 1, 0).getBlock();
        val blocks: MutableList<Block> = ArrayList<Block>()

        // for (int x = (int)(LX-0.4); x <= (int)(LX+0.4); x++) {
        // for (int y = (int)LY; y <= (int)LY+1.8; y++) {
        // for (int z = (int)(LZ-0.4); z <= (int)(LZ+0.4); z++) {
        // player.sendMessage("ブロックのx座標"+x+"ブロックのy座標"+y+"ブロックのｚ座標"+z);
        // blocks.add(location.getWorld().getBlockAt(location.clone().add(x, y, z)));
        // }
        // }
        // }
        blocks.add(location.getWorld()!!.getBlockAt(location.clone().add(0.4, 0.0, 0.4)))
        blocks.add(location.getWorld()!!.getBlockAt(location.clone().add(-0.4, 0.0, 0.4)))
        blocks.add(location.getWorld()!!.getBlockAt(location.clone().add(0.4, 0.0, -0.4)))
        blocks.add(location.getWorld()!!.getBlockAt(location.clone().add(-0.4, 0.0, -0.4)))
        blocks.add(location.getWorld()!!.getBlockAt(location.clone().add(0.0, 1.0, 0.0)))
        for (pblock in blocks) {
            // player.sendMessage("ブロックの位置はX"+pblock.getLocation().getX()+"Y"+pblock.getLocation().getY()+"Z"+pblock.getLocation().getZ());
            if (hasCollision(pblock, playerBoundingBox)) {
                contact = false
            }
        }

        // player.sendMessage("検証の結果"+contact);
        // どちらかのブロックが危険なコライダーを持っていないか確認
        return contact
    }

    // ブロックがプレイヤーの体と衝突するかを判定する関数衝突するとTRUE
    private fun hasCollision(
        block: Block,
        playerBoundingBox: BoundingBox,
    ): Boolean {
        if (block.isEmpty() || block.isPassable()) return false // 空気や通り抜け可能ならOK

        // ブロックのバウンディングボックス（ヒットボックス）を取得
        val blockBoundingBox = block.getBoundingBox()

        // プレイヤーの体とブロックが重なるかどうかを判定
        return playerBoundingBox.overlaps(blockBoundingBox)
    }
}
