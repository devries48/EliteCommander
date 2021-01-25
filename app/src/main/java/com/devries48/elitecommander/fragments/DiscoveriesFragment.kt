@file:Suppress("unused")

package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R

class DiscoveriesFragment : Fragment() {

    companion object {
        fun newInstance() = DiscoveriesFragment()
    }

    private lateinit var viewModel: DiscoveriesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discoveries, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DiscoveriesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}