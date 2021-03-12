package com.devries48.elitecommander.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.devries48.elitecommander.App
import com.devries48.elitecommander.models.SettingsModel
import com.google.gson.Gson

object SettingsUtils {

    enum class Key {
        ACCESS_TOKEN,
        REFRESH_TOKEN,
        SETTINGS_MODEL,
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

    fun setSettingsModel(model: SettingsModel) {
        model.timestamp= DateUtils.getCurrentDateString()
        val json: String =  Gson().toJson(model)
        if (json.isNotEmpty()) setString(App.getContext(), Key.SETTINGS_MODEL, json)
    }

    fun getSettingsModel(): SettingsModel {
        val jsonString = getString(App.getContext() ,Key.SETTINGS_MODEL)
        if (jsonString != null) {
            try {
                val model=Gson().fromJson(jsonString, SettingsModel::class.java)
                // Is
                return model
            }  finally {
                return SettingsModel()
            }
            //
        } else {
            return SettingsModel()
        }
    }

}