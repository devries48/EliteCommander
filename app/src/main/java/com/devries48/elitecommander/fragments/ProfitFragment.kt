package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.futured.donut.DonutSection
import com.devries48.elitecommander.R
import com.devries48.elitecommander.adapters.ProfitsRecyclerAdapter
import com.devries48.elitecommander.adapters.RowsRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentProfitBinding
import com.devries48.elitecommander.viewModels.MainViewModel
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class ProfitFragment : Fragment() {

    private val mViewModel: MainViewModel by navGraphViewModels(R.id.nav_main)

    private lateinit var mStatisticAdapter: RowsRecyclerAdapter
    private lateinit var mProfitAdapter: ProfitsRecyclerAdapter

    private var _binding: FragmentProfitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfitBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindProfitList()
        bindProfitChart()
    }

    private fun bindProfitList() {
        val list = mViewModel.getProfitStatistics()
        if (list.value == null) {
            println("LOG: ProfitFragment.bindProfitList is null!")
            return
        }
        val statisticLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mStatisticAdapter = RowsRecyclerAdapter(list.value!!)
        binding.statsRecyclerView.layoutManager = statisticLayoutManager
        binding.statsRecyclerView.adapter = mStatisticAdapter

        list.observe(
            viewLifecycleOwner
        ) { stats -> run { mStatisticAdapter.updateList(stats) } }
    }

    private fun bindProfitChart() {
        val chartList = mViewModel.getProfitChart()
        val sortedSections = chartList.value?.filter { it.percentage > 0.0 }?.sortedByDescending { it.amount }
        val profitLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mProfitAdapter = ProfitsRecyclerAdapter(sortedSections)
        binding.profitsRecyclerView.layoutManager = profitLayoutManager
        binding.profitsRecyclerView.adapter = mProfitAdapter

        chartList.observe(
            viewLifecycleOwner
        ) {
            run {
                val sections = ArrayList<DonutSection>()

                chartList.value?.forEach {
                    sections.add(
                        DonutSection(
                            this.resources.getString(it.getTitle()), it.getColor(), it.percentage
                        )
                    )
                }

                binding.donutView.cap = 100f
                binding.donutView.submitData(sections)

                val sorted = chartList.value?.sortedByDescending { it.percentage }
                mProfitAdapter.updateList(sorted)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}