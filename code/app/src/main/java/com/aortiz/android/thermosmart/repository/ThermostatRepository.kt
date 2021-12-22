package com.aortiz.android.thermosmart.repository

import androidx.lifecycle.map
import com.aortiz.android.thermosmart.database.DBThermostat
import com.aortiz.android.thermosmart.database.realtime.FirebaseDatabaseLiveData
import com.aortiz.android.thermosmart.database.realtime.RTDatabase
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.utils.OperationResult

class ThermostatRepository(private val rtdb: RTDatabase) {

    val thermostatList = rtdb.userThermostatList.map { list ->
        list?.map {
            it?.asDomainModel()
        }
    }

    fun getThermostat(thermostatId: String): FirebaseDatabaseLiveData<DBThermostat> {
        return rtdb.getThermostat(thermostatId)
    }

    fun setThermostatConfig(thermostat: Thermostat) {
        val dbObject = thermostat.asDBThermostat()
        rtdb.setThermostatConfig(dbObject.id, dbObject.configuration)
    }

    fun followThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        rtdb.followThermostat(id, cb)
    }

    fun unfollowThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        rtdb.unfollowThermostat(id, cb)
    }

    fun updateDeviceToken(token: String) {
        rtdb.updateDeviceToken(token)
    }

    fun load() {
        rtdb.load()
    }

}