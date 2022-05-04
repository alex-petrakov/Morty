package me.alexpetrakov.morty.characters.domain

interface CharactersRepository {

    suspend fun getCharacters(): List<Character>
}