package me.alexpetrakov.morty.common.data

import androidx.paging.*
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.alexpetrakov.morty.common.data.cache.CacheLifetime
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.db.character.toDomainModel
import me.alexpetrakov.morty.common.data.db.characterdetails.CharacterDetailsEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.toDomainModel
import me.alexpetrakov.morty.common.data.network.RickAndMortyApi
import me.alexpetrakov.morty.common.data.network.toEntity
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import me.alexpetrakov.morty.common.domain.repositories.CharactersRepository
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase,
    private val remoteMediatorProvider: Provider<CharactersRemoteMediator>,
    @CacheLifetime private val maxCacheLifetime: Duration
) : CharactersRepository {

    private val characterDetailsDao = db.characterDetailsDao()

    init {
        require(!maxCacheLifetime.isNegative) { "Cache lifetime should be positive ($maxCacheLifetime)" }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getCharacters(): Flow<PagingData<Character>> {
        val flowOfPagingData = Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = true,
                initialLoadSize = PRELOADED_PAGE_COUNT * DEFAULT_PAGE_SIZE
            ),
            remoteMediator = remoteMediatorProvider.get(),
            pagingSourceFactory = { db.characterDao().getAll() }
        ).flow
        return flowOfPagingData.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override suspend fun getCharacter(id: Int): CharacterDetails? {
        val entity = getCachedCharacter(id) ?: loadCharacterFromApi(id)?.also {
            characterDetailsDao.insert(it)
        }
        return entity?.toDomainModel()
    }

    private suspend fun getCachedCharacter(id: Int): CharacterDetailsEntity? {
        val cachedEntity = characterDetailsDao.getById(id) ?: return null
        return if (cachedEntity.isStale) {
            null
        } else {
            cachedEntity
        }
    }

    private suspend fun loadCharacterFromApi(id: Int): CharacterDetailsEntity? {
        return try {
            val detailsJson = api.getCharacterDetails(id)
            val episodeJson = api.getEpisode(detailsJson.episodeUrls[0])
            detailsJson.toEntity(episodeJson, lastUpdateInstant = Instant.now())
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException, is JsonDataException -> null
                else -> throw e
            }
        }
    }

    private val CharacterDetailsEntity.isStale: Boolean
        get() {
            val lifetime = Duration.between(lastUpdateInstant, Instant.now())
            return lifetime.isNegative || lifetime > maxCacheLifetime
        }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PRELOADED_PAGE_COUNT = 3
    }
}