package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import java.text.DecimalFormat

class StatisticsBuilder {
    private var mStatisticsList = ArrayList<StatisticModel>()
    var statistics = MutableLiveData<List<StatisticModel>>()

    init {
        post()
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
        insertStatistic(-1, type, pos, titleResId, value, showDelta, format, color)
    }

    fun insertStatistic(
        position: Int,
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
                if (position == -1)
                    mStatisticsList.add(stat) else
                    mStatisticsList.add(position, stat)
            }

            val formattedValue: String = when (format) {
                StatisticFormat.CURRENCY -> {
                    when (value) {
                        is Int -> formatCurrency(value)
                        is Long -> formatCurrency(value)
                        is Double -> formatCurrency(value)

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
                StatisticFormat.INTEGER -> {
                    formatInteger(value as Int)
                }
                StatisticFormat.TONS -> {
                    formatInteger(value as Int) + " TONS"
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
        return String.format("%,d hours", seconds / (60 * 60))
    }

    fun <T> formatCurrency(amount: T): String {
        val formatter = DecimalFormat("###,###,###,### CR")
        return formatter.format(amount)
    }

    private fun formatInteger(value: Int): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(value)
    }

    fun <T> formatDouble(value: T): String {
        val formatter = DecimalFormat("###,###,###.#")
        return formatter.format(value)
    }

    fun post() {
        statistics.postValue(mStatisticsList)
    }

    companion object {
        enum class StatisticType {
            CMDR_CREDITS,
            CMDR_LOCATION,
            CMDR_SHIP,
            CMDR_TIME_PLAYED,
            PROFIT_COMBAT_BOUNTIES,
            PROFIT_COMBAT_BONDS,
            PROFIT_COMBAT_ASSASSINATIONS,
            PROFIT_EXPLORATION,
            PROFIT_TRADING,
            PROFIT_SMUGGLING,
            PROFIT_MINING,
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
            INTEGER,
            TIME,
            TONS
        }
    }
}