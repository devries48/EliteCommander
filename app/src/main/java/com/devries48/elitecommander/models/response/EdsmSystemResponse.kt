package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

// API Call for coordinates: https://www.edsm.net/api-v1/systems?systemName=sol&coords=1&showcoords=1
class EdsmSystemResponse {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("id")
    var id: Long = 0

    @SerializedName("requirePermit")
    var permitRequired = false

    @SerializedName("coords")
    var information: EdsmSystemCoordinatesResponse? = null
}

//Distance to Sol = SQRT(x^2 + y^2 + z^2) from sol,
//Distance system A to B = ABS(SQRT((x1 - x0) ^ 2 + (y1 - y0) ^ 2 + (z1 - z0) ^ 2))
class EdsmSystemCoordinatesResponse {
    @SerializedName("x")
    val x: Double = 0.0

    @SerializedName("y")
    val y: Double = 0.0

    @SerializedName("z")
    val z: Double = 0.0
}

