package me.alexpetrakov.morty.characters.data.network

import retrofit2.http.GET
import retrofit2.http.Url

interface RickAndMortyApi {

    @GET
    suspend fun getCharacterPage(@Url pageUrl: String): CharacterPageJson

    @GET("character/")
    suspend fun getCharacterPage(): CharacterPageJson
}