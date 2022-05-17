package me.alexpetrakov.morty.common.data.cache

import androidx.room.withTransaction
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.db.page.PageEntity
import me.alexpetrakov.morty.common.data.network.CharacterPageJson
import me.alexpetrakov.morty.common.data.network.toCharacterEntities
import me.alexpetrakov.morty.common.data.network.toPageEntity
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class PageCache @Inject constructor(
    private val db: CharacterDatabase,
    @CacheLifetime private val maxCacheLifetime: Duration,
    private val clock: Clock
) {

    private val characterDao get() = db.characterDao()

    private val pageDao get() = db.pageDao()

    init {
        require(!maxCacheLifetime.isNegative) { "Cache lifetime must be positive ($maxCacheLifetime)" }
    }

    suspend fun hasFreshPages(): Boolean {
        val now = Instant.now(clock)
        val updatedAt = pageDao.lastUpdateInstant() ?: Instant.MIN
        val cacheLifetime = Duration.between(updatedAt, now)
        return !cacheLifetime.isNegative && cacheLifetime <= maxCacheLifetime
    }

    suspend fun getAssociatedPageFor(characterId: Int): PageEntity? {
        return pageDao.getByCharacterId(characterId)
    }

    suspend fun storePage(page: CharacterPageJson, pageUrl: String, invalidate: Boolean) {
        db.withTransaction {
            if (invalidate) {
                pageDao.deleteAll()
            }
            pageDao.insert(page.toPageEntity(pageUrl, Instant.now(clock)))
            characterDao.insertAll(page.toCharacterEntities(pageUrl))
        }
    }
}