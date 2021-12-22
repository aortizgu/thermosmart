package com.aortiz.android.thermosmart.thermostat.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.repository.ThermostatRepository

class ThermostatDetailViewModel(
    app: Application,
    repository: ThermostatRepository,
    thermostatId: String
) : AndroidViewModel(app) {

    val thermostat: LiveData<Thermostat> = repository.getThermostat(thermostatId).map {
        it.asDomainModel()
    }

}