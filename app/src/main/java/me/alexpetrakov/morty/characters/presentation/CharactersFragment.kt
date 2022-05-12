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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
        charactersAdapter.loadStateFlow
            .map { CompoundPagingState.from(it) }
            .distinctUntilChanged()
            .onEach(::render)
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun render(viewState: PagingData<CharacterUiModel>) {
        charactersAdapter.submitData(viewLifecycleOwner.lifecycle, viewState)
    }

    private fun render(pagingState: CompoundPagingState): Unit = with(binding) {
        val uiState = ViewState.of(
            pagingState,
            isRefreshing = swipeRefreshLayout.isRefreshing,
            hasLocalData = charactersAdapter.itemCount > 0
        )

        if (uiState == ViewState.LOADING_FIRST_PAGE) progressBar.show() else progressBar.hide()

        swipeRefreshLayout.isRefreshing = uiState == ViewState.REFRESHING

        swipeRefreshLayout.isVisible = uiState == ViewState.REFRESHING ||
                uiState == ViewState.REFRESH_ERROR ||
                uiState == ViewState.CONTENT
        binding.errorLayout.root.isVisible = uiState == ViewState.ERROR
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = CharactersFragment()
    }
}