package com.devries48.elitecommander.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.devries48.elitecommander.events.FrontierRanksEvent

data class RankModel(
    @ColorRes val colorResId: Int = 0,
    @DrawableRes val logoResId: Int = 0,
    val rank: FrontierRanksEvent.FrontierRank,
    val name: String,
    @StringRes val titleResId: Int = 0,
    val isPlayerRank: Boolean
)
