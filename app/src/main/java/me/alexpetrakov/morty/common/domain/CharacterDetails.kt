package me.alexpetrakov.morty.common.domain

data class CharacterDetails(
    val id: Int,
    val name: String,
    val species: String,
    val gender: Gender,
    val vitalStatus: VitalStatus,
    val origin: String,
    val lastKnownLocation: String,
    val firstEpisode: Episode,
    val episodeCount: Int,
    val imageUrl: String
)