package com.aortiz.android.thermosmart.thermostat.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

class ThermostatConfigViewModel(app: Application) : AndroidViewModel(app) {
    fun removeThermostat() {
        Timber.d("removeThermostat")
    }

    fun saveConfig() {
        Timber.d("saveConfig")
    }
}