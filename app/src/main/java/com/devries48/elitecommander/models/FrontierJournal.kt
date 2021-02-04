package com.devries48.elitecommander.models

import android.content.Context
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.toStringOrEmpty
import com.devries48.elitecommander.events.FrontierDiscoveriesEvent
import com.devries48.elitecommander.events.FrontierDiscovery
import com.devries48.elitecommander.events.FrontierDiscoverySummary
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName

//TODO: Cache result and cleanup raw events
class FrontierJournal {

    fun parseResponse(response: String) {

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

    fun getRanks(context: Context): FrontierRanksEvent {
        try {
            val rawRank = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_RANK }
            val rawProgress = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_PROGRESS }

            if (rawRank == null || rawProgress == null) {
                throw error("Error parsing rank events from journal")
            }

            val rank = Gson().fromJson(rawRank.json, FrontierJournalRankResponse::class.java)
            val progress =
                Gson().fromJson(rawProgress.json, FrontierJournalRankProgressResponse::class.java)

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
                progress.federation
            )
            val empireRank = FrontierRanksEvent.FrontierRank(
                context.resources.getStringArray(R.array.ranks_empire)[rank.empire],
                rank.empire,
                progress.empire
            )

            return FrontierRanksEvent(
                true, combatRank, tradeRank, exploreRank,
                cqcRank, federationRank, empireRank
            )
        } catch (e: Exception) {
            println("LOG: Error parsing ranking events from journal." + e.message)
            return FrontierRanksEvent(
                false, null, null,
                null, null, null, null
            )
        }
    }

    fun getCurrentDiscoveries(): FrontierDiscoveriesEvent {
        try {
            val rawDiscoveries = mRawEvents.filter { it.event == JOURNAL_EVENT_DISCOVERY }
            val rawMappings = mRawEvents.filter { it.event == JOURNAL_EVENT_MAP }

            if (rawDiscoveries.count() == 0) {
                return FrontierDiscoveriesEvent(true, null, null)
            }

            val discoveries = mutableListOf<Discovery>()
            val mappings = mutableListOf<Mapping>()

            // Format mappings, so it can be merged into the FrontierDiscovery class
            if (rawMappings.count() > 0) {
                rawMappings.forEach {
                    mappings.add(Gson().fromJson(it.json, Mapping::class.java))
                }
            }

            val summary = FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0)

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

                if (!discovery.wasDiscovered) addFirstDiscovered += 1

                if (map != null) {
                    addProbeCount += map.probesUsed
                    addMapCount += 1

                    if (map.efficiencyTarget >= map.probesUsed)
                        addBonusCount += 1

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
                        discovery.starType.toStringOrEmpty(),
                        discovery.wasDiscovered,
                        discovery.wasMapped
                    )
                    discoveries.add(currentDiscovery)
                }

                currentDiscovery.discoveryCount += 1
                currentDiscovery.mappedCount += addMapCount
                currentDiscovery.bonusCount += addBonusCount
                currentDiscovery.firstDiscoveredCount += addFirstDiscovered
                currentDiscovery.firstMappedCount += addFirstMapped
                currentDiscovery.firstDiscoveredAndMappedCount += addFirstDiscoveredAndMapped

                summary.DiscoveryTotal += 1
                summary.MappedTotal += addMapCount
                summary.efficiencyBonusTotal += addBonusCount
                summary.firstDiscoveryTotal += addFirstDiscovered
                summary.firstMappedTotal += addFirstMapped
                summary.firstDiscoveredAndMappedTotal += addFirstDiscoveredAndMapped
                summary.probesUsedTotal += addProbeCount
            }

            return FrontierDiscoveriesEvent(
                true,
                summary,
                discoveries.map { (_, _, planetClass, starType, _, _, discoveryCount, mapCount, bonusCount, firstDiscoveredCount, firstMappedCount, firstMappedAndDiscovered) ->
                    FrontierDiscovery(
                        planetClass.toStringOrEmpty(),
                        starType.toStringOrEmpty(),
                        discoveryCount,
                        mapCount,
                        bonusCount,
                        firstDiscoveredCount,
                        firstMappedCount,
                        firstMappedAndDiscovered
                    )
                }.sortedWith(compareBy<FrontierDiscovery> { it.discoveryCount }.thenBy { it.body }
                    .thenBy { it.star })
            )

        } catch (e: Exception) {
            println("LOG: Error parsing discovery events from journal." + e.message)
            return FrontierDiscoveriesEvent(false, null, null)
        }

    }

    private data class Discovery(
        @SerializedName("SystemAddress")
        val systemAddress: Long,
        @SerializedName("BodyID")
        val bodyID: Int,
        @SerializedName("PlanetClass")
        var planetClass: String?,
        @SerializedName("StarType")
        val starType: String?,
        @SerializedName("WasDiscovered")
        val wasDiscovered: Boolean,
        @SerializedName("WasMapped")
        val wasMapped: Boolean,
        var discoveryCount: Int = 0,
        var mappedCount: Int = 0,
        var bonusCount: Int = 0,
        var firstDiscoveredCount: Int = 0,
        var firstMappedCount: Int = 0,
        var firstDiscoveredAndMappedCount: Int = 0

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

    fun getStatistics(): FrontierJournalStatisticsResponse? {
        val event = mRawEvents.firstOrNull { it.event == JOURNAL_EVENT_STATISTICS }

        if (event != null) {
            return Gson().fromJson(
                event.json,
                FrontierJournalStatisticsResponse::class.java
            )
        }
        return null
    }

    fun getCodexEntries() {

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
                "FuelScoop"
            )

        private val mRawEvents: MutableList<RawEvent> = ArrayList()
    }
}

