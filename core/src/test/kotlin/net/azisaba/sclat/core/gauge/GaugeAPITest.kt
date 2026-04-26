package net.azisaba.sclat.core.gauge

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bukkit.ChatColor

class GaugeAPITest : StringSpec({
    "should generate gauge with correct filled and empty parts" {
        val value = 5
        val max = 10
        val color1 = "${ChatColor.GREEN}"
        val color2 = "${ChatColor.RED}"

        val result = GaugeAPI.toGauge(value, max, color1, color2)

        val expected = "${ChatColor.RESET}${ChatColor.GREEN}|||||${ChatColor.RED}|||||${ChatColor.RESET}"
        result shouldBe expected
    }

    "should generate empty gauge when value is 0" {
        val value = 0
        val max = 10
        val color1 = "${ChatColor.GREEN}"
        val color2 = "${ChatColor.RED}"

        val result = GaugeAPI.toGauge(value, max, color1, color2)

        val expected = "${ChatColor.RESET}${ChatColor.GREEN}${ChatColor.RED}||||||||||${ChatColor.RESET}"
        result shouldBe expected
    }

    "should generate full gauge when value equals max" {
        val value = 10
        val max = 10
        val color1 = "${ChatColor.GREEN}"
        val color2 = "${ChatColor.RED}"

        val result = GaugeAPI.toGauge(value, max, color1, color2)

        val expected = "${ChatColor.RESET}${ChatColor.GREEN}||||||||||${ChatColor.RED}${ChatColor.RESET}"
        result shouldBe expected
    }
})
