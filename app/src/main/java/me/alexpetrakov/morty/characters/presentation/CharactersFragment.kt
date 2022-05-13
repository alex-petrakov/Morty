package me.alexpetrakov.morty.characters.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.databinding.FragmentCharactersBinding

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private val viewModel by viewModels<CharactersViewModel>()

    private var _binding: FragmentCharactersBinding? = null

    private val binding get() = _binding!!

    private val charactersAdapter = CharactersAdapter(this)

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
            adapter = charactersAdapter.withLoadStateHeaderAndFooter(
                header = CharactersLoadStateAdapter { charactersAdapter.retry() },
                footer = CharactersLoadStateAdapter { charactersAdapter.retry() },
            )
        }
        swipeRefreshLayout.apply {
            val surfaceColorAt3Dp = ElevationOverlayProvider(requireContext())
                .compositeOverlayWithThemeSurfaceColorIfNeeded(3f)
            setProgressBackgroundColorSchemeColor(surfaceColorAt3Dp)
            setColorSchemeResources(R.color.primary)
            setProgressViewEndTarget(true, progressViewEndOffset)
            setOnRefreshListener { charactersAdapter.refresh() }
        }
        binding.errorLayout.retryButton.setOnClickListener { charactersAdapter.refresh() }
    }

    private fun subscribeToModel(): Unit = with(viewModel) {
        viewModel.viewState
            .onEach(::render)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        val viewStates = charactersAdapter.loadStateFlow
            .map { CompoundPagingState.from(it) }
            .distinctUntilChanged()
            .map { pagingState ->
                ViewState.of(
                    pagingState,
                    isRefreshing = binding.swipeRefreshLayout.isRefreshing,
                    hasLocalData = charactersAdapter.itemCount > 0
                )
            }

        viewStates
            .onEach(::render)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewStates
            .withPrevious()
            .mapNotNull(::statesToViewEffect)
            .onEach(::handle)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun Flow<ViewState>.withPrevious(): Flow<Pair<ViewState?, ViewState>> {
        return this.onStart<ViewState?> { emit(null) }
            .zip(this) { prev, current -> prev to current }
    }

    private fun statesToViewEffect(states: Pair<ViewState?, ViewState>): ViewEffect? {
        val (prev, current) = states
        return if (prev != null && prev != current && current == ViewState.REFRESH_ERROR) {
            ViewEffect.DISPLAY_REFRESH_ERROR
        } else {
            null
        }
    }

    private fun render(viewState: PagingData<CharacterUiModel>) {
        charactersAdapter.submitData(viewLifecycleOwner.lifecycle, viewState)
    }

    private fun render(viewState: ViewState): Unit = with(binding) {
        if (viewState == ViewState.LOADING_FIRST_PAGE) progressBar.show() else progressBar.hide()

        swipeRefreshLayout.isRefreshing = viewState == ViewState.REFRESHING

        swipeRefreshLayout.isVisible = viewState == ViewState.REFRESHING ||
                viewState == ViewState.REFRESH_ERROR ||
                viewState == ViewState.CONTENT
        binding.errorLayout.root.isVisible = viewState == ViewState.ERROR
    }

    private fun handle(viewEffect: ViewEffect): Unit = with(binding) {
        when (viewEffect) {
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