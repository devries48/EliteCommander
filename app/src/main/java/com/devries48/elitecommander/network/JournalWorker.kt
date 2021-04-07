package com.devries48.elitecommander.network

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

    private lateinit var mFrontierApi: FrontierInterface
    private lateinit var mCrawlerType: CrawlerType
    private var mLatestJournalDate: Date? = DateUtils.getCurrentDate()
    private var mCurrentDiscoveriesDate: Date? = null

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
                        if (raw.event !in mIgnoreEvents) {
                            rawEvents.add(raw)
                            println(raw.event)
                        }
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
                                journalDate.toDateString(
                                    DateUtils.dateFormatShort
                                )
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
            raiseFrontierRanksEvent(rawEvents!!)
            raiseFrontierStatisticsEvent(rawEvents)
            mLatestJournalDate = journalDate
        } else if (mCrawlerType == CrawlerType.CURRENT_DISCOVERIES) {
            raiseFrontierDiscoveriesEvents(rawEvents!!)
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

    private suspend fun raiseFrontierRanksEvent(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {

            try {
                val rawRank = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_RANK }
                val rawProgress = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_PROGRESS }
                val rawReputation = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_REPUTATION }

                if (rawRank == null || rawProgress == null || rawReputation == null) throw error("Error parsing rank events from journal")

                val rank = getRank(rawRank, rawEvents)
                val progress = Gson().fromJson(rawProgress.json, FrontierJournalRankProgressResponse::class.java)
                val reputation = Gson().fromJson(rawReputation.json, FrontierJournalRankReputationResponse::class.java)

                val combatRank = FrontierRanksEvent.FrontierRank(
                    rank.combat,
                    progress.combat
                )
                val tradeRank = FrontierRanksEvent.FrontierRank(
                    rank.trade,
                    progress.trade
                )
                val exploreRank = FrontierRanksEvent.FrontierRank(
                    rank.explore,
                    progress.explore
                )
                val cqcRank = FrontierRanksEvent.FrontierRank(
                    rank.cqc,
                    progress.cqc
                )
                val federationRank = FrontierRanksEvent.FrontierRank(
                    rank.federation,
                    progress.federation,
                    reputation.federation
                )
                val empireRank = FrontierRanksEvent.FrontierRank(
                    rank.empire,
                    progress.empire,
                    reputation.empire
                )

                val allianceRank = FrontierRanksEvent.FrontierRank(
                    rank.alliance,
                    0,
                    reputation.alliance
                )

                sendEvent(
                    FrontierRanksEvent(
                        true, combatRank, tradeRank, exploreRank,
                        cqcRank, federationRank, empireRank, allianceRank
                    )
                )
            } catch (e: Exception) {
                println("LOG: Error parsing ranking events from journal." + e.message)
                sendEvent(
                    FrontierRanksEvent(
                        false, null, null,
                        null, null, null, null
                    )
                )
            }
        }
    }

    private fun getRank(rawRank: RawEvent, rawEvents: List<RawEvent>): FrontierJournalRankResponse {
        val rank = Gson().fromJson(rawRank.json, FrontierJournalRankResponse::class.java)

        rawEvents.filter { it.event == JOURNAL_EVENT_PROMOTION }.forEach {
            val promotion = Gson().fromJson(it.json, FrontierJournalRankProgressResponse::class.java)

            if (promotion.combat > 0) rank.combat = promotion.combat
            if (promotion.cqc > 0) rank.cqc = promotion.cqc
            if (promotion.empire > 0) rank.empire = promotion.empire
            if (promotion.explore > 0) rank.explore = promotion.explore
            if (promotion.federation > 0) rank.federation = promotion.federation
            if (promotion.trade > 0) rank.trade = promotion.trade
        }
        return rank
    }

    private suspend fun raiseFrontierStatisticsEvent(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {
            try {
                val rawStatistics = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_STATISTICS }
                    ?: throw error("Error parsing statistics event from journal")

                val statistics = Gson().fromJson(rawStatistics.json, FrontierJournalStatisticsResponse::class.java)

                sendEvent(
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
                        ), FrontierMining(
                            statistics.mining.materialsCollected,
                            statistics.mining.miningProfits,
                            statistics.mining.quantityMined
                        ),
                        FrontierTrading(
                            statistics.trading.averageProfit,
                            statistics.trading.highestSingleTransaction,
                            statistics.trading.marketProfits,
                            statistics.trading.marketsTradedWith,
                            statistics.trading.resourcesTraded
                        ),
                        FrontierSmuggling(
                            statistics.smuggling.averageProfit,
                            statistics.smuggling.blackMarketsProfits,
                            statistics.smuggling.blackMarketsTradedWith,
                            statistics.smuggling.highestSingleTransaction,
                            statistics.smuggling.resourcesSmuggled
                        ),
                        FrontierSearchAndRescue(
                            statistics.searchAndRescue.searchRescueCount,
                            statistics.searchAndRescue.searchRescueProfit,
                            statistics.searchAndRescue.searchRescueTraded
                        )
                    )
                )

            } catch (e: Exception) {
                println("LOG: Error parsing statistics event from journal." + e.message)
                sendEvent(
                    FrontierStatisticsEvent(false, null, null, null, null, null, null, null)
                )
            }

        }
    }

    private suspend fun raiseFrontierDiscoveriesEvents(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {

            try {
                val rawDiscoveries = getRawDiscoveries(rawEvents)
                if (rawDiscoveries.count() == 0) return@withContext

                val rawMappings = rawEvents.filter { it.event == JOURNAL_EVENT_MAP }
                val discoveries = mutableListOf<Discovery>()
                val mappings = mutableListOf<Mapping>()

                // Format mappings, so it can be merged into the FrontierDiscovery class
                if (rawMappings.count() > 0) {
                    rawMappings.forEach {
                        mappings.add(Gson().fromJson(it.json, Mapping::class.java))
                    }
                }

                val summary = FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

                rawDiscoveries.forEach { event ->
                    processDiscovery(event,  discoveries, mappings, summary)
                }

                sendEvent(FrontierDiscoveriesEvent(
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
                sendEvent(FrontierDiscoveriesEvent(false, null, null))
            }
        }
    }

    private fun processDiscovery(
        event: RawEvent,
        discoveries: MutableList<Discovery>,
        mappings: MutableList<Mapping>,
        summary: FrontierDiscoverySummary
    ) {
        val discovery = Gson().fromJson(event.json, Discovery::class.java)

        // Skip asteroid belt's as they bring no profit or further interest
        if (discovery.planetClass.isNullOrEmpty() && discovery.starType.isNullOrEmpty())
            return

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

        if (!discovery.wasDiscovered)
            addFirstDiscovered += 1

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

        var currentDiscovery = getCurrentDiscovery(discoveries, discovery)
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

        summary.discoveryTotal += 1 - addFirstDiscovered - addFirstDiscoveredAndMapped
        summary.mappedTotal += addMapCount - addFirstMapped - addFirstDiscoveredAndMapped
        summary.efficiencyBonusTotal += addBonusCount
        summary.firstDiscoveryTotal += addFirstDiscovered
        summary.firstMappedTotal += addFirstMapped
        summary.firstDiscoveredAndMappedTotal += addFirstDiscoveredAndMapped
        summary.probesUsedTotal += addProbeCount
        summary.estimatedValue += estimatedValue
    }

    private fun getCurrentDiscovery(discoveries: MutableList<Discovery>, discovery: Discovery): Discovery? {
        return discoveries.firstOrNull {
            !it.planetClass.isNullOrEmpty() && it.planetClass == discovery.planetClass ||
                    !it.starType.isNullOrEmpty() && it.starType == discovery.starType
        }

    }

    private fun getRawDiscoveries(rawEvents: List<RawEvent>): List<RawEvent> {
        var rawDiscoveries = rawEvents.filter { it.event == JOURNAL_EVENT_DISCOVERY }

        val rawDataSold = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_DISCOVERIES_SOLD }
        if (rawDataSold != null) rawDiscoveries = rawDiscoveries.filter { it.timeStamp > rawDataSold.timeStamp }

        val rawDied = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_DIED }
        if (rawDied != null) rawDiscoveries = rawDiscoveries.filter { it.timeStamp > rawDied.timeStamp }

        if (rawDiscoveries.count() == 0) sendEvent(FrontierDiscoveriesEvent(true, null, null))

        return rawDiscoveries
    }

    private fun sendEvent(data: Any?) {
        EventBus.getDefault().post(data)
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
        private const val JOURNAL_EVENT_DIED = "Died"
        private const val JOURNAL_EVENT_PROMOTION = "Promotion"

        var mIgnoreEvents =
            arrayOf(
                "ApproachBody",
                "ApproachSettlement",
                "Bounty",
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
                "FactionKillBond",
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
                "MissionCompleted",
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
                "RedeemVoucher",
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
    }

    private class RawEvent(value: String) {
        var event: String
        var timeStamp: Date
        val json: JsonObject = JsonParser.parseString("{$value}").asJsonObject

        init {
            event = json.get("event").asString
            val timestampString = json.get("timestamp").asString
            timeStamp = DateUtils.fromDateString(timestampString, DateUtils.dateFormatGMT)
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
        var wasDiscovered: Boolean = true,
        @SerializedName("WasMapped")
        var wasMapped: Boolean = true,
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

