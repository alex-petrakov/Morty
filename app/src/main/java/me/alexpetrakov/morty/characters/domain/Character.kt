package me.alexpetrakov.morty.characters.domain

data class Character(
    val id: Int,
    val name: String,
    val species: String,
    val gender: Gender,
    val vitalStatus: VitalStatus,
    val imageUrl: String
)

enum class Gender {
    MALE, FEMALE, GENDERLESS, UNKNOWN
}

enum class VitalStatus {
    ALIVE, DEAD, UNKNOWN
}