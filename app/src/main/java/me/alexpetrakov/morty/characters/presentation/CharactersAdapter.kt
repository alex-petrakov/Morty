package me.alexpetrakov.morty.characters.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.alexpetrakov.morty.databinding.ItemCharacterBinding

class CharactersAdapter :
    ListAdapter<CharacterUiModel, CharactersAdapter.ViewHolder>(CharacterUiModel.DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCharacterBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCharacterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: CharacterUiModel): Unit = with(binding) {
            nameTextView.text = character.name
            descriptionTextView.text = character.description
        }
    }
}
