package be4rjp.sclat.world

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import net.minecraft.server.v1_14_R1.WorldData

class MatchWorldSelectorTest :
    StringSpec({

        data class TestWorldData(
            val fakeName: String,
        ) : WorldData() {
            override fun getName() = fakeName
        }

        "初期状態では空であること" {
            val selector = MatchWorldSelector()
            selector.size() shouldBe 0
            selector.allMapNames() shouldBe emptySet()
        }

        "addMap でマップを追加できること" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("map1")

            selector.addMap(worldData)

            selector.size() shouldBe 1
            selector.existMap("map1") shouldBe true
        }

        "addMap で複数のマップを追加できること" {
            val selector = MatchWorldSelector()
            val world1 = TestWorldData("map1")
            val world2 = TestWorldData("map2")
            val world3 = TestWorldData("map3")

            selector.addMap(world1)
            selector.addMap(world2)
            selector.addMap(world3)

            selector.size() shouldBe 3
            selector.allMapNames().size shouldBe 3
        }

        "existMap で存在しないマップは false を返すこと" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("map1")
            selector.addMap(worldData)

            selector.existMap("nonexistent") shouldBe false
        }

        "同名マップを追加すると上書きされること" {
            val selector = MatchWorldSelector()
            val world1 = TestWorldData("map1")
            val world2 = TestWorldData("map1")

            selector.addMap(world1)
            selector.size() shouldBe 1
            selector.addMap(world2)
            selector.size() shouldBe 1
        }

        "randomMap で空のセレクターは例外を投げること" {
            val selector = MatchWorldSelector()

            kotlin.runCatching { selector.randomMap() }.isSuccess shouldBe false
        }

        "randomMap で1つだけマップがある場合は常に同じマップを返すこと" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("onlyMap")
            selector.addMap(worldData)

            repeat(10) {
                selector.randomMap().name shouldBe "onlyMap"
            }
        }

        "randomMap で複数のマップがある場合はいずれかのマップを返すこと" {
            val selector = MatchWorldSelector()
            val world1 = TestWorldData("map1")
            val world2 = TestWorldData("map2")
            val world3 = TestWorldData("map3")
            selector.addMap(world1)
            selector.addMap(world2)
            selector.addMap(world3)

            val results = mutableSetOf<String>()
            repeat(100) {
                results.add(selector.randomMap().name)
            }

            results.size shouldBe 3
        }

        "allMapNames で追加した全てのマップ名が取得できること" {
            val selector = MatchWorldSelector()
            val world1 = TestWorldData("arena1")
            val world2 = TestWorldData("arena2")

            selector.addMap(world1)
            selector.addMap(world2)

            selector.allMapNames() shouldBe setOf("arena1", "arena2")
        }

        "空のマップ名でも追加できること" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("")

            selector.addMap(worldData)
            selector.size() shouldBe 1
            selector.existMap("") shouldBe true
        }

        "コンストラクタで初期マップを渡せること" {
            val world1 = TestWorldData("map1")
            val world2 = TestWorldData("map2")
            val initialWorlds = mutableMapOf<String, WorldData>("map1" to world1, "map2" to world2)

            val selector = MatchWorldSelector(initialWorlds)

            selector.size() shouldBe 2
            selector.existMap("map1") shouldBe true
            selector.existMap("map2") shouldBe true
        }

        "size は正しくマップ数を返すこと" {
            val selector = MatchWorldSelector()

            repeat(5) { i ->
                selector.addMap(TestWorldData("map$i"))
                selector.size() shouldBe i + 1
            }
        }

        "同一マップを追加・削除を繰り返した後、正しく動作すること" {
            val selector = MatchWorldSelector()
            val world = TestWorldData("map1")

            repeat(10) {
                selector.addMap(world)
                selector.size() shouldBe 1
            }
        }

        "非常に多くのマップを追加しても動作すること" {
            val selector = MatchWorldSelector()

            repeat(1000) { i ->
                selector.addMap(TestWorldData("map_$i"))
            }

            selector.size() shouldBe 1000
            selector.allMapNames().size shouldBe 1000
        }

        "ランダム選択が偏らないこと（統計的検証）" {
            val selector = MatchWorldSelector()
            repeat(10) { i ->
                selector.addMap(TestWorldData("map$i"))
            }

            val counts = mutableMapOf<String, Int>()
            repeat(10000) {
                val name = selector.randomMap().name
                counts[name] = counts.getOrDefault(name, 0) + 1
            }

            val minCount = counts.values.minOrNull()!!
            val maxCount = counts.values.maxOrNull()!!
            val ratio = maxCount.toDouble() / minCount.toDouble()

            ratio shouldBeLessThan 2.0
        }

        "Unicode を含むマップ名でも動作すること" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("マップ_アrena_αβγ")

            selector.addMap(worldData)

            selector.size() shouldBe 1
            selector.existMap("マップ_アrena_αβγ") shouldBe true
            selector.allMapNames().first() shouldBe "マップ_アrena_αβγ"
        }

        "特殊文字を含むマップ名でも動作すること" {
            val selector = MatchWorldSelector()
            val worldData = TestWorldData("map-with-dash_underscore.dot")

            selector.addMap(worldData)

            selector.size() shouldBe 1
            selector.existMap("map-with-dash_underscore.dot") shouldBe true
        }

        "WorldData の name プロパティが正しく使用されていること" {
            val selector = MatchWorldSelector()
            val worldData =
                object : WorldData() {
                    override fun getName() = "customName"
                }

            selector.addMap(worldData)

            selector.randomMap().getName() shouldBe "customName"
        }

        "全てのマップを削除後 追加する操作的テスト" {
            val selector = MatchWorldSelector()

            repeat(5) { i ->
                selector.addMap(TestWorldData("map$i"))
            }
            selector.size() shouldBe 5

            selector.allMapNames().forEach { name ->
                selector.addMap(TestWorldData("${name}_new"))
            }
            selector.size() shouldBe 10
        }

        "並列追加時の競合を考慮したテスト（シミュレート）" {
            val selector = MatchWorldSelector()
            val worlds = (1..100).map { TestWorldData("parallel_$it") }

            worlds.forEach { selector.addMap(it) }

            selector.size() shouldBe 100
            val uniqueNames = selector.allMapNames().toSet()
            uniqueNames.size shouldBe 100
        }

        "空文字と通常文字が混在するケース" {
            val selector = MatchWorldSelector()
            selector.addMap(TestWorldData(""))
            selector.addMap(TestWorldData("normal"))
            selector.addMap(TestWorldData(""))

            selector.size() shouldBe 2
            selector.existMap("") shouldBe true
            selector.existMap("normal") shouldBe true
        }
    })
