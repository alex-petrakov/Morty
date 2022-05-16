package me.alexpetrakov.morty.common.domain.repositories

interface ResourceProvider {

    fun getString(id: Int): String

    fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String
}