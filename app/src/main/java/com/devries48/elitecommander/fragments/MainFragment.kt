package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.adapters.ViewPagerAdapter
import com.devries48.elitecommander.adapters.ViewPagerTransform
import com.devries48.elitecommander.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var mAdapter: ViewPagerAdapter? = null
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val fragmentList = arrayListOf(
            CommanderFragment(),
            DiscoveriesFragment(),
            ProfitFragment(),
            StatisticsFragment()
        )

        mAdapter = ViewPagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        binding.viewPager.setPageTransformer(ViewPagerTransform())
        binding.viewPager.adapter = mAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}