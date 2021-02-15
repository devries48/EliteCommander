package com.devries48.elitecommander.network

import android.content.Context
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.DistanceSearchEvent
import com.devries48.elitecommander.interfaces.EdsmInterface
import org.greenrobot.eventbus.EventBus
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

object DistanceCalculatorNetwork {

    fun getDistanceToSol(ctx: Context, system: String) {
        val edsm: EdsmInterface? = RetrofitClient.getInstance()
            ?.getEdsmRetrofit(ctx.applicationContext)

        edsm?.getSystems(system, 0, 1, 0, 1)!!.enqueueWrap {
            onResponse = response@{
                val body = it.body()

                if (!it.isSuccessful || body == null) {
                    var msg = "Invalid response"

                    if (it.code() == 400)
                        msg = "400"

                    onFailure?.let { it1 -> it1(Exception(msg)) }
                    return@response
                }
                val coords = body[0]?.information
                if (coords == null) {
                    onFailure?.let { it1 -> it1(Exception("Invalid EDSM call")) }
                    return@response
                }
                val distance = sqrt(coords.x.pow(2) + coords.y.pow(2) + coords.z.pow(2))

                val distanceSearch: DistanceSearchEvent = try {
                    DistanceSearchEvent(
                        true,
                        round(distance),
                        "Sol",
                        system
                    )
                } catch (ex: Exception) {
                    DistanceSearchEvent(
                        false, 0.0, "",
                        ""
                    )
                }
                EventBus.getDefault().post(distanceSearch)
            }
            onFailure = {
                val success =
                    it?.message == "400"  // newly discovered system, not added to eddb (yet!)
                val distanceSearch = DistanceSearchEvent(
                    success, 0.0, "",
                    ""
                )
                if (!success)
                    println("Distance calculation failed: " + it?.message)

                EventBus.getDefault().post(distanceSearch)
            }
        }

    }

}
