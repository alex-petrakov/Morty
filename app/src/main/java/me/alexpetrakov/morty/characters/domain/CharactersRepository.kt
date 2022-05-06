package me.alexpetrakov.morty.characters.domain

import androidx.paging.PagingSource

interface CharactersRepository {

    fun getCharacters(): PagingSource<String, Character>
}