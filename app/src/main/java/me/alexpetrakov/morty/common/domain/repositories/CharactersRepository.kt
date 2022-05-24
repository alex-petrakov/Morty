package me.alexpetrakov.morty.common.domain.repositories

import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import me.alexpetrakov.morty.common.presentation.paging.PageRequestResult

interface CharactersRepository {

    suspend fun getCharacterPage(pageId: Int, forceRefresh: Boolean): PageRequestResult<Character>

    suspend fun getCharacter(id: Int): CharacterDetails?
}