package me.alexpetrakov.morty.characters.data

import me.alexpetrakov.morty.characters.data.network.CharacterJson
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val api: RickAndMortyApi
) : CharactersRepository {

    override suspend fun getCharacters(): List<Character> {
        return try {
            api.getCharacters(1).characters.map { it.toDomainModel() }
        } catch (e: HttpException) {
            emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }
}

private fun CharacterJson.toDomainModel(): Character {
    return Character(id, name, species, gender, vitalStatus, imageUrl)
}