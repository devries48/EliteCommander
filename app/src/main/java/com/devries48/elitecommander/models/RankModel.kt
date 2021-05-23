package com.devries48.elitecommander.models

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.devries48.elitecommander.App
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierRanksEvent
import com.devries48.elitecommander.utils.NamingUtils

class RankModel(type: RankType, frontierRank: FrontierRanksEvent.FrontierRank) {

    enum class RankType {
        COMBAT,
        EXPLORATION,
        TRADING,
        CQC,
        FEDERATION,
        EMPIRE,
        ALLIANCE
    }

    private val ctx = App.getContext()
    private var mType: RankType = type

    var rank: FrontierRanksEvent.FrontierRank = FrontierRanksEvent.FrontierRank()
    var isFactionRank: Boolean = false

    init {
        rank = frontierRank
        if (type == RankType.ALLIANCE || type == RankType.EMPIRE || type == RankType.FEDERATION)
            isFactionRank = true
    }

    fun getName(): String {
        @ArrayRes
        val array = when (mType) {
            RankType.COMBAT -> R.array.ranks_combat
            RankType.TRADING -> R.array.ranks_trading
            RankType.EXPLORATION -> R.array.ranks_exploration
            RankType.CQC -> R.array.ranks_cqc
            RankType.FEDERATION -> R.array.ranks_federation
            RankType.EMPIRE -> R.array.ranks_empire
            else -> null
        } ?: return ""

        val ranks = ctx.resources.getStringArray(array)

        return if (rank.value > ranks.size)
            ranks[0]
        else
            ranks[rank.value]
    }

    @DrawableRes
    fun getLogoResId(): Int {
        return when (mType) {
            RankType.COMBAT -> NamingUtils.getCombatRankDrawableId(rank.value)
            RankType.TRADING -> NamingUtils.getTradeRankDrawableId(rank.value)
            RankType.EXPLORATION -> NamingUtils.getExplorationRankDrawableId(rank.value)
            RankType.CQC -> NamingUtils.getCqcRankDrawableId(rank.value)
            RankType.FEDERATION -> NamingUtils.getFederationRankDrawableId(rank.value)
            RankType.EMPIRE -> NamingUtils.getEmpireRankDrawableId(rank.value)
            else -> NamingUtils.getAllianceRankDrawableId()
        }
    }

    @StringRes
    fun getTitleResId(): Int {
        return when (mType) {
            RankType.COMBAT -> R.string.rank_combat
            RankType.TRADING -> R.string.rank_trading
            RankType.EXPLORATION -> R.string.rank_explore
            RankType.CQC -> R.string.rank_cqc
            RankType.FEDERATION -> R.string.rank_federation
            RankType.EMPIRE -> R.string.rank_empire
            else -> R.string.rank_alliance
        }
    }
}
