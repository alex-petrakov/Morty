package me.alexpetrakov.morty.common.presentation.mappers

import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.common.domain.Gender
import me.alexpetrakov.morty.common.domain.ResourceProvider
import me.alexpetrakov.morty.common.domain.VitalStatus

fun VitalStatus.toUiModel(resourceProvider: ResourceProvider): String {
    val id = when (this) {
        VitalStatus.ALIVE -> R.string.app_alive
        VitalStatus.DEAD -> R.string.app_dead
        VitalStatus.UNKNOWN -> R.string.app_unknown
    }
    return resourceProvider.getString(id)
}

fun Gender.toUiModel(resourceProvider: ResourceProvider): String {
    val id = when (this) {
        Gender.MALE -> R.string.app_male
        Gender.FEMALE -> R.string.app_female
        Gender.GENDERLESS -> R.string.app_genderless
        Gender.UNKNOWN -> R.string.app_unknown
    }
    return resourceProvider.getString(id)
}