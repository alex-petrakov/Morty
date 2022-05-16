package me.alexpetrakov.morty.characters.data

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.db.CharacterDetailsEntity
import me.alexpetrakov.morty.characters.data.db.toDomainModel
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.data.network.toEntity
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharacterDetails
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import retrofit2.HttpException
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase
) : CharactersRepository {

    private val characterDetailsDao = db.characterDetailsDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getCharacters(): Flow<PagingData<Character>> {
        val flowOfPagingData = Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = true,
                initialLoadSize = PRELOADED_PAGE_COUNT * DEFAULT_PAGE_SIZE
            ),
            remoteMediator = CharactersRemoteMediator(api, db, MAX_CACHE_LIFETIME),
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
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
    }

    private val CharacterDetailsEntity.isStale: Boolean
        get() {
            val lifetime = Duration.between(lastUpdateInstant, Instant.now())
            return lifetime.isNegative || lifetime > MAX_CACHE_LIFETIME
        }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PRELOADED_PAGE_COUNT = 3

        private val MAX_CACHE_LIFETIME = Duration.ofHours(1)
    }
}