package com.devries48.elitecommander.events

data class FrontierRanksEvent @JvmOverloads
constructor(
    val success: Boolean,
    val combat: FrontierRank? = null,
    val trade: FrontierRank? = null,
    val explore: FrontierRank? = null,
    val cqc: FrontierRank? = null,
    val federation: FrontierRank? = null,
    val empire: FrontierRank? = null
) {

    data class FrontierRank(
        val name: String,
        val value: Int,
        val progress: Int,
        val reputation: Int = 0
    )
}