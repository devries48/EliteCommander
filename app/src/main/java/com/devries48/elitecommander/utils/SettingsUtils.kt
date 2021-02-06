package com.devries48.elitecommander.utils

import android.content.Context
import androidx.preference.PreferenceManager

object SettingsUtils {


    fun getBoolean(c: Context?, key: String?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(c)
            .getBoolean(key, false)
    }

    fun getInt(c: Context?, key: String?): Int {
        return PreferenceManager.getDefaultSharedPreferences(c)
            .getInt(key, -1)
    }

    fun setInt(c: Context?, key: String?, value: Int) {
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(c)
        val editor = preferenceManager.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getString(c: Context?, key: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(c)
            .getString(key, "")
    }

    fun setString(c: Context?, key: String?, value: String?) {
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(c)
        val editor = preferenceManager.edit()
        editor.putString(key, value)
        editor.apply()
    }
}