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
    val currentWealth: Int,
    val insuranceClaims: Int,
    val ownedShipCount: Int,
    val spentOnAmmoConsumables: Int,
    val spentOnFuel: Int,
    val spentOnInsurance: Int,
    val spentOnOutfitting: Int,
    val spentOnRepairs: Int,
    val spentOnShips: Int
)

data class FrontierCombat(
    val assassinationProfits: Int,
    val assassinations: Int,
    val bountiesClaimed: Int,
    val bountyHuntingProfit: Int,
    val combatBondProfits: Int,
    val combatBonds: Int,
    val highestSingleReward: Int,
    val skimmersKilled: Int
)

data class FrontierExploration(
    val efficientScans: Int,
    val explorationProfits: Int,
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
    val miningProfits: Int,
    val quantityMined: Int
)

data class FrontierTrading(
    val averageProfit: Double,
    val highestSingleTransaction: Int,
    val marketProfits: Int,
    val marketsTradedWith: Int,
    val resourcesTraded: Int
)
