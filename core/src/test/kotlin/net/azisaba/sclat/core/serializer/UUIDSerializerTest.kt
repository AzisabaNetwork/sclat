package net.azisaba.sclat.core.serializer

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.mamoe.yamlkt.Yaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

@Serializable
data class TestData(
    @Contextual val id: UUID,
)

class UUIDSerializerTest {
    private val yaml =
        Yaml {
            serializersModule =
                kotlinx.serialization.modules.SerializersModule {
                    contextual(UUID::class, UUIDSerializer)
                }
        }

    @Test
    fun `test serialize UUID`() {
        val uuid = UUID.randomUUID()
        val testData = TestData(uuid)
        val serialized = yaml.encodeToString(TestData.serializer(), testData)
        assertEquals("id: '$uuid'", serialized.trim())
    }

    @Test
    fun `test deserialize valid UUID with dashes`() {
        val uuid = UUID.randomUUID()
        val serialized = uuid.toString()
        val deserialized = yaml.decodeFromString(UUIDSerializer, serialized)
        assertEquals(uuid, deserialized)
    }

    @Test
    fun `test deserialize invalid UUID`() {
        val invalidUUID = "invalid-uuid"
        assertThrows(IllegalArgumentException::class.java) {
            yaml.decodeFromString(UUIDSerializer, invalidUUID)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `test invalid deserialization`() {
        val invalidUUIDString = "invalid-uuid"
        val decoder =
            object : Decoder {
                override val serializersModule = EmptySerializersModule()

                override fun decodeString(): String = invalidUUIDString

                override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
                    throw UnsupportedOperationException("Not implemented")

                override fun decodeBoolean(): Boolean = throw UnsupportedOperationException("Not implemented")

                override fun decodeByte(): Byte = throw UnsupportedOperationException("Not implemented")

                override fun decodeChar(): Char = throw UnsupportedOperationException("Not implemented")

                override fun decodeDouble(): Double = throw UnsupportedOperationException("Not implemented")

                override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = throw UnsupportedOperationException("Not implemented")

                override fun decodeFloat(): Float = throw UnsupportedOperationException("Not implemented")

                override fun decodeInline(descriptor: SerialDescriptor): Decoder = throw UnsupportedOperationException("Not implemented")

                override fun decodeInt(): Int = throw UnsupportedOperationException("Not implemented")

                override fun decodeLong(): Long = throw UnsupportedOperationException("Not implemented")

                override fun decodeNotNullMark(): Boolean = throw UnsupportedOperationException("Not implemented")

                override fun decodeNull(): Nothing = throw UnsupportedOperationException("Not implemented")

                override fun decodeShort(): Short = throw UnsupportedOperationException("Not implemented")
            }

        try {
            UUIDSerializer.deserialize(decoder)
            assert(false) { "Expected an exception for invalid UUID" }
        } catch (_: IllegalArgumentException) {
            // Expected exception
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `test serialization`() {
        val uuid = UUID.randomUUID()
        val encoder =
            object : Encoder {
                var encodedValue: String? = null
                override val serializersModule = EmptySerializersModule()

                override fun encodeString(value: String) {
                    encodedValue = value
                }

                override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
                    throw UnsupportedOperationException("Not implemented")

                override fun encodeBoolean(value: Boolean) = throw UnsupportedOperationException("Not implemented")

                override fun encodeByte(value: Byte) = throw UnsupportedOperationException("Not implemented")

                override fun encodeChar(value: Char) = throw UnsupportedOperationException("Not implemented")

                override fun encodeDouble(value: Double) = throw UnsupportedOperationException("Not implemented")

                override fun encodeEnum(
                    enumDescriptor: SerialDescriptor,
                    index: Int,
                ) = throw UnsupportedOperationException("Not implemented")

                override fun encodeFloat(value: Float) = throw UnsupportedOperationException("Not implemented")

                override fun encodeInline(descriptor: SerialDescriptor): Encoder = throw UnsupportedOperationException("Not implemented")

                override fun encodeInt(value: Int) = throw UnsupportedOperationException("Not implemented")

                override fun encodeLong(value: Long) = throw UnsupportedOperationException("Not implemented")

                override fun encodeNull() = throw UnsupportedOperationException("Not implemented")

                override fun encodeShort(value: Short) = throw UnsupportedOperationException("Not implemented")
            }

        UUIDSerializer.serialize(encoder, uuid)
        assert(uuid.toString() == encoder.encodedValue) { "Serialization failed" }
    }
}
