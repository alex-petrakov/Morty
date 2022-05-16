package me.alexpetrakov.morty.characters.presentation

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.*
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.DiffUtil
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import me.alexpetrakov.morty.common.presentation.mappers.toUiModel

enum class CompoundPagingState {
    LOADING, ERROR, REMOTE_ERROR, CONTENT;

    companion object {
        fun of(sourceState: LoadState, mediatorState: LoadState): CompoundPagingState {
            return when (sourceState) {
                is NotLoading -> when (mediatorState) {
                    is NotLoading -> CONTENT
                    Loading -> LOADING
                    is Error -> REMOTE_ERROR
                }
                Loading -> when (mediatorState) {
                    is NotLoading -> CONTENT
                    Loading -> LOADING
                    is Error -> LOADING
                }
                is Error -> when (mediatorState) {
                    is NotLoading -> ERROR
                    Loading -> ERROR
                    is Error -> ERROR
                }
            }
        }

        fun from(combinedLoadStates: CombinedLoadStates): CompoundPagingState {
            return of(combinedLoadStates.source.refresh, combinedLoadStates.mediator!!.refresh)
        }
    }
}

enum class ViewState {
    LOADING_FIRST_PAGE, REFRESHING, ERROR, REFRESH_ERROR, CONTENT;

    companion object {
        fun of(
            compoundPagingState: CompoundPagingState,
            isRefreshing: Boolean,
            hasLocalData: Boolean
        ): ViewState {
            return when (compoundPagingState) {
                CompoundPagingState.LOADING -> if (isRefreshing) {
                    REFRESHING
                } else {
                    LOADING_FIRST_PAGE
                }
                CompoundPagingState.ERROR -> ERROR
                CompoundPagingState.REMOTE_ERROR -> if (hasLocalData) {
                    REFRESH_ERROR
                } else {
                    ERROR
                }
                CompoundPagingState.CONTENT -> CONTENT
            }
        }
    }
}

enum class ViewEffect {
    DISPLAY_REFRESH_ERROR
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

fun PagingData<Character>.toPagingDataOfUiModels(
    resourceProvider: ResourceProvider
): PagingData<CharacterUiModel> {
    return map { it.toUiModel(resourceProvider) }
}

fun Character.toUiModel(resourceProvider: ResourceProvider): CharacterUiModel {
    val species = species.replaceFirstChar { it.titlecase() }
    val vitalStatus = vitalStatus.toUiModel(resourceProvider)
    val gender = gender.toUiModel(resourceProvider)
    return CharacterUiModel(id, name, "$vitalStatus · $species · $gender", imageUrl)
}