abstract class FrontierJournalBase

class FrontierJournalStatisticsResponse : FrontierJournalBase() {
    @SerializedName("Bank_Account")
    var bankAccount: BankAccount? = null

    @SerializedName("Combat")
    var combat: Combat? = null

    @SerializedName("Smuggling")
    var smuggling: Smuggling? = null

    inner class BankAccount {
        @SerializedName("Current_Wealth")
        var currentWealth: Long = 0

        @SerializedName("Spent_On_Ships")
        var spentOnShips: Long = 0

        @SerializedName("Spent_On_Outfitting")
        var spentOnOutfitting: Long = 0

        @SerializedName("Spent_On_Repairs")
        var spentOnRepairs: Long = 0

        @SerializedName("Spent_On_Fuel")
        var spentOnFuel: Long = 0

        @SerializedName("spentOnAmmoConsumables")
        var spentOnAmmoConsumables: Long = 0

        @SerializedName("Insurance_Claims")
        var insuranceClaims: Int = 0

        @SerializedName("Spent_On_Insurance")
        var spentOnInsurance: Long = 0

        @SerializedName("Owned_Ship_Count")
        var ownedShipCount: Int = 0
    }

    inner class Combat {
        @SerializedName("Bounties_Claimed")
        var bountiesClaimed: Int = 0

        @SerializedName("Bounty_Hunting_Profit")
        var bountyHuntingProfit: Long = 0

        @SerializedName("Combat_Bonds")
        var combatBonds: Int = 0

        @SerializedName("Combat_Bond_Profits")
        var combatBondProfits: Long = 0

        @SerializedName("Assassinations")
        var assassinations: Int = 0

        @SerializedName("Assassination_Profits")
        var assassinationProfits: Long = 0

        @SerializedName("Highest_Single_Reward")
        var highestSingleReward: Long = 0

        @SerializedName("Skimmers_Killed")
        var skimmersKilled: Int = 0
    }

    inner class Smuggling {
        @SerializedName("Black_Markets_Traded_With")
        var blackMarketsTradedWith: Int = 0

        @SerializedName("Black_Markets_Profits")
        var blackMarketsProfits: Long = 0

        @SerializedName("Resources_Smuggled")
        var resourcesSmuggled: Int = 0

        @SerializedName("Average_Profit")
        var averageProfit: Long = 0

        @SerializedName("Highest_Single_Transaction")
        var highestSingleTransaction: Long = 0
    }
}

