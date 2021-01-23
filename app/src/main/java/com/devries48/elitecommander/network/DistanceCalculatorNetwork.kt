package com.devries48.elitecommander.network


import android.content.Context
import com.devries48.elitecommander.events.DistanceSearchEvent
import com.devries48.elitecommander.models.DistanceResponse
import com.devries48.elitecommander.network.retrofit.EDApiRetrofit
import com.devries48.elitecommander.network.retrofit.RetrofitSingleton
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.internal.EverythingIsNonNull


object DistanceCalculatorNetwork {

    fun getDistance(ctx: Context, firstSystem: String?, secondSystem: String?) {
        val retrofit: EDApiRetrofit? = RetrofitSingleton.getInstance()
            ?.getEdApiRetrofit(ctx.applicationContext)

        val callback: Callback<DistanceResponse?> = object : Callback<DistanceResponse?> {
            @EverythingIsNonNull
            override fun onResponse(
                call: Call<DistanceResponse?>,
                response: Response<DistanceResponse?>
            ) {
                val body = response.body()

                if (!response.isSuccessful || body == null) {
                    var msg = "Invalid response"

                    if  (    response.code()==400) {
                        msg= "400"
                    }
                    onFailure(call, Exception(msg ))
                } else {
                    val distanceSearch: DistanceSearchEvent = try {
                        DistanceSearchEvent(
                            true,
                            body.distance,
                            body.fromSystem!!.name!!,
                            body.toSystem!!.name!!,
                            body.fromSystem!!.permitRequired,
                            body.toSystem!!.permitRequired
                        )
                    } catch (ex: Exception) {
                        DistanceSearchEvent(
                            false,0f, "",
                            "", startPermitRequired = false, endPermitRequired = false
                        )
                    }
                    EventBus.getDefault().post(distanceSearch)
                }
            }

            @EverythingIsNonNull
            override fun onFailure(call: Call<DistanceResponse?>, t: Throwable) {
                val success= t.message=="400"  // newly discovered system, not added to eddb (yet!)
                val distanceSearch = DistanceSearchEvent(
                    success, 0f, "",
                    "", startPermitRequired = false, endPermitRequired = false
                )
                EventBus.getDefault().post(distanceSearch)
            }
        }
        retrofit?.getDistance(firstSystem, secondSystem)!!.enqueue(callback)
    }
}
