package me.alexpetrakov.morty.characters.data

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.alexpetrakov.morty.characters.data.db.CharacterDatabase
import me.alexpetrakov.morty.characters.data.db.CharacterEntity
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.Character
import me.alexpetrakov.morty.characters.domain.CharactersRepository
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

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PRELOADED_PAGE_COUNT = 3
    }
}