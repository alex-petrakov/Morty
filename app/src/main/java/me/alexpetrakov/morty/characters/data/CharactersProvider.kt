package me.alexpetrakov.morty.characters.data

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.db.CharacterEntity
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharactersProvider @Inject constructor(
    private val api: RickAndMortyApi,
    private val db: CharacterDatabase
) : CharactersRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getCharacters(): Flow<PagingData<Character>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = true,
                initialLoadSize = PRELOADED_PAGE_COUNT * DEFAULT_PAGE_SIZE
            ),
            remoteMediator = CharactersRemoteMediator(api, db),
            pagingSourceFactory = { db.characterDao().getAll() }
        ).flow.map { data -> data.map { it.toDomainModel() } }
    }

    private fun CharacterEntity.toDomainModel(): Character {
        return Character(id, name, species, gender, vitalStatus, imageUrl)
    }

    override suspend fun getCharacter(id: Int): CharacterDetails? {
        return CharacterDetails(
            1,
            "Rick Sanchez",
            "Human",
            Gender.MALE,
            VitalStatus.ALIVE,
            "Earth",
            "Citadel of Ricks",
            Episode(1, "Pilot", "S01E01"),
            51,
            "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
        )
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PRELOADED_PAGE_COUNT = 3
    }
}