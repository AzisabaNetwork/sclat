package be4rjp.sclat.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import java.util.UUID

class UUIDSerializerUnitTest :
    StringSpec({
        "UUIDSerializer should serialize and deserialize UUID correctly" {
            val id = UUID.randomUUID()
            val obj = WithUUID(id)
            val json = Json.encodeToString(WithUUID.serializer(), obj)
            val parsed: WithUUID = Json.decodeFromString(WithUUID.serializer(), json)
            parsed.id shouldBe id
        }
    })
