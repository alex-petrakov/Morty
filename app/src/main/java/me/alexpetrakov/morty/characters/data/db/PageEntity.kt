package me.alexpetrakov.morty.characters.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "next_page_url") val nextPageUrl: String?,
    @ColumnInfo(name = "previous_page_url") val previousPageUrl: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)