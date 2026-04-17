package net.azisaba.sclat.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextAnimationTest {
    @Test
    fun `test next method`() {
        val textAnimation = TextAnimation("Hello", 5)

        val first = textAnimation.next()
        val second = textAnimation.next()
        val third = textAnimation.next()

        assertEquals("HelloHel", first)
        assertEquals("elloHell", second)
        assertEquals("lloHello", third)
    }

    @Test
    fun `test next method with shorter text`() {
        val textAnimation = TextAnimation("Hi", 2)

        val first = textAnimation.next()
        val second = textAnimation.next()
        val third = textAnimation.next()

        assertEquals("HiH", first)
        assertEquals("iHi", second)
        assertEquals("HiH", third)
    }

    @Test
    fun `test next method with longer text`() {
        val textAnimation = TextAnimation("ABCDEFG", 4)

        val first = textAnimation.next()
        val second = textAnimation.next()
        val third = textAnimation.next()

        assertEquals("ABCDEF", first)
        assertEquals("BCDEFG", second)
        assertEquals("CDEFGA", third)
    }
}
