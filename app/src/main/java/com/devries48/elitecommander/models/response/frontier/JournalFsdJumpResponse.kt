package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

data class JournalFsdJumpResponse(
    val event: String, // FSDJump
    @SerializedName("JumpDist")
    val jumpDist: Double, // 36.034
    @SerializedName("StarPos")
    val starPos: List<Double>,
    @SerializedName("StarSystem")
    val starSystem: String, // HIP 78085
    val timestamp: String // 2016-06-10T14:35:00Z
)