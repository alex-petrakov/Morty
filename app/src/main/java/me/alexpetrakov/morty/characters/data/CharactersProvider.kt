package me.alexpetrakov.morty.characters.data

import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import me.alexpetrakov.morty.characters.domain.Gender
import me.alexpetrakov.morty.characters.domain.VitalStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor() : CharactersRepository {

    override suspend fun getCharacters(): List<Character> {
        return listOf(
            Character(
                1,
                "Rick Sanchez",
                "Human",
                Gender.MALE,
                VitalStatus.ALIVE,
                "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
            ),
            Character(
                2,
                "Morty Smith",
                "Human",
                Gender.MALE,
                VitalStatus.ALIVE,
                "https://rickandmortyapi.com/api/character/avatar/2.jpeg"
            ),
        )
    }
}