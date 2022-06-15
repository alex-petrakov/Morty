package me.alexpetrakov.morty.characters.presentation

import androidx.recyclerview.widget.DiffUtil
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import me.alexpetrakov.morty.common.presentation.mappers.toUiModel
import me.alexpetrakov.morty.common.presentation.paging.PagingEffect
import me.alexpetrakov.morty.common.presentation.paging.PagingState

sealed class ViewState {
    data class Content(val characters: List<ListItem>) : ViewState()
    data class Refreshing(val characters: List<ListItem>) : ViewState()
    object Error : ViewState()
    object Loading : ViewState()
}

enum class ViewEffect {
    DISPLAY_REFRESH_ERROR,
    DISPLAY_PAGE_LOAD_ERROR
}

sealed interface ListItem {

    object DiffUtilCallback : DiffUtil.ItemCallback<ListItem>() {

        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.sameAs(newItem)
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.hasSameContentWith(newItem)
        }
    }

    fun sameAs(other: ListItem): Boolean

    fun hasSameContentWith(other: ListItem): Boolean
}

data class CharacterUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String
) : ListItem {

    override fun sameAs(other: ListItem): Boolean {
        return other is CharacterUiModel && this.id == other.id
    }

    override fun hasSameContentWith(other: ListItem): Boolean {
        return this == (other as CharacterUiModel)
    }
}

object LoadIndicator : ListItem {
    override fun sameAs(other: ListItem): Boolean = other is LoadIndicator

    override fun hasSameContentWith(other: ListItem): Boolean = true
}

object PageLoadError : ListItem {
    override fun sameAs(other: ListItem): Boolean = other is PageLoadError

    override fun hasSameContentWith(other: ListItem): Boolean = false
}


fun PagingState<Character>.toViewState(resourceProvider: ResourceProvider): ViewState {
    return when (this) {
        PagingState.Initial -> ViewState.Loading
        PagingState.Loading -> ViewState.Loading
        PagingState.Empty -> ViewState.Content(emptyList())
        is PagingState.Content -> ViewState.Content(items.toUiModel(resourceProvider))
        is PagingState.Error -> ViewState.Error
        is PagingState.LoadingPage -> ViewState.Content(
            items.toUiModel(resourceProvider) + LoadIndicator
        )
        is PagingState.PageLoadError -> ViewState.Content(
            items.toUiModel(resourceProvider) + PageLoadError
        )
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

fun PagingEffect.toViewEffect(): ViewEffect {
    return when (this) {
        is PagingEffect.RefreshError -> ViewEffect.DISPLAY_REFRESH_ERROR
        is PagingEffect.PageLoadingError -> ViewEffect.DISPLAY_PAGE_LOAD_ERROR
    }
}