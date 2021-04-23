package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.response.FrontierJournalStatisticsResponse
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JournalStatistics(worker: JournalWorker) {

    private val mWorker = worker

    internal suspend fun raiseFrontierStatisticsEvent(rawEvents: List<JournalWorker.RawEvent>) {
        withContext(Dispatchers.IO) {
            try {
                val rawStatistics = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_STATISTICS }
                    ?: throw error("Error parsing statistics event from journal")

                val statistics = Gson().fromJson(rawStatistics.json, FrontierJournalStatisticsResponse::class.java)

                sendWorkerEvent(
                    FrontierStatisticsEvent(
                        true, mWorker.lastJournalDate, FrontierBankAccount(
                            statistics.bankAccount.currentWealth,
                            statistics.bankAccount.insuranceClaims,
                            statistics.bankAccount.ownedShipCount,
                            statistics.bankAccount.spentOnAmmoConsumables,
                            statistics.bankAccount.spentOnFuel,
                            statistics.bankAccount.spentOnInsurance,
                            statistics.bankAccount.spentOnOutfitting,
                            statistics.bankAccount.spentOnRepairs,
                            statistics.bankAccount.spentOnShips
                        ), getFrontierCombat(statistics, rawEvents),
                        FrontierExploration(
                            statistics.exploration.efficientScans,
                            statistics.exploration.explorationProfits,
                            statistics.exploration.greatestDistanceFromStart,
                            statistics.exploration.highestPayout,
                            statistics.exploration.planetsScannedToLevel2,
                            statistics.exploration.planetsScannedToLevel3,
                            statistics.exploration.systemsVisited,
                            statistics.exploration.timePlayed,
                            statistics.exploration.totalHyperspaceDistance,
                            statistics.exploration.totalHyperspaceJumps
                        ), FrontierMining(
                            statistics.mining.materialsCollected,
                            statistics.mining.miningProfits,
                            statistics.mining.quantityMined
                        ),
                        FrontierTrading(
                            statistics.trading.averageProfit,
                            statistics.trading.highestSingleTransaction,
                            statistics.trading.marketProfits,
                            statistics.trading.marketsTradedWith,
                            statistics.trading.resourcesTraded
                        ),
                        FrontierSmuggling(
                            statistics.smuggling.averageProfit,
                            statistics.smuggling.blackMarketsProfits,
                            statistics.smuggling.blackMarketsTradedWith,
                            statistics.smuggling.highestSingleTransaction,
                            statistics.smuggling.resourcesSmuggled
                        ),
                        FrontierSearchAndRescue(
                            statistics.searchAndRescue.searchRescueCount,
                            statistics.searchAndRescue.searchRescueProfit,
                            statistics.searchAndRescue.searchRescueTraded
                        ),
                        FrontierPassengers(
                            statistics.passengers.passengersMissionsAccepted,
                            statistics.passengers.passengersMissionsBulk,
                            statistics.passengers.passengersMissionsDelivered,
                            statistics.passengers.passengersMissionsDisgruntled,
                            statistics.passengers.passengersMissionsEjected,
                            statistics.passengers.passengersMissionsVIP
                        ),
                        FrontierCrime(
                            statistics.crime.bountiesReceived,
                            statistics.crime.fines,
                            statistics.crime.highestBounty,
                            statistics.crime.notoriety,
                            statistics.crime.totalBounties,
                            statistics.crime.totalFines
                        )
                    )
                )

            } catch (e: Exception) {
                println("LOG: Error parsing statistics event from journal." + e.message)
                sendWorkerEvent(
                    FrontierStatisticsEvent(
                        false, null, null, null, null, null, null, null, null, null, null
                    )
                )
            }

        }
    }

    private fun getFrontierCombat(
        statistics: FrontierJournalStatisticsResponse,
        rawEvents: List<JournalWorker.RawEvent>
    ): FrontierCombat {
        val stats = FrontierCombat(
            statistics.combat.assassinationProfits,
            statistics.combat.assassinations,
            statistics.combat.bountiesClaimed,
            statistics.combat.bountyHuntingProfit,
            statistics.combat.combatBondProfits,
            statistics.combat.combatBonds,
            statistics.combat.highestSingleReward,
            statistics.combat.skimmersKilled
        )

/*
        val bounties = rawEvents.filter { it.event == JOURNAL_EVENT_COMBAT_BOUNTY }
        bounties.forEach {
            val bounty = Gson().fromJson(it.json, FrontierJournalStatisticsResponse.Combat::class.java)
            statistics.combat.
        }
*/

        return stats
    }

    companion object {
        internal const val JOURNAL_EVENT_STATISTICS = "Statistics"
        internal const val JOURNAL_EVENT_COMBAT_BOUNTY = "Bounty"
        internal const val JOURNAL_EVENT_COMBAT_VOUCHER = "RedeemVoucher"
    }

}