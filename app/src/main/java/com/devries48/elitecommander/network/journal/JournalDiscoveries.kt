package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.declarations.toStringOrEmpty
import com.devries48.elitecommander.events.FrontierDiscoveriesEvent
import com.devries48.elitecommander.events.FrontierDiscovery
import com.devries48.elitecommander.events.FrontierDiscoverySummary
import com.devries48.elitecommander.network.journal.JournalWorker.*
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.devries48.elitecommander.utils.DiscoveryValueCalculator
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@DelicateCoroutinesApi
class JournalDiscoveries {

    private var mSummary = FrontierDiscoverySummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private val mDiscoveries = mutableListOf<Discovery>()
    internal var isCompleted = false

    internal suspend fun processDiscoveries(rawEvents: List<RawEvent>) {
        withContext(Dispatchers.IO) {

            try {
                val rawDiscoveries = getRawDiscoveries(rawEvents)
                if (rawDiscoveries.count() == 0) return@withContext

                val rawMappings = rawEvents.filter { it.event == JOURNAL_EVENT_MAP }
                val mappings = mutableListOf<Mapping>()

                // Format mappings, so it can be merged into the FrontierDiscovery class
                if (rawMappings.count() > 0) {
                    rawMappings.forEach {
                        mappings.add(Gson().fromJson(it.json, Mapping::class.java))
                    }
                }

                rawDiscoveries.forEach { event ->
                    processDiscovery(event, mappings)
                }

            } catch (e: Exception) {
                sendWorkerEvent(
                    FrontierDiscoveriesEvent(
                        false,
                        "Error parsing discovery events from journal: " + e.message,
                        null,
                        null
                    )
                )
            }
        }
    }

    internal suspend fun raiseFrontierDiscoveriesEvents() {
        withContext(Dispatchers.IO) {

            if (mDiscoveries.count() == 0)
                sendWorkerEvent(FrontierDiscoveriesEvent(true, null, null, null))
            else
                sendWorkerEvent(
                    FrontierDiscoveriesEvent(
                        true,
                        null,
                        mSummary,
                        mDiscoveries.map { (_, _, planetClass, starType, _, _, _, _, _, discoveryCount, mapCount, bonusCount, firstDiscoveredCount, firstMappedCount, firstMappedAndDiscovered, estimatedValue) ->
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

        }
    }

    private fun processDiscovery(
        event: RawEvent,
        mappings: MutableList<Mapping>,
    ) {
        val discovery = Gson().fromJson(event.json, Discovery::class.java)

        // Skip asteroid belt's as they bring no profit or further interest
        if (discovery.planetClass.isNullOrEmpty() && discovery.starType.isNullOrEmpty())
            return

        // I regularly experienced a swap in the result, so fix it here
        if (!discovery.wasDiscovered && discovery.wasMapped) discovery.wasDiscovered = true

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

        var currentDiscovery = getCurrentDiscovery(discovery)
        if (currentDiscovery == null) {
            currentDiscovery = Discovery(
                discovery.systemAddress,
                discovery.bodyID,
                discovery.planetClass.toStringOrEmpty(),
                discovery.starType.toStringOrEmpty()
            )
            mDiscoveries.add(currentDiscovery)
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

        mSummary.discoveryTotal += 1 - addFirstDiscovered - addFirstDiscoveredAndMapped
        mSummary.mappedTotal += addMapCount - addFirstMapped - addFirstDiscoveredAndMapped
        mSummary.efficiencyBonusTotal += addBonusCount
        mSummary.firstDiscoveryTotal += addFirstDiscovered
        mSummary.firstMappedTotal += addFirstMapped
        mSummary.firstDiscoveredAndMappedTotal += addFirstDiscoveredAndMapped
        mSummary.probesUsedTotal += addProbeCount
        mSummary.estimatedValue += estimatedValue
    }

    private fun getCurrentDiscovery(discovery: Discovery): Discovery? {
        return mDiscoveries.firstOrNull {
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

        isCompleted = rawDataSold != null || rawDied != null

        return rawDiscoveries
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

    companion object {
        private const val JOURNAL_EVENT_DISCOVERY = "Scan"
        private const val JOURNAL_EVENT_MAP = "SAAScanComplete"
        private const val JOURNAL_EVENT_DISCOVERIES_SOLD = "MultiSellExplorationData"
        private const val JOURNAL_EVENT_DIED = "Died"
    }

}