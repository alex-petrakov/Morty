package me.alexpetrakov.morty.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import me.alexpetrakov.morty.characters.domain.Gender
import me.alexpetrakov.morty.characters.domain.VitalStatus
import me.alexpetrakov.morty.common.domain.ResourceProvider
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    repository: CharactersRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val pagingData = repository.getCharacters()
        .map { pagingData -> pagingData.toPagingDataOfUiModels(resourceProvider) }
        .cachedIn(viewModelScope)
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

private fun VitalStatus.toUiModel(resourceProvider: ResourceProvider): String {
    val id = when (this) {
        VitalStatus.ALIVE -> R.string.app_alive
        VitalStatus.DEAD -> R.string.app_dead
        VitalStatus.UNKNOWN -> R.string.app_unknown
    }
    return resourceProvider.getString(id)
}

private fun Gender.toUiModel(resourceProvider: ResourceProvider): String {
    val id = when (this) {
        Gender.MALE -> R.string.app_male
        Gender.FEMALE -> R.string.app_female
        Gender.GENDERLESS -> R.string.app_genderless
        Gender.UNKNOWN -> R.string.app_unknown
    }
    return resourceProvider.getString(id)
}
