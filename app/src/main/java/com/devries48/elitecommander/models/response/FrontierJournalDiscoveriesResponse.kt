package com.devries48.elitecommander.models.response

import com.google.gson.annotations.SerializedName

class FrontierJournalDiscoveriesResponse : FrontierJournalResponseBase() {
    /**
     * System identifier, together with 'BodyID' a ΅ScanSurface' event can find the matching body.
     */
    @SerializedName("SystemAddress")
    val systemAddress: Long = 0

    /**
     * The identifier of the body withing the system, together with 'SystemAddress' a ΅ScanSurface' event can find the matching body.
     */
    @SerializedName("BodyID")
    val bodyID: Int = 0

    /**
     * One of 'Basic', 'Detailed', 'NavBeacon', 'NavBeaconDetail', 'AutoScan'
     */
    @SerializedName("ScanType") // AutoScan, Detailed
    var scanType: Int = 0

    @SerializedName("PlanetClass")
    val planetClass: String = ""

    @SerializedName("StarType")
    val starType: String = ""

    @SerializedName("Subclass")
    val subclass: String = ""

    @SerializedName("WasDiscovered")
    val wasDiscovered: Boolean = false

    @SerializedName("WasMapped")
    val wasMapped: Boolean = false
}

/**
 * Response class for journal event: 'SAAScanComplete'
 */

class FrontierJournalScanSurface : FrontierJournalResponseBase() {
    /**
     * System identifier, together with 'BodyID' find the matching body in the 'Scan' events.
     */
    @SerializedName("SystemAddress")
    val systemAddress: Long = 0

    /**
     * The identifier of the body withing the system, together with 'SystemAddress' find the matching body in the 'Scan' events.
     */
    @SerializedName("BodyID")
    val bodyID: Int = 0

    @SerializedName("EfficiencyTarget")
    val efficiencyTarget: Int = 0

    @SerializedName("ProbesUsed")
    val probesUsed: Int = 0
}
