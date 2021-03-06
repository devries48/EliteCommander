package com.devries48.elitecommander.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.devries48.elitecommander.App
import com.devries48.elitecommander.R

class ProfitModel(profitType: ProfitType, profitAmount: Long) {

    constructor(_profitType: ProfitType, _profitAmount: Long, percentage: Float) : this(_profitType, _profitAmount) {
        this.percentage = percentage
    }

    enum class ProfitType {
        COMBAT,
        EXPLORATION,
        TRADING,
        MINING,
        SMUGGLING,
        SEARCH_RESCUE,
    }

    private var typeList: ArrayList<ProfitTypeModel> = ArrayList()

    var type: ProfitType? = profitType
    var amount: Long = profitAmount
    var percentage: Float = 0f

    @StringRes
    fun getTitle(): Int {
        return typeList.find { it.type == type!! }?.title!!
    }

    fun getColor(): Int {
        return typeList.find { it.type == type!! }?.color!!
    }

    @DrawableRes
    fun getColorDot(): Int {
        return typeList.find { it.type == type!! }?.dot!!
    }

    init {
        type = profitType
        amount = profitAmount
        val ctx = App.getContext()

        typeList.add(
            ProfitTypeModel(
                ProfitType.COMBAT,
                R.string.combat,
                ContextCompat.getColor(ctx, R.color.rank_combat),
                R.drawable.ic_dot_combat
            )
        )
        typeList.add(
            ProfitTypeModel(
                ProfitType.EXPLORATION,
                R.string.exploration,
                ContextCompat.getColor(ctx, R.color.rank_exploration),
                R.drawable.ic_dot_exploration
            )
        )
        typeList.add(
            ProfitTypeModel(
                ProfitType.TRADING,
                R.string.trading,
                ContextCompat.getColor(ctx, R.color.rank_trading),
                R.drawable.ic_dot_trading
            )
        )
        typeList.add(
            ProfitTypeModel(
                ProfitType.MINING,
                R.string.mining,
                ContextCompat.getColor(ctx, R.color.rank_mining),
                R.drawable.ic_dot_mining
            )
        )
        typeList.add(
            ProfitTypeModel(
                ProfitType.SMUGGLING,
                R.string.smuggling,
                ContextCompat.getColor(ctx, R.color.yellow),
                R.drawable.ic_dot_smuggling
            )
        )
        typeList.add(
            ProfitTypeModel(
                ProfitType.SEARCH_RESCUE,
                R.string.search_rescue,
                ContextCompat.getColor(ctx, R.color.orange),
                R.drawable.ic_dot_search_rescue
            )
        )
    }

    private class ProfitTypeModel() {

        constructor(_type: ProfitType, @StringRes _title: Int, _color: Int, @DrawableRes _dot: Int) : this() {
            type = _type
            title = _title
            color = _color
            dot = _dot
        }

        var type: ProfitType? = null

        @StringRes
        var title: Int? = null
        var color: Int? = null

        @DrawableRes
        var dot: Int? = null
    }
}

