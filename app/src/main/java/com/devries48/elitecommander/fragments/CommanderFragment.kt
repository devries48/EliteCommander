package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.adapters.StatisticsRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentCommanderBinding

class CommanderFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var mBinding: FragmentCommanderBinding
    private lateinit var mAdapter: StatisticsRecyclerAdapter

    // This property is only valid between onCreateView and  onDestroyView.
    private val binding get() = mBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_commander, container, false
        )
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        val list = mViewModel.getMainStatistics()

        mAdapter = StatisticsRecyclerAdapter(list.value!!)
        mBinding.statsRecyclerView.layoutManager = layoutManager
        mBinding.statsRecyclerView.adapter = mAdapter

        list.observe(viewLifecycleOwner,
            { stats ->
                run {
                    mAdapter.updateList(stats)
                }
            })
    }

}


