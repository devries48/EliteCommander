package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.response.FrontierBountyResponse
import com.devries48.elitecommander.models.response.FrontierJournalStatisticsResponse
import com.devries48.elitecommander.models.response.FrontierRedeemVoucher
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class JournalStatistics(worker: JournalWorker) {

    private val mWorker = worker
    private var mVoucherProfit = VoucherProfit()

    internal suspend fun raiseFrontierStatisticsEvent(rawEvents: List<JournalWorker.RawEvent>) {
        withContext(Dispatchers.IO) {
            try {
                val rawStatistics = rawEvents.lastOrNull { it.event == JOURNAL_EVENT_STATISTICS }
                    ?: throw error("Error parsing statistics event from journal")

                val statistics = Gson().fromJson(rawStatistics.json, FrontierJournalStatisticsResponse::class.java)
                setVoucherProfits(rawEvents)

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

    private fun setVoucherProfits(rawEvents: List<JournalWorker.RawEvent>) {

        val vouchers = rawEvents.filter { it.event == JOURNAL_EVENT_REDEEM_VOUCHER }
        vouchers.forEach {
            val voucher = Gson().fromJson(it.json, FrontierRedeemVoucher::class.java)
            val amount = voucher.factions.map { a -> a.amount }.sum()

            println("Voucher type: " + voucher.type)

            when (voucher.type.toLowerCase(Locale.ROOT)) {
                "bounty" -> mVoucherProfit.bounty += amount
                "combatbond" -> mVoucherProfit.combatBond += amount
                "trade" -> mVoucherProfit.trade += amount
                "settlement" -> mVoucherProfit.settlement += amount
                "scannable" -> mVoucherProfit.scannable += amount
                else -> println("Voucher type not found: " + voucher.type)
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

        val bounties = rawEvents.filter { it.event == JOURNAL_EVENT_COMBAT_BOUNTY }
        bounties.forEach {
            val bounty = Gson().fromJson(it.json, FrontierBountyResponse::class.java)
            if (bounty.reward == null)
                stats.bountiesClaimed += bounty.rewards?.size ?: 1
            else
                stats.bountiesClaimed += 1
        }

        stats.bountyHuntingProfit += mVoucherProfit.bounty

        return stats
    }

    companion object {
        internal const val JOURNAL_EVENT_STATISTICS = "Statistics"
        internal const val JOURNAL_EVENT_COMBAT_BOUNTY = "Bounty"
        internal const val JOURNAL_EVENT_REDEEM_VOUCHER = "RedeemVoucher"

        private data class VoucherProfit(
            var combatBond: Long = 0,
            var bounty: Long = 0,
            var trade: Long = 0,
            var settlement: Long = 0,
            var scannable: Long = 0
        )
    }
}