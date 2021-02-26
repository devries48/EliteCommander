package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.adapters.ViewPagerAdapter
import com.devries48.elitecommander.adapters.ViewPagerTransformer
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
        val view = binding.root

        val fragmentList = arrayListOf(
            RedirectFragment(),
            CommanderFragment(),
            DiscoveriesFragment(),
            EarningsFragment()
        )

        mAdapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.viewPager.setPageTransformer(ViewPagerTransformer())
        binding.viewPager.adapter = mAdapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun removeItem() {
        mAdapter?.removeItem(0)

    }
}