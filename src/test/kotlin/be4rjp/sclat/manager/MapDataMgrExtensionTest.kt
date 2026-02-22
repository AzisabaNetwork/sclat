package be4rjp.sclat.manager

import be4rjp.sclat.extension.getLocation
import be4rjp.sclat.extension.getLocationWithPitch
import be4rjp.sclat.extension.getLocationWithYaw
import be4rjp.sclat.extension.getSection
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection

class MapDataMgrExtensionTest :
    StringSpec({
        val mockWorld = mockk<World>(relaxed = true)
        val mockSection = mockk<ConfigurationSection>(relaxed = true)

        "getLocation は X, Y, Z に 0.5 を加算した Location を返すこと" {
            every { mockSection.getDouble("Spawn.X") } returns 100.0
            every { mockSection.getDouble("Spawn.Y") } returns 64.0
            every { mockSection.getDouble("Spawn.Z") } returns -200.0

            val location = mockSection.getLocation("Spawn", mockWorld)

            location.x shouldBe 100.5
            location.y shouldBe 64.0
            location.z shouldBe -199.5
            location.world shouldBe mockWorld
        }

        "getLocationWithYaw は yaw が設定された Location を返すこと" {
            every { mockSection.getDouble("Intro.X") } returns 0.0
            every { mockSection.getDouble("Intro.Y") } returns 50.0
            every { mockSection.getDouble("Intro.Z") } returns 0.0
            every { mockSection.getInt("Intro.Yaw") } returns 90

            val location = mockSection.getLocationWithYaw("Intro", mockWorld)

            location.x shouldBe 0.5
            location.y shouldBe 50.0
            location.z shouldBe 0.5
            location.yaw shouldBe 90.0f
        }

        "getLocationWithPitch は yaw と pitch が設定された Location を返すこと" {
            every { mockSection.getDouble("Result.X") } returns 10.0
            every { mockSection.getDouble("Result.Y") } returns 20.0
            every { mockSection.getDouble("Result.Z") } returns 30.0
            every { mockSection.getInt("Result.Yaw") } returns 180
            every { mockSection.getInt("Result.Pitch", 0) } returns 90

            val location = mockSection.getLocationWithPitch("Result", mockWorld)

            location.x shouldBe 10.5
            location.y shouldBe 20.0
            location.z shouldBe 30.5
            location.yaw shouldBe 180.0f
            location.pitch shouldBe 90.0f
        }

        "getLocationWithPitch で Pitch がない場合は 0 になること" {
            every { mockSection.getDouble("NoPitch.X") } returns 0.0
            every { mockSection.getDouble("NoPitch.Y") } returns 0.0
            every { mockSection.getDouble("NoPitch.Z") } returns 0.0
            every { mockSection.getInt("NoPitch.Yaw") } returns 0
            every { mockSection.getInt("NoPitch.Pitch", 0) } returns 0

            val location = mockSection.getLocationWithPitch("NoPitch", mockWorld)

            location.pitch shouldBe 0.0f
        }

        "負の座標値でも正しく動作すること" {
            every { mockSection.getDouble("Negative.X") } returns -100.5
            every { mockSection.getDouble("Negative.Y") } returns -10.0
            every { mockSection.getDouble("Negative.Z") } returns -50.5

            val location = mockSection.getLocation("Negative", mockWorld)

            location.x shouldBe -100.0
            location.y shouldBe -10.0
            location.z shouldBe -50.0
        }

        "yaw が 0 の場合も正しく設定されること" {
            every { mockSection.getDouble("ZeroYaw.X") } returns 0.0
            every { mockSection.getDouble("ZeroYaw.Y") } returns 0.0
            every { mockSection.getDouble("ZeroYaw.Z") } returns 0.0
            every { mockSection.getInt("ZeroYaw.Yaw") } returns 0

            val location = mockSection.getLocationWithYaw("ZeroYaw", mockWorld)

            location.yaw shouldBe 0.0f
        }

        "yaw が負の値でも正しく設定されること" {
            every { mockSection.getDouble("NegYaw.X") } returns 0.0
            every { mockSection.getDouble("NegYaw.Y") } returns 0.0
            every { mockSection.getDouble("NegYaw.Z") } returns 0.0
            every { mockSection.getInt("NegYaw.Yaw") } returns -90

            val location = mockSection.getLocationWithYaw("NegYaw", mockWorld)

            location.yaw shouldBe -90.0f
        }

        "yaw が 360 を超える値でも正しく設定されること" {
            every { mockSection.getDouble("BigYaw.X") } returns 0.0
            every { mockSection.getDouble("BigYaw.Y") } returns 0.0
            every { mockSection.getDouble("BigYaw.Z") } returns 0.0
            every { mockSection.getInt("BigYaw.Yaw") } returns 450

            val location = mockSection.getLocationWithYaw("BigYaw", mockWorld)

            location.yaw shouldBe 450.0f
        }

        "旧コードとの互換性: Intro ロケーションの座標変換が一致すること" {
            val oldWayX = 100
            val oldWayY = 64
            val oldWayZ = -200
            val oldWayYaw = 45

            every { mockSection.getInt("OldIntro.X") } returns oldWayX
            every { mockSection.getInt("OldIntro.Y") } returns oldWayY
            every { mockSection.getInt("OldIntro.Z") } returns oldWayZ
            every { mockSection.getInt("OldIntro.Yaw") } returns oldWayYaw

            val oldWayLocation = Location(mockWorld, oldWayX.toDouble(), oldWayY.toDouble(), oldWayZ.toDouble())
            oldWayLocation.yaw = oldWayYaw.toFloat()

            every { mockSection.getDouble("OldIntro.X") } returns oldWayX.toDouble()
            every { mockSection.getDouble("OldIntro.Y") } returns oldWayY.toDouble()
            every { mockSection.getDouble("OldIntro.Z") } returns oldWayZ.toDouble()

            oldWayLocation.x shouldBe 100.0
            oldWayLocation.y shouldBe 64.0
            oldWayLocation.z shouldBe -200.0
            oldWayLocation.yaw shouldBe 45.0f
        }

        "旧コードとの互換性: Team ロケーションは +0.5 オフセットが適用されること" {
            val teamX = 50
            val teamY = 70
            val teamZ = 100
            val teamYaw = 180

            every { mockSection.getInt("Team0.X") } returns teamX
            every { mockSection.getInt("Team0.Y") } returns teamY
            every { mockSection.getInt("Team0.Z") } returns teamZ
            every { mockSection.getInt("Team0.Yaw") } returns teamYaw

            val oldWayLocation = Location(mockWorld, teamX.toDouble(), teamY.toDouble(), teamZ.toDouble())
            oldWayLocation.x = oldWayLocation.x + 0.5
            oldWayLocation.z = oldWayLocation.z + 0.5
            oldWayLocation.yaw = teamYaw.toFloat()

            every { mockSection.getDouble("Team0.X") } returns teamX.toDouble()
            every { mockSection.getDouble("Team0.Y") } returns teamY.toDouble()
            every { mockSection.getDouble("Team0.Z") } returns teamZ.toDouble()
            every { mockSection.getInt("Team0.Yaw") } returns teamYaw

            val newWayLocation = mockSection.getLocationWithYaw("Team0", mockWorld)

            newWayLocation.x shouldBe oldWayLocation.x
            newWayLocation.z shouldBe oldWayLocation.z
        }

        "getSection は存在するセクションを返すこと" {
            val childSection = mockk<ConfigurationSection>()
            every { mockSection.getConfigurationSection("Child") } returns childSection

            val result = mockSection.getSection("Child")

            result shouldBe childSection
        }

        "getSection は存在しないセクションで null を返すこと" {
            every { mockSection.getConfigurationSection("NonExistent") } returns null

            val result = mockSection.getSection("NonExistent")

            result shouldBe null
        }
    })
