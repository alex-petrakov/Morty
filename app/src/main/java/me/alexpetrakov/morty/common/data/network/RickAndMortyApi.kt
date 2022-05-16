package me.alexpetrakov.morty.common.data.network

import me.alexpetrakov.morty.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface RickAndMortyApi {

    @GET
    suspend fun getCharacterPage(@Url pageUrl: String): CharacterPageJson

    @GET("character/{id}")
    suspend fun getCharacterDetails(@Path("id") id: Int): CharacterDetailsJson

    @GET
    suspend fun getEpisode(@Url episodeUrl: String): EpisodeJson

    companion object {
        const val FIRST_CHARACTER_PAGE_URL = "${BuildConfig.API_BASE_URL}character/?page=1"
    }
}