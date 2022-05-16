package me.alexpetrakov.morty.characterdetails.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import me.alexpetrakov.morty.common.domain.CharactersRepository
import me.alexpetrakov.morty.common.domain.ResourceProvider
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourceProvider: ResourceProvider,
    private val charactersRepository: CharactersRepository,
    private val router: Router
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: SharedFlow<ViewState> get() = _viewState

    init {
        loadDetails()
    }

    fun onNavigateUp() {
        router.exit()
    }

    fun onRetry() {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _viewState.value = charactersRepository.getCharacter(savedStateHandle.characterId)
                .toViewState(resourceProvider)
        }
    }

    private val SavedStateHandle.characterId: Int
        get() = this[ARG_CHARACTER_ID]
            ?: throw IllegalStateException("Required character ID argument is missing")

    companion object {
        const val ARG_CHARACTER_ID = "CHARACTER_ID"
    }
}