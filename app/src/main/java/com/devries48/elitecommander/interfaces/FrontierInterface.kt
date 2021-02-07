package com.devries48.elitecommander.interfaces

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface FrontierInterface {
    @get:GET("profile")
    val profileRaw: Call<ResponseBody?>?

    @get:GET("profile")
    val profile: Call<Any?>?

    @get:GET("journal/2021/01/15")
    val journalRaw: Call<ResponseBody?>?

    @get:GET("journal/2021/01/15")
    val journal: Call<Any?>?
}