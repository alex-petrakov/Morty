package me.alexpetrakov.morty.characterdetails.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val ARG_CHARACTER_ID = "CHARACTER_ID"
    }
}