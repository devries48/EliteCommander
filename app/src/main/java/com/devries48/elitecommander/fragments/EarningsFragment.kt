package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.FragmentEarningsBinding

class EarningsFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private var _binding: FragmentEarningsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}