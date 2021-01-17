package com.devries48.elitecommander.network.retrofit

import com.devries48.elitecommander.models.DistanceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EDApiRetrofit {

    @GET("distance_calculator/")
    fun getDistance(
        @Query("firstSystem") firstSystem: String?,
        @Query("secondSystem") secondSystem: String?
    ): Call<DistanceResponse?>?

}
