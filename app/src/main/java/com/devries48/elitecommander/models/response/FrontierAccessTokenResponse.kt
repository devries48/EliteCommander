package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

class FrontierAccessTokenResponse {
    @SerializedName("access_token")
    var accessToken: String? = null

    @SerializedName("refresh_token")
    var refreshToken: String? = null
}