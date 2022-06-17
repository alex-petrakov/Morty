package me.alexpetrakov.morty.common.data.remote

import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.alexpetrakov.morty.common.data.Page
import me.alexpetrakov.morty.common.data.remote.network.CharacterPageJson
import me.alexpetrakov.morty.common.data.remote.network.RickAndMortyApi
import me.alexpetrakov.morty.common.data.remote.network.toDomainModel
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRemoteDataSource @Inject constructor(private val api: RickAndMortyApi) {

    suspend fun getCharacterPage(id: Int): Page? {
        return try {
            val pageNumbers = getPageNumberRange(id)
            val pages = coroutineScope { loadPages(pageNumbers) }
                .dropLastWhile { response -> response.code() == 404 }
                .map { response ->
                    response.body() ?: throw JsonDataException("Unable to parse body")
                }
            if (pages.isEmpty()) {
                return null
            }
            val characters = pages
                .flatMap { page -> page.characters }
                .map { character -> character.toDomainModel() }
            val hasMorePages = pages.last().hasMorePages
            Page(id, characters, hasMorePages)
        } catch (e: Exception) {
            when (e) {
                is IOException, is JsonDataException -> null
                else -> throw e
            }
        }
    }

    private suspend fun loadPages(pageNumbers: IntRange): List<Response<CharacterPageJson>> {
        return coroutineScope {
            pageNumbers.map { number ->
                async { api.getCharacterPage(number) }
            }.awaitAll()
        }
    }

    private fun getPageNumberRange(pageId: Int): IntRange {
        val last = pageId * 3
        val first = last - 2
        return first..last
    }

    private val CharacterPageJson.hasMorePages: Boolean get() = pageInfo.nextUrl != null

    suspend fun getCharacterDetails(id: Int): CharacterDetails? {
        return try {
            val detailsJson = api.getCharacterDetails(id)
            val episodeJson = api.getEpisode(detailsJson.episodeUrls[0])
            detailsJson.toDomainModel(episodeJson.toDomainModel())
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException, is JsonDataException -> null
                else -> throw e
            }
        }
    }
}