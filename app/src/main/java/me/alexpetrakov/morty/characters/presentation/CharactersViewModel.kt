package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.characters.domain.Character
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val charactersRepository: PagingSource<String, Character>
) : ViewModel() {

    val viewState = Pager(PagingConfig(pageSize = 20)) { charactersRepository }.flow
        .map { pagingData -> pagingData.toPageOfUiModels() }
        .cachedIn(viewModelScope)
}

private suspend fun PagingData<Character>.toPageOfUiModels(): PagingData<CharacterUiModel> {
    return withContext(Dispatchers.Default) {
        map { it.toUiModel() }
    }
}

private fun Character.toUiModel(): CharacterUiModel {
    return CharacterUiModel(id, name, "Description", imageUrl)
}
