package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

class FrontierProfileResponse {
    @SerializedName("commander")
    var commander: FrontierProfileCommanderResponse? = null

    @SerializedName("lastSystem")
    var lastSystem: FrontierProfileSystemResponse? = null

    @SerializedName("ship")
    var ship: FrontierProfileShipResponse? = null

    inner class FrontierProfileCommanderResponse {
        @SerializedName("name")
        var name: String? = null

        @SerializedName("credits")
        var credits: Long = 0

        @SerializedName("debt")
        var debt: Long = 0

        @SerializedName("currentShipId")
        var currentShipId: Int = 0

        @SerializedName("rank")
        var rank: FrontierProfileCommanderRankResponse? = null
    }

    inner class FrontierProfileCommanderRankResponse {
        @SerializedName("combat")
        var combat = 0

        @SerializedName("trade")
        var trade = 0

        @SerializedName("explore")
        var explore = 0

        @SerializedName("crime")
        var crime = 0

        @SerializedName("service")
        var service = 0

        @SerializedName("empire")
        var empire = 0

        @SerializedName("federation")
        var federation = 0

        @SerializedName("power")
        var power = 0

        @SerializedName("cqc")
        var cqc = 0
    }

    inner class FrontierProfileSystemResponse {
        @SerializedName("name")
        var name: String? = null
    }

    inner class FrontierProfileShipResponse {
        @SerializedName("shipID")
        var shipID: String? = null

        @SerializedName("health")
        var health: FrontierProfileShipHealthResponse? = null
    }

    inner class FrontierProfileShipHealthResponse {
        @SerializedName("hull")
        var hull: Int = 0

        @SerializedName("integrity")  // (1,000,000 - integrity) / 10,000 = percentage left (so integrity 0 = 100%)
        var integrity: Int = 0
    }

}