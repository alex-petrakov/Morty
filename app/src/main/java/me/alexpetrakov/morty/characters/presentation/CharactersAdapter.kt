package me.alexpetrakov.morty.characters.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import me.alexpetrakov.morty.databinding.ItemCharacterBinding

class CharactersAdapter(
    private val fragment: Fragment
) : ListAdapter<CharacterUiModel, CharactersAdapter.ViewHolder>(CharacterUiModel.DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCharacterBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCharacterBinding,
        private val fragment: Fragment
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(character: CharacterUiModel): Unit = with(binding) {
            Glide.with(fragment)
                .load(character.imageUrl)
                .circleCrop()
                .transition(withCrossFade())
                .into(avatarImageView)
            nameTextView.text = character.name
            descriptionTextView.text = character.description
        }
    }
}
