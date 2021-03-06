package com.devries48.elitecommander.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.devries48.elitecommander.App
import com.devries48.elitecommander.models.StatisticSettingsModel
import com.devries48.elitecommander.utils.DateUtils.DateFormatType.GMT
import com.devries48.elitecommander.utils.DateUtils.toDateString
import com.google.gson.Gson

object SettingsUtils {

    enum class Key {
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        STATISTIC_VALUES,
    }

    fun getString(c: Context, key: Key): String? {
        return PreferenceManager.getDefaultSharedPreferences(c)
            .getString(key.toString(), "")
    }

    fun setString(c: Context, key: Key, value: String) {
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(c)
        val editor = preferenceManager.edit()
        editor.putString(key.toString(), value)
        editor.apply()
    }

    fun canSaveSettings(model: StatisticSettingsModel): Boolean {
        return (model.credits != null && model.timePlayed != null)
    }

    fun setStatisticsSettings(model: StatisticSettingsModel) {
        if (model.timestamp != null && canSaveSettings(model))
            return

        model.timestamp = DateUtils.getCurrentDateString(GMT)
        val json: String = Gson().toJson(model)

        if (json.isNotBlank()) {
            setString(App.getContext(), Key.STATISTIC_VALUES, json)
            println("STATISTIC_VALUES saved")
            println(json)
        }
    }

    fun getStatisticSettings(): StatisticSettingsModel {
        val jsonString = getString(App.getContext(), Key.STATISTIC_VALUES)

        if (!jsonString.isNullOrBlank()) {
            try {
                val model = Gson().fromJson(jsonString, StatisticSettingsModel::class.java)

                // If a power cycle has occurred since last session, clear the values
                val modelDate = DateUtils.fromDateString(model.timestamp!!, GMT)
                val cycleDate = DateUtils.getLastCycleDateGMT()

                return when {
                    modelDate.before(cycleDate) || model.credits == null -> {
                        println("STATISTIC_VALUES cycled")
                        StatisticSettingsModel()
                    }
                    model.credits != null && model.timePlayed != null -> {
                        println("STATISTIC_VALUES loaded: " + modelDate.toDateString(GMT))
                        model
                    }
                    else -> {
                        println("STATISTIC_VALUES incomplete")
                        StatisticSettingsModel()
                    }
                }
            } catch (e: Exception) {
                println("STATISTIC_VALUES new: " + e.message)
                return StatisticSettingsModel()
            }
        } else {
            println("STATISTIC_VALUES new")
            return StatisticSettingsModel()
        }
    }
}