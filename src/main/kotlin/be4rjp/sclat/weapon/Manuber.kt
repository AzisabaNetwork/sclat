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
    fun maneuverRunnable(player: Player) {
        val delay: BukkitRunnable =
            object : BukkitRunnable() {
                var p: Player = player
                var loc: Location = player.location
                var loc2: Location = player.location
                var before: Location = player.location
                var before2: Location = player.location

                // int sl = 0;
                // スライドの仕様改変
                var slRecharge1: Boolean = true
                var slRecharge2: Boolean = true

                // スライドに使う変数の定義Trueの時は使用可能Falseの時は使用不可能を表している
                var distcheck: Double = 0.0
                var check: Boolean = false // スライドの解除後判定用

                override fun run() {
                    val data = getPlayerData(p)
                    val ploc = p.location

                    if (!data!!.isInMatch || !p.isOnline) {
                        cancel()
                        return
                    }

                    val location = p.location

                    var x = location.x - before.x
                    var z = location.z - before.z
                    var vec = p.eyeLocation.direction
                    if (x != 0.0 || z != 0.0) {
                        vec = Vector(x, 0.0, z)
                    } else {
                        x = location.x - before2.x
                        z = location.z - before2.z
                        if (x != 0.0 || z != 0.0) {
                            vec = Vector(x, 0.0, z)
                        }
                    }
                    before2 = before.clone()
                    before = location.clone()

                    // float ink = data.getWeaponClass().getMainWeapon().getSlideNeedINK();

                    // マニューバー系
                    if (data.weaponClass?.mainWeapon!!.isManeuver) {
                        // if(p.getExp() >= ink) {
                        if (data.isSneaking &&
                            slRecharge2 &&
                            !data.isSliding &&
                            (
                                p
                                    .inventory
                                    .itemInMainHand
                                    .type
                                    ==
                                    data
                                        .weaponClass!!
                                        .mainWeapon!!
                                        .weaponIteamStack!!
                                        .type
                            )
                        ) { // slをsl_recharge_2に変更することで優先順位が低い方のスライドが残っている時のみ使えるようにしました
                            val jvec = (Vector(vec.getX(), 0.0, vec.getZ())).normalize()
                            val ev = jvec.clone().normalize().multiply(-2)
                            check = true

                            // p.setExp(p.getExp() - ink);

                            // エフェクト
                            val bd =
                                getPlayerData(player)!!
                                    .team!!
                                    .teamColor!!
                                    .wool!!
                                    .createBlockData()
                            val random = 1.0

                            if (getPlayerData(player)!!.armor > 9999) {
                                getPlayerData(player)!!.armor = 0.0
                            }
                            p.world.playSound(p.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.4f, 1.5f)

                            distcheck = entityWallHit(p, jvec.clone())
                            while (distcheck > 0 &&
                                !isSafeLocation(p, location.clone().add(jvec.clone().multiply(distcheck)))
                            ) {
                                distcheck -= 0.2
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
                                    .add(jvec.clone().multiply(entityArmorstandHit(p, jvec.clone(), distcheck))),
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
                                for (o_player in plugin.server.onlinePlayers) {
                                    if (getPlayerData(o_player)!!.settings!!.showEffectBombEx()) {
                                        if (o_player.world === location.world) {
                                            if (o_player
                                                    .location
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
                            data.isSneaking = false
                            data.isSliding = true
                            data.canShoot = false
                            // 優先順位が高い方のスライドがFalseだった場合に低い方をFalseにするようにしました高い方がtrueであった場合は高い方がFalseになります
                            if (!slRecharge1) {
                                slRecharge2 = false
                            } else {
                                slRecharge1 = false
                            }
                            // sl++;
                            val task01: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.velocity = Vector(0, 0, 0)
                                            data.isUsingManeuver = (true)
                                            data.canShoot = true
                                        }

                                        if (i == 9) {
                                            loc = p.location
                                            data.isUsingManeuver = (true)
                                        }

                                        if (i == 10) {
                                            // data.isUsingManeuver = (false);
                                            // if(sl_recharge_2) {
                                            data.isSliding = false
                                            // }
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            val task00: BukkitRunnable =
                                object : BukkitRunnable() {
                                    var i: Int = 1

                                    override fun run() {
                                        if (i == 3) {
                                            p.velocity = Vector(0, 0, 0)
                                            data.isUsingManeuver = (true)
                                            data.canShoot = true
                                        }

                                        if (i == 9) {
                                            loc2 = p.location
                                            data.isUsingManeuver = (true)
                                        }

                                        if (i == 10) {
                                            // data.isUsingManeuver = (false);
                                            data.isSliding = false
                                            cancel()
                                        }
                                        i++
                                    }
                                }
                            if (slRecharge2) {
                                // task0_1.runTaskTimer(Main.getPlugin(), 0, 1);
                                task01.runTaskTimer(plugin, 0, 1)
                            } else {
                                task00.runTaskTimer(plugin, 0, 1)
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

                    if (!data.isSliding) {
                        if ((loc.x == ploc.x && loc.z == ploc.z) ||
                            (loc2.x == ploc.x && loc2.z == ploc.z)
                        ) {
                            data.isUsingManeuver = (true)
                        } else {
                            if (check) {
                                // p.sendMessage("現在地はX＝"+p.getLocation().getX()+" Y="+p.getLocation().getY()+"
                                // Z="+p.getLocation().getZ());
                                // p.sendMessage("LOC1はX＝"+loc.getX()+" Y="+loc.getY()+" Z="+loc.getZ());
                                // p.sendMessage("LOC2はX＝"+loc2.getX()+" Y="+loc2.getY()+" Z="+loc2.getZ());
                                val task4: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            slRecharge1 = true
                                            // p.sendMessage("スライド１リチャージ完了です");
                                            // check = true;
                                        }
                                    }

                                val task5: BukkitRunnable =
                                    object : BukkitRunnable() {
                                        override fun run() {
                                            slRecharge1 = true
                                            slRecharge2 = true
                                            // p.sendMessage("スライド2リチャージ完了です");
                                            // check = true;
                                        }
                                    }
                                if (slRecharge2) {
                                    task4.runTaskLater(plugin, 90) // CT
                                } else {
                                    task5.runTaskLater(plugin, 90)
                                }
                                check = false
                            }
                            data.isUsingManeuver = (false)
                        }
                    }

                    // loc = ploc;
                }
            }
        delay.runTaskTimer(plugin, 0, 1)
    }

    private fun entityWallHit(
        p: Player,
        direction: Vector,
    ): Double {
        val entityLocation = p.location.clone()
        val distance = 5.2 // レイの長さ
        val world = p.world
        val rayresult = world.rayTraceBlocks(entityLocation, direction, distance)
        // if (result != null && result.getHitBlock() != null) {
        return if (rayresult != null && rayresult.hitBlock != null) {
            val hitlocation = rayresult.hitPosition.toLocation(world)
            val raydistance = entityLocation.distance(hitlocation)
            if (raydistance - 0.4 > 0) {
                raydistance - 0.4
            } else {
                0.0
            }
        } else {
            4.9
        }
    }

    private fun entityArmorstandHit(
        p: Player,
        direction: Vector,
        dist: Double,
    ): Double {
        val entityLocation = p.location.clone()
        val distance = dist // レイの長さ
        var distance2: Double
        val world = p.world
        // if (result != null && result.getHitBlock() != null) {
        val isArmorStand = Predicate { entity: Entity? -> entity!!.type == EntityType.ARMOR_STAND }

        // rayTraceEntitiesでエンティティとの衝突を判定
        val result = world.rayTraceEntities(entityLocation, direction, distance, 0.5, isArmorStand)

        if (result != null && result.hitEntity != null) {
            // 衝突までの距離を計算
            val hitEntity = result.hitEntity
            val armorStand = hitEntity as ArmorStand
            if (armorStand.customName != null) {
                if (armorStand.customName == "SplashShield") {
                    val ssdata = getSplashShieldDataFromArmorStand(armorStand)
                    if (getPlayerData(p)!!.team != getPlayerData(ssdata!!.player)!!.team && ssdata.isDeploy) {
                        val hitLocation = result.hitPosition.toLocation(world)
                        distance2 = entityLocation.distance(hitLocation)
                        if (dist - distance2 > 0.7) {
                            distance2 += 0.4
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
        val world = location.world
        var contact = true
        if (world == null) return false

        // player.sendMessage("テレポートの位置はX"+LX+"Y"+LY+"Z"+LZ);

        // プレイヤーの体が占める範囲 (高さ2ブロック、幅1ブロック)
        val playerBoundingBox =
            BoundingBox(
                location.x - 0.4,
                location.y,
                location.z - 0.4,
                location.x + 0.4,
                location.y + 1.9,
                location.z + 0.4, // location.clone().getX() - 0.4, location.clone().getY(),
                // location.clone().getZ() - 0.4,
                // location.clone().getX() + 0.4, location.clone().getY() + 1.9,
                // location.clone().getZ() + 0.4
            )

        // プレイヤーの足元と頭の位置のブロックを取得
        // Block blockAtFeet = location.getBlock();
        // Block blockAtHead = location.clone().add(0, 1, 0).getBlock();
        val blocks: MutableList<Block> = ArrayList()

        // for (int x = (int)(LX-0.4); x <= (int)(LX+0.4); x++) {
        // for (int y = (int)LY; y <= (int)LY+1.8; y++) {
        // for (int z = (int)(LZ-0.4); z <= (int)(LZ+0.4); z++) {
        // player.sendMessage("ブロックのx座標"+x+"ブロックのy座標"+y+"ブロックのｚ座標"+z);
        // blocks.add(location.getWorld().getBlockAt(location.clone().add(x, y, z)));
        // }
        // }
        // }
        blocks.add(location.world!!.getBlockAt(location.clone().add(0.4, 0.0, 0.4)))
        blocks.add(location.world!!.getBlockAt(location.clone().add(-0.4, 0.0, 0.4)))
        blocks.add(location.world!!.getBlockAt(location.clone().add(0.4, 0.0, -0.4)))
        blocks.add(location.world!!.getBlockAt(location.clone().add(-0.4, 0.0, -0.4)))
        blocks.add(location.world!!.getBlockAt(location.clone().add(0.0, 1.0, 0.0)))
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
        if (block.isEmpty || block.isPassable) return false // 空気や通り抜け可能ならOK

        // ブロックのバウンディングボックス（ヒットボックス）を取得
        val blockBoundingBox = block.boundingBox

        // プレイヤーの体とブロックが重なるかどうかを判定
        return playerBoundingBox.overlaps(blockBoundingBox)
    }
}
