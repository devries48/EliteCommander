package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

/**
 * Response class for journal event: 'Reputation'
 */
class JournalRankReputationResponse : JournalResponseBase() {
    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("Alliance")
    var alliance: Int = 0
}
