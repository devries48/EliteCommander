package com.devries48.elitecommander.network

import android.content.Context
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.DistanceSearchEvent
import com.devries48.elitecommander.interfaces.EddbInterface
import org.greenrobot.eventbus.EventBus

object DistanceCalculatorNetwork {

    fun getDistance(ctx: Context, firstSystem: String?, secondSystem: String?) {
        val eddb: EddbInterface? = RetrofitSingleton.getInstance()
            ?.getEdApiRetrofit(ctx.applicationContext)

        eddb?.getDistance(firstSystem, secondSystem)!!.enqueueWrap {
            onResponse = {
                val body = it.body()

                if (!it.isSuccessful || body == null) {
                    var msg = "Invalid response"

                    if (it.code() == 400)
                        msg = "400"

                    onFailure?.let { it1 -> it1(Exception(msg)) }
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
                            false, 0f, "",
                            "", startPermitRequired = false, endPermitRequired = false
                        )
                    }
                    EventBus.getDefault().post(distanceSearch)
                }

            }
            onFailure = {
                val success =
                    it?.message == "400"  // newly discovered system, not added to eddb (yet!)
                val distanceSearch = DistanceSearchEvent(
                    success, 0f, "",
                    "", startPermitRequired = false, endPermitRequired = false
                )
                EventBus.getDefault().post(distanceSearch)
            }
        }

    }
}
