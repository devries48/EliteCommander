package com.devries48.elitecommander.frontier

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.frontier.events.events.*
import com.devries48.elitecommander.frontier.events.events.RanksEvent.Rank
import com.devries48.elitecommander.frontier.models.models.FrontierProfileResponse
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


class CommanderApi(ctx: Context) {

    private var context: Context = ctx
    private var frontierRetrofit: FrontierRetrofit? = null

    init {
        frontierRetrofit = RetrofitSingleton.getInstance()
            ?.getFrontierRetrofit(context.applicationContext)
    }

    fun getCommanderStatus() {
        val callback: Callback<ResponseBody?> = object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                @NotNull response: Response<ResponseBody?>
            ) {

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
                    val commanderProfileEvent: CommanderProfileEvent
                    val creditsEvent: CreditsEvent
                    val ranksEvent: RanksEvent

                    try {
                        // Position
                        val commanderName: String = profileResponse.commander?.name!!
                        val credits: Long = profileResponse.commander?.credits!!
                        val debt: Long = profileResponse.commander?.debt!!
                        val systemName = profileResponse.lastSystem?.name!!

                        commanderProfileEvent = profileResponse.commander?.let {
                            CommanderProfileEvent(
                                true,
                                commanderName,
                                credits,
                                debt,
                                systemName
                            )
                        }!!
                        sendResultMessage(commanderProfileEvent)

                        // CreditsEvent
                        creditsEvent = profileResponse.commander?.let {
                            CreditsEvent(
                                true, it.credits,
                                profileResponse.commander!!.debt
                            )
                        }!!
                        sendResultMessage(creditsEvent)

                        // RanksEvent
                        ranksEvent = getRanksFromApiBody(profileResponse)
                        sendResultMessage(ranksEvent)

                        // FleetEvent
                        if (rawResponse != null) {
                            handleFleetParsing(rawResponse)
                        }
                    } catch (ex: Exception) {
                        onFailure(call, Exception("Invalid response"))
                    }
                }
            }

            private fun getRanksFromApiBody(apiResponse: FrontierProfileResponse): RanksEvent {
                val apiRanks = apiResponse.commander!!.rank

                // combat
                val combatRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_combat)[apiRanks!!.combat],
                    apiRanks.combat, -1
                )

                // trade
                val tradeRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_trade)[apiRanks.trade],
                    apiRanks.trade, -1
                )

                // explore
                val exploreRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_explorer)[apiRanks.explore],
                    apiRanks.explore, -1
                )

                // CQC
                val cqcRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_cqc)[apiRanks.cqc],
                    apiRanks.cqc, -1
                )

                // federation
                val federationRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_federation)[apiRanks.federation],
                    apiRanks.federation, -1
                )

                // empire
                val empireRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_empire)[apiRanks.empire],
                    apiRanks.empire, -1
                )
                return RanksEvent(
                    true, combatRank, tradeRank, exploreRank,
                    cqcRank, federationRank, empireRank
                )
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                val credits = CreditsEvent(false, 0, 0)
                val pos = CommanderProfileEvent(false, "", 0, 0, "")
                val ranks = RanksEvent(
                    false, null, null,
                    null, null, null, null
                )
                val fleet = FleetEvent(false, ArrayList())
                sendResultMessage(credits)
                sendResultMessage(pos)
                sendResultMessage(ranks)
                sendResultMessage(fleet)
            }
        }
        frontierRetrofit?.profileRaw?.enqueue(callback)
    }

    private fun handleFleetParsing(rawProfileResponse: JsonObject) {
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
        val shipsList: MutableList<Ship> = ArrayList()
        for (entry in responseList) {
            val rawShip = entry.asJsonObject
            var shipName: String? = null
            if (rawShip.has("shipName")) {
                shipName = rawShip["shipName"].asString
            }
            val value = rawShip["value"].asJsonObject
            val isCurrentShip = rawShip["id"].asInt == currentShipId
            val newShip = Ship(
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
        sendResultMessage(FleetEvent(true, shipsList))
    }


    private fun sendResultMessage(data: Any?) {
        EventBus.getDefault().post(data)
    }

}