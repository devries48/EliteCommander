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
import com.devries48.elitecommander.adapters.DiscoveriesRecyclerAdapter
import com.devries48.elitecommander.databinding.FragmentDiscoveriesBinding

class DiscoveriesFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var mBinding: FragmentDiscoveriesBinding
    private lateinit var mAdapter: DiscoveriesRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_discoveries, container, false
        )
        mBinding.viewModel = mViewModel
        mBinding.lifecycleOwner = this

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        val list = mViewModel.getCurrentDiscoveries()

        mAdapter = DiscoveriesRecyclerAdapter(list.value)
        mBinding.discoveriesRecyclerView.layoutManager = layoutManager
        mBinding.discoveriesRecyclerView.adapter = mAdapter

        list.observe(viewLifecycleOwner,
            { discoveries ->
                run {
                    mAdapter.updateList(discoveries)
                }
            })
    }

}