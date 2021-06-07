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
import com.devries48.elitecommander.databinding.FragmentFindNearestBinding
import com.devries48.elitecommander.viewModels.SearchViewModel

class FindNearestFragment : Fragment() {

    private val mViewModel: SearchViewModel by navGraphViewModels(R.id.nav_search)
    private lateinit var mStatisticAdapter: StatisticsRecyclerAdapter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindNearestFacilities()
    }

    private fun bindNearestFacilities() {
        val list = mViewModel.getNearestFacilities()
        if (list.value == null) {
            println("LOG: FindNearestFragment.getNearestFacilities is null!")
            return
        }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mStatisticAdapter = StatisticsRecyclerAdapter(list.value!!)
        binding.nearestRecyclerView.layoutManager = layoutManager
        binding.nearestRecyclerView.adapter = mStatisticAdapter

        list.observe(viewLifecycleOwner,
            { stats -> run { mStatisticAdapter.updateList(stats) } })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}