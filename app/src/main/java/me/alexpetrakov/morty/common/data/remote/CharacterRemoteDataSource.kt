package me.alexpetrakov.morty.common.data.remote

import com.squareup.moshi.JsonDataException
import me.alexpetrakov.morty.common.data.Page
import me.alexpetrakov.morty.common.data.remote.network.CharacterPageJson
import me.alexpetrakov.morty.common.data.remote.network.RickAndMortyApi
import me.alexpetrakov.morty.common.data.remote.network.toDomainModel
import me.alexpetrakov.morty.common.domain.model.CharacterDetails
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRemoteDataSource @Inject constructor(private val api: RickAndMortyApi) {

    suspend fun getCharacterPage(id: Int): Page? {
        return try {
            val response = api.getCharacterPage(id)
            Page(
                id,
                response.characters.map { it.toDomainModel() },
                response.hasMorePages
            )
        } catch (e: Exception) {
            when (e) {
                is IOException, is HttpException, is JsonDataException -> null
                else -> throw e
            }
        }
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