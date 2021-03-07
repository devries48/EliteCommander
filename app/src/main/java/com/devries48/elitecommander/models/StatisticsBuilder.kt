package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import java.text.DecimalFormat

class StatisticsBuilder {
    private val mStatisticsList = ArrayList<StatisticModel>()
    var statistics = MutableLiveData<List<StatisticModel>>()

    init {
        postValues()
    }

    enum class StatisticType {
        CMDR_CREDITS,
        CMDR_LOCATION,
        CMDR_SHIP,
        CMDR_TIME_PLAYED,
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
        DOUBLE,
        TIME
    }

    fun addStatistic(
        type: StatisticType,
        pos: StatisticPosition,
        @StringRes titleResId: Int,
        value: Any,
        showDelta: Boolean = false,
        format: StatisticFormat = StatisticFormat.NONE,
        color: StatisticColor = StatisticColor.DEFAULT,
    ) {
        synchronized(mStatisticsList) {
            var stat: StatisticModel? = mStatisticsList.find { it.type == type }

            if (stat == null) {
                stat = StatisticModel()
                stat.type = type
                mStatisticsList.add(stat)
            }

            val formattedValue: String = when (format) {
                StatisticFormat.CURRENCY -> {
                    when (value) {
                        is Long -> formatCurrency(value)
                        else -> value.toString()
                    }
                }
                StatisticFormat.DOUBLE -> {
                    when (value) {
                        is Int -> formatDouble(value)
                        is Long -> formatDouble(value)
                        else -> value.toString()
                    }
                }
                StatisticFormat.TIME -> {
                    formatHours(value as Int)
                }

                else -> value.toString()
            }

            when (pos) {
                StatisticPosition.LEFT -> {
                    stat.leftTitleResId = titleResId
                    stat.leftValue = formattedValue
                    stat.leftColor = color
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
    }

    private fun formatHours(seconds: Int): String {
        return String.format("%d hours", seconds / (60 * 60))
    }

    fun formatCurrency(amount: Long): String {
        val formatter = DecimalFormat("###,###,###,###")
        return formatter.format(amount)
    }

    fun <T> formatDouble(value: T): String {
        val formatter = DecimalFormat("###,###,###.#")
        return formatter.format(value)
    }

    fun postValues() {
        statistics.postValue(mStatisticsList)
    }
}