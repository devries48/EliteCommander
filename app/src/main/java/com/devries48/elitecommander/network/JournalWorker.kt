package com.devries48.elitecommander.network

import com.devries48.elitecommander.App
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.getResult
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.response.FrontierJournalRankProgressResponse
import com.devries48.elitecommander.models.response.FrontierJournalRankReputationResponse
import com.devries48.elitecommander.models.response.FrontierJournalRankResponse
import com.devries48.elitecommander.utils.DateUtils
import com.devries48.elitecommander.utils.DateUtils.removeDays
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class JournalWorker(frontierApi: FrontierInterface?) {

    private lateinit var mFrontierApi: FrontierInterface
    private lateinit var mCrawlerType: CrawlerType

    init {
        if (frontierApi != null) {
            mFrontierApi = frontierApi
        }
    }

    private var journalDate: Date? by Delegates.observable(null) { _, _, newDate ->
        if (newDate != null) {
            GlobalScope.launch {
                var code: Int? = 0
                var response: String? = null
                var rawEvents: List<RawEvent>? = null

                getJournal(newDate) { c, r ->
                    code = c
                    response = r
                }
                if (code == 200)
                    rawEvents = response?.let { parseResponse(it) }

                crawlJournal(code, rawEvents)
            }
        }
    }

    private suspend fun crawlJournal(code: Int?, rawEvents: List<RawEvent>?) {
        withContext(Dispatchers.IO) {
            if (code != 200) {
                //return
                println("CODE:$code")
                throw Exception("Not Implemented yet!")
                val minimumDate = DateUtils.eliteStartDate
                journalDate = journalDate?.removeDays()
            }

            if (mCrawlerType == CrawlerType.CURRENT_JOURNAL) {
                raiseFrontierRanksEvent(rawEvents!!)
            }
        }
    }

    // Raises FrontierRanksEvent,
    fun getCurrentJournal() {
        mCrawlerType = CrawlerType.CURRENT_JOURNAL

        // Start searching for the latest journal
        journalDate = DateUtils.getCurrentDate().removeDays()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getJournal(date: Date, callback: (c: Int?, r: String?) -> Unit) {
        val dateString: String = SimpleDateFormat("yyyy/MM/dd", Locale.ROOT).format(date)

        try {

            withContext(Dispatchers.IO) {
                val result = mFrontierApi.getJournal(dateString)?.getResult()
                callback.invoke(200, result?.string())
            }

        } catch (e: Exception) {
            println(e.message.toString())
        }
    }

    private suspend fun parseResponse(response: String): List<RawEvent> {
        val rawEvents: MutableList<RawEvent> = ArrayList()

        withContext(Dispatchers.IO) {
            rawEvents.clear()

            response.replace("\r\n", "").replace("\n", "")
                .trim().drop(1).dropLast(1).split("}{").map {
                    try {
                        val raw = RawEvent(it.trim())
                        if (raw.event !in mIgnoreEvents) rawEvents.add(raw)
                    } catch (e: java.lang.Exception) {
                        println("-----------------------")
                        println(it.trim())
                    }
                }

            println("Journal events present: " + rawEvents.size)
        }
        return rawEvents
    }

    private fun raiseFrontierRanksEvent(rawEvents: List<RawEvent>) {
        if (mEventCache.sendCachedRanksEvent()) return // Raise cached event if available

        val context = App.getContext()

        try {
            val rawRank = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_RANK }
            val rawProgress = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_PROGRESS }
            val rawReputation = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_REPUTATION }

            if (rawRank == null || rawProgress == null || rawReputation == null) {
                throw error("Error parsing rank events from journal")
            }

            val rank = Gson().fromJson(rawRank.json, FrontierJournalRankResponse::class.java)
            val progress = Gson().fromJson(rawProgress.json, FrontierJournalRankProgressResponse::class.java)
            val reputation = Gson().fromJson(rawReputation.json, FrontierJournalRankReputationResponse::class.java)

            val combatRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_combat)[rank.combat],
                rank.combat,
                progress.combat
            )
            val tradeRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_trade)[rank.trade],
                rank.trade,
                progress.trade
            )
            val exploreRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_explorer)[rank.explore],
                rank.explore,
                progress.explore
            )
            val cqcRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_cqc)[rank.cqc],
                rank.cqc,
                progress.cqc
            )
            val federationRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_federation)[rank.federation],
                rank.federation,
                progress.federation,
                reputation.federation
            )
            val empireRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_empire)[rank.empire],
                rank.empire,
                progress.empire,
                reputation.empire
            )

            val allianceRank = FrontierRanksEvent.FrontierRank(
                context.resources.getString(R.string.rank_alliance),
                rank.alliance,
                0,
                reputation.alliance
            )

            mEventCache.ranksEvent = FrontierRanksEvent(
                true, combatRank, tradeRank, exploreRank,
                cqcRank, federationRank, empireRank, allianceRank
            )
        } catch (e: Exception) {
            println("LOG: Error parsing ranking events from journal." + e.message)
            mEventCache.sendEvent(
                FrontierRanksEvent(
                    false, null, null,
                    null, null, null, null
                )
            )
        }
    }

    companion object {

        private enum class CrawlerType {
            CURRENT_JOURNAL
        }

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

        private var mEventCache: EventCache = EventCache()

    }

    private class RawEvent(value: String) {
        var event: String
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
        }
    }

    private class EventCache {
        var ranksEvent: FrontierRanksEvent? = null
            set(value) {
                field = value
                sendEvent(value)
            }

        fun sendEvent(data: Any?) {
            EventBus.getDefault().post(data)
        }

        fun sendCachedRanksEvent(): Boolean {
            return if (ranksEvent != null) {
                sendEvent(ranksEvent)
                true
            } else false
        }

    }

}

