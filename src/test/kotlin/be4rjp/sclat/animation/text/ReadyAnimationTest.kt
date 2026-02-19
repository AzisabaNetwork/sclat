package be4rjp.sclat.animation.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReadyAnimationTest :
    StringSpec({

        "1文字のとき：最初がグレー、次が白（計2ステップ）で終了すること" {
            val anim = ReadyAnimation("A")
            anim.next() shouldBe "§7A" // step 0
            anim.next() shouldBe "A§7" // step 1
            anim.next() shouldBe null // 終了
        }

        "空白文字（半角スペースのみ）を渡すと例外を投げること" {
            // require(text.isNotBlank()) の検証
            shouldThrow<IllegalArgumentException> {
                ReadyAnimation(" ")
            }
        }

        "カラーコードが含まれる場合、分割位置によっては表示が壊れることを認識しているか" {
            // 現在のロジック：${text.take(pos)}§7${text.drop(pos)}
            // §cA (長さ3) の index 1 で分割すると "§§7cA" となり、色が剥がれる
            val anim = ReadyAnimation("§aGO")

            anim.next() shouldBe "§7§aGO" // 初期状態
            anim.next() shouldBe "§§7aGO" // 【注意】カラー記号の直後で分割される
        }

        "アニメーション終了後、何度 next() を呼んでも null が返り続けること" {
            val anim = ReadyAnimation("OK")
            repeat(3) { anim.next() } // 全ステップ消化

            repeat(10) {
                anim.next() shouldBe null
            }
        }

        "非常に長い文字列でもインデックスエラーが起きないこと" {
            val longText = "A".repeat(100)
            val anim = ReadyAnimation(longText)

            var count = 0
            while (anim.next() != null) {
                count++
            }
            count shouldBe 101 // 0文字目〜100文字目の後ろ、計101回
        }

        "マルチバイト文字（日本語）でも正しく分割できること" {
            // Kotlinの String.take/drop は文字数単位なので日本語もOK
            val anim = ReadyAnimation("準備")
            anim.next() shouldBe "§7準備"
            anim.next() shouldBe "準§7備"
            anim.next() shouldBe "準備§7"
            anim.next() shouldBe null
        }

        "READYのアニメーション遷移" {
            val anim = ReadyAnimation("READY")

            anim.next() shouldBe "§7READY"
            anim.next() shouldBe "R§7EADY"
            anim.next() shouldBe "RE§7ADY"
            anim.next() shouldBe "REA§7DY"
            anim.next() shouldBe "READ§7Y"
            anim.next() shouldBe "READY§7"
            anim.next() shouldBe null
        }

        "空文字のバリデーション" {
            shouldThrow<IllegalArgumentException> {
                ReadyAnimation("")
            }
        }

        "最小文字数の動作" {
            val anim = ReadyAnimation("!")
            anim.next() shouldBe "§7!"
            anim.next() shouldBe "!§7"
            anim.next() shouldBe null
        }
    })
