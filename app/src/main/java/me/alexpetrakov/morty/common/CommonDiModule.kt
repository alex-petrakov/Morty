package me.alexpetrakov.morty.common

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.common.data.AndroidResourceProvider
import me.alexpetrakov.morty.common.data.CharactersProvider
import me.alexpetrakov.morty.common.data.local.CacheLifetime
import me.alexpetrakov.morty.common.data.local.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.remote.network.RickAndMortyApi
import me.alexpetrakov.morty.common.domain.repositories.CharactersRepository
import me.alexpetrakov.morty.common.domain.repositories.ResourceProvider
import retrofit2.Retrofit
import java.time.Clock
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CommonDiModule {

    @Binds
    fun bindCharactersRepository(provider: CharactersProvider): CharactersRepository

    @Binds
    fun bindResourceProvider(androidResourceProvider: AndroidResourceProvider): ResourceProvider

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

        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()

        @Provides
        @CacheLifetime
        fun provideCacheLifetime(): Duration = Duration.ofHours(1)
    }
}