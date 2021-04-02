package com.devries48.elitecommander.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val defaultFormat = "yyyy/MM/dd HH:mm:ss"
    const val dateFormatShort = "yyyy/MM/dd"
    const val dateFormatGMT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val dateFormatCycleGMT = "yyyy-MM-dd'T'07:00:00'Z'"

    val eliteStartDate: Date
        get() {
            return fromDateString("2018/01/01", dateFormatShort)
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

    private fun getCurrentDateGMT(): Date {
        Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        return Calendar.getInstance().time
    }

    fun getCurrentDateString(format: String = defaultFormat): String {
        return toDateString(format, getCurrentDate())
    }

    private fun addDays(value: Date, n: Int = 1): Date {
        val date = value.toDateString()
        val formatter = SimpleDateFormat(defaultFormat, Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = formatter.parse(date)!!
        cal.add(Calendar.DATE, n)

        return cal.time
    }

    fun Date.removeDays(n: Int = 1): Date {
        return addDays(this, n * -1)
    }

    fun getLastCycleDateGMT(): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        val currentDate: Date? = if (cal[Calendar.DAY_OF_WEEK] == Calendar.THURSDAY)
            getCurrentDateGMT() else null

        cal.add(Calendar.WEEK_OF_YEAR, -1)
        while (cal[Calendar.DAY_OF_WEEK] != Calendar.THURSDAY) {
            cal.add(Calendar.DAY_OF_WEEK,1)
        }
        var cycleDate = cal.time.toDateString(dateFormatCycleGMT)

        if (currentDate != null && currentDate > fromDateString(cycleDate, dateFormatGMT))
            cycleDate = currentDate.toDateString(dateFormatCycleGMT)

        return fromDateString(cycleDate, dateFormatGMT)
    }
}