package be4rjp.sclat.animation.text

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class TextAnimationTest :
    StringSpec({

        // --- Basic & English Cases ---
        "英数字：標準的なアルファベットのスクロール" {
            val animator = TextAnimation("Hello", 3)
            animator.next().startsWith("Hel") shouldBe true
        }

        "英数字：'i' や 'l' など SMALLS 文字が連続する場合" {
            // SMALLSに 'i', 'l' が含まれるため、表示文字数が増えるはず
            val animator = TextAnimation("illilli", 3)
            val result = animator.next()
            result.length shouldBeGreaterThan 3
        }

        "英数字：'1' (SMALLS) と '2' (Not SMALLS) の混在" {
            val animator = TextAnimation("121212", 2)
            // '1' は SMALLS(plus++) かつ 1byte(hankaku++) なので大幅に伸びる
            animator.next().length shouldBeGreaterThan 2
        }

        // --- Japanese Specific Cases ---
        "日本語：全角カタカナのみ（補正なし）" {
            val animator = TextAnimation("ニュース", 2)
            animator.next() shouldBe "ニュ"
        }

        "日本語：全角の '！' が SMALLS として機能すること" {
            val animator = TextAnimation("あ！い", 2)
            // '！' は SMALLS に含まれているため plus++
            animator.next() shouldBe "あ！い"
        }

        "日本語：漢字とひらがなの混在" {
            val animator = TextAnimation("東京都渋谷区", 3)
            animator.next() shouldBe "東京都"
        }

        // --- Symbols & Space ---
        "記号：ドット '.' とスラッシュ '/' の連続" {
            val animator = TextAnimation("./././", 2)
            // 両方 SMALLS なので index 0 から 2文字以上取得
            animator.next().length shouldBeGreaterThan 2
        }

        "空白：半角スペースによる幅補正" {
            val animator = TextAnimation("A B C", 2)
            // ' ' は SMALLS なので "A B" まで取得されるはず
            animator.next() shouldBe "A B"
        }

        "ループ：非常に長い length 設定時の挙動" {
            val animator = TextAnimation("Short", 10)
            // text.repeat(4) しているので、length 10 でもエラーにならず取得できる
            animator.next().length shouldBeGreaterThan 5
        }

        "境界値：空文字入力時の安全性" {
            val animator = TextAnimation("", 0)
            animator.next() shouldBe ""
        }

        "耐久：連続で 100回 next() を呼び出した際の index 整合性" {
            val animator = TextAnimation("InfiniteLoop", 4)
            var lastResult = ""
            repeat(100) {
                lastResult = animator.next()
            }
            lastResult.isNotEmpty() shouldBe true
        }

        "薄い文字の判定：2文字以上の半角で初めて length が増える" {
            val animator = TextAnimation("abcde", 2)
            // 'a','b' は 1バイト。 hankaku = 2. 2 / 2 = 1.
            // length(2) + 1 = 3文字取得される
            animator.next() shouldBe "abc"
        }

        "SMALLSの判定：SMALLSに含まれる文字は1文字でも length が増える" {
            // 'i' は SMALLS なので plus++ される
            val animator = TextAnimation("iiiiii", 1)
            // plus = 1, hankaku = 1. length(1) + 1 + (1/2) = 2
            animator.next() shouldBe "ii"
        }

        "複合：日本語と英語が混ざった時の実際の挙動" {
            val animator = TextAnimation("今週のNEWS", 5)
            // [今(3), 週(3), の(3), N(1), E(1)] -> hankaku = 2
            // plus = 0
            // length(5) + 0 + (2/2) = 6
            // 結果は「今週のNEWS」まで（6文字）
            animator.next() shouldBe "今週のNEW"
        }

        "ループ：インデックスのリセット確認" {
            val animator = TextAnimation("ABC", 2)
            // 1回目: index 0, length 2 -> "ABC" (hankaku 2/2=1 により+1)
            animator.next() shouldBe "ABC"
            // 2回目: index 1, length 2 -> "BC" + "A" (repeatにより) -> "BCA"
            animator.next() shouldBe "BCA"
            // 3回目: index 2, length 2 -> "CA" + "B" -> "CAB"
            animator.next() shouldBe "CAB"
            // 4回目: index 0 にリセット
            animator.next() shouldBe "ABC"
        }
    })
