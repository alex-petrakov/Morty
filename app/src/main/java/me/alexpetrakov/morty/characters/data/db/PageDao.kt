package me.alexpetrakov.morty.characters.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PageDao {

    @Insert
    suspend fun insert(pageEntity: PageEntity)

    @Query("SELECT * FROM pages WHERE id = (SELECT page_id FROM characters WHERE id = :characterId)")
    suspend fun getByCharacterId(characterId: Int): PageEntity?

    @Query("DELETE FROM pages")
    suspend fun deleteAll()
}