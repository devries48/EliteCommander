package com.devries48.elitecommander.network

import com.devries48.elitecommander.App
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.FrontierFleetEvent
import com.devries48.elitecommander.events.FrontierProfileEvent
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.events.FrontierShip
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.FrontierJournal
import com.devries48.elitecommander.models.response.FrontierProfileResponse
import com.devries48.elitecommander.utils.NamingUtils
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class CommanderNetwork {

    private var mFrontierApi: FrontierInterface? = null
    private var mJournalWorker: JournalWorker? = null
    private lateinit var mJournal: FrontierJournal

    private var mIsJournalParsed by Delegates.observable(false) { _, _, newValue ->
        if (newValue) loadCurrentJournal()
    }

    init {
        mFrontierApi = RetrofitClient.getInstance()?.getFrontierRetrofit(App.getContext())
        mJournalWorker = JournalWorker(mFrontierApi)
    }

    /**
     *  - Loads profile,capture FrontierProfileEvent for the result.
     */
    fun loadProfile() {
        mFrontierApi?.profileRaw?.enqueueWrap {
            onResponse = response@{
                if (it.code() != 200) {
                    onFailure?.let { it1 -> it1(Exception(it.code().toString())) }
                    return@response
                }

                val profileResponse: FrontierProfileResponse?
                val rawResponse: JsonObject?

                try {
                    val responseString: String? = it.body()?.string()
                    rawResponse = JsonParser.parseString(responseString).asJsonObject
                    profileResponse = Gson().fromJson(
                        rawResponse,
                        FrontierProfileResponse::class.java
                    )
                } catch (e: Exception) {
                    onFailure?.let { it1 -> it1(e) }
                    return@response
                }

                if (!it.isSuccessful || profileResponse == null) {
                    onFailure?.let { it1 -> it1(Exception("Empty profile response")) }
                    return@response
                }

                val frontierProfileEvent: FrontierProfileEvent

                try {
                    val commanderName: String = profileResponse.commander?.name!!
                    val credits: Long = profileResponse.commander?.credits!!
                    val debt: Long = profileResponse.commander?.debt!!
                    val systemName = profileResponse.lastSystem?.name!!
                    val hull = profileResponse.ship?.health?.hull!!
                    val integrity = 1000000 - profileResponse.ship?.health?.integrity!!

                    frontierProfileEvent = profileResponse.commander?.let {
                        FrontierProfileEvent(
                            true,
                            commanderName,
                            credits,
                            debt,
                            systemName,
                            hull,
                            integrity
                        )
                    }!!
                    sendResultMessage(frontierProfileEvent)

                    // FrontierFleetEvent
                    if (rawResponse != null) handleFleetParsing(this@CommanderNetwork, rawResponse)
                } catch (ex: Exception) {
                    onFailure?.let { it1 -> it1(Exception(ex)) }
                }
            }
            onFailure = {
                println(it?.message)
                val pos = FrontierProfileEvent(false, "", 0, 0, "", 0, 0)
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
    }

    /**
     *  - Loads ranks from journal,capture FrontierRanksEvent for the result.
     *  - Loads discoveries from journal, capture FrontierDiscoveriesEvent for the result.
     */
    fun loadCurrentJournal() {
        if (mIsJournalParsed) {
            sendResultMessage(mJournal.getCurrentDiscoveries())
        } else {
            mJournalWorker?.getCurrentJournal()
        }
    }

 /*   private suspend fun loadJournal() {
        mFrontierApi?.getJournal("2021/01/14")?.enqueueWrap {
            onResponse = {
                if (it.code() != 200) {
                    onFailure?.let { it1 -> it1(Exception(it.code().toString())) }
                } else {
                    mJournal = FrontierJournal()

                    try {
                        val responseString: String? = it.body()?.string()
                        //mJournal.parseResponse(responseString!!)
                        mIsJournalParsed = true
                    } catch (e: Exception) {
                        onFailure?.let { it1 -> it1(Exception(it.code().toString())) }
                    }
                }
            }
            onFailure = {
                println("LOG: Response failure - " + it?.message)
            }
        }
    }
*/
    fun getDistanceToSol(systemName: String?) {
        if (systemName != null) {
            DistanceCalculatorNetwork.getDistanceToSol(App.getContext(), systemName)
        }
    }

    private fun handleFleetParsing(commanderNetwork: CommanderNetwork, rawProfileResponse: JsonObject) {
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
        commanderNetwork.sendResultMessage(FrontierFleetEvent(true, shipsList))
    }

    private fun sendResultMessage(data: Any?) {
        EventBus.getDefault().post(data)
    }

}
