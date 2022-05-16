package me.alexpetrakov.morty.common

import android.content.Context
import androidx.room.Room
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.common.data.AndroidResourceProvider
import me.alexpetrakov.morty.common.data.CharactersProvider
import me.alexpetrakov.morty.common.data.db.CharacterDatabase
import me.alexpetrakov.morty.common.data.network.RickAndMortyApi
import me.alexpetrakov.morty.common.domain.CharactersRepository
import me.alexpetrakov.morty.common.domain.ResourceProvider
import retrofit2.Retrofit
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
        fun provideCicerone(): Cicerone<Router> = Cicerone.create()

        @Provides
        @Singleton
        fun providerRouter(cicerone: Cicerone<Router>): Router = cicerone.router

        @Provides
        @Singleton
        fun provideNavigatorHolder(cicerone: Cicerone<Router>): NavigatorHolder =
            cicerone.getNavigatorHolder()

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