package me.alexpetrakov.morty.characterdetails.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.common.presentation.extensions.viewLifecycle
import me.alexpetrakov.morty.databinding.FragmentCharacterDetailsBinding

@AndroidEntryPoint
class CharacterDetailsFragment : Fragment() {

    private val viewModel by viewModels<CharacterDetailsViewModel>()

    private var _binding: FragmentCharacterDetailsBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
            .excludeTarget(R.id.toolbar, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
            .excludeTarget(R.id.toolbar, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()
        subscribeToModel()
    }

    private fun prepareView(): Unit = with(binding) {
        toolbar.setNavigationOnClickListener { viewModel.onNavigateUp() }
        errorLayout.retryButton.setOnClickListener { viewModel.onRetry() }
    }

    private fun subscribeToModel(): Unit = with(viewModel) {
        viewState
            .onEach(::render)
            .flowWithLifecycle(viewLifecycle)
            .launchIn(viewModelScope)
    }

    private fun render(state: ViewState): Unit = with(binding) {
        contentView.isVisible = state is ViewState.Content
        if (state == ViewState.Loading) progressBar.show() else progressBar.hide()
        errorLayout.root.isVisible = state == ViewState.Error

        if (state is ViewState.Content) {
            val character = state.character
            Glide.with(this@CharacterDetailsFragment)
                .load(character.imageUrl)
                .circleCrop()
                .transition(withCrossFade())
                .into(avatarImageView)
            nameTextView.text = character.name
            descriptionTextView.text = character.description
            originTile.setBody(character.origin)
            lastKnownLocationTile.setBody(character.lastKnownLocation)
            firstEpisodeTile.setBody(character.firstEpisode)
            episodeCountTile.setBody(character.episodeCount)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(characterId: Int): CharacterDetailsFragment {
            return CharacterDetailsFragment().apply {
                arguments = bundleOf(
                    CharacterDetailsViewModel.ARG_CHARACTER_ID to characterId
                )
            }
        }
    }
}