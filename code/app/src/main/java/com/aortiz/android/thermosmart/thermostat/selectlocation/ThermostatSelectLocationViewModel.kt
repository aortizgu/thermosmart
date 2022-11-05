package com.aortiz.android.thermosmart.thermostat.selectlocation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import timber.log.Timber

class ThermostatSelectLocationViewModel(
    app: Application,
    private val repository: ThermostatRepository
) : AndroidViewModel(app) {

    enum class SaveState {
        IDLE, SAVED, ERROR
    }

    private val _saveState = MutableLiveData(SaveState.IDLE)
    val saveState: LiveData<SaveState>
        get() = _saveState

    private val _errorCode = MutableLiveData<ERROR?>(null)
    val errorCode: LiveData<ERROR?>
        get() = _errorCode

    fun setControllerLocation(
        id: String,
        latitude: Double,
        longitude: Double,
        location: String
    ) {
        Timber.i("setControllerLocation: id $id latitude $latitude longitude $longitude location $location")
        if (saveState.value == SaveState.IDLE) {
            repository.setControllerLocation(id, latitude, longitude, location) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("updateThermostat:reply: success ${result.data}")
                        _saveState.value = SaveState.SAVED
                    }
                    is OperationResult.Error -> {
                        Timber.d("updateThermostat:reply: error ${result.exception}")
                        _saveState.value = SaveState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }

    }
//    fun updateThermostat(thermostat: Thermostat) {
//        Timber.d("updateThermostat $thermostat")
//        if (saveState.value == SaveState.IDLE) {
//            repository.setThermostatConfig(thermostat) { result ->
//                when (result) {
//                    is OperationResult.Success -> {
//                        Timber.d("updateThermostat:reply: success ${result.data}")
//                        _saveState.value = SaveState.SAVED
//                    }
//                    is OperationResult.Error -> {
//                        Timber.d("updateThermostat:reply: error ${result.exception}")
//                        _saveState.value = SaveState.ERROR
//                        _errorCode.value = result.code
//                    }
//                }
//            }
//        }
//
//    }

    fun clearSavedState() {
        _saveState.value = SaveState.IDLE
    }

    fun clearError() {
        _errorCode.value = null
    }


}