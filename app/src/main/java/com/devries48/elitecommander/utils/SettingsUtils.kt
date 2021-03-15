package com.devries48.elitecommander.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.devries48.elitecommander.App
import com.devries48.elitecommander.models.StatisticSettingsModel
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

    fun setStatisticsSettings(model: StatisticSettingsModel) {
        if (model.timestamp != null)
            return

        model.timestamp = DateUtils.getCurrentDateString(DateUtils.dateFormatGMT)
        val json: String = Gson().toJson(model)
        if (json.isNotEmpty()) {
            setString(App.getContext(), Key.STATISTIC_VALUES, json)
            println("STATISTIC_VALUES saved")
        }
    }

    fun getStatisticSettings(): StatisticSettingsModel {
        val jsonString = getString(App.getContext(), Key.STATISTIC_VALUES)
        if (jsonString != null) {
            return try {
                val model = Gson().fromJson(jsonString, StatisticSettingsModel::class.java)

                // If a power cycle has occurred since last session, clear the values
                val modelDate = DateUtils.fromDateString(model.timestamp!!, DateUtils.dateFormatGMT)
                val cycleDate = DateUtils.getLastCycleDateGMT()

                if (modelDate.before(cycleDate))
                    StatisticSettingsModel()
                else
                    model
            } catch (e: Exception) {
                StatisticSettingsModel()
            }
        } else {
            return StatisticSettingsModel()
        }
    }



}