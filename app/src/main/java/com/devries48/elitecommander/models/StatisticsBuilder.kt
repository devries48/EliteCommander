package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import java.text.DecimalFormat

class StatisticsBuilder {

    var statistics= MutableLiveData<List<StatisticModel>>()
    private val mStatisticsList = ArrayList<StatisticModel>()
    private val mCurrencyFormat = DecimalFormat("###,###,###,### CR")
    private val mDoubleFormat = DecimalFormat("###,###,###.#")

    enum class StatisticType {
        CMDR_CREDITS,
        CMDR_LOCATION,
        CMDR_SHIP,
        PROFIT_COMBAT_BOUNTIES,
        PROFIT_COMBAT_BONDS,
        PROFIT_COMBAT_ASSASSINATIONS,
        PROFIT_TRADING,
        PROFIT_EXPLORATION,
        PROFIT_SMUGGLING,
        PROFIT_SEARCH_RESCUE
    }

    enum class StatisticPosition {
        LEFT,
        CENTER,
        RIGHT
    }

    enum class StatisticColor {
        DEFAULT,
        DIMMED,
        WARNING
    }

    enum class StatisticFormat {
        NONE,
        CURRENCY,
        DOUBLE
    }

    fun addStatistic(
        type: StatisticType,
        pos: StatisticPosition,
        @StringRes titleResId: Int,
        value: Any,
        showDelta: Boolean = false,
        format: StatisticFormat=StatisticFormat.NONE,
        color: StatisticColor= StatisticColor.DEFAULT,
    ) {
        var stat: StatisticModel? = mStatisticsList.find { it.type == type }

        if (stat == null) {
            stat = StatisticModel()
            stat.type = type

            mStatisticsList.add(stat)
        }

        val formattedValue: String? = when (format){
            StatisticFormat.CURRENCY-> {
                when (value) {
                    is Int ->  mCurrencyFormat.format(value)
                    is Long -> mCurrencyFormat.format(value)
                    else -> value.toString()
                }
            }
            StatisticFormat.DOUBLE-> {
                when (value) {
                    is Int ->  mDoubleFormat.format(value)
                    is Long -> mDoubleFormat.format(value)
                    else -> value.toString()
                }
            }
            else -> value.toString()
        }

        when (pos) {
            StatisticPosition.LEFT -> {
                stat.leftTitleResId = titleResId
                stat.leftValue = formattedValue
                stat.leftShowDelta = showDelta
            }
            StatisticPosition.CENTER -> {
                stat.middleTitleResId = titleResId
                stat.middleValue = formattedValue
                stat.middleColor = color
                stat.middleShowDelta = showDelta
            }
            else -> {
                stat.rightTitleResId = titleResId
                stat.rightValue = formattedValue
                stat.rightColor = color
                stat.rightShowDelta = showDelta
            }
        }

    }

    fun postValues() {
        statistics.postValue(mStatisticsList)
    }
}