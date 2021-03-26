package com.devries48.elitecommander.events

data class FrontierDiscoveriesEvent(
    val success: Boolean = false,
    val summary: FrontierDiscoverySummary?,
    val discoveries: List<FrontierDiscovery>?
)

data class FrontierDiscoverySummary(
    var discoveryTotal: Int,
    var mappedTotal: Int,
    val wasDiscovered: Int,
    val wasMapped: Int,
    var efficiencyBonusTotal: Int,
    var firstDiscoveryTotal: Int,
    var firstMappedTotal: Int,
    var firstDiscoveredAndMappedTotal: Int,
    var probesUsedTotal: Int,
    var estimatedValue: Long
)

data class FrontierDiscovery(
    val body: String,
    val star: String,
    val discoveryCount: Int,
    val mappedCount: Int,
    val efficiencyBonusCount: Int,
    val firstDiscoveredCount: Int,
    val firstMappedCount: Int,
    val firstDiscoveredAndMappedCount: Int,
    val estimatedValue: Long
)