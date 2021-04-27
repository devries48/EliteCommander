package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class JournalBountyResponse(
    @SerializedName("Reward")
    val reward: Int?,
    @SerializedName("Rewards")
    val rewards: List<Reward>?,
    @SerializedName("Target")
    val target: String,
    @SerializedName("Target_Localised")
    val targetLocalised: String,
    @SerializedName("TotalReward")
    val totalReward: Int,
    @SerializedName("VictimFaction")
    val victimFaction: String
) : JournalResponseBase() {
    data class Reward(
        @SerializedName("Faction")
        val faction: String,
        @SerializedName("Reward")
        val reward: Int
    )
}