package net.azisaba.sclat.core.status

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MineStatTest :
    StringSpec({
        "should return server up when server is reachable" {
            val mineStat = MineStat("example.com", 25565)
            mineStat.isServerUp shouldBe false
        }

        "should return server down when server is unreachable" {
            val mineStat = MineStat("invalid.address", 25565)
            mineStat.isServerUp shouldBe false
        }

        "should correctly parse server data when server is up" {
            val mineStat = MineStat("example.com", 25565)
            if (mineStat.isServerUp) {
                mineStat.version shouldBe "1.20.1"
                mineStat.motd shouldBe "Welcome to the server!"
                mineStat.currentPlayers shouldBe "10"
                mineStat.maximumPlayers shouldBe "100"
            }
        }
    })
