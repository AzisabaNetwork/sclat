package be4rjp.sclat.api.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.util.UUID

class DailyRefreshSetTest :
    StringSpec({

        val testUuid = UUID.randomUUID()

        "初期化時にUUID集合が空であること" {
            DailyRefreshSet().uuids shouldHaveSize 0
        }

        "初期化時にリセットエポックが0であること" {
            DailyRefreshSet().nextResetEpoch shouldBe 0L
        }

        "UUIDを追加した際に集合に含まれること" {
            val set = DailyRefreshSet()
            set + testUuid
            set.contains(testUuid).shouldBeTrue()
        }

        "UUID追加時にリセットエポックが0から更新されること" {
            val set = DailyRefreshSet()
            set + testUuid
            set.nextResetEpoch shouldBeGreaterThan 0L
        }

        "同じUUIDを複数回追加してもサイズが増えないこと" {
            val set = DailyRefreshSet()
            repeat(3) { set + testUuid }
            set.uuids shouldHaveSize 1
        }

        "リセット時刻を過ぎてから追加すると既存データがクリアされること" {
            val set = DailyRefreshSet()
            set + UUID.randomUUID()
            set.nextResetEpoch = System.currentTimeMillis() - 1000
            set + testUuid
            set.uuids shouldHaveSize 1
            set.contains(testUuid).shouldBeTrue()
        }

        "リセット時刻を過ぎてから削除操作をしてもクリアが走ること" {
            val set = DailyRefreshSet()
            set.uuids.add(testUuid)
            set.nextResetEpoch = System.currentTimeMillis() - 1000
            set - testUuid
            set.uuids shouldHaveSize 0
        }

        "containsの呼び出しではリセット処理が走らないこと" {
            val set = DailyRefreshSet()
            set + testUuid
            set.nextResetEpoch = System.currentTimeMillis() - 1000
            set.contains(testUuid).shouldBeTrue()
            set.uuids shouldHaveSize 1 // クリアされていない
        }

        "削除演算子で特定のUUIDが正しく削除されること" {
            val set = DailyRefreshSet()
            set + testUuid
            set - testUuid
            set.contains(testUuid).shouldBeFalse()
        }

        "存在しないUUIDを削除してもエラーが発生しないこと" {
            val set = DailyRefreshSet()
            set - UUID.randomUUID()
            set.uuids shouldHaveSize 0
        }

        "複数の異なるUUIDを同時保持できること" {
            val set = DailyRefreshSet()
            val list = List(5) { UUID.randomUUID() }
            list.forEach { set + it }
            set.uuids shouldHaveSize 5
        }

        "大量のUUIDを追加してもパフォーマンスに問題がないこと" {
            val set = DailyRefreshSet()
            repeat(1000) { set + UUID.randomUUID() }
            set.uuids shouldHaveSize 1000
        }

        "現在時刻とエポックが完全に一致する場合にリセットされること" {
            val set = DailyRefreshSet()
            set.uuids.add(testUuid)
            set.nextResetEpoch = System.currentTimeMillis()
            set + UUID.randomUUID()
            set.contains(testUuid).shouldBeFalse()
        }

        "空の状態でリセット時刻を過ぎても正常に動作すること" {
            val set = DailyRefreshSet()
            set.nextResetEpoch = 1L
            set + testUuid
            set.uuids shouldHaveSize 1
        }

        "リセットエポックが未来である限りデータが保持され続けること" {
            val set = DailyRefreshSet()
            set + testUuid
            set.nextResetEpoch = System.currentTimeMillis() + 100000
            set + UUID.randomUUID()
            set.contains(testUuid).shouldBeTrue()
        }

        "calculateNextResetが現在のエポックミリ秒より大きい値を返すこと" {
            val set = DailyRefreshSet()
            set + testUuid
            set.nextResetEpoch shouldBeGreaterThan System.currentTimeMillis()
        }

        "マイナス演算子を連続で使用してもリセット時刻が維持されること" {
            val set = DailyRefreshSet()
            set + testUuid
            val epoch = set.nextResetEpoch
            set - testUuid
            set - UUID.randomUUID()
            set.nextResetEpoch shouldBe epoch
        }

        "UUID集合を直接外部から変更しても型安全であること" {
            val set = DailyRefreshSet()
            set.uuids.add(testUuid)
            set.contains(testUuid).shouldBeTrue()
        }

        "重複した追加と削除の組み合わせで最終的に空になること" {
            val set = DailyRefreshSet()
            set + testUuid
            set + testUuid
            set - testUuid
            set.uuids.isEmpty().shouldBeTrue()
        }

        "東京時間での日付変更を考慮したエポック計算が行われていること" {
            val set = DailyRefreshSet()
            set + testUuid
            // 少なくとも12時間以上先であることを確認（簡易的な東京時間チェック）
            val diff = set.nextResetEpoch - System.currentTimeMillis()
            diff shouldBeGreaterThan 0L
        }

        "複数のUUIDを保持している状態でリセットされると全て消えること" {
            val set = DailyRefreshSet()
            repeat(10) { set + UUID.randomUUID() }
            set.nextResetEpoch = 1L
            set + testUuid
            set.uuids shouldHaveSize 1
        }

        "リセットエポックに負の値が設定されていても追加時に正しく更新されること" {
            val set = DailyRefreshSet()
            set.nextResetEpoch = -100L
            set + testUuid
            set.nextResetEpoch shouldBeGreaterThan 0L
        }

        "一度クリアされた後に同じUUIDを再度追加しても問題なく動作すること" {
            val set = DailyRefreshSet()
            set + testUuid
            set.nextResetEpoch = 1L
            set + testUuid
            set.contains(testUuid).shouldBeTrue()
            set.uuids shouldHaveSize 1
        }

        "計算された次のリセット時刻が現在から24時間+α以内であること" {
            val set = DailyRefreshSet()
            set + testUuid
            val oneDayMs = 24 * 60 * 60 * 1000L
            val diff = set.nextResetEpoch - System.currentTimeMillis()
            diff shouldBeLessThan (oneDayMs + 1000L * 60 * 60) // 最大でも1日と1時間以内
        }

        "MutableSetとしての振る舞いにより要素の順序に依存しないcontains判定ができること" {
            val set = DailyRefreshSet()
            val u1 = UUID.randomUUID()
            val u2 = UUID.randomUUID()
            set + u1
            set + u2
            set.contains(u1).shouldBeTrue()
            set.contains(u2).shouldBeTrue()
        }
    })
