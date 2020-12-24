package com.devries48.elitecommander.frontier

import com.google.gson.annotations.SerializedName

class FrontierAccessTokenResponse {
    @SerializedName("access_token")
    var AccessToken: String? = null

    @SerializedName("refresh_token")
    var RefreshToken: String? = null
}