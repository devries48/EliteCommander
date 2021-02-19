package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

/**
 * Response class for journal event: 'Reputation'
 */
class FrontierJournalRankReputationResponse : FrontierJournalResponseBase() {
    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("Alliance")
    var alliance: Int = 0
}
