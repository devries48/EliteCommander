package com.devries48.elitecommander.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.devries48.elitecommander.events.FrontierRanksEvent
// TODO: Remove resources, see EarningsModel
data class RankModel(
    @DrawableRes val logoResId: Int = 0,
    val rank: FrontierRanksEvent.FrontierRank,
    val name: String,
    @StringRes val titleResId: Int = 0,
    val isFactionRank: Boolean = false
)
