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
import com.devries48.elitecommander.adapters.RowsRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentStatisticsBinding
import com.devries48.elitecommander.viewModels.MainViewModel
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class StatisticsFragment : Fragment() {

    private val mViewModel: MainViewModel by navGraphViewModels(R.id.nav_main)
    private lateinit var mCombatAdapter: RowsRecyclerAdapter
    private lateinit var mExplorationAdapter: RowsRecyclerAdapter
    private lateinit var mTradingAdapter: RowsRecyclerAdapter
    private lateinit var mPassengerAdapter: RowsRecyclerAdapter

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindCombatStats()
        bindExplorationStats()
        bindTradingStats()
        bindPassengerStats()
    }

    private fun bindCombatStats() {
        val list = mViewModel.getCombatStatistics()
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mCombatAdapter = RowsRecyclerAdapter(list.value!!)
        binding.combatRecyclerView.layoutManager = manager
        binding.combatRecyclerView.adapter = mCombatAdapter
    }

    private fun bindExplorationStats() {
        val list = mViewModel.getExplorationStatistics()
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mExplorationAdapter = RowsRecyclerAdapter(list.value!!)
        binding.explorationRecyclerView.layoutManager = manager
        binding.explorationRecyclerView.adapter = mExplorationAdapter
    }

    private fun bindTradingStats() {
        val list = mViewModel.getTradingStatistics()
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mTradingAdapter = RowsRecyclerAdapter(list.value!!)
        binding.tradingRecyclerView.layoutManager = manager
        binding.tradingRecyclerView.adapter = mTradingAdapter
    }

    private fun bindPassengerStats() {
        val list = mViewModel.getPassengerStatistics()
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mPassengerAdapter = RowsRecyclerAdapter(list.value!!)
        binding.passengersRecyclerView.layoutManager = manager
        binding.passengersRecyclerView.adapter = mPassengerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}