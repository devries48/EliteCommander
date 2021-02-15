package com.devries48.elitecommander.network

import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.FrontierJournal
import com.devries48.elitecommander.utils.DateUtils
import com.devries48.elitecommander.utils.DateUtils.removeDays
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class JournalWorker(frontierApi: FrontierInterface?) {

    private lateinit var mFrontierApi: FrontierInterface

    init {
        if (frontierApi != null) {
            mFrontierApi = frontierApi
        }
    }

    var journal: FrontierJournal? by Delegates.observable(null) { _, _, newJournal ->
        if (newJournal != null) {

        }
    }

    var journalDate: Date? by Delegates.observable(null) { _, _, newDate ->
        if (newDate != null) {
            GlobalScope.launch {
                    var code: Int? = 0
                    var response: String? = null

                    getJournal(newDate) { c, r ->
                        code = c
                        response = r
                    }
                response?.let { parseResponse(it) }
            }
        }
    }

    fun findLatestJournal() {
        val minimumDate = DateUtils.eliteStartDate

        journalDate = DateUtils.getCurrentDate().removeDays()

        //            while (journalDate > minimumDate) {
//
//
//                GlobalScope.launch {
//                    val result = async { getJournal(journalDate) }
//                    val res=result.await()
//                    println("RESULT")
//                    println(res)
//                }
//
//                journalDate = journalDate.removeDays()
//            }

    }

    private suspend fun getJournal(date: Date, callback: (c: Int?, r: String?) -> Unit) {
        withContext(Dispatchers.IO) {
            val dateString: String = SimpleDateFormat("yyyy/MM/dd", Locale.ROOT).format(date)

            mFrontierApi.getJournal(dateString)?.enqueueWrap {
                onResponse = {
                    val code = it.code()
                    var response: String? = null
                    if (code == 200) response = it.body()?.string()
                    callback.invoke(code, response)
                }
                onFailure = {
                    println("LOG: Response failure - " + it?.message)
                    callback(0, null)
                }
            }
        }
    }

    private suspend fun parseResponse(response: String) {
        withContext(Dispatchers.IO) {
            mRawEvents.clear()

            response.replace("\r\n", "").replace("\n", "")
                .trim().drop(1).dropLast(1).split("}{").map {
                    try {
                        val raw = RawEvent(it.trim())
                        if (raw.event !in mIgnoreEvents) mRawEvents.add(raw)
                    } catch (e: java.lang.Exception) {
                        println("-----------------------")
                        println(it.trim())
                    }
                }

            println("Journal events present: " + mRawEvents.size)
        }
    }

    private class RawEvent(value: String) {
        var event: String
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
        }
    }

    companion object {
        private const val JOURNAL_EVENT_STATISTICS = "Statistics"
        private const val JOURNAL_EVENT_RANK = "Rank"
        private const val JOURNAL_EVENT_PROGRESS = "Progress"
        private const val JOURNAL_EVENT_REPUTATION = "Reputation"
        private const val JOURNAL_EVENT_DISCOVERY = "Scan"
        private const val JOURNAL_EVENT_MAP = "SAAScanComplete"

        var mIgnoreEvents =
            arrayOf(
                "Commander",
                "Materials",
                "LoadGame",
                "LoadGame",
                "EngineerProgress",
                "Location",
                "Powerplay",
                "Music",
                "Touchdown",
                "Missions",
                "Loadout",
                "SAASignalsFound",
                "Cargo",
                "Liftoff",
                "ReservoirReplenished",
                "NavRoute",
                "FSDTarget",  // RemainingJumpsInRoute (multiple)
                "StartJump",
                "SupercruiseEntry",
                "LeaveBody",
                "FSDJump",
                "FSSDiscoveryScan",
                "FSSAllBodiesFound",
                "FuelScoop",
                "MultiSellExplorationData"
            )

        private val mRawEvents: MutableList<RawEvent> = ArrayList()
    }

}

