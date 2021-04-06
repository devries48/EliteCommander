package com.devries48.elitecommander.models

import com.google.gson.annotations.SerializedName

class StatisticSettingsModel {

    @SerializedName("timestamp")
    var timestamp: String? = null

    @SerializedName("credits")
    var credits: Long? = null

    @SerializedName("assets")
    var assets: Long? = null

    @SerializedName("timeplayed")
    var timePlayed: Int? = null

    @SerializedName("bountiesProfit")
    var bountiesProfit: Long? = null

    @SerializedName("bountiesTotal")
    var bountiesTotal: Int? = null

    @SerializedName("bondsProfit")
    var bondsProfit: Long? = null

    @SerializedName("bondsTotal")
    var bondsTotal: Int? = null

    @SerializedName("assassinationsProfit")
    var assassinationsProfit: Long? = null

    @SerializedName("assassinationsTotal")
    var assassinationsTotal: Int? = null

    @SerializedName("explorationProfit")
    var explorationProfit: Long? = null

    @SerializedName("tradingProfit")
    var tradingProfit: Long? = null

    @SerializedName("tradingMarkets")
    var tradingMarkets: Int? = null

    @SerializedName("blackMarketProfit")
    var blackMarketProfit: Long? = null

    @SerializedName("miningProfit")
    var miningProfit: Long? = null

    @SerializedName("miningTotal")
    var miningTotal: Int? = null

    @SerializedName("rescueProfit")
    var rescueProfit: Long? = null

    @SerializedName("rescueTotal")
    var rescueTotal: Int? = null

    @SerializedName("totalKills")
    var totalKills: Int? = null

    @SerializedName("skimmersKilled")
    var skimmersKilled: Int? = null

    @SerializedName("totalHyperspaceDistance")
    var totalHyperspaceDistance: Int?=null

    @SerializedName("totalHyperspaceJumps")
    var totalHyperspaceJumps: Int?=null

    @SerializedName("systemsVisited")
    var systemsVisited: Int?=null

}