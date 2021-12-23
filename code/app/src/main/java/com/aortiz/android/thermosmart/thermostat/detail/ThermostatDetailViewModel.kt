package com.aortiz.android.thermosmart.thermostat.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import kotlinx.coroutines.launch

class ThermostatDetailViewModel(
    app: Application,
    val repository: ThermostatRepository,
    thermostatId: String
) : AndroidViewModel(app) {

    val thermostat: LiveData<Thermostat> = repository.getThermostatLiveData(thermostatId).map {
        it.asDomainModel()
    }

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
}