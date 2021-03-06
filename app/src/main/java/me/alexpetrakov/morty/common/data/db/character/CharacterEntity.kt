package me.alexpetrakov.morty.common.data.db.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import me.alexpetrakov.morty.common.data.db.page.PageEntity
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.Gender
import me.alexpetrakov.morty.common.domain.model.VitalStatus

@Entity(
    tableName = "characters",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["url"],
            childColumns = ["page_url"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class CharacterEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "page_url", index = true) val pageUrl: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "species") val species: String,
    @ColumnInfo(name = "gender") val gender: Gender,
    @ColumnInfo(name = "vital_status") val vitalStatus: VitalStatus,
    @ColumnInfo(name = "image_url") val imageUrl: String
)

fun CharacterEntity.toDomainModel(): Character {
    return Character(id, name, species, gender, vitalStatus, imageUrl)
}