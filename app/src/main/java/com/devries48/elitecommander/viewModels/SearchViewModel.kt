package com.devries48.elitecommander.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.default
import com.devries48.elitecommander.models.RowBuilder
import com.devries48.elitecommander.models.RowModel
import com.devries48.elitecommander.network.CommanderClient
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SearchViewModel : ViewModel() {

    private val mCommanderApi = CommanderClient.instance
    private val mBuilderNearest: RowBuilder = RowBuilder()
    private val mIsNearestBusy = MutableLiveData<Boolean>().default(false)

    var currentSystem: LiveData<String> = mCommanderApi.currentSystem
    var isNearestBusy: MutableLiveData<Boolean> = mIsNearestBusy

    fun getNearestFacilities(): LiveData<List<RowModel>> {
        mBuilderNearest.addSearch(
            RowBuilder.SearchType.NEAREST_INTERSTELLAR,
            RowBuilder.RowPosition.LEFT,
            R.string.Interstellar_Factor,
            "TEST LY"
        )
        mBuilderNearest.post()

        return mBuilderNearest.rows
    }

}