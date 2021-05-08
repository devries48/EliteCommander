package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class JournalMultiSellData(
    @SerializedName("BaseValue")
    val baseValue: Int, // 2938186
    @SerializedName("Bonus")
    val bonus: Int, // 291000
    @SerializedName("Discovered")
    val discovered: List<Discovered>,
    @SerializedName("TotalEarnings")
    val totalEarnings: Int // 3229186
) : JournalResponseBase() {
    data class Discovered(
        @SerializedName("NumBodies")
        val numBodies: Int, // 23
        @SerializedName("SystemName")
        val systemName: String // HIP 84742
    )
}

data class JournalSellData(
    @SerializedName("BaseValue")
    val baseValue: Int, // 10822
    @SerializedName("Bonus")
    val bonus: Int, // 3959
    @SerializedName("Discovered")
    val discovered: List<String>,
    @SerializedName("Systems")
    val systems: List<String>,
    @SerializedName("TotalEarnings")
    val totalEarnings: Int // 44343
) : JournalResponseBase()