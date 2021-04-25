package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierJournalRedeemVoucherResponse(
    val event: String,
    @SerializedName("Factions")
    val factions: List<Faction>?,
    val timestamp: String,
    @SerializedName("Type")
    val type: String,
    @SerializedName("Amount")
    val amount: Int?,
    @SerializedName("Faction")
    val faction: String?
) {
    data class Faction(
        @SerializedName("Amount")
        val amount: Int,
        @SerializedName("Faction")
        val faction: String
    )
}
