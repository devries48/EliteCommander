package com.devries48.elitecommander.interfaces

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FrontierInterface {
    @get:GET("profile")
    val profileRaw: Call<ResponseBody?>?

    /**
      GET journal/[year (4 numbers)]/[month (2 numbers)]/[day (2 numbers)]>

      Response codes:
        Code	Status	        Description
        200	    OK	            This means you got the entire journal for the specified request
        204	    No Content	    This means that the player has not (yet) played this day
        206	    Partial Content	The request did not get the entire journal, best solution is to keep trying until you get 200 - OK
        401	    Unauthorized
     */
    @GET("journal/{date}")
    fun getJournal(@Path("date", encoded = true) date: String?): Call<ResponseBody?>?
}