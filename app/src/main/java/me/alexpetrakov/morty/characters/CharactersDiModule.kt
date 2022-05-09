package me.alexpetrakov.morty.characters

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.characters.data.CharactersProvider
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CharactersDiModule {

    @Binds
    fun bindCharactersRepository(provider: CharactersProvider): CharactersRepository

    companion object {

        @Provides
        @Singleton
        fun provideRickAndMortyApi(retrofit: Retrofit): RickAndMortyApi {
            return retrofit.create(RickAndMortyApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCharacterDatabase(@ApplicationContext context: Context): CharacterDatabase {
            return Room.databaseBuilder(context, CharacterDatabase::class.java, "morty.db")
                .build()
        }
    }
}