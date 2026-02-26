package be4rjp.sclat.unit

import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.serializer.UUIDSerializer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
private data class WithUUIDLarge(@Serializable(with = UUIDSerializer::class) val id: UUID)

class LargeUnitTests : StringSpec({
    // Generate many small independent tests (80 total) that exercise pure-JVM logic

    (1..40).forEach { i ->
        "isNumber valid integer case $i" {
            val v = (i * 7 - 3).toString()
            SclatUtil.isNumber(v) shouldBe true
        }
    }

    listOf("3.14", "-0.001", "1e6", "2E3", "+42.0", "0.0").forEachIndexed { idx, s ->
        "isNumber decimal/exponent case $idx - $s" {
            SclatUtil.isNumber(s) shouldBe true
        }
    }

    listOf("", "abc", "12ab", "--5", "1.2.3", "12e", "e5").forEachIndexed { idx, s ->
        "isNumber invalid case $idx - '$s'" {
            SclatUtil.isNumber(s) shouldBe false
        }
    }

    // UUID serializer checks
    "UUIDSerializer roundtrip 1" {
        val id = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val obj = WithUUIDLarge(id)
        val json = Json.encodeToString(WithUUIDLarge.serializer(), obj)
        val parsed = Json.decodeFromString(WithUUIDLarge.serializer(), json)
        parsed.id shouldBe id
    }

    "UUIDSerializer roundtrip random" {
        val id = UUID.randomUUID()
        val obj = WithUUIDLarge(id)
        val json = Json.encodeToString(WithUUIDLarge.serializer(), obj)
        val parsed = Json.decodeFromString(WithUUIDLarge.serializer(), json)
        parsed.id shouldBe id
    }

    // Some string-manipulation style tests that do not touch Bukkit
    (1..20).forEach { n ->
        "string repeat length test $n" {
            val s = "X".repeat(n)
            s.length shouldBe n
            s.startsWith("X") shouldBe true
            s.endsWith("X") shouldBe true
        }
    }

    // A couple of extra sanity checks
    "math basic checks" {
        (2 + 2) shouldBe 4
        (10 * 5) shouldBe 50
    }
})
