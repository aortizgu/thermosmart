package com.aortiz.android.thermosmart.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aortiz.android.thermosmart.database.local.SharedPreferencesDatabase

class AppConfigViewModel(
    app: Application,
    private val sharedPreferencesDatabase: SharedPreferencesDatabase
) : AndroidViewModel(app) {

    var showInFahrenheit: Boolean
        get() = sharedPreferencesDatabase.sharedPreferencesDao.getShowInFahrenheitConfig()
        set(value) = sharedPreferencesDatabase.sharedPreferencesDao.setShowInFahrenheitConfig(value)
}