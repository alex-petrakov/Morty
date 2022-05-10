package me.alexpetrakov.morty.characters.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface PageDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(pageEntity: PageEntity)

    @Query("SELECT * FROM pages WHERE url = (SELECT page_url FROM characters WHERE id = :characterId)")
    suspend fun getByCharacterId(characterId: Int): PageEntity?

    @Query("DELETE FROM pages")
    suspend fun deleteAll()
}