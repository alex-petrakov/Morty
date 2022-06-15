package me.alexpetrakov.morty.characters.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.characters.presentation.CharacterUiModel
import me.alexpetrakov.morty.characters.presentation.ListItem
import me.alexpetrakov.morty.characters.presentation.LoadIndicator
import me.alexpetrakov.morty.databinding.ItemCharacterBinding
import me.alexpetrakov.morty.databinding.ItemPageLoadIndicatorBinding

class CharactersAdapter(
    private val fragment: Fragment,
    private val onCharacterClick: (character: CharacterUiModel) -> Unit,
    private val onRequestNextPage: () -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(ListItem.DiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_character -> {
                val binding = ItemCharacterBinding.inflate(layoutInflater, parent, false)
                CharacterViewHolder(
                    binding,
                    fragment,
                    onClick = { position ->
                        onCharacterClick.invoke(getItem(position) as CharacterUiModel)
                    }
                )
            }
            R.layout.item_page_load_indicator -> {
                val binding = ItemPageLoadIndicatorBinding.inflate(layoutInflater, parent, false)
                LoadIndicatorViewHolder(binding)
            }
            else -> throw IllegalStateException("Unexpected view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == itemCount - 10) {
            onRequestNextPage()
        }
        when (holder) {
            is CharacterViewHolder -> holder.bind(getItem(position) as CharacterUiModel)
            is LoadIndicatorViewHolder -> {}
            else -> throw IllegalStateException("Unexpected view holder type ${holder::class}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CharacterUiModel -> R.layout.item_character
            is LoadIndicator -> R.layout.item_page_load_indicator
            else -> throw IllegalStateException(
                "Unexpected item type ${getItem(position)::class} at position $position"
            )
        }
    }

    class CharacterViewHolder(
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

        fun bind(character: CharacterUiModel): Unit = with(binding) {
            val imageUrl = character.imageUrl
            val name = character.name
            val description = character.description
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

    class LoadIndicatorViewHolder(
        binding: ItemPageLoadIndicatorBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
