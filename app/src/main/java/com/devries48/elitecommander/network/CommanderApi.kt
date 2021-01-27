package com.devries48.elitecommander.network

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.devries48.elitecommander.App
import com.devries48.elitecommander.events.FrontierFleetEvent
import com.devries48.elitecommander.events.FrontierProfileEvent
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.events.FrontierShip
import com.devries48.elitecommander.models.FrontierJournal
import com.devries48.elitecommander.models.FrontierProfileResponse
import com.devries48.elitecommander.network.retrofit.EDApiRetrofit
import com.devries48.elitecommander.network.retrofit.FrontierRetrofit
import com.devries48.elitecommander.network.retrofit.RetrofitSingleton
import com.devries48.elitecommander.utils.NamingUtils
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.annotations.NotNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: https://stackoverflow.com/questions/43233025/use-retrofit-methods-more-expressive-way

class CommanderApi {

    private var mFrontierRetrofit: FrontierRetrofit? = null
    private var mEdApiRetrofit: EDApiRetrofit? = null
    private var mIsJournalParsed = MutableLiveData<Boolean>()
    private lateinit var mJournal: FrontierJournal

    init {
        mFrontierRetrofit = RetrofitSingleton.getInstance()
            ?.getFrontierRetrofit(App.getContext())

        mEdApiRetrofit = RetrofitSingleton.getInstance()
            ?.getEdApiRetrofit(App.getContext())
    }

    fun loadProfile() {
        val callback: Callback<ResponseBody?> = object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                @NotNull response: Response<ResponseBody?>
            ) {
                if (response.code() != 200) {
                    println("LOG: Error getting profile response - " + response.message())
                    return
                }

                // Parse as string and as body
                var profileResponse: FrontierProfileResponse? = null
                var rawResponse: JsonObject? = null

                try {
                    val responseString: String? = response.body()?.string()
                    rawResponse = JsonParser.parseString(responseString).asJsonObject
                    profileResponse = Gson().fromJson(
                        rawResponse,
                        FrontierProfileResponse::class.java
                    )
                } catch (e: Exception) {
                    onFailure(call, Exception("Invalid response"))
                }

                if (!response.isSuccessful || profileResponse == null) {
                    onFailure(call, Exception("Invalid response"))
                } else {
                    val frontierProfileEvent: FrontierProfileEvent

                    try {
                        val commanderName: String = profileResponse.commander?.name!!
                        val credits: Long = profileResponse.commander?.credits!!
                        val debt: Long = profileResponse.commander?.debt!!
                        val systemName = profileResponse.lastSystem?.name!!
                        val hull = profileResponse.ship?.health?.hull!!

                        frontierProfileEvent = profileResponse.commander?.let {
                            FrontierProfileEvent(
                                true,
                                commanderName,
                                credits,
                                debt,
                                systemName,
                                hull
                            )
                        }!!
                        sendResultMessage(frontierProfileEvent)

                        // FrontierFleetEvent
                        if (rawResponse != null) handleFleetParsing(this@CommanderApi, rawResponse)
                    } catch (ex: Exception) {
                        onFailure(call, Exception("Invalid response"))
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                val pos = FrontierProfileEvent(false, "", 0, 0, "", 0)
                val ranks = FrontierRanksEvent(
                    false, null, null,
                    null, null, null, null
                )
                val fleet = FrontierFleetEvent(false, ArrayList())
                sendResultMessage(pos)
                sendResultMessage(ranks)
                sendResultMessage(fleet)
            }
        }
        mFrontierRetrofit?.profileRaw?.enqueue(callback)
    }

    /**
     *  - Loads ranks from journal,capture FrontierRanksEvent for the result.
     */
    fun loadRanks() {
        if (mIsJournalParsed.value != true) {
            val observer: Observer<Boolean> = Observer<Boolean> { value ->
                if (value == true) {
                    sendResultMessage(mJournal.getRanks(App.getContext()))
                    return@Observer
                }
            }
            mIsJournalParsed.observeForever(observer)
            parseJournal()
        } else {
            sendResultMessage(mJournal.getRanks(App.getContext()))
        }
    }

    fun getDistanceToSol(systemName: String?) {
        DistanceCalculatorNetwork.getDistance(App.getContext(), "Sol", systemName)
    }

    private fun parseJournal() {
        val callback: Callback<ResponseBody?> = object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                @NotNull response: Response<ResponseBody?>
            ) {
                if (response.code() != 200) {
                    println("LOG: Error getting journal response - " + response.message())
                    return
                }

                mJournal = FrontierJournal()

                try {
                    val responseString: String? = response.body()?.string()
                    mJournal.parseResponse(responseString!!)
                } catch (e: Exception) {
                    println("LOG: Error parsing journal response - " + e.message)
                    return
                }
                mIsJournalParsed.postValue(true)
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                val pos = FrontierProfileEvent(false, "", 0, 0, "", 0)
                val fleet = FrontierFleetEvent(false, ArrayList())
                sendResultMessage(pos)
                sendResultMessage(fleet)
            }
        }
        if (mIsJournalParsed.value == true) mFrontierRetrofit?.journalRaw?.enqueue(callback)
    }

    private fun handleFleetParsing(commanderApi: CommanderApi, rawProfileResponse: JsonObject) {
        val currentShipId = rawProfileResponse["commander"]
            .asJsonObject["currentShipId"]
            .asInt

        // Sometimes the cAPI return an array, sometimes an object with indexes
        val responseList: MutableList<JsonElement> = ArrayList()
        if (rawProfileResponse["ships"].isJsonObject) {
            for ((_, value) in rawProfileResponse["ships"].asJsonObject.entrySet()) {
                responseList.add(value)
            }
        } else {
            for (ship in rawProfileResponse["ships"].asJsonArray) {
                responseList.add(ship)
            }
        }
        val shipsList: MutableList<FrontierShip> = ArrayList()
        for (entry in responseList) {
            val rawShip = entry.asJsonObject
            var shipName: String? = null
            if (rawShip.has("shipName")) {
                shipName = rawShip["shipName"].asString
            }
            val value = rawShip["value"].asJsonObject
            val isCurrentShip = rawShip["id"].asInt == currentShipId
            val newShip = FrontierShip(
                rawShip["id"].asInt,
                NamingUtils.getShipName(rawShip["name"].asString),
                shipName,
                rawShip["starsystem"].asJsonObject["name"].asString,
                rawShip["station"].asJsonObject["name"].asString,
                value["hull"].asLong,
                value["modules"].asLong,
                value["cargo"].asLong,
                value["total"].asLong,
                isCurrentShip
            )
            if (isCurrentShip) {
                shipsList.add(0, newShip)
            } else {
                shipsList.add(newShip)
            }
        }
        commanderApi.sendResultMessage(FrontierFleetEvent(true, shipsList))
    }

    private fun sendResultMessage(data: Any?) {
        EventBus.getDefault().post(data)
    }

}