package me.alexpetrakov.morty.characters.data.network

import me.alexpetrakov.morty.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Url

interface RickAndMortyApi {

    @GET
    suspend fun getCharacterPage(@Url pageUrl: String): CharacterPageJson

    @GET("character/")
    suspend fun getCharacterPage(): CharacterPageJson

    companion object {
        const val FIRST_CHARACTER_PAGE_URL = "${BuildConfig.API_BASE_URL}character/?page=1"
    }
}