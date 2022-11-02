package com.aortiz.android.thermosmart.thermostat.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import timber.log.Timber

class ThermostatConfigViewModel(app: Application, private val repository: ThermostatRepository) :
    AndroidViewModel(app) {

    val name = MutableLiveData<String?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()
    val location = MutableLiveData<String?>()

    enum class UnfollowState {
        IDLE, UNFOLLOWED, ERROR
    }

    private val _unfollowState = MutableLiveData(UnfollowState.IDLE)
    val unfollowState: LiveData<UnfollowState>
        get() = _unfollowState

    private val _errorCode = MutableLiveData<ERROR?>(null)
    val errorCode: LiveData<ERROR?>
        get() = _errorCode

    fun unfollowThermostat(id: String) {
        Timber.d("unfollowThermostat")
        if (unfollowState.value == UnfollowState.IDLE) {
            repository.unfollowThermostat(id) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("unfollowThermostat:reply: success ${result.data}")
                        _unfollowState.value = UnfollowState.UNFOLLOWED
                    }
                    is OperationResult.Error -> {
                        Timber.d("unfollowThermostat:reply: error ${result.exception}")
                        _unfollowState.value = UnfollowState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun saveConfig(thermostat: Thermostat) {
        Timber.d("saveConfig")
        thermostat.name = name.value
        thermostat.latitude = latitude.value
        thermostat.longitude = longitude.value
        thermostat.location = location.value
        Timber.d("saveConfig: new thermostat ${thermostat.name}")
        repository.setThermostatConfig(thermostat)
    }

    fun initData(thermostat: Thermostat) {
        if (name.value == null) {
            name.value = thermostat.name
        }
        if (latitude.value == null) {
            latitude.value = thermostat.latitude
        }
        if (longitude.value == null) {
            longitude.value = thermostat.longitude
        }
        if (location.value == null) {
            location.value = thermostat.location
        }
    }

    fun onClear() {
        name.value = null
        latitude.value = null
        longitude.value = null
        location.value = null
    }

    fun clearState() {
        _unfollowState.value = UnfollowState.IDLE
    }

    fun clearError() {
        _errorCode.value = null
    }

}