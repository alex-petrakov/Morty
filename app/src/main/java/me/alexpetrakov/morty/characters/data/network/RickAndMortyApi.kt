package me.alexpetrakov.morty.characters.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface RickAndMortyApi {

    @GET("character/")
    suspend fun getCharacters(@Query("page") page: Int): CharacterPageJson
}