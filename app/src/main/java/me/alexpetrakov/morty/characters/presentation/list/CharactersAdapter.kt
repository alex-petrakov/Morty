package me.alexpetrakov.morty.characters.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import me.alexpetrakov.morty.characters.presentation.CharacterUiModel
import me.alexpetrakov.morty.databinding.ItemCharacterBinding

class CharactersAdapter(
    private val fragment: Fragment,
    private val onCharacterClick: (character: CharacterUiModel) -> Unit
) : PagingDataAdapter<CharacterUiModel, CharactersAdapter.ViewHolder>(CharacterUiModel.DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCharacterBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(
            binding,
            fragment,
            onClick = { position -> getItem(position)?.let(onCharacterClick) }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCharacterBinding,
        private val fragment: Fragment,
        private val onClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(position)
                }
            }
        }

        fun bind(character: CharacterUiModel?): Unit = with(binding) {
            val imageUrl = character?.imageUrl ?: ""
            val name = character?.name ?: ""
            val description = character?.description ?: ""
            loadImage(imageUrl)
            nameTextView.text = name
            descriptionTextView.text = description
        }

        private fun ItemCharacterBinding.loadImage(imageUrl: String) {
            Glide.with(fragment)
                .load(imageUrl)
                .circleCrop()
                .transition(withCrossFade())
                .into(avatarImageView)
        }
    }
}
