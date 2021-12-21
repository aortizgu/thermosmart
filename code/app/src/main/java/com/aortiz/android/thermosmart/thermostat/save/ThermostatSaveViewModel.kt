package com.aortiz.android.thermosmart.thermostat.save

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber

class ThermostatSaveViewModel(app: Application) : AndroidViewModel(app) {
    fun save() {
        Timber.d("save")
    }
}