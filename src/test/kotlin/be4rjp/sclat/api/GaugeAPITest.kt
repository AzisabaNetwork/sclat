package be4rjp.sclat.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.bukkit.ChatColor

class GaugeAPITest :
    StringSpec({
        "GaugeAPI should generate correct gauge string" {
            val gaugeString = GaugeAPI.toGauge(5, 10, ChatColor.GREEN.toString(), ChatColor.RED.toString())
            gaugeString shouldBe "${ChatColor.RESET}${ChatColor.GREEN}|||||${ChatColor.RED}|||||${ChatColor.RESET}"
        }

        "GaugeAPI should handle zero value correctly" {
            val gaugeString = GaugeAPI.toGauge(0, 10, ChatColor.GREEN.toString(), ChatColor.RED.toString())
            gaugeString shouldBe "${ChatColor.RESET}${ChatColor.GREEN}${ChatColor.RED}||||||||||${ChatColor.RESET}"
        }

        "GaugeAPI should handle max value correctly" {
            val gaugeString = GaugeAPI.toGauge(10, 10, ChatColor.GREEN.toString(), ChatColor.RED.toString())
            gaugeString shouldBe "${ChatColor.RESET}${ChatColor.GREEN}||||||||||${ChatColor.RED}${ChatColor.RESET}"
        }
    })
