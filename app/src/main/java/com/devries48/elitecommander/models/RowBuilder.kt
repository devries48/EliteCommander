package com.devries48.elitecommander.models

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.devries48.elitecommander.utils.DateUtils.DateFormatType.DATETIME
import com.devries48.elitecommander.utils.DateUtils.toDateString
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class RowBuilder {
    private var mRowList = ArrayList<RowModel>()
    var rows = MutableLiveData<List<RowModel>>()

    init {
        post()
    }

    fun addStatistic(
        type: StatisticType,
        pos: RowPosition,
        @StringRes titleResId: Int,
        value: Any,
        oldValue: Any? = null,
        format: RowFormat = RowFormat.NONE,
        color: RowColor = RowColor.DEFAULT,
    ) {
        addRow(type.toString(), pos, titleResId, value, oldValue, format, color)
    }

    fun addSearch(
        type: SearchType,
        pos: RowPosition,
        @StringRes titleResId: Int,
        value: Any,
        oldValue: Any? = null,
        format: RowFormat = RowFormat.NONE,
        color: RowColor = RowColor.DEFAULT,
    ) {
        addRow(type.toString(), pos, titleResId, value, oldValue, format, color)
    }

    private fun addRow(
        type: String,
        pos: RowPosition,
        @StringRes titleResId: Int,
        value: Any,
        oldValue: Any? = null,
        format: RowFormat = RowFormat.NONE,
        color: RowColor = RowColor.DEFAULT,
    ) {
        synchronized(mRowList) {
            var stat: RowModel? = mRowList.firstOrNull { it.type == type }

            if (stat == null) {
                stat = RowModel()
                stat.type = type
                mRowList.add(stat)
            }

            val formattedValue = formatValue(value, format)
            val formattedDelta: String? =
                if (oldValue != null) {
                    val delta = getDelta(value, oldValue)
                    var fmt = format
                    if (fmt != RowFormat.TIME) fmt = RowFormat.LONG
                    delta?.let { formatValue(it, fmt, formatZero = false, showPlus = true) }
                } else null

            when (pos) {
                RowPosition.LEFT -> {
                    stat.leftTitleResId = titleResId
                    stat.leftValue = formattedValue
                    stat.leftColor = color
                    stat.leftDelta = formattedDelta
                }
                RowPosition.CENTER -> {
                    stat.middleTitleResId = titleResId
                    stat.middleValue = formattedValue
                    stat.middleColor = color
                    stat.middleDelta = formattedDelta
                }
                else -> {
                    stat.rightTitleResId = titleResId
                    stat.rightValue = formattedValue
                    stat.rightColor = color
                    stat.rightDelta = formattedDelta
                }
            }
        }
    }

    private fun formatValue(
        value: Any,
        format: RowFormat,
        formatZero: Boolean = true,
        showPlus: Boolean = false
    ): String? {
        if (!formatZero && isZero(value)) return null

        val formatted =
            when (format) {
                RowFormat.CURRENCY ->
                    when (value) {
                        is Int -> formatCurrency(value)
                        is Long -> formatCurrency(value)
                        is Double -> formatCurrency(value)
                        else -> value.toString()
                    }
                RowFormat.LONG -> {
                    when (value) {
                        is Int -> formatDouble(value)
                        is Long -> formatDouble(value)
                        else -> value.toString()
                    }
                }
                RowFormat.LIGHTYEAR ->
                    when (value) {
                        is Int -> formatInteger(value) + " LY"
                        is Double -> formatDouble(value) + " LY"
                        else -> value.toString()
                    }
                RowFormat.DATETIME -> formatDate(value as Date)
                RowFormat.TIME -> formatHours(value as Int)
                RowFormat.INTEGER -> formatInteger(value as Int)
                RowFormat.TONS -> formatInteger(value as Int) + " TONS"
                else -> value.toString()
            }

        return addPlus(formatted, value, showPlus)
    }

    private fun addPlus(formatted: String, value: Any, showPlus: Boolean): String {
        return if (showPlus &&
            (value is Int && value > 0 ||
                    value is Long && value > 0L ||
                    value is Double && value > 0.0)
        )
            "+$formatted"
        else formatted
    }

    fun post() {
        rows.postValue(mRowList)
    }

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
        PROFIT_SEARCH_RESCUE,
        COMBAT_TOTAL_KILLS,
        COMBAT_KILLS,
        EXPLORATION_HYPERSPACE,
        EXPLORATION_SYSTEMS_VISITED,
        EXPLORATION_SCANS,
        PASSENGERS_DELIVERED,
        PASSENGERS_TYPE,
        TRADING_MARKETS,
        TRADING_RESOURCES
    }

    enum class SearchType {
        NEAREST_INTERSTELLAR,
        NEAREST_RAW,
        NEAREST_ENCODED,
        NEAREST_MANUFACTURED,
        NEAREST_TECHNOLOGY_BROKER
    }

    enum class RowPosition {
        LEFT,
        CENTER,
        RIGHT
    }

    enum class RowColor {
        DEFAULT,
        DIMMED,
        WARNING
    }

    enum class RowFormat {
        NONE,
        CURRENCY,
        LONG,
        INTEGER,
        DATETIME,
        TIME,
        TONS,
        LIGHTYEAR
    }

    companion object {

        private fun formatDate(date: Date): String {
            return date.toDateString(DATETIME)
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

        private fun <T> isZero(value: T): Boolean {
            when (value) {
                is Int -> if (value == 0) return true
                is Long -> if (value == 0L) return true
                is Double -> if (value == 0.0) return true
            }
            return false
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> getDelta(value: T, oldValue: T?): T? {
            if (oldValue == null) return null

            return when (value) {
                is Int -> (value as Int - oldValue as Int) as T
                is Long -> (value as Long - oldValue as Long) as T
                is Double -> (value as Double - oldValue as Double) as T
                else -> null
            }
        }
    }
}
