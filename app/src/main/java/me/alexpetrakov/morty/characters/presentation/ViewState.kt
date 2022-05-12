package me.alexpetrakov.morty.characters.presentation

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.*
import androidx.recyclerview.widget.DiffUtil

enum class CompoundPagingState {
    LOADING, ERROR, REMOTE_ERROR, CONTENT;

    companion object {
        fun of(sourceState: LoadState, mediatorState: LoadState): CompoundPagingState {
            return when {
                sourceState is NotLoading && mediatorState is NotLoading -> CONTENT
                sourceState is NotLoading && mediatorState is Loading -> LOADING
                sourceState is NotLoading && mediatorState is Error -> REMOTE_ERROR
                sourceState is Loading && mediatorState is NotLoading -> CONTENT
                sourceState is Loading && mediatorState is Loading -> LOADING
                sourceState is Loading && mediatorState is Error -> LOADING
                sourceState is Error && mediatorState is NotLoading -> ERROR
                sourceState is Error && mediatorState is Loading -> ERROR
                sourceState is Error && mediatorState is Error -> ERROR
                else -> throw IllegalStateException("Unexpected state")
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