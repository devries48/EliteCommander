package com.devries48.elitecommander.events

import java.util.*

data class FrontierStatisticsEvent(
    val success: Boolean,
    val error: String?,
    val lastJournalDate: Date?,
    val bankAccount: FrontierBankAccount?,
    val combat: FrontierCombat?,
    val exploration: FrontierExploration?,
    val mining: FrontierMining?,
    val trading: FrontierTrading?,
    val smuggling: FrontierSmuggling?,
    val searchAndRescue: FrontierSearchAndRescue?,
    val passengers: FrontierPassengers?,
    val crime: FrontierCrime?
)

data class FrontierBankAccount(
    val currentWealth: Long,
    val insuranceClaims: Int,
    val ownedShipCount: Int,
    val spentOnAmmoConsumables: Long,
    val spentOnFuel: Long,
    val spentOnInsurance: Long,
    val spentOnOutfitting: Long,
    val spentOnRepairs: Long,
    val spentOnShips: Long
)

data class FrontierCombat(
    var assassinationProfits: Long,
    var assassinations: Int,
    var bountiesClaimed: Int,
    var bountyHuntingProfit: Long,
    var combatBondProfits: Long,
    var combatBonds: Int,
    val highestSingleReward: Int,
    var skimmersKilled: Int
)

data class FrontierExploration(
    val efficientScans: Int,
    val explorationProfits: Long,
    val greatestDistanceFromStart: Double,
    val highestPayout: Int,
    val planetsScannedToLevel2: Int,
    val planetsScannedToLevel3: Int,
    var systemsVisited: Int,
    val timePlayed: Int,
    var totalHyperspaceDistance: Int,
    var totalHyperspaceJumps: Int
)

data class FrontierMining(
    val materialsCollected: Int,
    val miningProfits: Long,
    var quantityMined: Int
)

data class FrontierTrading(
    val averageProfit: Double,
    val highestSingleTransaction: Int,
    val marketProfits: Long,
    val marketsTradedWith: Int,
    val resourcesTraded: Int
)

data class FrontierSearchAndRescue(
    val searchRescueCount: Int,
    val searchRescueProfit: Long,
    val searchRescueTraded: Int
)

data class FrontierSmuggling(
    val averageProfit: Int,
    val blackMarketsProfits: Long,
    val blackMarketsTradedWith: Int,
    val highestSingleTransaction: Long,
    val resourcesSmuggled: Int
)

data class FrontierPassengers(
    val passengersMissionsAccepted: Int,
    val passengersMissionsBulk: Int,
    val passengersMissionsDelivered: Int,
    val passengersMissionsDisgruntled: Int,
    val passengersMissionsEjected: Int,
    val passengersMissionsVIP: Int
)

data class FrontierCrime(
    val bountiesReceived: Int,
    val fines: Int,
    val highestBounty: Int,
    val notoriety: Int,
    val totalBounties: Long,
    val totalFines: Long
)