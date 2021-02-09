package com.devries48.elitecommander.interfaces

import com.devries48.elitecommander.models.response.EdsmSystemResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EdsmInterface {

    @GET("api-v1/systems")
    fun getSystems(
        @Query("systemName") systemName: String?,
        @Query("showId") showId: Int,
        @Query("showInformation") showInformation: Int,
        @Query("showPermit") showPermit: Int,
        @Query("coords") showCoords: Int
    ): Call<List<EdsmSystemResponse?>?>?
}