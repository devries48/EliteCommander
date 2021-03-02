package com.devries48.elitecommander.events

data class FrontierStatisticsEvent (
    val success: Boolean,
    val bankAccount: FrontierBankAccount?,
    val combat: FrontierCombat?,
    val exploration: FrontierExploration?,
    val mining: FrontierMining?,
    val trading: FrontierTrading?
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
    val assassinationProfits: Long,
    val assassinations: Int,
    val bountiesClaimed: Int,
    val bountyHuntingProfit: Long,
    val combatBondProfits: Long,
    val combatBonds: Int,
    val highestSingleReward: Int,
    val skimmersKilled: Int
)

data class FrontierExploration(
    val efficientScans: Int,
    val explorationProfits: Long,
    val greatestDistanceFromStart: Double,
    val highestPayout: Int,
    val planetsScannedToLevel2: Int,
    val planetsScannedToLevel3: Int,
    val systemsVisited: Int,
    val timePlayed: Int,
    val totalHyperspaceDistance: Int,
    val totalHyperspaceJumps: Int
)

data class FrontierMining(
    val materialsCollected: Int,
    val miningProfits: Long,
    val quantityMined: Int
)

data class FrontierTrading(
    val averageProfit: Double,
    val highestSingleTransaction: Int,
    val marketProfits: Long,
    val marketsTradedWith: Int,
    val resourcesTraded: Int
)