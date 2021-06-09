package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.events.*
import com.devries48.elitecommander.models.response.frontier.*
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@DelicateCoroutinesApi
class JournalStatistics(worker: JournalWorker) {

    private val mWorker = worker
    private lateinit var mVoucherProfit: VoucherProfit

    internal suspend fun raiseFrontierStatisticsEvent(rawEvents: List<JournalWorker.RawEvent>) {
        withContext(Dispatchers.IO) {
            try {
                val rawStatistics =
                    rawEvents.firstOrNull { it.event == JournalConstants.EVENT_STATISTICS }
                        ?: throw error("No rows event found!")

                val statistics = Gson().fromJson(rawStatistics.json, JournalStatisticsResponse::class.java)
                mVoucherProfit = setVoucherProfits(rawEvents)

                sendWorkerEvent(
                    FrontierStatisticsEvent(
                        true,
                        null,
                        mWorker.lastJournalDate,
                        FrontierBankAccount(
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
                println()
                sendWorkerEvent(
                    FrontierStatisticsEvent(
                        false,
                        "Error parsing rows event from journal." + e.message,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                )
            }
        }
    }

    private fun setVoucherProfits(rawEvents: List<JournalWorker.RawEvent>): VoucherProfit {
        val profit = VoucherProfit()

        val vouchers = rawEvents.filter { it.event == JournalConstants.EVENT_REDEEM_VOUCHER }
        vouchers.forEach {
            val voucher = Gson().fromJson(it.json, JournalRedeemVoucherResponse::class.java)
            val amount = voucher.amount ?: voucher.factions?.map { a -> a.amount }?.sum()!!

            when (voucher.type.lowercase(Locale.ROOT)) {
                "bounty" -> profit.bounty += amount
                "combatbond" -> profit.combatBond += amount
                "trade" -> profit.trade += amount
                "settlement" -> profit.settlement += amount
                "scannable" -> profit.scannable += amount
                "codex" -> profit.codex += amount
                else -> println("Voucher type not found: " + voucher.type)
            }
        }

        return profit
    }

    private fun getFrontierCombat(
        statistics: JournalStatisticsResponse,
        rawEvents: List<JournalWorker.RawEvent>
    ): FrontierCombat {
        val stats =
            FrontierCombat(
                statistics.combat.assassinationProfits,
                statistics.combat.assassinations,
                statistics.combat.bountiesClaimed,
                statistics.combat.bountyHuntingProfit,
                statistics.combat.combatBondProfits,
                statistics.combat.combatBonds,
                statistics.combat.highestSingleReward,
                statistics.combat.skimmersKilled
            )

        val bounties = rawEvents.filter { it.event == JournalConstants.EVENT_COMBAT_BOUNTY }
        bounties.forEach {
            val bounty = Gson().fromJson(it.json, JournalBountyResponse::class.java)
            if (bounty.reward == null) stats.bountiesClaimed += bounty.rewards?.size ?: 1
            else stats.skimmersKilled += 1
        }

        stats.combatBonds += rawEvents.count { it.event == JournalConstants.EVENT_COMBAT_BOND }

        val assassinations = rawEvents.filter { it.event == JournalConstants.EVENT_MISSION_COMPLETED }
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
        val stats =
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
            )

        var dist = 0.0
        var visitCount = 0
        var bonusCount = 0

        val scans = rawEvents.filter { it.event == JournalConstants.EVENT_DISCOVERY }
        val jumps = rawEvents.filter { it.event == JournalConstants.EVENT_FSD_JUMP }
        val maps = rawEvents.filter { it.event == JournalConstants.EVENT_MAP }
        val dataSold =
            rawEvents.filter {
                it.event == JournalConstants.EVENT_DISCOVERIES_SOLD || it.event == JournalConstants.EVENT_DISCOVERY_SOLD
            }
        val visits = arrayListOf<String>()

        scans.forEach {
            val scan = Gson().fromJson(it.json, JournalDiscoveries.Discovery::class.java)
            if (scan.starType?.isNotBlank() == true &&
                scan.bodyName?.isNotBlank() == true &&
                !visits.contains(scan.bodyName)
            )
                visits.add(scan.bodyName)
        }

        jumps.forEach {
            val jump = Gson().fromJson(it.json, JournalFsdJumpResponse::class.java)
            if (visits.contains(jump.starSystem) ||
                visits.filter { name -> name.startsWith(jump.starSystem + " ") }.count() > 1
            )
                visitCount += 1

            dist += jump.jumpDist
        }

        maps.forEach {
            val map = Gson().fromJson(it.json, JournalDiscoveries.Mapping::class.java)
            if (map.efficiencyTarget >= map.probesUsed) bonusCount += 1
        }

        dataSold.forEach {
            val data = Gson().fromJson(it.json, SellData::class.java)
            stats.explorationProfits += data.totalEarnings
        }
        stats.explorationProfits += mVoucherProfit.codex

        stats.totalHyperspaceJumps += jumps.count()
        stats.systemsVisited += visitCount
        stats.totalHyperspaceDistance += dist.toInt()
        stats.planetsScannedToLevel2 += scans.count()
        stats.planetsScannedToLevel3 = stats.planetsScannedToLevel2
        stats.efficientScans += bonusCount

        return stats
    }

    private fun getFrontierMining(
        statistics: JournalStatisticsResponse,
        rawEvents: List<JournalWorker.RawEvent>
    ): FrontierMining {
        val stats =
            FrontierMining(
                statistics.mining.materialsCollected,
                statistics.mining.miningProfits,
                statistics.mining.quantityMined
            )

        stats.quantityMined += rawEvents.count { it.event == JournalConstants.EVENT_MINING_REFINED }

        return stats
    }

    private data class VoucherProfit(
        var combatBond: Long = 0,
        var bounty: Long = 0,
        var trade: Long = 0,
        var settlement: Long = 0,
        var scannable: Long = 0,
        var codex: Long = 0
    )

    data class SellData(
        @SerializedName("TotalEarnings") val totalEarnings: Int
    )
}
