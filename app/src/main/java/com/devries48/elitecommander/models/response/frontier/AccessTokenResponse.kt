package com.devries48.elitecommander.models.response.frontier

import com.google.gson.annotations.SerializedName

class AccessTokenResponse {
    @SerializedName("access_token")
    var accessToken: String? = null

    @SerializedName("refresh_token")
    var refreshToken: String? = null
}