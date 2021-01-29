@file:Suppress("unused")

package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.FragmentDiscoveriesBinding

class DiscoveriesFragment : Fragment() {

    companion object {
        fun newInstance() = DiscoveriesFragment()
    }

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private lateinit var mBinding: FragmentDiscoveriesBinding

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

}