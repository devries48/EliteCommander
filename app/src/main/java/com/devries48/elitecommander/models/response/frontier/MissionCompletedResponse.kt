package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class MissionCompletedResponse(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Reward")
    val reward: Int
) : JournalResponseBase()