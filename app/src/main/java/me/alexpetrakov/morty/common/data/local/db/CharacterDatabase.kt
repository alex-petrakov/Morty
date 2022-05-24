package me.alexpetrakov.morty.common.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.alexpetrakov.morty.common.data.local.db.character.CharacterDao
import me.alexpetrakov.morty.common.data.local.db.character.CharacterEntity
import me.alexpetrakov.morty.common.data.local.db.characterdetails.CharacterDetailsDao
import me.alexpetrakov.morty.common.data.local.db.characterdetails.CharacterDetailsEntity
import me.alexpetrakov.morty.common.data.local.db.page.PageDao
import me.alexpetrakov.morty.common.data.local.db.page.PageEntity

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