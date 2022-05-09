package me.alexpetrakov.morty.characters.domain

data class Character(
    val id: Int,
    val name: String,
    val species: String,
    val gender: Gender,
    val vitalStatus: VitalStatus,
    val imageUrl: String
)

enum class Gender(val id: Int) {
    MALE(0),
    FEMALE(1),
    GENDERLESS(2),
    UNKNOWN(3);

    companion object {
        fun from(id: Int): Gender {
            return values().find { it.id == id }
                ?: throw IllegalArgumentException("Unexpected id: $id")
        }
    }
}

enum class VitalStatus(val id: Int) {
    ALIVE(0),
    DEAD(1),
    UNKNOWN(2);

    companion object {
        fun from(id: Int): VitalStatus {
            return values().find { it.id == id }
                ?: throw IllegalArgumentException("Unexpected id: $id")
        }
    }
}