package net.azisaba.sclat.core.shape

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.World

class SphereTest :
    StringSpec({
        "getSphere should generate correct number of points" {
            val mockWorld = mockk<World>()
            val baseLoc = Location(mockWorld, 0.0, 0.0, 0.0)
            val radius = 5.0
            val accuracy = 10

            val spherePoints = Sphere.getSphere(baseLoc, radius, accuracy)

            // Verify the size of the generated points
            spherePoints.shouldHaveSize((180 / accuracy) * (360 / accuracy))
        }

        "getXZCircle should generate correct number of points" {
            val mockWorld = mockk<World>()
            val baseLoc = Location(mockWorld, 0.0, 0.0, 0.0)
            val radius = 5.0
            val rAccuracy = 1.0
            val accuracy = 10

            val circlePoints = Sphere.getXZCircle(baseLoc, radius, rAccuracy, accuracy)

            // Verify the size of the generated points
            circlePoints.shouldHaveSize(588)
        }
    })
