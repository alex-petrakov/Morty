package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.alexpetrakov.morty.AppScreens
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.repositories.CharactersRepository
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import me.alexpetrakov.morty.common.presentation.paging.Pager
import me.alexpetrakov.morty.common.presentation.paging.PagingState
import me.alexpetrakov.morty.common.presentation.paging.ViewController
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    repository: CharactersRepository,
    private val resourceProvider: ResourceProvider,
    private val router: Router
) : ViewModel(), ViewController<Character> {

    private val pager: Pager<Character> = Pager(repository::getCharacterPage, this)

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> get() = _viewState

    private val _viewEffect = MutableSharedFlow<ViewEffect>()
    val viewEffect: SharedFlow<ViewEffect> get() = _viewEffect

    init {
        pager.refresh()
    }

    fun onRefresh() {
        pager.refresh()
    }

    fun onLoadNextPage() {
        pager.loadNextPage()
    }

    override fun setPagingState(state: PagingState<Character>) {
        _viewState.value = state.toViewState(resourceProvider)
    }

    override fun displayRefreshError(e: Exception) {
        viewModelScope.launch {
            _viewEffect.emit(ViewEffect.DISPLAY_REFRESH_ERROR)
        }
    }

    override fun displayPageLoadError(e: Exception) {
        viewModelScope.launch {
            _viewEffect.emit(ViewEffect.DISPLAY_PAGE_LOAD_ERROR)
        }
    }

    fun onCharacterClicked(character: CharacterUiModel) {
        router.navigateTo(AppScreens.characterDetails(character.id))
    }

    override fun onCleared() {
        pager.release()
    }
}