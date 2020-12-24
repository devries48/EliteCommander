package com.devries48.elitecommander.frontier

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET


interface FrontierRetrofit {
    @get:GET("profile")
    val profileRaw: Call<ResponseBody?>?

    @get:GET("profile")
    val profile: Call<Any?>?
}
