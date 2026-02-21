package be4rjp.sclat.api.raytrace

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bukkit.util.Vector

class RayTraceTest :
    FunSpec({

        context("getPosition should calculate correct coordinates") {
            withData(
                nameFn = { "Distance ${it.first} should result in ${it.second}" },
                // (blocksAway, expectedVector)
                Pair(0.0, Vector(0.0, 0.0, 0.0)),
                Pair(1.0, Vector(1.0, 0.0, 0.0)),
                Pair(5.5, Vector(5.5, 0.0, 0.0)),
                Pair(-1.0, Vector(-1.0, 0.0, 0.0)),
            ) { (dist, expected) ->
                val ray = RayTrace(Vector(0.0, 0.0, 0.0), Vector(1.0, 0.0, 0.0))
                val result = ray.getPosition(dist)
                result.x shouldBe (expected.x plusOrMinus 0.001)
                result.y shouldBe (expected.y plusOrMinus 0.001)
                result.z shouldBe (expected.z plusOrMinus 0.001)
            }
        }

        context("Intersection Logic (Hits and Misses)") {
            val min = Vector(1.0, 1.0, 1.0)
            val max = Vector(2.0, 2.0, 2.0)

            data class IntersectionCase(
                val name: String,
                val origin: Vector,
                val dir: Vector,
                val shouldHit: Boolean,
            )

            withData(
                // --- Direct Hits ---
                IntersectionCase("Hit from X-axis", Vector(0.0, 1.5, 1.5), Vector(1.0, 0.0, 0.0), true),
                IntersectionCase("Hit from Y-axis", Vector(1.5, 0.0, 1.5), Vector(0.0, 1.0, 0.0), true),
                IntersectionCase("Hit from Z-axis", Vector(1.5, 1.5, 0.0), Vector(0.0, 0.0, 1.0), true),
                IntersectionCase("Diagonal Hit", Vector(0.0, 0.0, 0.0), Vector(1.0, 1.0, 1.0), true),
                IntersectionCase("Inside start", Vector(1.5, 1.5, 1.5), Vector(1.0, 0.0, 0.0), true),
                IntersectionCase("Edge hit (Min)", Vector(1.0, 1.0, 1.0), Vector(1.0, 0.0, 0.0), true),
                IntersectionCase("Edge hit (Max)", Vector(2, 2, 2), Vector(-1.0, 0.0, 0.0), true),
                // --- Near Misses ---
                IntersectionCase("Miss X (Too high)", Vector(0.0, 2.1, 1.5), Vector(1.0, 0.0, 0.0), false),
                IntersectionCase("Miss X (Too low)", Vector(0.0, 0.9, 1.5), Vector(1.0, 0.0, 0.0), false),
                IntersectionCase("Miss Y (Left)", Vector(0.5, 0.0, 1.5), Vector(0.0, 1.0, 0.0), false),
                IntersectionCase("Miss Z (Right)", Vector(1.5, 1.5, 0.0), Vector(0.0, 0.0, -1.0), false),
                // --- Directional & Range Cases ---
                IntersectionCase("Wrong direction", Vector(0.0, 1.5, 1.5), Vector(-1.0, 0.0, 0.0), false),
                IntersectionCase("Parallel but offset X", Vector(0.0, 0.0, 0.0), Vector(1.0, 0.0, 0.0), false),
                IntersectionCase("Parallel but offset Y", Vector(0.0, 0.0, 0.0), Vector(0.0, 1.0, 0.0), false),
                IntersectionCase("Parallel but offset Z", Vector(0.0, 0.0, 0.0), Vector(0.0, 0.0, 1.0), false),
            ) { case ->
                val ray = RayTrace(case.origin, case.dir)
                // Using 10.0 blocks range and 0.1 accuracy
                ray.intersects(min, max, 10.0, 0.1) shouldBe case.shouldHit
            }
        }

        context("traverse step density") {
            test("traverse returns correct number of steps") {
                val ray = RayTrace(Vector(0.0, 0.0, 0.0), Vector(1.0, 0.0, 0.0))
                val result = ray.traverse(1.0, 0.1)
                // 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 = 11 points
                result.size shouldBe 11
            }
        }

        context("positionOfIntersection precision") {
            test("returns exact point of first contact within accuracy") {
                val ray = RayTrace(Vector(0.0, 1.5, 1.5), Vector(1.0, 0.0, 0.0))
                val hit = ray.positionOfIntersection(Vector(2.0, 1.0, 1.0), Vector(3.0, 2.0, 2.0), 5.0, 0.01)
                hit shouldNotBe null
                hit?.x!! shouldBe (2.0 plusOrMinus 0.01)
            }
        }
    })
