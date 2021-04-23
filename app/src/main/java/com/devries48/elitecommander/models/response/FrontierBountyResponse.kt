package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierBountyResponse(
    val event: String,
    @SerializedName("Rewards")
    val rewards: List<Reward>?,
    @SerializedName("Target")
    val target: String,
    @SerializedName("Target_Localised")
    val targetLocalised: String,
    val timestamp: String,
    @SerializedName("TotalReward")
    val totalReward: Int,
    @SerializedName("VictimFaction")
    val victimFaction: String
) {
    data class Reward(
        @SerializedName("Faction")
        val faction: String,
        @SerializedName("Reward")
        val reward: Int
    )
}