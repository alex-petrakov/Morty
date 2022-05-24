package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import me.alexpetrakov.morty.AppScreens
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.repositories.CharactersRepository
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import me.alexpetrakov.morty.common.presentation.paging.Pager
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    repository: CharactersRepository,
    private val resourceProvider: ResourceProvider,
    private val router: Router
) : ViewModel() {

    private val pager: Pager<Character> = Pager(repository::getCharacterPage)

    val viewState: StateFlow<ViewState> = pager.pagingState
        .map { pagingState -> pagingState.toViewState(resourceProvider) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ViewState.Loading)

    val viewEffect: SharedFlow<ViewEffect> = pager.pagingEffect
        .map { pagingEffect -> pagingEffect.toViewEffect() }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        pager.refresh()
    }

    fun onRefresh() {
        pager.refresh()
    }

    fun onLoadNextPage() {
        pager.loadNextPage()
    }

    fun onCharacterClicked(character: CharacterUiModel) {
        router.navigateTo(AppScreens.characterDetails(character.id))
    }

    override fun onCleared() {
        pager.release()
    }
}