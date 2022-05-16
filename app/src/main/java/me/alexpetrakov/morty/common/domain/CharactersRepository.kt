package me.alexpetrakov.morty.common.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface CharactersRepository {

    fun getCharacters(): Flow<PagingData<Character>>

    suspend fun getCharacter(id: Int): CharacterDetails?
}