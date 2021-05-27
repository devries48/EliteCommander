package com.devries48.elitecommander.network.journal

import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.models.response.frontier.JournalRankProgressResponse
import com.devries48.elitecommander.models.response.frontier.JournalRankReputationResponse
import com.devries48.elitecommander.models.response.frontier.JournalRankResponse
import com.devries48.elitecommander.network.journal.JournalWorker.Companion.sendWorkerEvent
import com.devries48.elitecommander.network.journal.JournalWorker.RawEvent
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@DelicateCoroutinesApi
internal class JournalRanks {

    internal suspend fun raiseFrontierRanksEvent(rawEvents: List<RawEvent>) {

        withContext(Dispatchers.IO) {
            try {
                val rawRank = rawEvents.firstOrNull { it.event == JournalConstants.EVENT_RANK }
                val rawProgress = rawEvents.firstOrNull { it.event == JournalConstants.EVENT_PROGRESS }
                val rawReputation = rawEvents.firstOrNull { it.event == JournalConstants.EVENT_REPUTATION }

                if (rawRank == null || rawProgress == null || rawReputation == null) throw error("Error parsing rank events from journal")

                val rankAndProgress = getRankAndProgress(rawRank, rawProgress, rawEvents)
                val rank = rankAndProgress.first
                val progress = rankAndProgress.second
                val reputation = Gson().fromJson(rawReputation.json, JournalRankReputationResponse::class.java)

                val combatRank = FrontierRanksEvent.FrontierRank(
                    rank.combat,
                    progress.combat
                )
                val tradeRank = FrontierRanksEvent.FrontierRank(
                    rank.trade,
                    progress.trade
                )
                val exploreRank = FrontierRanksEvent.FrontierRank(
                    rank.explore,
                    progress.explore
                )
                val cqcRank = FrontierRanksEvent.FrontierRank(
                    rank.cqc,
                    progress.cqc
                )
                val federationRank = FrontierRanksEvent.FrontierRank(
                    rank.federation,
                    progress.federation,
                    reputation.federation
                )
                val empireRank = FrontierRanksEvent.FrontierRank(
                    rank.empire,
                    progress.empire,
                    reputation.empire
                )

                val allianceRank = FrontierRanksEvent.FrontierRank(
                    rank.alliance,
                    0,
                    reputation.alliance
                )

                sendWorkerEvent(
                    FrontierRanksEvent(
                        true, null, combatRank, tradeRank, exploreRank,
                        cqcRank, federationRank, empireRank, allianceRank
                    )
                )
            } catch (e: Exception) {
                println("LOG: Error parsing ranking events from journal." + e.message)
                sendWorkerEvent(
                    FrontierRanksEvent(
                        false, null, null,
                        null, null, null, null
                    )
                )
            }
        }
    }

    private fun getRankAndProgress(
        rawRank: RawEvent,
        rawProgress: RawEvent,
        rawEvents: List<RawEvent>
    ): Pair<JournalRankResponse, JournalRankProgressResponse> {
        val rank = Gson().fromJson(rawRank.json, JournalRankResponse::class.java)
        val progress = Gson().fromJson(rawProgress.json, JournalRankProgressResponse::class.java)
        val promotions = rawEvents.filter { it.event == JournalConstants.EVENT_PROMOTION }

        if (promotions.none())
            return rank to progress


        promotions.forEach {
            val promotion = Gson().fromJson(it.json, JournalRankProgressResponse::class.java)

            if (promotion.combat > 0 && rank.combat != promotion.combat) {
                rank.combat = promotion.combat
                progress.combat = 0
            }

            if (promotion.cqc > 0 && rank.cqc != promotion.cqc) {
                rank.cqc = promotion.cqc
                progress.cqc = 0
            }

            if (promotion.empire > 0 && rank.empire != promotion.empire) {
                rank.empire = promotion.empire
                progress.empire = 0
            }

            if (promotion.explore > 0 && rank.explore != promotion.explore) {
                rank.explore = promotion.explore
                progress.explore = 0
            }

            if (promotion.federation > 0 && rank.federation != promotion.federation) {
                rank.federation = promotion.federation
                progress.federation = 0
            }

            if (promotion.trade > 0 && rank.trade != promotion.trade) {
                rank.trade = promotion.trade
                progress.trade = 0
            }
        }

        return rank to progress
    }
}