package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devries48.elitecommander.R
import com.devries48.elitecommander.adapters.StatisticsRecyclerAdapter
import com.devries48.elitecommander.databinding.CommanderFragmentBinding
import com.devries48.elitecommander.frontier.api.CommanderApi

class CommanderFragment : Fragment() {

    private lateinit var mViewModel: CommanderViewModel
    private lateinit var mBinding: CommanderFragmentBinding
    private lateinit var mAdapter: StatisticsRecyclerAdapter

    // This property is only valid between onCreateView and  onDestroyView.
    private val binding get() = mBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = context?.let { CommanderApi(it) }
        mViewModel = ViewModelProvider(
            this,
            CommanderViewModelFactory(api)
        ).get(CommanderViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.commander_fragment, container, false
        )
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)

        mAdapter = StatisticsRecyclerAdapter(mViewModel.getMainStatistics())
        mBinding.statsRecyclerView.layoutManager= layoutManager
        mBinding.statsRecyclerView.adapter = mAdapter
    }
}


