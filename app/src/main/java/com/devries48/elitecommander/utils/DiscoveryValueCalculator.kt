package com.devries48.elitecommander.utils

import com.devries48.elitecommander.network.JournalWorker
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

object DiscoveryValueCalculator {

    /*
 *  @SEE https://forums.frontier.co.uk/showthread.php/232000-Exploration-value-formulae/
 */
    //TODO: Main star value = Normal Main Star Calculation + SUM(MAX(Planetary Body FSS Value / 3.0, 500)) + SUM(Stellar Body FSS Value / 3.0)
    //TODO: There is a bonus of 1k per body for fully FSSing the system (so 8k), and a bonus of 10k per mappable body for fully mapping the system (so 70k).

    @JvmStatic
    fun calculate(
        discovery: JournalWorker.Discovery,
        isMapped: Boolean,
        hasEfficiencyBonus: Boolean
    ): Long {

        var value: Double

        if (!discovery.starType.isNullOrEmpty()) {
            val starId = NamingUtils.getStarTypeAlias(discovery.starType)
            val mass = if (discovery.stellarMass == null) 1.0 else discovery.stellarMass!!

            value = 1200.0

            if (starId in arrayOf(
                    51,
                    501,
                    502,
                    503,
                    504,
                    505,
                    506,
                    507,
                    508,
                    509,
                    510,
                    511,
                    512,
                    513,
                    514
                )
            ) value = 14057.0 // White dwarf
            if (starId in arrayOf(91, 92)) value = 22628.0 // Neutron Star, Black Hole
            if (starId == 93)
                value = 33.5678 // Supermassive Black Hole

            // (k + (m * k / 66.25))
            return round(value + (mass * value / 66.25)).toLong()

        } else if (!discovery.planetClass.isNullOrEmpty()) {
            val planetId = NamingUtils.getPlanetBodyAlias(discovery.planetClass)
            val isTerraformable = !discovery.terraformState.isNullOrEmpty()
            var bonus = if (isTerraformable) 93328.0 else 0.0
            val mass = if (discovery.mass == null) 1.0 else discovery.mass!!

            value = 300.0

            if (planetId == 1) // Metal-rich body
            {
                value = 21790.0
                if (isTerraformable) bonus = 65631.0
            }

            if (planetId == 51) value = 96932.0 // Ammonia world
            if (planetId == 71) value = 1656.0 // Class I gas giant

            if (planetId in arrayOf(2, 72)) // High metal content world / Class II gas giant
            {
                value = 9654.0
                if (isTerraformable) bonus = 100677.0
            }

            if (planetId in arrayOf(31, 41)) // Earth-like world / Water world
            {
                value = 64831.0
                if (isTerraformable) bonus = 116295.0
            }

            // CALCULATION
            val q = 0.56591828
            var mapMultiplier = 1.0

            value += bonus

            if (isMapped) {
                mapMultiplier = 3.3333333333

                if (!discovery.wasDiscovered && !discovery.wasMapped)
                    mapMultiplier = 3.699622554
                else if (!discovery.wasMapped)
                    mapMultiplier = 8.0956

                if (hasEfficiencyBonus) mapMultiplier *= 1.25
            }

            // $value = max(($value + ($value * pow($mass, 0.2) * $q)) * $mapMultiplier, 500);
            value = max((value + (value * mass.pow(0.2) * q)) * mapMultiplier, 500.0)

            if (!discovery.wasDiscovered) value *= 2.6

            return round(value).toLong()
        }

        return 0
    }
}