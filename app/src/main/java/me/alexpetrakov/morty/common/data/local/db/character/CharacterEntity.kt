package me.alexpetrakov.morty.common.data.local.db.character

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import me.alexpetrakov.morty.common.data.local.db.page.PageEntity
import me.alexpetrakov.morty.common.domain.model.Character
import me.alexpetrakov.morty.common.domain.model.Gender
import me.alexpetrakov.morty.common.domain.model.VitalStatus

@Entity(
    tableName = "characters",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["id"],
            childColumns = ["page_id"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class CharacterEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "page_id", index = true) val pageId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "species") val species: String,
    @ColumnInfo(name = "gender") val gender: Gender,
    @ColumnInfo(name = "vital_status") val vitalStatus: VitalStatus,
    @ColumnInfo(name = "image_url") val imageUrl: String
)

fun CharacterEntity.toDomainModel(): Character {
    return Character(id, name, species, gender, vitalStatus, imageUrl)
}

fun Character.toEntity(pageId: Int): CharacterEntity {
    return CharacterEntity(id, pageId, name, species, gender, vitalStatus, imageUrl)
}