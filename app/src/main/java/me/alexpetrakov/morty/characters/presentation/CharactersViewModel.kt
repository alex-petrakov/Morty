package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import me.alexpetrakov.morty.AppScreens
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import me.alexpetrakov.morty.common.domain.ResourceProvider
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