package me.alexpetrakov.morty.characterdetails.presentation

import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.characters.domain.CharacterDetails
import me.alexpetrakov.morty.common.domain.ResourceProvider
import me.alexpetrakov.morty.common.presentation.mappers.toUiModel

sealed class ViewState {
    data class Content(val character: CharacterDetailsUiModel) : ViewState()
    object Error : ViewState()
    object Loading : ViewState()
}

fun CharacterDetails?.toViewState(resourceProvider: ResourceProvider): ViewState {
    return when (this) {
        null -> ViewState.Error
        else -> ViewState.Content(toUiModel(resourceProvider))
    }
}

data class CharacterDetailsUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val origin: String,
    val lastKnownLocation: String,
    val firstEpisode: String,
    val episodeCount: String,
    val imageUrl: String
)

fun CharacterDetails.toUiModel(resourceProvider: ResourceProvider): CharacterDetailsUiModel {
    val species = species.replaceFirstChar { it.titlecase() }
    val vitalStatus = vitalStatus.toUiModel(resourceProvider)
    val gender = gender.toUiModel(resourceProvider)
    val episodeCount = resourceProvider.getQuantityString(
        R.plurals.app_formattable_episode_count,
        episodeCount,
        episodeCount
    )
    return CharacterDetailsUiModel(
        id,
        name,
        "$vitalStatus · $species · $gender",
        origin.replaceFirstChar { it.titlecase() },
        lastKnownLocation.replaceFirstChar { it.titlecase() },
        "${firstEpisode.codeName} ${firstEpisode.name}",
        episodeCount,
        imageUrl
    )
}