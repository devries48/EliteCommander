package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierJournalCombatBondResponse(
    @SerializedName("AwardingFaction")
    val awardingFaction: String,
    val event: String,
    @SerializedName("Reward")
    val reward: Int,
    val timestamp: String,
    @SerializedName("VictimFaction")
    val victimFaction: String
)