package me.alexpetrakov.morty.characters.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.db.CharacterEntity
import me.alexpetrakov.morty.characters.data.db.PageEntity
import me.alexpetrakov.morty.characters.data.network.CharacterJson
import me.alexpetrakov.morty.characters.data.network.CharacterPageJson
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalPagingApi::class)
class CharactersRemoteMediator(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase
) : RemoteMediator<Int, CharacterEntity>() {

    private val characterDao = db.characterDao()

    private val pageDao = db.pageDao()

    override suspend fun initialize(): InitializeAction {
        val now = Instant.now()
        val updatedAt = pageDao.lastUpdateInstant() ?: Instant.MIN
        val actualCacheLifetime = Duration.between(updatedAt, now)
        return if (actualCacheLifetime.isNegative || actualCacheLifetime > MAX_CACHE_LIFETIME) {
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
            LoadType.REFRESH -> refresh(state)
            LoadType.PREPEND -> prepend(state)
            LoadType.APPEND -> append(state)
        }
    }

    private suspend fun loadAndStore(pageUrl: String, refresh: Boolean): MediatorResult {
        val response = try {
            loadCharacterPage(pageUrl)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }

        db.withTransaction {
            if (refresh) {
                pageDao.deleteAll()
            }
            pageDao.insert(
                PageEntity(pageUrl, response.nextPageUrl, response.prevPageUrl, Instant.now())
            )
            characterDao.insertAll(response.characters.toEntities(pageUrl))
        }

        return MediatorResult.Success(
            endOfPaginationReached = !response.hasMorePages
        )
    }

    private suspend fun refresh(
        state: PagingState<Int, CharacterEntity>,
        firstPageUrl: String = RickAndMortyApi.FIRST_CHARACTER_PAGE_URL
    ): MediatorResult {
        val lastAccessedCharacter = state.lastAccessedCharacter
        val currentPage = lastAccessedCharacter?.let { pageDao.getByCharacterId(it.id) }
        val pageUrl = currentPage?.url ?: firstPageUrl
        return loadAndStore(pageUrl, refresh = true)
    }

    private suspend fun append(state: PagingState<Int, CharacterEntity>): MediatorResult {
        val lastCharacter = state.lastItemOrNull()
        val currentPage =
            lastCharacter?.let { pageDao.getByCharacterId(it.id) } ?: return MediatorResult.Success(
                endOfPaginationReached = false
            )
        val pageUrl =
            currentPage.nextPageUrl ?: return MediatorResult.Success(endOfPaginationReached = true)
        return loadAndStore(pageUrl, refresh = false)
    }

    private suspend fun prepend(state: PagingState<Int, CharacterEntity>): MediatorResult {
        val firstCharacter = state.firstItemOrNull()
        val currentPage = firstCharacter?.let { pageDao.getByCharacterId(it.id) }
            ?: return MediatorResult.Success(endOfPaginationReached = false)
        val pageUrl = currentPage.previousPageUrl ?: return MediatorResult.Success(
            endOfPaginationReached = true
        )
        return loadAndStore(pageUrl, refresh = false)
    }

    private val PagingState<Int, CharacterEntity>.lastAccessedCharacter: CharacterEntity?
        get() = anchorPosition?.let { closestItemToPosition(it) }

    private suspend fun loadCharacterPage(pageUrl: String? = null): CharacterPageJson {
        return if (pageUrl == null) {
            api.getCharacterPage()
        } else {
            api.getCharacterPage(pageUrl)
        }
    }

    companion object {
        private val MAX_CACHE_LIFETIME = Duration.ofHours(1)
    }
}

private val CharacterPageJson.hasMorePages get() = pageInfo.nextUrl != null

private val CharacterPageJson.nextPageUrl get() = pageInfo.nextUrl

private val CharacterPageJson.prevPageUrl get() = pageInfo.previousUrl

private suspend fun List<CharacterJson>.toEntities(pageUrl: String): List<CharacterEntity> {
    return withContext(Dispatchers.Default) {
        map { it.toEntity(pageUrl) }
    }
}

private fun CharacterJson.toEntity(pageUrl: String): CharacterEntity {
    return CharacterEntity(id, pageUrl, name, species, gender, vitalStatus, imageUrl)
}