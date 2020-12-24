package com.devries48.elitecommander.frontier


import com.google.gson.annotations.SerializedName


class FrontierAccessTokenRequestBody {
    @SerializedName("grant_type")
    var GrantType: String? = null

    @SerializedName("client_id")
    var ClientId: String? = null

    @SerializedName("code_verifier")
    var CodeVerifier: String? = null

    @SerializedName("code")
    var Code: String? = null

    @SerializedName("redirect_uri")
    var RedirectUri: String? = null

    @SerializedName("refresh_token")
    var RefreshToken: String? = null
}