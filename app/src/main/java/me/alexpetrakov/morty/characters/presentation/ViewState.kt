package me.alexpetrakov.morty.characters.presentation

import androidx.recyclerview.widget.DiffUtil
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import me.alexpetrakov.morty.common.presentation.mappers.toUiModel
import me.alexpetrakov.morty.common.presentation.paging.PagingState

sealed class ViewState {
    data class Content(val characters: List<CharacterUiModel>) : ViewState()
    data class Refreshing(val characters: List<CharacterUiModel>) : ViewState()
    object Error : ViewState()
    object Loading : ViewState()
}

enum class ViewEffect {
    DISPLAY_REFRESH_ERROR,
    DISPLAY_PAGE_LOAD_ERROR
}

data class CharacterUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String
) {

    object DiffUtilCallback : DiffUtil.ItemCallback<CharacterUiModel>() {
        override fun areItemsTheSame(old: CharacterUiModel, new: CharacterUiModel): Boolean {
            return old.id == new.id
        }

        override fun areContentsTheSame(old: CharacterUiModel, new: CharacterUiModel): Boolean {
            return old == new
        }
    }
}


fun PagingState<Character>.toViewState(resourceProvider: ResourceProvider): ViewState {
    return when (this) {
        PagingState.Loading -> ViewState.Loading
        PagingState.Empty -> ViewState.Content(emptyList())
        is PagingState.Content -> ViewState.Content(items.toUiModel(resourceProvider))
        is PagingState.Error -> ViewState.Error
        is PagingState.LoadingPage -> ViewState.Content(items.toUiModel(resourceProvider))
        is PagingState.Refreshing -> ViewState.Refreshing(items.toUiModel(resourceProvider))
    }
}

fun List<Character>.toUiModel(resourceProvider: ResourceProvider): List<CharacterUiModel> {
    return map { it.toUiModel(resourceProvider) }
}

fun Character.toUiModel(resourceProvider: ResourceProvider): CharacterUiModel {
    val species = species.replaceFirstChar { it.titlecase() }
    val vitalStatus = vitalStatus.toUiModel(resourceProvider)
    val gender = gender.toUiModel(resourceProvider)
    return CharacterUiModel(id, name, "$vitalStatus · $species · $gender", imageUrl)
}