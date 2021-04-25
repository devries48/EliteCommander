package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierJournalMultiSellDataResponse(
    @SerializedName("BaseValue")
    val baseValue: Int, // 2938186
    @SerializedName("Bonus")
    val bonus: Int, // 291000
    @SerializedName("Discovered")
    val discovered: List<Discovered>,
    val event: String, // MultiSellExplorationData
    val timestamp: String, // 2018-11-14T10:35:35Z
    @SerializedName("TotalEarnings")
    val totalEarnings: Int // 3229186
) {
    data class Discovered(
        @SerializedName("NumBodies")
        val numBodies: Int, // 23
        @SerializedName("SystemName")
        val systemName: String // HIP 84742
    )
}

data class FrontierJournalSellDataResponse(
    @SerializedName("BaseValue")
    val baseValue: Int, // 10822
    @SerializedName("Bonus")
    val bonus: Int, // 3959
    @SerializedName("Discovered")
    val discovered: List<String>,
    val event: String, // SellExplorationData
    @SerializedName("Systems")
    val systems: List<String>,
    val timestamp: String, // 2016-06-10T14:32:03Z
    @SerializedName("TotalEarnings")
    val totalEarnings: Int // 44343
)