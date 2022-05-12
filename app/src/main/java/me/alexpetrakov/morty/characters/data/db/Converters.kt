package me.alexpetrakov.morty.characters.data.db

import androidx.room.TypeConverter
import me.alexpetrakov.morty.characters.domain.Gender
import me.alexpetrakov.morty.characters.domain.VitalStatus
import java.time.Instant

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

    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.epochSecond
    }

    @TypeConverter
    fun toInstant(secondsSinceEpoch: Long): Instant {
        return Instant.ofEpochSecond(secondsSinceEpoch)
    }
}