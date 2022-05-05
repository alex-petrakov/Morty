package me.alexpetrakov.morty.characters

import androidx.paging.PagingSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.characters.data.CharactersProvider
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.Character
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CharactersDiModule {

    @Binds
    fun bindCharactersPagingSource(provider: CharactersProvider): PagingSource<String, Character>

    companion object {

        @Provides
        @Singleton
        fun provideRickAndMortyApi(retrofit: Retrofit): RickAndMortyApi {
            return retrofit.create(RickAndMortyApi::class.java)
        }
    }
}