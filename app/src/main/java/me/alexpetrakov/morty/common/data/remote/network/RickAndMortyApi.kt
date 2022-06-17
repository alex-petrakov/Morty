package me.alexpetrakov.morty.common.data.remote.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface RickAndMortyApi {

    @GET("character/")
    suspend fun getCharacterPage(@Query("page") page: Int): retrofit2.Response<CharacterPageJson>

    @GET("character/{id}")
    suspend fun getCharacterDetails(@Path("id") id: Int): CharacterDetailsJson

    @GET
    suspend fun getEpisode(@Url episodeUrl: String): EpisodeJson
}