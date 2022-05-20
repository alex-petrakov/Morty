package me.alexpetrakov.morty.characters.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.characters.presentation.list.CharactersAdapter
import me.alexpetrakov.morty.common.presentation.extensions.viewLifecycle
import me.alexpetrakov.morty.common.presentation.extensions.viewLifecycleScope
import me.alexpetrakov.morty.databinding.FragmentCharactersBinding

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private val viewModel by viewModels<CharactersViewModel>()

    private var _binding: FragmentCharactersBinding? = null

    private val binding get() = _binding!!

    private val charactersAdapter = CharactersAdapter(
        this,
        onCharacterClick = { character -> viewModel.onCharacterClicked(character) },
        onRequestNextPage = { viewModel.onLoadNextPage() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
            .excludeTarget(R.id.toolbar, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
            .excludeTarget(R.id.toolbar, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()
        subscribeToModel()
    }

    private fun prepareView(): Unit = with(binding) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = charactersAdapter
        }
        swipeRefreshLayout.apply {
            val surfaceColorAt3Dp = ElevationOverlayProvider(requireContext())
                .compositeOverlayWithThemeSurfaceColorIfNeeded(3f)
            setProgressBackgroundColorSchemeColor(surfaceColorAt3Dp)
            setColorSchemeResources(R.color.primary)
            setProgressViewEndTarget(true, progressViewEndOffset)
            setOnRefreshListener { viewModel.onRefresh() }
        }
        binding.errorLayout.retryButton.setOnClickListener { viewModel.onRefresh() }
    }

    private fun subscribeToModel(): Unit = with(viewModel) {
        viewModel.viewState
            .onEach(::render)
            .flowWithLifecycle(viewLifecycle)
            .launchIn(viewLifecycleScope)
        viewModel.viewEffect
            .onEach(::handle)
            .flowWithLifecycle(viewLifecycle)
            .launchIn(viewLifecycleScope)
    }

    private fun render(viewState: ViewState): Unit = with(binding) {
        if (viewState == ViewState.Loading) progressBar.show() else progressBar.hide()
        swipeRefreshLayout.isRefreshing = viewState is ViewState.Refreshing
        swipeRefreshLayout.isVisible = viewState is ViewState.Refreshing ||
                viewState is ViewState.Content
        binding.errorLayout.root.isVisible = viewState is ViewState.Error
        when (viewState) {
            is ViewState.Content -> charactersAdapter.submitList(viewState.characters)
            is ViewState.Refreshing -> charactersAdapter.submitList(viewState.characters)
            else -> {} // Intentionally do nothing
        }
    }

    private fun handle(viewEffect: ViewEffect): Unit = with(binding) {
        when (viewEffect) {
            ViewEffect.DISPLAY_PAGE_LOAD_ERROR -> Snackbar.make(
                root,
                "Unable to load page",
                Snackbar.LENGTH_SHORT
            ).show()
            ViewEffect.DISPLAY_REFRESH_ERROR -> Snackbar.make(
                root,
                R.string.app_unable_to_refresh,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = CharactersFragment()
    }
}