package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.AppScreens
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import me.alexpetrakov.morty.common.domain.ResourceProvider
import me.alexpetrakov.morty.common.presentation.mappers.toUiModel
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    repository: CharactersRepository,
    private val resourceProvider: ResourceProvider,
    private val router: Router
) : ViewModel() {

    val pagingData = repository.getCharacters()
        .map { pagingData -> pagingData.toPagingDataOfUiModels(resourceProvider) }
        .cachedIn(viewModelScope)

    fun onCharacterClicked(character: CharacterUiModel) {
        router.navigateTo(AppScreens.characterDetails(character.id))
    }
}

private suspend fun PagingData<Character>.toPagingDataOfUiModels(
    resourceProvider: ResourceProvider
): PagingData<CharacterUiModel> {
    return withContext(Dispatchers.Default) {
        map { it.toUiModel(resourceProvider) }
    }
}

private fun Character.toUiModel(resourceProvider: ResourceProvider): CharacterUiModel {
    val species = species.replaceFirstChar { it.titlecase() }
    val vitalStatus = vitalStatus.toUiModel(resourceProvider)
    val gender = gender.toUiModel(resourceProvider)
    return CharacterUiModel(id, name, "$vitalStatus · $species · $gender", imageUrl)
}