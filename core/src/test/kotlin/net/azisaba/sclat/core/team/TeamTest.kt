package net.azisaba.sclat.core.team

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TeamTest : StringSpec({

    "addPaintCount should increment point" {
        val team = Team(1)
        team.addPaintCount()
        team.point shouldBe 1
    }

    "subtractPaintCount should decrement point" {
        val team = Team(1)
        team.addPaintCount()
        team.subtractPaintCount()
        team.point shouldBe 0
    }

    "addKillCount should increment killCount" {
        val team = Team(1)
        team.addKillCount()
        team.killCount shouldBe 1
    }

    "addGatiCount should increment gatiCount" {
        val team = Team(1)
        team.addGatiCount()
        team.gatiCount shouldBe 1
    }

    "addRateTotal should add to rateTotal" {
        val team = Team(1)
        team.addRateTotal(10)
        team.rateTotal shouldBe 10
    }

    "subtractRateTotal should subtract from rateTotal" {
        val team = Team(1)
        team.addRateTotal(10)
        team.subtractRateTotal(5)
        team.rateTotal shouldBe 5
    }
})
