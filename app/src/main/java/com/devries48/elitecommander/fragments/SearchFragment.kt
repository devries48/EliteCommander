package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.devries48.elitecommander.adapters.ViewPagerAdapter
import com.devries48.elitecommander.adapters.ViewPagerTransform
import com.devries48.elitecommander.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var mAdapter: ViewPagerAdapter? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val fragmentList: ArrayList<Fragment> = arrayListOf(
            FindNearestFragment()
        )

        mAdapter = ViewPagerAdapter(
            fragmentList,
            childFragmentManager,
            lifecycle
        )

        setupViewPager()

        return binding.root
    }

    private fun setupViewPager() {
        val pager = binding.searchViewPager
        pager.setPageTransformer(ViewPagerTransform())
        pager.adapter = mAdapter

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // We're only interested when the pager offset is exactly centered. This
                // will help create a convincing illusion of two-way paging.
                if (positionOffsetPixels != 0) {
                    return
                }
                when (position) {
                    0 -> pager.setCurrentItem(mAdapter!!.itemCount - 2, false)
                    mAdapter!!.itemCount - 1 -> pager.setCurrentItem(1, false)
                }
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}