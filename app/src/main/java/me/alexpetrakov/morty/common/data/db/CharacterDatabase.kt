package me.alexpetrakov.morty.common.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.alexpetrakov.morty.common.data.db.character.CharacterDao
import me.alexpetrakov.morty.common.data.db.character.CharacterEntity
import me.alexpetrakov.morty.common.data.db.characterdetails.CharacterDetailsDao
import me.alexpetrakov.morty.common.data.db.characterdetails.CharacterDetailsEntity
import me.alexpetrakov.morty.common.data.db.page.PageDao
import me.alexpetrakov.morty.common.data.db.page.PageEntity

@Database(
    entities = [CharacterEntity::class, PageEntity::class, CharacterDetailsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CharacterDatabase : RoomDatabase() {

    abstract fun characterDao(): CharacterDao

    abstract fun pageDao(): PageDao

    abstract fun characterDetailsDao(): CharacterDetailsDao
}