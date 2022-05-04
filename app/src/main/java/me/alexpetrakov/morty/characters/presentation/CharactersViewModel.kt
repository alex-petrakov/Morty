package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val charactersRepository: CharactersRepository
) : ViewModel() {

    val viewState: LiveData<ViewState> = liveData {
        emit(charactersRepository.getCharacters().toViewState())
    }
}

private suspend fun List<Character>.toViewState(): ViewState {
    return withContext(Dispatchers.Default) {
        ViewState(map { it.toUiModel() })
    }
}

private fun Character.toUiModel(): CharacterUiModel {
    return CharacterUiModel(id, name, "Description", imageUrl)
}
