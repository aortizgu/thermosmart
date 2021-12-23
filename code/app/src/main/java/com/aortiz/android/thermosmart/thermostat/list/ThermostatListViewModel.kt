package com.aortiz.android.thermosmart.thermostat.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.aortiz.android.thermosmart.database.DBThermostat
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.notifications.ThermosmartFirebaseMessagingService.Companion.refreshToken
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.OperationResult
import kotlinx.coroutines.launch

class ThermostatListViewModel(app: Application, repository: ThermostatRepository) :
    AndroidViewModel(app) {

    var thermostatList : LiveData<List<Thermostat>> = repository.getUserThermostatListLiveData()
    init {
        viewModelScope.launch {
            repository.load()
            refreshToken {
                when (it) {
                    is OperationResult.Success -> repository.updateDeviceToken(it.data)
                }
            }
        }
    }
}