package me.alexpetrakov.morty.characters

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.characters.data.CharactersProvider
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CharactersDiModule {

    @Binds
    fun bindCharactersRepository(charactersProvider: CharactersProvider): CharactersRepository

    companion object {

        @Provides
        @Singleton
        fun provideRickAndMortyApi(retrofit: Retrofit): RickAndMortyApi {
            return retrofit.create(RickAndMortyApi::class.java)
        }
    }
}