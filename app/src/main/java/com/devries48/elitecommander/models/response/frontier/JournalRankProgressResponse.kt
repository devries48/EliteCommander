package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

/**
 * Response class for journal event: 'Progress'
 */
class JournalRankProgressResponse : JournalResponseBase() {
    @SerializedName("Combat")
    var combat: Int = 0

    @SerializedName("Trade")
    var trade: Int = 0

    @SerializedName("Explore")
    var explore: Int = 0

    @SerializedName("Empire")
    var empire: Int = 0

    @SerializedName("Federation")
    var federation: Int = 0

    @SerializedName("CQC")
    var cqc: Int = 0
}
