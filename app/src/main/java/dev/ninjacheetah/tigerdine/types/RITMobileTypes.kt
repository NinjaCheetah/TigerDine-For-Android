package dev.ninjacheetah.tigerdine.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

@Serializable
data class DiningLocationRMParser(
    val id: Int,
    val clicId: Int,
    val name: String,
    val summary: String,
    val description: String,
    val mdoId: Int,
    val onDemandId: Int? = null,
    val hasMenu: Boolean,
    val hasMenu2: Boolean,
    val openTimeString: String,
    val dotwString: String,
    val isOpen: Boolean,
    val closesTime: String,
    val opensTime: String,
    val open24h: Boolean,
    val openHours: Map<String, List<OpenHoursRM>>,
    @Serializable(with = VisitingChefsRMSerializer::class)
    val visitingChefs: Map<String, List<VisitingChefRM>> = emptyMap(),
    @Serializable(with = DailySpecialsRMSerializer::class)
    val dailySpecials: List<DailySpecialRM> = emptyList(),
    @Serializable(with = DietaryTagsRMSerializer::class)
    val dietaryTags: List<String> = emptyList(),
    val url: String = ""
) {
    @Serializable
    data class OpenHoursRM(
        val startTime: String,
        val endTime: String,
        val isOpen: Boolean
    )

    @Serializable
    data class VisitingChefRM(
        val name: String,
        val startTime: String,
        val endTime: String,
        val description: String
    )

    @Serializable
    class DailySpecialRM {
        override fun equals(other: Any?): Boolean = other is DailySpecialRM
        override fun hashCode(): Int = DailySpecialRM::class.hashCode()
    }
}

object VisitingChefsRMSerializer : KSerializer<Map<String, List<DiningLocationRMParser.VisitingChefRM>>> {
    private val delegate = MapSerializer(
        String.serializer(),
        ListSerializer(DiningLocationRMParser.VisitingChefRM.serializer())
    )
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Map<String, List<DiningLocationRMParser.VisitingChefRM>>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<String, List<DiningLocationRMParser.VisitingChefRM>> {
        val jsonDecoder = decoder as? JsonDecoder ?: return emptyMap()
        return try {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonObject) {
                jsonDecoder.json.decodeFromJsonElement(delegate, element)
            } else {
                emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}

object DailySpecialsRMSerializer : KSerializer<List<DiningLocationRMParser.DailySpecialRM>> {
    private val delegate = ListSerializer(DiningLocationRMParser.DailySpecialRM.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<DiningLocationRMParser.DailySpecialRM>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<DiningLocationRMParser.DailySpecialRM> {
        val jsonDecoder = decoder as? JsonDecoder ?: return emptyList()
        return try {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonArray) {
                jsonDecoder.json.decodeFromJsonElement(delegate, element)
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

object DietaryTagsRMSerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder ?: return emptyList()
        return try {
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonArray) {
                jsonDecoder.json.decodeFromJsonElement(delegate, element)
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

@Serializable
data class FoodTruckRMParser(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val imageAltTag: String,
    val url: String,
    val datesAvailable: List<DatesAvailableRM>
) {
    @Serializable
    data class DatesAvailableRM(
        val date: String,
        val startTime: String,
        val endTime: String,
        val url: String,
        val urlTitle: String,
        val location: String
    )
}
