package me.alexpetrakov.morty.common.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.squareup.moshi.JsonDataException
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.db.character.CharacterEntity
import me.alexpetrakov.morty.common.data.db.page.PageEntity
import me.alexpetrakov.morty.common.data.network.CharacterPageJson
import me.alexpetrakov.morty.common.data.network.RickAndMortyApi
import me.alexpetrakov.morty.common.data.network.toEntity
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase,
    @CacheLifetime private val maxCacheLifetime: Duration
) : RemoteMediator<Int, CharacterEntity>() {

    private val characterDao = db.characterDao()

    private val pageDao = db.pageDao()

    init {
        require(!maxCacheLifetime.isNegative) { "Cache lifetime should be positive ($maxCacheLifetime)" }
    }

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
        return when (loadType) {
            LoadType.REFRESH -> loadPageNear(state.lastAccessedItemOrNull)
            LoadType.PREPEND -> loadPageBefore(state.firstItemOrNull())
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

    private suspend fun loadPageBefore(character: CharacterEntity?): MediatorResult {
        val characterId = character?.id ?: return MediatorResult.Success(true)
        val pageUrl = getPageKeyBefore(characterId) ?: return MediatorResult.Success(true)
        return when (val response = loadPageIntoCache(pageUrl, invalidateCache = false)) {
            null -> MediatorResult.Error(Throwable())
            else -> MediatorResult.Success(endOfPaginationReached = !response.hasPreviousPages)
        }
    }

    private suspend fun loadPageAfter(character: CharacterEntity?): MediatorResult {
        val characterId = character?.id ?: return MediatorResult.Success(true)
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
        cacheResponse(response, pageUrl, invalidateCache)
        return response
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

    private suspend fun getPageKeyNear(characterId: Int): String? {
        return pageDao.getByCharacterId(characterId)?.url
    }

    private suspend fun getPageKeyAfter(characterId: Int): String? {
        return pageDao.getByCharacterId(characterId)?.nextPageUrl
    }

    private suspend fun getPageKeyBefore(characterId: Int): String? {
        return pageDao.getByCharacterId(characterId)?.previousPageUrl
    }
}

private val PagingState<Int, CharacterEntity>.lastAccessedItemOrNull: CharacterEntity?
    get() = anchorPosition?.let { closestItemToPosition(it) }

private val CharacterPageJson.hasNextPages get() = pageInfo.nextUrl != null

private val CharacterPageJson.hasPreviousPages get() = pageInfo.previousUrl != null

private val CharacterPageJson.nextPageUrl get() = pageInfo.nextUrl

private val CharacterPageJson.prevPageUrl get() = pageInfo.previousUrl