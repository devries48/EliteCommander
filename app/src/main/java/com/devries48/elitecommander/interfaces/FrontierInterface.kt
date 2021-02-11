package com.devries48.elitecommander.interfaces

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FrontierInterface {
    @get:GET("profile")
    val profileRaw: Call<ResponseBody?>?

    @GET("journal/{date}")  // GET journal/[year (4 numbers)]/[month (2 numbers)]/[day (2 numbers)]>
    fun getJournal(@Path("date", encoded = true) date: String?): Call<ResponseBody?>?
}