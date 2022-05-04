package me.alexpetrakov.morty.characters.presentation

import androidx.recyclerview.widget.DiffUtil

data class ViewState(val characters: List<CharacterUiModel>)

data class CharacterUiModel(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String
) {

    object DiffUtilCallback : DiffUtil.ItemCallback<CharacterUiModel>() {
        override fun areItemsTheSame(old: CharacterUiModel, new: CharacterUiModel): Boolean {
            return old.id == new.id
        }

        override fun areContentsTheSame(old: CharacterUiModel, new: CharacterUiModel): Boolean {
            return old == new
        }
    }
}