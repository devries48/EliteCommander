package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.declarations.getResult
import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.utils.DateUtils
import com.devries48.elitecommander.utils.DateUtils.DateFormatType.*
import com.devries48.elitecommander.utils.DateUtils.removeDays
import com.devries48.elitecommander.utils.DateUtils.toDateString
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class JournalWorker(frontierApi: FrontierInterface?) {

    private lateinit var mFrontierApi: FrontierInterface
    private lateinit var mCrawlerType: CrawlerType
    private var mLatestJournalDate: Date? = DateUtils.getCurrentDate()
    private var mCurrentDiscoveriesDate: Date? = null

    private val mJournalRanks: JournalRanks = JournalRanks()
    private val mJournalStats: JournalStatistics = JournalStatistics(this)
    private val mJournalDiscoveries: JournalDiscoveries = JournalDiscoveries()

    internal var lastJournalDate: Date? = null

    init {
        if (frontierApi != null) mFrontierApi = frontierApi
    }

    // Raises FrontierRanksEvent
    // Raises FrontierStatisticsEvent
    // Raises FrontierDiscoveriesEvent
    fun getCurrentJournal() {
        mCrawlerType = CrawlerType.CURRENT_JOURNAL
        processJournal(mLatestJournalDate)
    }

    private fun processJournal(date: Date?) {
        if (date != null) {
            GlobalScope.launch {
                var code: Int? = 0
                var response: String? = null
                var rawEvents: List<RawEvent>? = null

                getJournal(date) { c, r ->
                    code = c
                    response = r?.string()
                    // capture response here: println(response)
                }
                if (code == 401) return@launch
                if (code == 200)
                    rawEvents = response?.let { parseResponse(it) }

                crawlJournal(code, rawEvents)

                if (code == 200 && mCrawlerType == CrawlerType.CURRENT_JOURNAL) {
                    mCrawlerType = CrawlerType.CURRENT_DISCOVERIES
                    mCurrentDiscoveriesDate = mLatestJournalDate
                    crawlJournal(code, rawEvents)
                }
            }
        }
    }

    private suspend fun getJournal(date: Date, callback: (c: Int?, r: ResponseBody?) -> Unit) {
        val dateString: String = SimpleDateFormat("yyyy/MM/dd", Locale.ROOT).format(date)
        println("LOG: Get journal: $dateString")

        try {
            val result = mFrontierApi.getJournal(dateString)?.getResult()
            callback.invoke(result?.first, result?.second)
        } catch (e: Exception) {
            println(e.message.toString())
            callback.invoke(1313, null)
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
                        if (lastJournalDate == null || raw.timeStamp > lastJournalDate)
                            lastJournalDate = raw.timeStamp

                    } catch (e: Exception) {
                        println("LOG: Error parsing journal event, " + e.message)
                        println("LOG: Event: " + it.trim())
                    }
                }

            println("Journal events present: " + rawEvents.size)
        }
        return rawEvents
    }

    private suspend fun crawlJournal(code: Int?, rawEvents: List<RawEvent>?) {
        withContext(Dispatchers.IO) {

            val journalDate = when (mCrawlerType) {
                CrawlerType.CURRENT_JOURNAL -> mLatestJournalDate!!
                else -> mCurrentDiscoveriesDate
            }
            println("LOG: Process journal: $journalDate type: $mCrawlerType")

            when (code) {
                200 -> {
                    handleCrawlResult(journalDate, rawEvents)
                    return@withContext
                }
                204 -> handleCrawlNoResult(journalDate)
                206, 429 -> {
                    // Partial content, wait and try again / // Too many request, wait and try again
                    delay(1000)
                    processJournal(journalDate)
                }
                else -> {
                    if (journalDate?.coerceAtLeast(DateUtils.eliteStartDate) == DateUtils.eliteStartDate) {
                        println(
                            "LOG: Cannot get journals earlier than the date cap ${
                                journalDate.toDateString(SHORT)
                            }"
                        )
                        return@withContext
                    }
                }
            }
        }
    }

    private suspend fun handleCrawlResult(journalDate: Date?, rawEvents: List<RawEvent>?) {
        if (mCrawlerType == CrawlerType.CURRENT_JOURNAL) {
            mJournalRanks.raiseFrontierRanksEvent(rawEvents!!)
            mJournalStats.raiseFrontierStatisticsEvent(rawEvents)
            mLatestJournalDate = journalDate
        } else if (mCrawlerType == CrawlerType.CURRENT_DISCOVERIES) {
            mJournalDiscoveries.raiseFrontierDiscoveriesEvents(rawEvents!!)
            mCurrentDiscoveriesDate = journalDate
        }
    }

    private fun handleCrawlNoResult(journalDate: Date?) {
        // No content, go one day back
        val date = journalDate?.removeDays()
        processJournal(date)

        when (mCrawlerType) {
            CrawlerType.CURRENT_JOURNAL -> mLatestJournalDate = date
            else -> mCurrentDiscoveriesDate = date
        }
    }

    companion object {

        private enum class CrawlerType {
            CURRENT_JOURNAL,
            CURRENT_DISCOVERIES
        }

        var mIgnoreEvents =
            arrayOf(
                "ApproachBody",
                "ApproachSettlement",
                "BuyAmmo",
                "BuyDrones",
                "BuyTradeData",
                "Cargo",
                "CargoDepot",
                "CockpitBreached",
                "CodexEntry",
                "CollectCargo",
                "Commander",
                "CommitCrime",
                "CommunityGoal",
                "CommunityGoalJoin",
                "CommunityGoalReward",
                "DatalinkScan",
                "DatalinkVoucher",
                "Docked",
                "DockingCancelled",
                "DockingDenied",
                "DockingGranted",
                "DockingRequested",
                "DockSRV",
                "EjectCargo",
                "EngineerContribution",
                "EngineerCraft",
                "EngineerProgress",
                "EscapeInterdiction",
                "FSSAllBodiesFound",
                "FSSDiscoveryScan",
                "FSDJump",
                "FSDTarget",
                "FSSSignalDiscovered",
                "FuelScoop",
                "HeatDamage",
                "HeatWarning",
                "HullDamage",
                "Interdicted",
                "Interdiction",
                "LaunchDrone",
                "LaunchSRV",
                "LeaveBody",
                "Loadout",
                "Liftoff",
                "LoadGame",
                "Location",
                "Market",
                "MarketBuy",
                "MarketSell",
                "MaterialCollected",
                "Materials",
                "MaterialTrade",
                "ModuleInfo",
                "MissionAbandoned",
                "MissionAccepted",
                "MissionFailed",
                "MissionRedirected",
                "Missions",
                "ModuleBuy",
                "ModuleRetrieve",
                "ModuleSell",
                "ModuleSellRemote",
                "ModuleStore",
                "ModuleSwap",
                "Music",
                "NavBeaconScan",
                "NavRoute",
                "Outfitting",
                "Passengers",
                "PayBounties",
                "PayFines",
                "Powerplay",
                "PowerplaySalary",
                "RebootRepair",
                "ReceiveText",
                "RefuelAll",
                "Repair",
                "RepairAll",
                "ReservoirReplenished",
                "Resurrect",
                "SAASignalsFound",
                "Scanned",
                "SearchAndRescue",
                "SellDrones",
                "SetUserShipName",
                "ShipTargeted",
                "Shipyard",
                "ShipyardSwap",
                "ShipyardTransfer",
                "StartJump",
                "StoredModules",
                "StoredShips",
                "SupercruiseEntry",
                "SupercruiseExit",
                "SquadronCreated",
                "SquadronStartup",
                "Touchdown",
                "UnderAttack",
                "Undocked",
                "USSDrop"
            )

        internal fun sendWorkerEvent(data: Any?) {
            EventBus.getDefault().post(data)
        }
    }

    internal class RawEvent(value: String) {
        var event: String
        var timeStamp: Date
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
            val timestampString = json.get("timestamp").asString
            timeStamp = DateUtils.fromDateString(timestampString, GMT)
        }
    }
}