package com.aortiz.android.thermosmart.database.local

import android.content.Context
import com.aortiz.android.thermosmart.R

interface SharedPreferencesDao {
    fun getShowInFahrenheitConfig(): Boolean
    fun setShowInFahrenheitConfig(value: Boolean)
}

class SharedPreferencesDatabase(context: Context) {
    var sharedPreferencesDao: SharedPreferencesDao = SharedPreferencesDaoImpl(context)

    class SharedPreferencesDaoImpl(private val context: Context) : SharedPreferencesDao {
        override fun getShowInFahrenheitConfig(): Boolean {
            val defaultValue = context.resources.getBoolean(R.bool.show_in_fahrenheit)
            val sharedPref = context.getSharedPreferences(
                context.resources.getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            return sharedPref.getBoolean(
                context.resources.getString(R.string.show_in_fahrenheit_key),
                defaultValue
            )
        }

        override fun setShowInFahrenheitConfig(value: Boolean) {
            val sharedPref = context.getSharedPreferences(
                context.resources.getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putBoolean(context.resources.getString(R.string.show_in_fahrenheit_key), value)
                apply()
            }
        }
    }
}