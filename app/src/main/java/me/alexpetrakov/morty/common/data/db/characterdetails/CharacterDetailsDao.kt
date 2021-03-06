package me.alexpetrakov.morty.common.data.db.characterdetails

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface CharacterDetailsDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(character: CharacterDetailsEntity)

    @Query("SELECT * FROM character_details WHERE id = :id")
    suspend fun getById(id: Int): CharacterDetailsEntity?
}