package me.alexpetrakov.morty.characters

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.alexpetrakov.morty.characters.data.CharactersProvider
import me.alexpetrakov.morty.characters.domain.CharactersRepository

@Module
@InstallIn(SingletonComponent::class)
interface CharactersDiModule {
    @Binds
    fun bindCharactersRepository(charactersProvider: CharactersProvider): CharactersRepository
}