package me.alexpetrakov.morty.common.data.db.page

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface PageDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(pageEntity: PageEntity)

    @Query("SELECT * FROM pages WHERE pages.id = :id")
    suspend fun getById(id: Int): PageEntity?

    @Query("DELETE FROM pages")
    suspend fun deleteAll()
}