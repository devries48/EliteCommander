package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

data class FrontierMissionCompletedResponse(
    val timestamp: String,
    val event: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Reward")
    val reward: Int,
)