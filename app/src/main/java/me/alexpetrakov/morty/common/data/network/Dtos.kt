package me.alexpetrakov.morty.common.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.alexpetrakov.morty.common.data.db.character.CharacterEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.CharacterDetailsEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.EpisodeEntity
import me.alexpetrakov.morty.common.data.db.page.PageEntity
import me.alexpetrakov.morty.common.domain.model.Gender
import me.alexpetrakov.morty.common.domain.model.VitalStatus
import java.time.Instant

@JsonClass(generateAdapter = true)
data class CharacterPageJson(
    @Json(name = "info") val pageInfo: PageInfoJson,
    @Json(name = "results") val characters: List<CharacterJson>
)

fun CharacterPageJson.toPageEntity(url: String, updatedAt: Instant): PageEntity {
    return PageEntity(url, pageInfo.nextUrl, pageInfo.previousUrl, updatedAt)
}

fun CharacterPageJson.toCharacterEntities(pageUrl: String): List<CharacterEntity> {
    return characters.map { it.toEntity(pageUrl) }
}

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

fun CharacterJson.toEntity(pageUrl: String): CharacterEntity {
    return CharacterEntity(id, pageUrl, name, species, gender, vitalStatus, imageUrl)
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

fun CharacterDetailsJson.toEntity(
    episodeJson: EpisodeJson,
    lastUpdateInstant: Instant
): CharacterDetailsEntity {
    return CharacterDetailsEntity(
        id,
        name,
        species,
        gender,
        vitalStatus,
        originLocation.name,
        lastKnownLocation.name,
        episodeJson.toEntity(),
        episodeUrls.size,
        imageUrl,
        lastUpdateInstant
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

fun EpisodeJson.toEntity(): EpisodeEntity {
    return EpisodeEntity(id, name, codeName)
}