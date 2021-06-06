package com.devries48.elitecommander.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.devries48.elitecommander.network.CommanderClient

class SearchViewModel : ViewModel() {

    private val mCommanderApi = CommanderClient.instance

    lateinit var currentSystem: LiveData<String>

    init {
        if (mCommanderApi != null) {
            currentSystem = mCommanderApi.currentSystem
        }

    }

}