package me.alexpetrakov.morty.characters.data.db

import androidx.room.TypeConverter
import me.alexpetrakov.morty.characters.domain.Gender
import me.alexpetrakov.morty.characters.domain.VitalStatus

class Converters {

    @TypeConverter
    fun fromGender(gender: Gender): Int {
        return gender.id
    }

    @TypeConverter
    fun toGender(id: Int): Gender {
        return Gender.from(id)
    }

    @TypeConverter
    fun fromVitalStatus(vitalStatus: VitalStatus): Int {
        return vitalStatus.id
    }

    @TypeConverter
    fun toVitalStatus(id: Int): VitalStatus {
        return VitalStatus.from(id)
    }
}