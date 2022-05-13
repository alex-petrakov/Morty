package me.alexpetrakov.morty.common

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.common.data.AndroidResourceProvider
import me.alexpetrakov.morty.common.domain.ResourceProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CommonDiModule {

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
    }
}