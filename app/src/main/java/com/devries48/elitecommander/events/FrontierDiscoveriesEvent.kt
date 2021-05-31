package com.devries48.elitecommander.events

data class FrontierDiscoveriesEvent(
    val success: Boolean = false,
    val error: String?,
    val summary: FrontierDiscoverySummary?,
    val discoveries: List<FrontierDiscovery>?
)

data class FrontierDiscoverySummary(
    var discoveryTotal: Int,
    var mappedTotal: Int,
    var discoveredAndMappedTotal: Int,
    val wasDiscovered: Int,
    val wasMapped: Int,
    var efficiencyBonusTotal: Int,
    var firstDiscoveryTotal: Int,
    var firstMappedTotal: Int,
    var firstDiscoveredAndMappedTotal: Int,
    var probesUsedTotal: Int,
    var estimatedValue: Long,
    var tripDistance: Double,
    var tripJumps: Int,
    var lastDocked: String?
)

data class FrontierDiscovery(
    val body: String,
    val star: String,
    val discovered: Int,
    val mapped: Int,
    val discoveredAndMapped: Int,
    val efficiencyBonus: Int,
    val firstDiscovered: Int,
    val firstMapped: Int,
    val firstDiscoveredAndMapped: Int,
    val estimatedValue: Long
)