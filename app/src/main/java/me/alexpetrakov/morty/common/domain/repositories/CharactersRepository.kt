package me.alexpetrakov.morty.common.domain.repositories

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.CharacterDetails

interface CharactersRepository {

    fun getCharacters(): Flow<PagingData<Character>>

    suspend fun getCharacter(id: Int): CharacterDetails?
}