package com.devries48.elitecommander.models

import com.google.gson.annotations.SerializedName

class StatisticSettingsModel {
    @SerializedName("timestamp")
    var timestamp: String?=null

    @SerializedName("credits")
    var credits:Long?=null

    @SerializedName("assets")
    var assets:Long?=null
}