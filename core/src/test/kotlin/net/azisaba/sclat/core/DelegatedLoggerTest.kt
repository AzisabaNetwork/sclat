package net.azisaba.sclat.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class DelegatedLoggerTest :
    StringSpec({
        "DelegatedLogger should return the same logger instance for the same class" {
            val testObject =
                object {
                    val logger1 by DelegatedLogger()
                    val logger2 by DelegatedLogger()
                }
            testObject.logger1 shouldBe testObject.logger2
        }

        "DelegatedLogger should return different logger instances for different classes" {
            class TestClass1 {
                val logger by DelegatedLogger()
            }

            class TestClass2 {
                val logger by DelegatedLogger()
            }

            val testClass1 = TestClass1()
            val testClass2 = TestClass2()

            testClass1.logger shouldNotBe testClass2.logger
        }

        "get class name correctly on sclat" {
            logger.name shouldBe "Sclat:DelegatedLoggerTest"
        }
    }) {
    companion object {
        private val logger by DelegatedLogger()
    }
}
