package me.alexpetrakov.morty.common.data.db.character

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM characters WHERE page_id = :pageId ORDER BY id")
    fun getAllByPageId(pageId: Int): List<CharacterEntity>

    @Query("DELETE FROM characters")
    suspend fun deleteAll()
}