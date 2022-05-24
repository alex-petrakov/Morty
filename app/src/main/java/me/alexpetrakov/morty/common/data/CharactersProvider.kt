package me.alexpetrakov.morty.common.data

import me.alexpetrakov.morty.common.data.local.CharactersLocalDataSource
import me.alexpetrakov.morty.common.data.remote.CharacterRemoteDataSource
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

    override suspend fun getCharacterPage(
        pageId: Int,
        forceRefresh: Boolean
    ): PageRequestResult<Character> {
        val page = localDataSource.getPage(pageId, forceRefresh)
            ?: remoteDataSource.getCharacterPage(pageId)?.also { page ->
                localDataSource.storePage(page)
            } ?: return PageRequestResult.Failure(UnableToLoadPageException())
        return PageRequestResult.Success(page.characters, page.hasMorePages)
    }

    override suspend fun getCharacter(id: Int): CharacterDetails? {
        return localDataSource.getCharacterDetails(id)
            ?: remoteDataSource.getCharacterDetails(id)?.also { character ->
                localDataSource.storeCharacterDetails(character)
            }
    }

    class UnableToLoadPageException(
        message: String = "Unable to load page",
        cause: Throwable? = null
    ) : RuntimeException(message, cause)
}