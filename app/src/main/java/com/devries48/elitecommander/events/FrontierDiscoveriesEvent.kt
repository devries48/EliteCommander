package com.devries48.elitecommander.events

data class FrontierDiscoveriesEvent(
    val success: Boolean = false,
    val summary: FrontierDiscoverySummary?,
    val discoveries: List<FrontierDiscovery>?
)

data class FrontierDiscoverySummary(
    var DiscoveryTotal: Int,
    var MappedTotal: Int,
    val WasDiscovered: Int,
    val wasMapped: Int,
    var efficiencyBonusTotal: Int,
    var firstDiscoveryTotal: Int,
    var firstMappedTotal: Int,
    var firstDiscoveredAndMappedTotal: Int,
    var probesUsedTotal: Int
)

data class FrontierDiscovery(
    val body: String,
    val star: String,
    val discoveryCount: Int,
    val mappedCount: Int,
    val efficiencyBonusCount: Int,
    val firstDiscoveredCount: Int,
    val firstMappedCount: Int,
    val firstDiscoveredAndMappedCount: Int
)