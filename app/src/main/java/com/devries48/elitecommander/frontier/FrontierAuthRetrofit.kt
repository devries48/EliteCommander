package com.devries48.elitecommander.frontier

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface FrontierAuthRetrofit {
    @POST("token")
    fun getAccessToken(@Body body: FrontierAccessTokenRequestBody): Call<FrontierAccessTokenResponse>
}
