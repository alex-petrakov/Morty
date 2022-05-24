package me.alexpetrakov.morty.common.data.local.db.page

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "has_next_page") val hasNextPage: Boolean,
    @ColumnInfo(name = "updated_at") val lastUpdateInstant: Instant
)