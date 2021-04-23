package com.devries48.elitecommander.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    enum class DateFormatType {
        DEFAULT,
        SHORT,
        GMT,
        CYCLE,
        DATETIME
    }

    private const val defaultFormat = "yyyy/MM/dd HH:mm:ss"

    val eliteStartDate: Date
        get() {
            return fromDateString("2018/01/01", DateFormatType.SHORT)
        }

    fun Date.toDateString(format: DateFormatType = DateFormatType.DEFAULT): String {
        return toDateString(format, this)
    }

    fun toDateString(type: DateFormatType, date: Date): String {
        val format = getFormat(type)

        return if (format.isNotEmpty()) {
            val f = SimpleDateFormat(format, Locale.getDefault())
            f.format(date)
        } else {
            val df: DateFormat = DateFormat.getDateTimeInstance()
            df.format(date)
        }
    }

    private fun getFormat(type: DateFormatType): String {
        return when (type) {
            DateFormatType.DEFAULT -> defaultFormat
            DateFormatType.SHORT -> "yyyy/MM/dd"
            DateFormatType.GMT -> "yyyy-MM-dd'T'HH:mm:ss'Z'"
            DateFormatType.CYCLE -> "yyyy-MM-dd'T'07:00:00'Z'"
            else -> ""
        }
    }

    fun fromDateString(value: String, type: DateFormatType = DateFormatType.DEFAULT): Date {
        val sdf = SimpleDateFormat(getFormat(type), Locale.getDefault())
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

    fun getCurrentDateString(type: DateFormatType = DateFormatType.DEFAULT): String {
        return toDateString(type, getCurrentDate())
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
            cal.add(Calendar.DAY_OF_WEEK, 1)
        }
        var cycleDate = cal.time.toDateString(DateFormatType.CYCLE)

        if (currentDate != null && currentDate > fromDateString(cycleDate, DateFormatType.GMT))
            cycleDate = currentDate.toDateString(DateFormatType.CYCLE)

        return fromDateString(cycleDate, DateFormatType.GMT)
    }
}