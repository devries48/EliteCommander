package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.CommanderFragmentBinding
import com.devries48.elitecommander.frontier.api.CommanderApi


class CommanderFragment : Fragment() {

    private lateinit var viewModel: CommanderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = context?.let { CommanderApi(it) }
        viewModel = ViewModelProvider(
            this,
            CommanderViewModelFactory(api)
        ).get(CommanderViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: CommanderFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.commander_fragment, container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner=this

        return binding.root
    }
}


