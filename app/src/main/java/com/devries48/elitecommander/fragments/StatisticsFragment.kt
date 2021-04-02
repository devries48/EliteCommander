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
import com.devries48.elitecommander.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var mCombatAdapter: StatisticsRecyclerAdapter

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

        val list = mViewModel.getCombatStatistics()

        val combatManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        mCombatAdapter = StatisticsRecyclerAdapter(list.value!!)
        binding.combatRecyclerView.layoutManager = combatManager
        binding.combatRecyclerView.adapter = mCombatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}