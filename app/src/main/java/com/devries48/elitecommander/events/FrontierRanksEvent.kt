package com.devries48.elitecommander.events

data class FrontierRanksEvent(
    val success: Boolean,
    val combat: FrontierRank? = null,
    val trade: FrontierRank? = null,
    val explore: FrontierRank? = null,
    val cqc: FrontierRank? = null,
    val federation: FrontierRank? = null,
    val empire: FrontierRank? = null,
    val alliance: FrontierRank? = null
)
{
    data class FrontierRank(
        val value: Int,
        val progress: Int,
        val reputation: Int = 0
    ) {
        constructor():this(0,0,0)
    }
}