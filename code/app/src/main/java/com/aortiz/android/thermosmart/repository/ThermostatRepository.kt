package com.aortiz.android.thermosmart.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aortiz.android.thermosmart.domain.Thermostat

class ThermostatRepository {
    private val _thermostatList = MutableLiveData(
        listOf(
            Thermostat(
                "name",
                true,
                "location",
                0.0,
                0.0,
                0.0,
                0.0,
                "id1"
            )
        )
    )
    val thermostatList: LiveData<List<Thermostat>>
        get() = _thermostatList
}