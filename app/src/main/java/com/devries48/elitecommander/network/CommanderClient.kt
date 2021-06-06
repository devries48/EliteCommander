package com.devries48.elitecommander.network

import androidx.lifecycle.MutableLiveData
import com.devries48.elitecommander.App
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.FrontierFleetEvent
import com.devries48.elitecommander.events.FrontierProfileEvent
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.events.FrontierShip
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.response.frontier.Profile
import com.devries48.elitecommander.network.journal.JournalWorker
import com.devries48.elitecommander.utils.NamingUtils
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

@DelicateCoroutinesApi
class CommanderClient private constructor() {

    private object HOLDER {
        val INSTANCE = CommanderClient()
    }

    companion object {
        val instance: CommanderClient by lazy { HOLDER.INSTANCE }
    }

    private var mFrontierApi: FrontierInterface? = null
    private var mJournalWorker: JournalWorker? = null

    val currentSystem = MutableLiveData("")

    init {
        mFrontierApi = RetrofitClient.getInstance()?.getFrontierRetrofit(App.getContext())
        mJournalWorker = JournalWorker(mFrontierApi)
    }

    /**
     *  - Loads profile & fleet, capture FrontierProfileEvent & FrontierFleetEvent for the result.
     */
    fun loadProfile() {
        mFrontierApi?.profileRaw?.enqueueWrap {
            onResponse = {
                handleProfileResponse(it)
            }
            onFailure = {
                handleProfileFailure(it)
            }
        }
    }

    // 401 error will be redirected to the login fragment
    private fun handleProfileResponse(res: Response<ResponseBody?>) {
        try {
            if (res.code() == 401) return
            if (res.code() != 200) throw Exception(res.code().toString())

            val profile: Profile?
            val rawResponse: JsonObject?
            val responseString: String? = res.body()?.string()

            rawResponse = JsonParser.parseString(responseString).asJsonObject
            profile = Gson().fromJson(
                rawResponse,
                Profile::class.java
            )

            if (!res.isSuccessful || profile == null) throw Exception("Empty profile response")

            handleProfileParsing(profile)
            if (rawResponse != null) handleFleetParsing(this@CommanderClient, rawResponse)
        } catch (t: Throwable) {
            handleProfileFailure(t)
        }
    }

    private fun handleProfileFailure(t: Throwable?) {
        val pos = FrontierProfileEvent(false, t?.message, "", 0, 0, "", 0, 0)
        val ranks = FrontierRanksEvent(
            false, null, null,
            null, null, null, null
        )
        val fleet = FrontierFleetEvent(false, t?.message, ArrayList())
        sendResultMessage(pos)
        sendResultMessage(ranks)
        sendResultMessage(fleet)
    }

    /**
     *  - Loads ranks from journal,capture FrontierRanksEvent for the result.
     *  - Loads discoveries from journal, capture FrontierDiscoveriesEvent for the result.
     */
    fun loadCurrentJournal() {
        mJournalWorker?.getCurrentJournal()
    }

    fun getDistanceToSol(systemName: String?) {
        if (systemName != null && systemName != "Sol") {
            DistanceCalculatorNetwork.getDistanceToSol(App.getContext(), systemName)
        }
    }

    private fun handleProfileParsing(profile: Profile) {
        val frontierProfileEvent: FrontierProfileEvent
        val commanderName: String = profile.commander?.name!!
        val credits: Long = profile.commander?.credits!!
        val debt: Long = profile.commander?.debt!!
        val hull = profile.ship?.health?.hull!!
        val integrity = 1000000 - profile.ship?.health?.integrity!!

        currentSystem.postValue(profile.lastSystem?.name)

        frontierProfileEvent = profile.commander?.let {
            FrontierProfileEvent(
                true,
                null,
                commanderName,
                credits,
                debt,
                profile.lastSystem?.name!!,
                hull,
                integrity
            )
        }!!
        sendResultMessage(frontierProfileEvent)
    }

    private fun handleFleetParsing(commanderClient: CommanderClient, rawProfileResponse: JsonObject) {
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
        commanderClient.sendResultMessage(FrontierFleetEvent(true, null, shipsList))
    }

    private fun sendResultMessage(data: Any?) {
        EventBus.getDefault().post(data)
    }
}
