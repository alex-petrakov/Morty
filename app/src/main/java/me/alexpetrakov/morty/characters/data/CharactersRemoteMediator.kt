package me.alexpetrakov.morty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.squareup.moshi.JsonDataException
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.db.character.CharacterEntity
import me.alexpetrakov.morty.characters.data.db.page.PageEntity
import me.alexpetrakov.morty.characters.data.network.CharacterPageJson
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.data.network.toEntity
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase,
    private val maxCacheLifetime: Duration
) : RemoteMediator<Int, CharacterEntity>() {

    private val characterDao = db.characterDao()

    private val pageDao = db.pageDao()

    override suspend fun initialize(): InitializeAction {
        val now = Instant.now()
        val updatedAt = pageDao.lastUpdateInstant() ?: Instant.MIN
        val actualCacheLifetime = Duration.between(updatedAt, now)
        return if (actualCacheLifetime.isNegative || actualCacheLifetime > maxCacheLifetime) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {
        val pageUrl = when (val action = determineNextActionFor(loadType, state)) {
            Action.Finish -> return MediatorResult.Success(endOfPaginationReached = true)
            Action.SkipPage -> return MediatorResult.Success(endOfPaginationReached = false)
            is Action.LoadPage -> action.pageUrl
        }

        val response = try {
            api.getCharacterPage(pageUrl)
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException, is JsonDataException -> {
                    return MediatorResult.Error(e)
                }
                else -> throw e
            }
        }

        cacheResponse(response, pageUrl, invalidateCache = loadType == LoadType.REFRESH)
        return MediatorResult.Success(endOfPaginationReached = !response.hasMorePages)
    }

    private suspend fun determineNextActionFor(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): Action {
        return when (loadType) {
            LoadType.REFRESH -> refresh(state)
            LoadType.PREPEND -> prepend(state)
            LoadType.APPEND -> append(state)
        }
    }

    private suspend fun refresh(
        state: PagingState<Int, CharacterEntity>,
        firstPageUrl: String = RickAndMortyApi.FIRST_CHARACTER_PAGE_URL
    ): Action {
        val lastAccessedCharacter = state.lastAccessedCharacter
        val currentPage = getPageFor(lastAccessedCharacter)
        val pageUrl = currentPage?.url ?: firstPageUrl
        return Action.LoadPage(pageUrl)
    }

    private suspend fun append(state: PagingState<Int, CharacterEntity>): Action {
        val lastCharacter = state.lastItemOrNull()
        val currentPage = getPageFor(lastCharacter) ?: return Action.SkipPage
        val pageUrl = currentPage.nextPageUrl ?: return Action.Finish
        return Action.LoadPage(pageUrl)
    }

    private suspend fun prepend(state: PagingState<Int, CharacterEntity>): Action {
        val firstCharacter = state.firstItemOrNull()
        val currentPage = getPageFor(firstCharacter) ?: return Action.SkipPage
        val pageUrl = currentPage.previousPageUrl ?: return Action.Finish
        return Action.LoadPage(pageUrl)
    }

    private suspend fun getPageFor(firstCharacter: CharacterEntity?): PageEntity? {
        return firstCharacter?.let { pageDao.getByCharacterId(it.id) }
    }

    private suspend fun cacheResponse(
        response: CharacterPageJson,
        pageUrl: String,
        invalidateCache: Boolean
    ) {
        db.withTransaction {
            if (invalidateCache) {
                pageDao.deleteAll()
            }
            pageDao.insert(
                PageEntity(pageUrl, response.nextPageUrl, response.prevPageUrl, Instant.now())
            )
            characterDao.insertAll(response.characters.map { it.toEntity(pageUrl) })
        }
    }

    private sealed class Action {
        object Finish : Action()
        object SkipPage : Action()
        data class LoadPage(val pageUrl: String) : Action()
    }
}

private val PagingState<Int, CharacterEntity>.lastAccessedCharacter: CharacterEntity?
    get() = anchorPosition?.let { closestItemToPosition(it) }

private val CharacterPageJson.hasMorePages get() = pageInfo.nextUrl != null

private val CharacterPageJson.nextPageUrl get() = pageInfo.nextUrl

private val CharacterPageJson.prevPageUrl get() = pageInfo.previousUrl