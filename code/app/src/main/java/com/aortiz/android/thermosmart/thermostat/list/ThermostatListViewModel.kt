package com.aortiz.android.thermosmart.thermostat.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aortiz.android.thermosmart.repository.ThermostatRepository

class ThermostatListViewModel(app: Application, repository: ThermostatRepository) :
    AndroidViewModel(app) {

    val thermostatList = repository.thermostatList

}