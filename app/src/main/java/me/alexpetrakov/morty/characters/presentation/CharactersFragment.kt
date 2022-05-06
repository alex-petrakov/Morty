package me.alexpetrakov.morty.characters.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
            adapter = charactersAdapter
        }
    }

    private fun subscribeToModel(): Unit = with(viewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewState.collectLatest(::render)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            charactersAdapter.loadStateFlow.collectLatest(::render)
        }
        charactersAdapter.loadStateFlow
    }

    private fun render(viewState: PagingData<CharacterUiModel>) {
        charactersAdapter.submitData(viewLifecycleOwner.lifecycle, viewState)
    }

    private fun render(loadStates: CombinedLoadStates): Unit = with(binding) {
        val refreshState = loadStates.refresh
        if (refreshState is LoadState.Loading) progressBar.show() else progressBar.hide()
        recyclerView.isVisible = refreshState is LoadState.NotLoading
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = CharactersFragment()
    }
}