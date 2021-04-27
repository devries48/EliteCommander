package com.devries48.elitecommander.interfaces

import com.devries48.elitecommander.models.httpRequest.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.models.response.frontier.AccessTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FrontierAuthInterface {
    @POST("token")
    fun getAccessToken(@Body body: FrontierAccessTokenRequestBody): Call<AccessTokenResponse>
}
