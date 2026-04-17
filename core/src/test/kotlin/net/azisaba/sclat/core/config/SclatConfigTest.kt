package net.azisaba.sclat.core.config

import io.kotest.core.spec.style.StringSpec
import net.mamoe.yamlkt.Yaml

class SclatConfigTest :
    StringSpec({
        "test on prod file" {
            val rawJson =
                """
Lobby:
  WorldName: lobby
  X: 1
  Y: 2
  Z: 3
  Yaw: 180
TutorialClear:
  WorldName: lobby
  X: 4
  Y: 5
  Z: 6
  Yaw: 180
Tutorial:
  WorldName: lobby
  X: 7
  Y: 8
  Z: 9
  Yaw: -90
Hologram:
  WorldName: lobby
  X: 10
  Y: 11
  Z: 12
  Yaw: 180
RankingHolograms:
  WorldName: lobby
  X: 13
  Y: 14.5
  Z: 15.5
SquidRecovery: 0.1
NormalRecovery: 0.1
BlockUpdateRate: 20
MaxPlayerCount: 20
StartPlayerCount: 2
SquidSpeed: 0.1
PlayerWalkSpeed: 0.1
CanVoting: true
DefaultClass: わかばシューター
WorkMode: Normal
ServerType: LOBBY
MakeRankingPeriod: 20
HologramUpdatePeriod: 20
ParticlesRenderDistance: 16
Shop: true
ResourcePackURL: https://azisaba.net/sclat/resourcepack.zip
nBGM:
  Helloworld: helloworld.nbs
fBGM:
  NotAtAll: notatall.nbs
StatusShare:
  Port: 25590
EquipShare:
  sclatsv1:
    Port: 25100
    Host: match-sv1
  Trial:
    Port: 25100
    Host: weapon-test
                """.trimIndent()
            yaml.decodeFromString(
                SclatConfig.serializer(),
                rawJson,
            )
        }

        "self encode and decode test" {
            val encodedJson =
                yaml.encodeToString(
                    SclatConfig(
                        workMode = "Trial",
                        serverType = "Lobby",
                        blockUpdateRate = 20,
                        resourcePackUrl = "https://example.com/resourcepack.zip",
                        defaultClass = "defaultWeapon",
                        maxPlayerCount = 20,
                        canVoting = false,
                        startPlayerCount = 2,
                        hologram = LocationConfig("TestWorld", 0.0, 64.0, 0.0),
                        rankingHolograms = LocationConfig("TestWorld", 0.0, 64.0, 0.0),
                        normalRecovery = 1.0,
                        squidRecovery = 1.0,
                        squidSpeed = 1.0,
                    ),
                )
            yaml.decodeFromString(SclatConfig.serializer(), encodedJson)
        }
    }) {
    companion object {
        val yaml =
            Yaml {
                encodeDefaultValues = true
            }
    }
}
