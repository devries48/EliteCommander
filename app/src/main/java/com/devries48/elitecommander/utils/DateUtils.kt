package com.devries48.elitecommander.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val defaultFormat = "yyyy/MM/dd HH:mm:ss"
    const val shortDateFormat = "yyyy/MM/dd"
    const val journalDateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    val eliteStartDate: Date
        get() {
            return fromDateString("2018/01/01", shortDateFormat)
        }

    fun Date.toDateString(format: String = defaultFormat): String {
        return toDateString(format, this)
    }

    fun toDateString(format: String = defaultFormat, date: Date): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    fun fromDateString(value: String, format: String = this@DateUtils.defaultFormat): Date {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        return sdf.parse(value)!!
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    fun getCurrentDateString(format: String = defaultFormat): String {
        return toDateString(format, getCurrentDate())
    }

    private fun addDays(value: Date, n: Int = 1): Date {
        val date = value.toDateString()
        val formatter = SimpleDateFormat(defaultFormat, Locale.getDefault())
        val calender = Calendar.getInstance()
        calender.time = formatter.parse(date)!!
        calender.add(Calendar.DATE, n)

        return calender.time
    }

    fun Date.removeDays(n: Int = 1): Date {
        return addDays(this, n * -1)
    }

    fun getLastThursday(): Date? {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        cal[Calendar.DAY_OF_WEEK] = Calendar.THURSDAY
        return cal.time
    }
}