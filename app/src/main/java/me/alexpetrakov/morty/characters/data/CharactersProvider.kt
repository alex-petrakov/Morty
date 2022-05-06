package me.alexpetrakov.morty.characters.data

import androidx.paging.PagingSource
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val api: RickAndMortyApi
) : CharactersRepository {

    override fun getCharacters(): PagingSource<String, Character> {
        return CharactersPagingSource(api)
    }
}