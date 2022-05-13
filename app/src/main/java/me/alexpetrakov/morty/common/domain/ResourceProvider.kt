package me.alexpetrakov.morty.common.domain

interface ResourceProvider {
    fun getString(id: Int): String
}