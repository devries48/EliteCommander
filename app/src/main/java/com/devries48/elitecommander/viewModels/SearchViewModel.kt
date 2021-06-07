package com.devries48.elitecommander.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devries48.elitecommander.declarations.default
import com.devries48.elitecommander.models.StatisticModel
import com.devries48.elitecommander.models.StatisticsBuilder
import com.devries48.elitecommander.network.CommanderClient

class SearchViewModel : ViewModel() {

    private val mCommanderApi = CommanderClient.instance
    private val mBuilderNearest: StatisticsBuilder = StatisticsBuilder()
    private val mIsNearestBusy = MutableLiveData<Boolean>().default(false)

    lateinit var currentSystem: LiveData<String>
    var isNearestBusy: MutableLiveData<Boolean> = mIsNearestBusy

    init {
        if (mCommanderApi != null) {
            currentSystem = mCommanderApi.currentSystem
        }

    }

    fun getNearestFacilities(): LiveData<List<StatisticModel>> {
        return mBuilderNearest.statistics
    }


}