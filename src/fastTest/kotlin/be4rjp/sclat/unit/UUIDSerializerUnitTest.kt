package be4rjp.sclat.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import be4rjp.sclat.api.serializer.UUIDSerializer

@Serializable
private data class WithUUID(@Serializable(with = UUIDSerializer::class) val id: UUID)

class UUIDSerializerTest : StringSpec({
    "UUIDSerializer should serialize and deserialize UUID correctly" {
        val id = UUID.randomUUID()
        val obj = WithUUID(id)
        val json = Json.encodeToString(WithUUID.serializer(), obj)
        val parsed = Json.decodeFromString(WithUUID.serializer(), json)
        parsed.id shouldBe id
    }
})
