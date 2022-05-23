package me.alexpetrakov.morty.common.data

import me.alexpetrakov.morty.common.data.cache.CharactersLocalDataSource
import me.alexpetrakov.morty.common.data.network.CharacterRemoteDataSource
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import me.alexpetrakov.morty.common.domain.repositories.CharactersRepository
import me.alexpetrakov.morty.common.presentation.paging.PageRequestResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val localDataSource: CharactersLocalDataSource,
    private val remoteDataSource: CharacterRemoteDataSource
) : CharactersRepository {

    override suspend fun getCharacterPage(key: Int): PageRequestResult<Character> {
        val page =
            localDataSource.getPage(key) ?: remoteDataSource.getCharacterPage(key)?.also { page ->
                localDataSource.storePage(page, false)
            } ?: return PageRequestResult.Failure(RuntimeException())
        return PageRequestResult.Success(page.characters, page.hasMorePages)
    }

    override suspend fun getCharacter(id: Int): CharacterDetails? {
        return localDataSource.getCharacterDetails(id)
            ?: remoteDataSource.getCharacterDetails(id)?.also { character ->
                localDataSource.storeCharacterDetails(character)
            }
    }
}