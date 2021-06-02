package com.devries48.elitecommander.network

import android.content.Context
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.DistanceSearchEvent
import com.devries48.elitecommander.interfaces.EdsmInterface
import com.devries48.elitecommander.models.response.EdsmSystemCoordinatesResponse
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

                if (!it.isSuccessful || body == null || body.none()) {
                    onFailure?.let { it1 -> it1(Exception("Invalid EDSM response")) }
                    return@response
                }

                val coords = body[0]?.information
                if (coords == null) {
                    onFailure?.let { it1 -> it1(Exception("Invalid EDSM call")) }
                    return@response
                }

                handleDistanceCalculation(system, coords)
            }
            onFailure = {
                handleDistanceFailure(it)
            }
        }
    }

    private fun handleDistanceCalculation(system: String, coords: EdsmSystemCoordinatesResponse) {
        println("EDSM")
        println(coords.x)
        println(coords.y)
        println(coords.z)
        println("EDSM")

        val distance = sqrt(coords.x.pow(2) + coords.y.pow(2) + coords.z.pow(2))

        val distanceSearch: DistanceSearchEvent = try {
            DistanceSearchEvent(true, null, round(distance), "Sol", system)
        } catch (ex: Exception) {
            DistanceSearchEvent(false, ex.message, 0.0, "", "")
        }

        EventBus.getDefault().post(distanceSearch)
    }

    private fun handleDistanceFailure(it: Throwable?) {
        // newly discovered system, not added to eddb (yet!)
        val success = it?.message == "400"
        val distanceSearch = DistanceSearchEvent(success, it?.message, 0.0, "", "")

        EventBus.getDefault().post(distanceSearch)
    }
}
