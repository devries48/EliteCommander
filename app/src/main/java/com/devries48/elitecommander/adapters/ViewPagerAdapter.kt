package com.devries48.elitecommander.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(list: ArrayList<Fragment>, fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    private var mFragmentManager: FragmentManager? = null
    private val mFragmentList = list.prepareForTwoWayPaging()

    init {
        mFragmentManager = fm
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getItemId(position: Int): Long {
        return mFragmentList[position].hashCode().toLong()
    }

    private fun <T> List<T>.prepareForTwoWayPaging(): List<T> {
        val first = first()
        val last = last()
        return toMutableList().apply {
            add(0, last)
            add(first)
        }
    }
}