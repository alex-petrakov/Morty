package me.alexpetrakov.morty.characters.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import me.alexpetrakov.morty.databinding.ItemLoadStateBinding

class CharactersLoadStateAdapter(
    private val onRetry: () -> Unit
) : LoadStateAdapter<CharactersLoadStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLoadStateBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onRetry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class ViewHolder(
        private val binding: ItemLoadStateBinding,
        private val onRetry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.errorLayout.retryButton.setOnClickListener { onRetry() }
        }

        fun bind(loadState: LoadState): Unit = with(binding) {
            errorLayout.root.isVisible = loadState is LoadState.Error
            progressBar.isVisible = loadState is LoadState.Loading
        }
    }
}