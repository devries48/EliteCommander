package com.devries48.elitecommander.frontier.api.models

import com.google.gson.annotations.SerializedName

class FrontierProfileResponse {
    @SerializedName("commander")
    var Commander: FrontierProfileCommanderResponse? = null

    @SerializedName("lastSystem")
    var LastSystem: FrontierProfileSystemResponse? = null

    inner class FrontierProfileCommanderResponse {
        @SerializedName("name")
        var Name: String? = null

        @SerializedName("credits")
        var Credits: Long = 0

        @SerializedName("debt")
        var Debt: Long = 0

        @SerializedName("rank")
        var Rank: FrontierProfileCommanderRankResponse? = null
    }

    inner class FrontierProfileCommanderRankResponse {
        @SerializedName("combat")
        var Combat = 0

        @SerializedName("trade")
        var Trade = 0

        @SerializedName("explore")
        var Explore = 0

        @SerializedName("crime")
        var Crime = 0

        @SerializedName("service")
        var Service = 0

        @SerializedName("empire")
        var Empire = 0

        @SerializedName("federation")
        var Federation = 0

        @SerializedName("power")
        var Power = 0

        @SerializedName("cqc")
        var Cqc = 0
    }

    inner class FrontierProfileSystemResponse {
        @SerializedName("name")
        var Name: String? = null
    }
}