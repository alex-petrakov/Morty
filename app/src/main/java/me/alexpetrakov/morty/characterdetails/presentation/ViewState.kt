package me.alexpetrakov.morty.characterdetails.presentation

sealed class ViewState {
    data class Content(val character: CharacterDetailsUiModel) : ViewState()
    object Error : ViewState()
    object Loading : ViewState()
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