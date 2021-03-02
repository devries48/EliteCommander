package com.devries48.elitecommander.models

class EarningModel(val t: EarningType, val a: Long) {

    constructor(t: EarningType, a: Long, p: Float) : this(t, a) {
        this.percentage = p
    }

    enum class EarningType {
        COMBAT,
        EXPLORATION,
        TRADING,
        MINING
    }

    var type: EarningType? = null
    var amount: Long = 0
    var percentage: Float = 0f

    init {
        type = t
        amount = a
    }

}