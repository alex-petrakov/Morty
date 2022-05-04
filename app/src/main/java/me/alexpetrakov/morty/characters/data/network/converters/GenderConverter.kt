package me.alexpetrakov.morty.characters.data.network.converters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import me.alexpetrakov.morty.characters.domain.Gender

class GenderConverter {

    private val jsonToGender = mapOf(
        "Male" to Gender.MALE,
        "Female" to Gender.FEMALE,
        "Genderless" to Gender.GENDERLESS,
        "unknown" to Gender.UNKNOWN
    )

    private val genderToJson = mapOf(
        Gender.MALE to "Male",
        Gender.FEMALE to "Female",
        Gender.GENDERLESS to "Genderless",
        Gender.UNKNOWN to "unknown"
    )

    @FromJson
    fun fromJson(json: String): Gender {
        return jsonToGender[json] ?: throw IllegalStateException("Unexpected gender: $json")
    }

    @ToJson
    fun toJson(gender: Gender): String {
        return genderToJson[gender] ?: throw IllegalStateException("Unexpected gender: $gender")
    }
}