package com.aortiz.android.thermosmart.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aortiz.android.thermosmart.database.local.SharedPreferencesDatabase
import com.aortiz.android.thermosmart.repository.ThermostatRepository

class AppConfigViewModel(
    app: Application,
    private val repository: ThermostatRepository
) : AndroidViewModel(app) {

    var showInFahrenheit: Boolean
        get() = repository.getShowInFahrenheitConfig()
        set(value) = repository.setShowInFahrenheitConfig(value)
}