package com.devries48.elitecommander.events

data class FrontierDiscoveriesEvent(
    val success: Boolean = false,
    val summary: FrontierDiscoverySummary?,
    val discoveries: List<FrontierDiscovery>?
)

data class FrontierDiscoverySummary(
    val DiscoveryTotal: Int,
    val MappedTotal: Int,
    val WasDiscovered: Int,
    val wasMapped: Int,
    val efficiencyBonusTotal:Int,
    val firstDiscoveryTotal:Int,
    val firstMappedTotal: Int,
    val probesUsedTotal: Int
)

data class FrontierDiscovery(
    val name: String,
    val discoveryCount: Int,
    val mappedCount: Int,
    val efficiencyBonusCount: Int,
    val firstDiscoveredCount: Int,
    val firstMappedCount: Int
)