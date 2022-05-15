package me.alexpetrakov.morty.characterdetails.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router
) : ViewModel() {

    val viewState = flow {
        emit(ViewState.Loading)
        delay(2500L)
        emit(
            ViewState.Content(
                CharacterDetailsUiModel(
                    1,
                    "Rick Sanchez",
                    "Alive · Human · Male",
                    "Earth",
                    "Citadel of Ricks",
                    "Pilot",
                    "51 episodes",
                    "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
                )
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ViewState.Loading)

    fun onNavigateUp() {
        router.exit()
    }

    fun onRetry() {
        TODO("Not implemented")
    }

    companion object {
        const val ARG_CHARACTER_ID = "CHARACTER_ID"
    }
}