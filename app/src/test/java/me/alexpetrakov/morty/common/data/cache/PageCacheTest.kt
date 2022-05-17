package me.alexpetrakov.morty.common.data.cache

import io.mockk.*
import kotlinx.coroutines.runBlocking
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.db.character.CharacterDao
import me.alexpetrakov.morty.common.data.db.page.PageDao
import me.alexpetrakov.morty.common.data.hours
import me.alexpetrakov.morty.common.data.minus
import me.alexpetrakov.morty.common.data.minutes
import me.alexpetrakov.morty.common.data.plus
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class PageCacheTest {

    private val clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))

    private val dbStub = mockk<CharacterDatabase>()

    private val characterDaoStub = mockk<CharacterDao>()

    private val pageDaoMock = mockk<PageDao>()

    init {
        every { dbStub.characterDao() } returns characterDaoStub
        every { dbStub.pageDao() } returns pageDaoMock
    }

    @Test
    fun `constructor does not accept negative cache lifetime`() {
        assertThrows(IllegalArgumentException::class.java) {
            PageCache(dbStub, (-1).minutes, clock)
        }
    }

    @Test
    fun `hasFreshPages() returns false when there are no pages`() {
        val pageCache = PageCache(dbStub, 1.hours, clock)
        coEvery { pageDaoMock.lastUpdateInstant() } returns null

        val actual = runBlocking { pageCache.hasFreshPages() }

        assertFalse(actual)
        coVerify { pageDaoMock.lastUpdateInstant() }
        confirmVerified(pageDaoMock)
    }

    @Test
    fun `hasFreshPages() return false when cache update instant is in the future`() {
        val pageCache = PageCache(dbStub, 1.hours, clock)
        coEvery { pageDaoMock.lastUpdateInstant() } returns (clock + 1.minutes).instant()

        val actual = runBlocking { pageCache.hasFreshPages() }

        assertFalse(actual)
        coVerify { pageDaoMock.lastUpdateInstant() }
        confirmVerified(pageDaoMock)
    }

    @Test
    fun `hasFreshPages() returns false when there are stale pages`() {
        val pageCache = PageCache(dbStub, 1.hours, clock)
        coEvery { pageDaoMock.lastUpdateInstant() } returns (clock - 61.minutes).instant()

        val actual = runBlocking { pageCache.hasFreshPages() }

        assertFalse(actual)
        coVerify { pageDaoMock.lastUpdateInstant() }
        confirmVerified(pageDaoMock)
    }

    @Test
    fun `hasFreshPages() returns true when there are fresh pages`() {
        val pageCache = PageCache(dbStub, 1.hours, clock)
        coEvery { pageDaoMock.lastUpdateInstant() } returns (clock - 60.minutes).instant()

        val actual = runBlocking { pageCache.hasFreshPages() }

        assertTrue(actual)
        coVerify { pageDaoMock.lastUpdateInstant() }
        confirmVerified(pageDaoMock)
    }
}