package me.alexpetrakov.morty.common.data.cache

import androidx.room.withTransaction
import me.alexpetrakov.morty.common.data.Page
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.db.character.toDomainModel
import me.alexpetrakov.morty.common.data.db.character.toEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.CharacterDetailsEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.toDomainModel
import me.alexpetrakov.morty.common.data.db.characterdetails.toEntity
import me.alexpetrakov.morty.common.data.db.page.PageEntity
import me.alexpetrakov.morty.common.data.toPageEntity
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class CharactersLocalDataSource @Inject constructor(
    private val db: CharacterDatabase,
    @CacheLifetime private val maxCacheLifetime: Duration,
    private val clock: Clock
) {

    private val characterDao get() = db.characterDao()

    private val pageDao get() = db.pageDao()

    private val characterDetailsDao get() = db.characterDetailsDao()

    init {
        require(!maxCacheLifetime.isNegative) { "Cache lifetime must be positive ($maxCacheLifetime)" }
    }

    suspend fun getPage(id: Int, invalidate: Boolean): Page? {
        return db.withTransaction {
            if (invalidate) {
                pageDao.deleteAll()
            }
            val pageEntity = pageDao.getById(id)
                ?.takeIf { it.isFresh(now(), maxCacheLifetime) }
                ?: return@withTransaction null
            val characterEntities = characterDao.getAllByPageId(id)
            Page(id, characterEntities.map { it.toDomainModel() }, pageEntity.hasNextPage)
        }
    }

    private fun PageEntity.isFresh(now: Instant, maxLifetime: Duration): Boolean {
        val lifetime = Duration.between(lastUpdateInstant, now)
        return !lifetime.isNegative && lifetime <= maxLifetime
    }

    suspend fun storePage(page: Page) {
        db.withTransaction {
            pageDao.insert(page.toPageEntity(now()))
            characterDao.insertAll(page.characters.map { it.toEntity(page.id) })
        }
    }

    suspend fun getCharacterDetails(id: Int): CharacterDetails? {
        return characterDetailsDao.getById(id)
            ?.takeIf { it.isFresh(now(), maxCacheLifetime) }
            ?.toDomainModel()
    }

    private fun CharacterDetailsEntity.isFresh(now: Instant, maxLifetime: Duration): Boolean {
        val lifetime = Duration.between(lastUpdateInstant, now)
        return !lifetime.isNegative && lifetime <= maxLifetime
    }

    suspend fun storeCharacterDetails(characterDetails: CharacterDetails) {
        characterDetailsDao.insert(characterDetails.toEntity(now()))
    }

    private fun now(): Instant = Instant.now(clock)
}