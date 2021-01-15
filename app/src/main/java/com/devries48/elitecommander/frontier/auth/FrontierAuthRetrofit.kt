package com.devries48.elitecommander.frontier.auth

import com.devries48.elitecommander.frontier.models.models.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.frontier.models.models.FrontierAccessTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface FrontierAuthRetrofit {
    @POST("token")
    fun getAccessToken(@Body body: FrontierAccessTokenRequestBody): Call<FrontierAccessTokenResponse>
}
