package com.devries48.elitecommander.models

class ProfitModel(val t: ProfitType, val a: Long) {

    constructor(t: ProfitType, a: Long, p: Float) : this(t, a) {
        this.percentage = p
    }

    enum class ProfitType {
        COMBAT,
        EXPLORATION,
        TRADING,
        MINING,
        SMUGGLING,
        SEARCH_RESCUE,
        OTHER
    }

    var type: ProfitType? = null
    var amount: Long = 0
    var percentage: Float = 0f

    init {
        type = t
        amount = a
    }

}