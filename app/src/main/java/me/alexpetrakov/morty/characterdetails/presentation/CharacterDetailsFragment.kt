package me.alexpetrakov.morty.characterdetails.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.alexpetrakov.morty.databinding.FragmentCharacterDetailsBinding

@AndroidEntryPoint
class CharacterDetailsFragment : Fragment() {

    private val viewModel by viewModels<CharacterDetailsViewModel>()

    private var _binding: FragmentCharacterDetailsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)
        return binding.root
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