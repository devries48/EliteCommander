package com.devries48.elitecommander.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val defaultFormat = "yyyy/MM/dd HH:mm:ss"
    const val shortDateFormat = "yyyy/MM/dd"

    val eliteStartDate: Date
        get() {
            return fromDateString("2018/01/01", shortDateFormat)
        }

    fun Date.toDateString(format: String = defaultFormat): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(this)
    }

    private fun fromDateString(value: String, format: String = this@DateUtils.defaultFormat): Date {
        return SimpleDateFormat(format, Locale.getDefault()).parse(value)!!
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    private fun addDays(value: Date, n: Int = 1): Date {
        val date = value.toDateString()
        val formatter = SimpleDateFormat(defaultFormat, Locale.getDefault())
        val calender = Calendar.getInstance()
        calender.time = formatter.parse(date)!!
        calender.add(Calendar.DATE, n)

        return calender.time
    }

    fun Date.removeDays( n: Int = 1): Date {
        return addDays(this, n * -1)
    }

}