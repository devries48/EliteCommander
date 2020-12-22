package com.devries48.elitecommander.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.elitecommander.R

class CommanderFragment : Fragment() {

    companion object {
        fun newInstance() = CommanderFragment()
    }

    private lateinit var viewModel: CommanderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.commander_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommanderViewModel::class.java)
        // TODO: Use the ViewModel
    }

}