package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

abstract class FrontierJournalResponseBase

/**
 * Response class for journal event: 'Rank'
 */
class FrontierJournalRankResponse : FrontierJournalResponseBase() {
    @SerializedName("Combat")
    var combat: Int = 0

    @SerializedName("Trade")
    var trade: Int = 0

    @SerializedName("Explore")
    var explore: Int = 0

    @SerializedName("CQC")
    var cqc: Int = 0

    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("Alliance")
    var alliance: Int = 0
}