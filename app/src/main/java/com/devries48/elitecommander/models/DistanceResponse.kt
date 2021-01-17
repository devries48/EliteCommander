package com.devries48.elitecommander.models

import com.google.gson.annotations.SerializedName


class DistanceResponse {
    @SerializedName("distance")
    var distance = 0f

    @SerializedName("from")
    var fromSystem: DistanceSystem? = null

    @SerializedName("to")
    var toSystem: DistanceSystem? = null

    inner class DistanceSystem {
        @SerializedName("x")
        var x = 0f

        @SerializedName("y")
        var y = 0f

        @SerializedName("z")
        var z = 0f

        @SerializedName("id")
        var id: Long = 0

        @SerializedName("name")
        var name: String? = null

        @SerializedName("permit_required")
        var permitRequired = false
    }
}
