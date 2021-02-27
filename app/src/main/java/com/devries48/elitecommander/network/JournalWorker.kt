package com.devries48.elitecommander.network

import com.devries48.elitecommander.App
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.getResult
import com.devries48.elitecommander.declarations.toStringOrEmpty
import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.interfaces.FrontierInterface
import com.devries48.elitecommander.models.response.FrontierJournalRankProgressResponse
import com.devries48.elitecommander.models.response.FrontierJournalRankReputationResponse
import com.devries48.elitecommander.models.response.FrontierJournalRankResponse
import com.devries48.elitecommander.models.response.FrontierJournalStatisticsResponse
import com.devries48.elitecommander.utils.DateUtils
import com.devries48.elitecommander.utils.DateUtils.removeDays
import com.devries48.elitecommander.utils.DateUtils.toDateString
import com.devries48.elitecommander.utils.DiscoveryValueCalculator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class JournalWorker(frontierApi: FrontierInterface?) {

    init {
        if (frontierApi != null) {
            mFrontierApi = frontierApi
        }
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
                }
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

            var journalDate = when (mCrawlerType) {
                CrawlerType.CURRENT_JOURNAL -> mLatestJournalDate!!
                else -> mCurrentDiscoveriesDate
            }
            println("LOG: Process journal: $journalDate type: $mCrawlerType")

            if (code == 200) {
                if (mCrawlerType == CrawlerType.CURRENT_JOURNAL) {
                    raiseFrontierRanksEvent(rawEvents!!)
                    raiseFrontierStatisticsEvent(rawEvents)
                    mLatestJournalDate = journalDate
                } else if (mCrawlerType == CrawlerType.CURRENT_DISCOVERIES) {
                    raiseFrontierDiscoveriesEvents(rawEvents!!)
                    mCurrentDiscoveriesDate = journalDate
                }
            } else {
                when (code) {
                    204 ->  // No content, go one day back
                    {
                        journalDate = journalDate?.removeDays()
                        processJournal(journalDate)

                        when (mCrawlerType) {
                            CrawlerType.CURRENT_JOURNAL -> mLatestJournalDate = journalDate
                            else -> mCurrentDiscoveriesDate = journalDate
                        }
                    }
                    206 -> // Partial content, wait and try again...
                    {
                        delay(1000)
                        processJournal(journalDate)
                    }
                    429 -> // Too many request, wait and try again
                    {
                        delay(1000)
                        processJournal(journalDate)
                    }

                    else -> {
                        if (journalDate?.coerceAtLeast(DateUtils.eliteStartDate) == DateUtils.eliteStartDate) {
                            println(
                                "LOG: Cannot get journals earlier than the date cap ${
                                    journalDate.toDateString(
                                        DateUtils.shortDateFormat
                                    )
                                }"
                            )
                            return@withContext
                        }
                    }
                }

            }
        }
    }

    private suspend fun raiseFrontierRanksEvent(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {
            if (mEventCache.sendCachedRanksEvent()) return@withContext // Raise cached event if available
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

                mEventCache.setRanksEvent(
                    FrontierRanksEvent(
                        true, combatRank, tradeRank, exploreRank,
                        cqcRank, federationRank, empireRank, allianceRank
                    )
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
    }

    private suspend fun raiseFrontierStatisticsEvent(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {
            if (mEventCache.sendCachedStatisticsEvent()) return@withContext // Raise cached event if available

            try {
                val rawStatistics = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_STATISTICS }
                    ?: throw error("Error parsing statistics event from journal")

                val statistics = Gson().fromJson(rawStatistics.json, FrontierJournalStatisticsResponse::class.java)

                mEventCache.setStatisticsEvent(
                    FrontierStatisticsEvent(
                        true, FrontierBankAccount(
                            statistics.bankAccount.currentWealth,
                            statistics.bankAccount.insuranceClaims,
                            statistics.bankAccount.ownedShipCount,
                            statistics.bankAccount.spentOnAmmoConsumables,
                            statistics.bankAccount.spentOnFuel,
                            statistics.bankAccount.spentOnInsurance,
                            statistics.bankAccount.spentOnOutfitting,
                            statistics.bankAccount.spentOnRepairs,
                            statistics.bankAccount.spentOnShips
                        ), FrontierCombat(
                            statistics.combat.assassinationProfits,
                            statistics.combat.assassinations,
                            statistics.combat.bountiesClaimed,
                            statistics.combat.bountyHuntingProfit,
                            statistics.combat.combatBondProfits,
                            statistics.combat.combatBonds,
                            statistics.combat.highestSingleReward,
                            statistics.combat.skimmersKilled
                        ), FrontierExploration(
                            statistics.exploration.efficientScans,
                            statistics.exploration.explorationProfits,
                            statistics.exploration.greatestDistanceFromStart,
                            statistics.exploration.highestPayout,
                            statistics.exploration.planetsScannedToLevel2,
                            statistics.exploration.planetsScannedToLevel3,
                            statistics.exploration.systemsVisited,
                            statistics.exploration.timePlayed,
                            statistics.exploration.totalHyperspaceDistance,
                            statistics.exploration.totalHyperspaceJumps
                        )
                            , FrontierMining(
                    statistics.mining.materialsCollected,
                            statistics.mining.miningProfits,
                            statistics.mining.quantityMined
                )
                ,
                        FrontierTrading(
                                statistics.trading.averageProfit,
                            statistics.trading.highestSingleTransaction,
                            statistics.trading.marketProfits,
                            statistics.trading.marketsTradedWith,
                            statistics.trading.resourcesTraded
                        )
                    )

                )

            } catch (e: Exception) {
                println("LOG: Error parsing statistics event from journal." + e.message)
                mEventCache.sendEvent(
                    FrontierStatisticsEvent(false, null, null, null, null, null)
                )
            }

        }
    }

    private suspend fun raiseFrontierDiscoveriesEvents(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {
            if (mEventCache.sendCachedCurrentDiscoveriesEvent()) return@withContext // Raise cached event if available

            try {
                var rawDiscoveries = rawEvents.filter { it.event == JOURNAL_EVENT_DISCOVERY }
                val rawMappings = rawEvents.filter { it.event == JOURNAL_EVENT_MAP }

                val rawDataSold = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_DISCOVERIES_SOLD }
                if (rawDataSold != null) rawDiscoveries = rawDiscoveries.filter { it.timeStamp > rawDataSold.timeStamp }

                if (rawDiscoveries.count() == 0) {
                    mEventCache.sendEvent(FrontierDiscoveriesEvent(true, null, null))
                }

                val discoveries = mutableListOf<Discovery>()
                val mappings = mutableListOf<Mapping>()

                // Format mappings, so it can be merged into the FrontierDiscovery class
                if (rawMappings.count() > 0) {
                    rawMappings.forEach {
                        mappings.add(Gson().fromJson(it.json, Mapping::class.java))
                    }
                }

                val summary = FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

                rawDiscoveries.forEach { d ->
                    val discovery = Gson().fromJson(d.json, Discovery::class.java)

                    // Skip asteroid belt's as they bring no profit or further interest
                    if (discovery.planetClass.isNullOrEmpty() && discovery.starType.isNullOrEmpty())
                        return@forEach

                    val map = mappings.firstOrNull {
                        it.systemAddress == discovery.systemAddress && it.bodyID == discovery.bodyID
                    }

                    var addMapCount = 0
                    var addBonusCount = 0
                    var addFirstDiscovered = 0
                    var addFirstMapped = 0
                    var addFirstDiscoveredAndMapped = 0
                    var addProbeCount = 0
                    var hasEfficiencyBonus = false

                    if (!discovery.wasDiscovered) addFirstDiscovered += 1

                    if (map != null) {
                        addProbeCount += map.probesUsed
                        addMapCount += 1

                        if (map.efficiencyTarget >= map.probesUsed) {
                            addBonusCount += 1
                            hasEfficiencyBonus = true
                        }

                        if (!discovery.wasMapped) {
                            if (!discovery.wasDiscovered) {
                                addFirstDiscoveredAndMapped += 1
                                addFirstDiscovered -= 1
                            } else {
                                addFirstMapped += 1
                            }
                        }
                    }

                    var currentDiscovery =
                        discoveries.firstOrNull {
                            !it.planetClass.isNullOrEmpty() && it.planetClass == discovery.planetClass ||
                                    !it.starType.isNullOrEmpty() && it.starType == discovery.starType
                        }
                    if (currentDiscovery == null) {
                        currentDiscovery = Discovery(
                            discovery.systemAddress,
                            discovery.bodyID,
                            discovery.planetClass.toStringOrEmpty(),
                            discovery.starType.toStringOrEmpty()
                        )
                        discoveries.add(currentDiscovery)
                    }

                    // Calculate estimated scan values for current body
                    currentDiscovery.mass = discovery.mass
                    currentDiscovery.stellarMass = discovery.stellarMass
                    currentDiscovery.terraformState = discovery.terraformState
                    currentDiscovery.wasDiscovered = discovery.wasDiscovered
                    currentDiscovery.wasMapped = discovery.wasMapped

                    val estimatedValue = DiscoveryValueCalculator.calculate(
                        currentDiscovery,
                        map != null,
                        hasEfficiencyBonus
                    )

                    currentDiscovery.discoveryCount += 1 - addFirstDiscovered - addFirstDiscoveredAndMapped
                    currentDiscovery.mappedCount += addMapCount - addFirstMapped - addFirstDiscoveredAndMapped
                    currentDiscovery.bonusCount += addBonusCount
                    currentDiscovery.firstDiscoveredCount += addFirstDiscovered
                    currentDiscovery.firstMappedCount += addFirstMapped
                    currentDiscovery.firstDiscoveredAndMappedCount += addFirstDiscoveredAndMapped
                    currentDiscovery.estimatedValue += estimatedValue

                    summary.DiscoveryTotal += 1 - addFirstDiscovered - addFirstDiscoveredAndMapped
                    summary.MappedTotal += addMapCount - addFirstMapped - addFirstDiscoveredAndMapped
                    summary.efficiencyBonusTotal += addBonusCount
                    summary.firstDiscoveryTotal += addFirstDiscovered
                    summary.firstMappedTotal += addFirstMapped
                    summary.firstDiscoveredAndMappedTotal += addFirstDiscoveredAndMapped
                    summary.probesUsedTotal += addProbeCount
                    summary.estimatedValue += estimatedValue
                }

                mEventCache.sendEvent(FrontierDiscoveriesEvent(
                    true,
                    summary,
                    discoveries.map { (_, _, planetClass, starType, _, _, _, _, _, discoveryCount, mapCount, bonusCount, firstDiscoveredCount, firstMappedCount, firstMappedAndDiscovered, estimatedValue) ->
                        FrontierDiscovery(
                            planetClass.toStringOrEmpty(),
                            starType.toStringOrEmpty(),
                            discoveryCount,
                            mapCount,
                            bonusCount,
                            firstDiscoveredCount,
                            firstMappedCount,
                            firstMappedAndDiscovered,
                            estimatedValue
                        )
                    }
                        .sortedWith(compareBy<FrontierDiscovery> { it.discoveryCount + it.firstDiscoveredCount + it.firstDiscoveredAndMappedCount }.thenBy { it.body }
                            .thenBy { it.star })
                )
                )

            } catch (e: Exception) {
                println("LOG: Error parsing discovery events from journal." + e.message)
                mEventCache.sendEvent(FrontierDiscoveriesEvent(false, null, null))
            }
        }
    }

    companion object {

        private enum class CrawlerType {
            CURRENT_JOURNAL,
            CURRENT_DISCOVERIES
        }

        private const val JOURNAL_EVENT_STATISTICS = "Statistics"
        private const val JOURNAL_EVENT_RANK = "Rank"
        private const val JOURNAL_EVENT_PROGRESS = "Progress"
        private const val JOURNAL_EVENT_REPUTATION = "Reputation"
        private const val JOURNAL_EVENT_DISCOVERY = "Scan"
        private const val JOURNAL_EVENT_MAP = "SAAScanComplete"
        private const val JOURNAL_EVENT_DISCOVERIES_SOLD = "MultiSellExplorationData"

        var mIgnoreEvents =
            arrayOf(
                "ApproachBody",
                "ApproachSettlement",
                "BuyAmmo",
                "BuyDrones",
                "Cargo",
                "CargoDepot",
                "Commander",
                "CommunityGoal",
                "Docked",
                "DockingGranted",
                "DockingRequested",
                "EngineerProgress",
                "FSSAllBodiesFound",
                "FSSDiscoveryScan",
                "FSDJump",
                "FSDTarget",  // RemainingJumpsInRoute (multiple)
                "FSSSignalDiscovered",
                "FuelScoop",
                "HullDamage",
                "LeaveBody",
                "Loadout",
                "Liftoff",
                "LoadGame",
                "Location",
                "MaterialCollected",
                "Materials",
                "MissionAccepted",
                "Missions",
                "Music",
                "NavBeaconScan",
                "NavRoute",
                "Outfitting",
                "Powerplay",
                "ReceiveText",
                "ReservoirReplenished",
                "RefuelAll",
                "RepairAll",
                "SAASignalsFound",
                "Scanned",
                "ShipTargeted",
                "Shipyard",
                "ShipyardTransfer",
                "StartJump",
                "StoredModules",
                "StoredShips",
                "SupercruiseEntry",
                "SupercruiseExit",
                "Touchdown",
                "UnderAttack",
                "Undocked"
            )

        private var mEventCache: EventCache = EventCache()
        private lateinit var mFrontierApi: FrontierInterface
        private lateinit var mCrawlerType: CrawlerType
        private var mLatestJournalDate: Date? = DateUtils.getCurrentDate()
        private var mCurrentDiscoveriesDate: Date? = null

    }

    private class RawEvent(value: String) {
        var event: String
        var timeStamp: Date
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
            val timestampString = json.get("timestamp").asString
            timeStamp = DateUtils.fromDateString(timestampString, DateUtils.journalDateFormat)
        }
    }

    private class EventCache {
        private var ranksEvent: FrontierRanksEvent? = null
        private var statisticsEvent: FrontierStatisticsEvent? = null
        private var currentDiscoveriesEvent: FrontierDiscoveriesEvent? = null

        fun setRanksEvent(event: FrontierRanksEvent) {
            ranksEvent = event
            sendEvent(event)
        }

        fun setStatisticsEvent(event: FrontierStatisticsEvent) {
            statisticsEvent = event
            sendEvent(event)
        }

        fun setDiscoveriesEvent(event: FrontierDiscoveriesEvent) {
            currentDiscoveriesEvent = event
            sendEvent(event)
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

        fun sendCachedCurrentDiscoveriesEvent(): Boolean {
            return if (currentDiscoveriesEvent != null) {
                sendEvent(currentDiscoveriesEvent)
                true
            } else false
        }

        fun sendCachedStatisticsEvent(): Boolean {
            return if (statisticsEvent != null) {
                sendEvent(statisticsEvent)
                true
            } else false
        }

    }

    data class Discovery(
        @SerializedName("SystemAddress")
        val systemAddress: Long,
        @SerializedName("BodyID")
        val bodyID: Int,
        @SerializedName("PlanetClass")
        val planetClass: String?,
        @SerializedName("StarType")
        val starType: String?,
        @SerializedName("WasDiscovered")
        var wasDiscovered: Boolean = false,
        @SerializedName("WasMapped")
        var wasMapped: Boolean = false,
        @SerializedName("MassEM")
        var mass: Double? = 0.0,
        @SerializedName("StellarMass")
        var stellarMass: Double? = 0.0,
        @SerializedName("TerraformState")
        var terraformState: String? = "",

        var discoveryCount: Int = 0,
        var mappedCount: Int = 0,
        var bonusCount: Int = 0,
        var firstDiscoveredCount: Int = 0,
        var firstMappedCount: Int = 0,
        var firstDiscoveredAndMappedCount: Int = 0,
        var estimatedValue: Long = 0
    )

    private data class Mapping(
        @SerializedName("SystemAddress")
        val systemAddress: Long,
        @SerializedName("BodyID")
        val bodyID: Int,
        @SerializedName("EfficiencyTarget")
        val efficiencyTarget: Int,
        @SerializedName("ProbesUsed")
        val probesUsed: Int
    )

}

