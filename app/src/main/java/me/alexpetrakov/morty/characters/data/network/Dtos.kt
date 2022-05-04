package me.alexpetrakov.morty.characters.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.alexpetrakov.morty.characters.domain.Gender
import me.alexpetrakov.morty.characters.domain.VitalStatus

@JsonClass(generateAdapter = true)
data class CharacterPageJson(
    @Json(name = "info") val pageInfo: PageInfoJson,
    @Json(name = "results") val characters: List<CharacterJson>
)

@JsonClass(generateAdapter = true)
data class PageInfoJson(
    @Json(name = "next") val nextUrl: String?,
    @Json(name = "prev") val previousUrl: String?
)

@JsonClass(generateAdapter = true)
data class CharacterJson(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "species") val species: String,
    @Json(name = "gender") val gender: Gender,
    @Json(name = "status") val vitalStatus: VitalStatus,
    @Json(name = "image") val imageUrl: String
)