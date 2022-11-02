package com.aortiz.android.thermosmart.thermostat.save

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import timber.log.Timber

class ThermostatSaveViewModel(app: Application, private val repository: ThermostatRepository) :
    AndroidViewModel(app) {

    enum class SaveState {
        IDLE, FOLLOWED, ERROR
    }

    private val _followState = MutableLiveData(SaveState.IDLE)
    val followState: LiveData<SaveState>
        get() = _followState


    private val _errorCode = MutableLiveData<ERROR?>(null)
    val errorCode: LiveData<ERROR?>
        get() = _errorCode

    fun followThermostat(id: String) {
        Timber.d("followThermostat")
        if (followState.value == SaveState.IDLE) {
            repository.followThermostat(id) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("followThermostat:reply: success ${result.data}")
                        _followState.value = SaveState.FOLLOWED
                    }
                    is OperationResult.Error -> {
                        Timber.d("followThermostat:reply: error ${result.exception}")
                        _followState.value = SaveState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun clearState() {
        _followState.value = SaveState.IDLE
    }

    fun clearError() {
        _errorCode.value = null
    }
}