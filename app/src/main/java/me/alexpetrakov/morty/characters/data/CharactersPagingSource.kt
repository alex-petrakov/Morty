package me.alexpetrakov.morty.characters.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.alexpetrakov.morty.characters.data.network.CharacterJson
import me.alexpetrakov.morty.characters.data.network.CharacterPageJson
import me.alexpetrakov.morty.characters.data.network.RickAndMortyApi
import me.alexpetrakov.morty.characters.domain.Character
import retrofit2.HttpException
import java.io.IOException

class CharactersPagingSource(private val api: RickAndMortyApi) : PagingSource<String, Character>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Character> {
        return try {
            val page = loadCharacterPage(params.key)
            val pageInfo = page.pageInfo
            val characters = page.characters.toDomainModel()
            LoadResult.Page(characters, pageInfo.previousUrl, pageInfo.nextUrl)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: IOException) {
            LoadResult.Error(e)
        }
    }

    private suspend fun loadCharacterPage(pageUrl: String? = null): CharacterPageJson {
        return if (pageUrl == null) {
            api.getCharacterPage()
        } else {
            api.getCharacterPage(pageUrl)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Character>): String? {
        return null
    }
}

private suspend fun List<CharacterJson>.toDomainModel(): List<Character> {
    return withContext(Dispatchers.Default) {
        map { it.toDomainModel() }
    }
}

private fun CharacterJson.toDomainModel(): Character {
    return Character(id, name, species, gender, vitalStatus, imageUrl)
}