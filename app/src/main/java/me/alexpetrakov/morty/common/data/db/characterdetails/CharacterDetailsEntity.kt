package me.alexpetrakov.morty.common.data.db.characterdetails

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import me.alexpetrakov.morty.common.domain.model.Episode
import me.alexpetrakov.morty.common.domain.model.Gender
import me.alexpetrakov.morty.common.domain.model.VitalStatus
import java.time.Instant

@Entity(tableName = "character_details")
data class CharacterDetailsEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "species") val species: String,
    @ColumnInfo(name = "gender") val gender: Gender,
    @ColumnInfo(name = "vital_status") val vitalStatus: VitalStatus,
    @ColumnInfo(name = "origin_location") val originLocation: String,
    @ColumnInfo(name = "last_known_location") val lastKnownLocation: String,
    @Embedded val firstEpisode: EpisodeEntity,
    @ColumnInfo(name = "episode_count") val episodeCount: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "updated_at") val lastUpdateInstant: Instant
)

fun CharacterDetailsEntity.toDomainModel(): CharacterDetails {
    return CharacterDetails(
        id,
        name,
        species,
        gender,
        vitalStatus,
        originLocation,
        lastKnownLocation,
        firstEpisode.toDomainModel(),
        episodeCount,
        imageUrl
    )
}

data class EpisodeEntity(
    @ColumnInfo(name = "episode_id") val id: Int,
    @ColumnInfo(name = "episode_name") val name: String,
    @ColumnInfo(name = "episode_code_name") val codeName: String
)

fun EpisodeEntity.toDomainModel(): Episode {
    return Episode(id, name, codeName)
}