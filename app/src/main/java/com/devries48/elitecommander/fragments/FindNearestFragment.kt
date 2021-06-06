package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.FragmentFindNearestBinding
import com.devries48.elitecommander.viewModels.SearchViewModel

class FindNearestFragment : Fragment() {

    private val mViewModel: SearchViewModel by navGraphViewModels(R.id.nav_search)

    private var _binding: FragmentFindNearestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindNearestBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}