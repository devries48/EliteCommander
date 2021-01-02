package com.devries48.elitecommander.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devries48.elitecommander.frontier.api.CommanderApi

class CommanderViewModel(api: CommanderApi?) : ViewModel() {

    private val _name = MutableLiveData("Unknown")
    private val _location = MutableLiveData("Unknown")

    val name:LiveData<String> =_name
    val location: LiveData<String> =_location

    init {
        _name.value= "Bassie van Toor"
        _location.value= "vlijmen"
        //api.getCommanderStatus()
    }
}

// Override ViewModelProvider.NewInstanceFactory to create the ViewModel (VM).
class CommanderViewModelFactory(private val api: CommanderApi?): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = CommanderViewModel(api) as T
}
