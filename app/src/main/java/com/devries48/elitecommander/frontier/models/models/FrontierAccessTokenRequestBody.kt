package com.devries48.elitecommander.frontier.models.models

import com.google.gson.annotations.SerializedName

class FrontierAccessTokenRequestBody {
    @SerializedName("grant_type")
    var grantType: String? = null

    @SerializedName("client_id")
    var clientId: String? = null

    @SerializedName("code_verifier")
    var codeVerifier: String? = null

    @SerializedName("code")
    var code: String? = null

    @SerializedName("redirect_uri")
    var redirectUri: String? = null

    @SerializedName("refresh_token")
    var refreshToken: String? = null
}