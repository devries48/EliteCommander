package com.devries48.elitecommander.frontier.api

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.frontier.FrontierRetrofit
import com.devries48.elitecommander.frontier.RetrofitSingleton
import com.devries48.elitecommander.frontier.api.events.*
import com.devries48.elitecommander.frontier.api.events.Ranks.Rank
import com.devries48.elitecommander.frontier.api.models.FrontierProfileResponse
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
                    val pos: CommanderPosition
                    val credits: Credits
                    val ranks: Ranks
                    try {
                        // Position
                        val commanderName: String = profileResponse.Commander?.Name!!

                        pos = profileResponse.LastSystem?.Name?.let {
                            CommanderPosition(
                                true,
                                commanderName,
                                it,
                                false
                            )
                        }!!
                        sendResultMessage(pos)

                        // Credits
                        credits = profileResponse.Commander?.let {
                            Credits(
                                true, it.Credits,
                                profileResponse.Commander!!.Debt
                            )
                        }!!
                        sendResultMessage(credits)

                        // Ranks
                        ranks = getRanksFromApiBody(profileResponse)
                        sendResultMessage(ranks)

                        // Fleet
                        if (rawResponse != null) {
                            handleFleetParsing(rawResponse)
                        }
                    } catch (ex: Exception) {
                        onFailure(call, Exception("Invalid response"))
                    }
                }
            }

            private fun getRanksFromApiBody(apiResponse: FrontierProfileResponse): Ranks {
                val apiRanks = apiResponse.Commander!!.Rank

                // Combat
                val combatRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_combat)[apiRanks!!.Combat],
                    apiRanks.Combat, -1
                )

                // Trade
                val tradeRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_trade)[apiRanks.Trade],
                    apiRanks.Trade, -1
                )

                // Explore
                val exploreRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_explorer)[apiRanks.Explore],
                    apiRanks.Explore, -1
                )

                // CQC
                val cqcRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_cqc)[apiRanks.Cqc],
                    apiRanks.Cqc, -1
                )

                // Federation
                val federationRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_federation)[apiRanks.Federation],
                    apiRanks.Federation, -1
                )

                // Empire
                val empireRank = Rank(
                    context.resources
                        .getStringArray(R.array.ranks_empire)[apiRanks.Empire],
                    apiRanks.Empire, -1
                )
                return Ranks(
                    true, combatRank, tradeRank, exploreRank,
                    cqcRank, federationRank, empireRank
                )
            }


            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                val credits = Credits(false, 0, 0)
                val pos = CommanderPosition(false, "", "", false)
                val ranks = Ranks(
                    false, null, null,
                    null, null, null, null
                )
                val fleet = Fleet(false, ArrayList())
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
        sendResultMessage(Fleet(true, shipsList))
    }


    private fun sendResultMessage(data: Any?) {
        EventBus.getDefault().post(data)
    }

}