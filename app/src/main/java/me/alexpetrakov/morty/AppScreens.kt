package me.alexpetrakov.morty

import com.github.terrakok.cicerone.androidx.FragmentScreen
import me.alexpetrakov.morty.characters.presentation.CharactersFragment

object AppScreens {

    fun characters() = FragmentScreen { CharactersFragment.newInstance() }

    fun characterDetails(): FragmentScreen = TODO("Not implemented")
}