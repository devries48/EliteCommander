package com.devries48.elitecommander.interfaces

import com.devries48.elitecommander.models.response.DistanceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EddbInterface {

    @GET("distance_calculator/")
    fun getDistance(
        @Query("firstSystem") firstSystem: String?,
        @Query("secondSystem") secondSystem: String?
    ): Call<DistanceResponse?>?

}
