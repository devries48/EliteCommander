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
import com.devries48.elitecommander.adapters.StatisticsRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentCommanderBinding
import com.devries48.elitecommander.viewModels.MainViewModel

class CommanderFragment : Fragment() {

    private val mViewModel: MainViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var mAdapter: StatisticsRecyclerAdapter

    private var _binding: FragmentCommanderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommanderBinding.inflate(inflater, container, false)
        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        val list = mViewModel.getMainStatistics()

        mAdapter = StatisticsRecyclerAdapter(list.value)
        binding.statsRecyclerView.layoutManager = layoutManager
        binding.statsRecyclerView.adapter = mAdapter

        list.observe(viewLifecycleOwner,
            { stats ->
                run {
                    mAdapter.updateList(stats)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}