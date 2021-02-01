package com.devries48.elitecommander.network.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface FrontierRetrofit {
    @get:GET("profile")
    val profileRaw: Call<ResponseBody?>?

    @get:GET("profile")
    val profile: Call<Any?>?

    @get:GET("journal/2021/01/14")
    val journalRaw: Call<ResponseBody?>?

    @get:GET("journal/2021/01/14")
    val journal: Call<Any?>?
}