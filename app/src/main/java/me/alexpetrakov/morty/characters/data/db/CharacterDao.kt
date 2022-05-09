package me.alexpetrakov.morty.characters.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM characters ORDER BY id")
    fun getAll(): PagingSource<Int, CharacterEntity>

    @Query("DELETE FROM characters")
    suspend fun deleteAll()
}