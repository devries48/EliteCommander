package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.adapters.DiscoveriesRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentDiscoveriesBinding
import com.devries48.elitecommander.viewModels.MainViewModel

class DiscoveriesFragment : Fragment() {

    private val mViewModel: MainViewModel by navGraphViewModels(R.id.nav_main)
    private lateinit var mAdapter: DiscoveriesRecyclerAdapter

    private var _binding: FragmentDiscoveriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoveriesBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        val list = mViewModel.getCurrentDiscoveries()

        mAdapter = DiscoveriesRecyclerAdapter(list.value)
        binding.discoveriesRecyclerView.layoutManager = layoutManager
        binding.discoveriesRecyclerView.adapter = mAdapter

        list.observe(viewLifecycleOwner,
            { discoveries ->
                run {
                    mAdapter.updateList(discoveries)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}