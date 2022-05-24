package me.alexpetrakov.morty.common.data.remote.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.alexpetrakov.morty.common.domain.model.*

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

fun CharacterJson.toDomainModel(): Character {
    return Character(id, name, species, gender, vitalStatus, imageUrl)
}

@JsonClass(generateAdapter = true)
data class CharacterDetailsJson(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "species") val species: String,
    @Json(name = "gender") val gender: Gender,
    @Json(name = "status") val vitalStatus: VitalStatus,
    @Json(name = "origin") val originLocation: LocationJson,
    @Json(name = "location") val lastKnownLocation: LocationJson,
    @Json(name = "image") val imageUrl: String,
    @Json(name = "episode") val episodeUrls: List<String>
)

fun CharacterDetailsJson.toDomainModel(episode: Episode): CharacterDetails {
    return CharacterDetails(
        id,
        name,
        species,
        gender,
        vitalStatus,
        originLocation.name,
        lastKnownLocation.name,
        episode,
        episodeUrls.size,
        imageUrl
    )
}

@JsonClass(generateAdapter = true)
data class LocationJson(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String,
)

@JsonClass(generateAdapter = true)
data class EpisodeJson(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "episode") val codeName: String
)

fun EpisodeJson.toDomainModel(): Episode {
    return Episode(id, name, codeName)
}