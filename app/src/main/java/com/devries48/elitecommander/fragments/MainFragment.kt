package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.adapters.ViewPagerAdapter
import com.devries48.elitecommander.adapters.ViewPagerTransformer
import kotlinx.android.synthetic.main.fragment_main.view.*  // Bind view

class MainFragment : Fragment() {

    private var mAdapter: ViewPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

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
        view.viewPager.setPageTransformer(ViewPagerTransformer())
        view.viewPager.adapter = mAdapter

        return view
    }

    fun removeItem() {
        mAdapter?.removeItem(0)

    }
}