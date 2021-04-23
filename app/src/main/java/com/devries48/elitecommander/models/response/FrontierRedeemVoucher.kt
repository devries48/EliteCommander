package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierRedeemVoucher(
    val event: String,
    @SerializedName("Factions")
    val factions: List<Faction>,
    val timestamp: String,
    @SerializedName("Type")
    val type: String
) {
    data class Faction(
        @SerializedName("Amount")
        val amount: Int,
        @SerializedName("Faction")
        val faction: String
    )
}