package com.aortiz.android.thermosmart.thermostat.detail

import android.app.Application
import androidx.lifecycle.*
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant

class ThermostatDetailViewModel(
    app: Application,
    private val repository: ThermostatRepository,
    private val thermostatId: String
) : AndroidViewModel(app) {

    enum class UpdateState {
        IDLE, UPDATED, ERROR
    }

    val thermostat: LiveData<Thermostat> = repository.getThermostatLiveData(thermostatId).map {
        it.asDomainModel()
    }
    val connectedState: LiveData<Boolean> = repository.getConnectedStateLiveData()
    val connectedStateFiltered: MutableLiveData<Boolean> =  MutableLiveData(true)

    private val _updateState = MutableLiveData(UpdateState.IDLE)
    val updateState: LiveData<UpdateState>
        get() = _updateState

    private val _errorCode = MutableLiveData<ERROR?>(null)
    val errorCode: LiveData<ERROR?>
        get() = _errorCode

    val cityName: LiveData<String?> = repository.cityName
    val exteriorImage: LiveData<String?> = repository.exteriorImage
    val exteriorTemp: LiveData<Double?> = repository.exteriorTemp

    fun loadWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.loadWeatherData(lat, lon)
        }
    }

    fun unloadWeatherData() {
        repository.unloadWeatherData()
    }

    fun changeDeviceName(name: String) {
        Timber.d("changeDeviceName $name")
        if (updateState.value == UpdateState.IDLE) {
            repository.setControllerName(thermostatId, name) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("changeDeviceName:reply: success ${result.data}")
                        _updateState.value = UpdateState.UPDATED
                    }
                    is OperationResult.Error -> {
                        Timber.d("changeDeviceName:reply: error ${result.exception}")
                        _updateState.value = UpdateState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun setControllerHeatingConfig(heatingConfig: Thermostat.Configuration.Heating) {
        Timber.d("setControllerHeatingConfig $heatingConfig")
        if (updateState.value == UpdateState.IDLE) {
            repository.setControllerHeatingConfig(thermostatId, heatingConfig) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("setControllerHeatingConfig:reply: success ${result.data}")
                        _updateState.value = UpdateState.UPDATED
                    }
                    is OperationResult.Error -> {
                        Timber.d("setControllerHeatingConfig:reply: error ${result.exception}")
                        _updateState.value = UpdateState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun setControllerWateringConfig(wateringConfig: Thermostat.Configuration.Watering) {
        Timber.d("setControllerWateringConfig $wateringConfig")
        if (updateState.value == UpdateState.IDLE) {
            repository.setControllerWateringConfig(thermostatId, wateringConfig) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("setControllerBoilerThreshold:reply: success ${result.data}")
                        _updateState.value = UpdateState.UPDATED
                    }
                    is OperationResult.Error -> {
                        Timber.d("setControllerBoilerThreshold:reply: error ${result.exception}")
                        _updateState.value = UpdateState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }


    fun startWatering() {
        Timber.d("startWatering")
        if (updateState.value == UpdateState.IDLE) {
            repository.setControllerLastWateringActivation(
                thermostatId,
                Instant.now().epochSecond
            ) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("setControllerBoilerThreshold:reply: success ${result.data}")
                        _updateState.value = UpdateState.UPDATED
                    }
                    is OperationResult.Error -> {
                        Timber.d("setControllerBoilerThreshold:reply: error ${result.exception}")
                        _updateState.value = UpdateState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = UpdateState.IDLE
    }

    fun clearError() {
        _errorCode.value = null
    }

}