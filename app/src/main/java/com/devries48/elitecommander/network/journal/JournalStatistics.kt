package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.response.frontier.JournalBountyResponse
import com.devries48.elitecommander.models.response.frontier.JournalRedeemVoucherResponse
import com.devries48.elitecommander.models.response.frontier.JournalStatisticsResponse
import com.devries48.elitecommander.models.response.frontier.MissionCompletedResponse
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class JournalStatistics(worker: JournalWorker) {

    private val mWorker = worker
    private lateinit var mVoucherProfit: VoucherProfit

    internal suspend fun raiseFrontierStatisticsEvent(rawEvents: List<JournalWorker.RawEvent>) {
        withContext(Dispatchers.IO) {
            try {
                val rawStatistics = rawEvents.firstOrNull { it.event == JOURNAL_EVENT_STATISTICS }
                    ?: throw error("No statistics event found!")

                val statistics = Gson().fromJson(rawStatistics.json, JournalStatisticsResponse::class.java)
                mVoucherProfit = VoucherProfit()
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
                        ),
                        getFrontierCombat(statistics, rawEvents),
                        getFrontierExploration(statistics, rawEvents),
                        getFrontierMining(statistics, rawEvents),
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
            val voucher = Gson().fromJson(it.json, JournalRedeemVoucherResponse::class.java)
            val amount = voucher.amount ?: voucher.factions?.map { a -> a.amount }?.sum()!!

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
        statistics: JournalStatisticsResponse,
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
            val bounty = Gson().fromJson(it.json, JournalBountyResponse::class.java)
            if (bounty.reward == null)
                stats.bountiesClaimed += bounty.rewards?.size ?: 1
            else
                stats.skimmersKilled += 1
        }

        stats.combatBonds += rawEvents.count { it.event == JOURNAL_EVENT_COMBAT_BOND }

        val assassinations = rawEvents.filter { it.event == JOURNAL_EVENT_MISSION_COMPLETED }
        println("missions: " + assassinations.count())
        assassinations.forEach {
            val mission = Gson().fromJson(it.json, MissionCompletedResponse::class.java)
            println("mission: " + mission.name)
            if (mission.name.startsWith("MISSION_assassinate")) {
                stats.assassinations += 1
                stats.assassinationProfits += mission.reward
            }
        }

        stats.bountyHuntingProfit += mVoucherProfit.bounty
        stats.combatBondProfits += mVoucherProfit.combatBond

        return stats
    }

    private fun getFrontierExploration(
        statistics: JournalStatisticsResponse,
        rawEvents: List<JournalWorker.RawEvent>
    ): FrontierExploration {
        val stats = FrontierExploration(
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
        )

        return stats
    }

    private fun getFrontierMining(
        statistics: JournalStatisticsResponse,
        rawEvents: List<JournalWorker.RawEvent>
    ): FrontierMining {
        val stats = FrontierMining(
            statistics.mining.materialsCollected,
            statistics.mining.miningProfits,
            statistics.mining.quantityMined
        )

        stats.quantityMined += rawEvents.count { it.event == JOURNAL_EVENT_MINING_REFINED }

        return stats
    }

    companion object {
        internal const val JOURNAL_EVENT_STATISTICS = "Statistics"
        internal const val JOURNAL_EVENT_COMBAT_BOUNTY = "Bounty"
        internal const val JOURNAL_EVENT_COMBAT_BOND = "FactionKillBond"
        internal const val JOURNAL_EVENT_MISSION_COMPLETED = "MissionCompletedResponse"
        internal const val JOURNAL_EVENT_REDEEM_VOUCHER = "RedeemVoucher"
        internal const val JOURNAL_EVENT_MINING_REFINED = "MiningRefined"


        private data class VoucherProfit(
            var combatBond: Long = 0,
            var bounty: Long = 0,
            var trade: Long = 0,
            var settlement: Long = 0,
            var scannable: Long = 0
        )
    }
}