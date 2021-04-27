package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class JournalRedeemVoucherResponse(
    @SerializedName("Factions")
    val factions: List<Faction>?,
    @SerializedName("Type")
    val type: String,
    @SerializedName("Amount")
    val amount: Int?,
    @SerializedName("Faction")
    val faction: String?
) : JournalResponseBase() {
    data class Faction(
        @SerializedName("Amount")
        val amount: Int,
        @SerializedName("Faction")
        val faction: String
    )
}
