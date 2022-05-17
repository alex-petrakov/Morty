package me.alexpetrakov.morty.common.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH
import androidx.paging.RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH
import com.squareup.moshi.JsonDataException
import me.alexpetrakov.morty.common.data.cache.PageCache
import me.alexpetrakov.morty.common.data.db.character.CharacterEntity
import me.alexpetrakov.morty.common.data.network.CharacterPageJson
import me.alexpetrakov.morty.common.data.network.RickAndMortyApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator @Inject constructor(
    private val api: RickAndMortyApi,
    private val pageCache: PageCache,
) : RemoteMediator<Int, CharacterEntity>() {

    override suspend fun initialize(): InitializeAction {
        return if (pageCache.hasFreshPages()) SKIP_INITIAL_REFRESH else LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {
        return when (loadType) {
            LoadType.REFRESH -> loadPageNear(state.lastAccessedItemOrNull)
            LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> loadPageAfter(state.lastItemOrNull())
        }
    }

    private suspend fun loadPageNear(
        character: CharacterEntity?,
        firstPageUrl: String = RickAndMortyApi.FIRST_CHARACTER_PAGE_URL
    ): MediatorResult {
        val pageUrl = when (val characterId = character?.id) {
            null -> firstPageUrl
            else -> getPageKeyNear(characterId) ?: firstPageUrl
        }
        return when (val response = loadPageIntoCache(pageUrl, invalidateCache = true)) {
            null -> MediatorResult.Error(Throwable())
            else -> MediatorResult.Success(endOfPaginationReached = !response.hasNextPages)
        }
    }

    private suspend fun loadPageAfter(character: CharacterEntity?): MediatorResult {
        val characterId = character?.id ?: return MediatorResult.Success(false)
        val pageUrl = getPageKeyAfter(characterId) ?: return MediatorResult.Success(true)
        return when (val response = loadPageIntoCache(pageUrl, invalidateCache = false)) {
            null -> MediatorResult.Error(Throwable())
            else -> MediatorResult.Success(endOfPaginationReached = !response.hasNextPages)
        }
    }

    private suspend fun loadPageIntoCache(
        pageUrl: String,
        invalidateCache: Boolean
    ): CharacterPageJson? {
        val response = try {
            api.getCharacterPage(pageUrl)
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException, is JsonDataException -> {
                    return null
                }
                else -> throw e
            }
        }
        pageCache.storePage(response, pageUrl, invalidateCache)
        return response
    }

    private suspend fun getPageKeyNear(characterId: Int): String? {
        return pageCache.getAssociatedPageFor(characterId)?.url
    }

    private suspend fun getPageKeyAfter(characterId: Int): String? {
        return pageCache.getAssociatedPageFor(characterId)?.nextPageUrl
    }
}

private val PagingState<Int, CharacterEntity>.lastAccessedItemOrNull: CharacterEntity?
    get() = anchorPosition?.let { closestItemToPosition(it) }

private val CharacterPageJson.hasNextPages get() = pageInfo.nextUrl != null

private val CharacterPageJson.hasPreviousPages get() = pageInfo.previousUrl != null