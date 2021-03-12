package com.devries48.elitecommander.models

import com.google.gson.annotations.SerializedName

class SettingsModel {
    @SerializedName("timestamp")
    var timestamp: String?=null

    @SerializedName("credits")
    val credits:Long=0
    @SerializedName("assets")

    val assets:Long=0
}