package com.aortiz.android.thermosmart.domain

import android.os.Parcelable
import com.aortiz.android.thermosmart.database.DBThermostat
import com.aortiz.android.thermosmart.database.DBThermostatConfiguration
import com.aortiz.android.thermosmart.database.DBThermostatStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
class Thermostat(
    val id: String?,
    var active: Boolean?,
    var temperature: Double?,
    var name: String?,
    var location: String?,
    var threshold: Double?,
    var latitude: Double?,
    var longitude: Double?,
    var followers: ArrayList<String>?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is Thermostat && id != other.id
    }

    fun asDBThermostat(): DBThermostat {
        return DBThermostat(
            id,
            DBThermostatStatus(active, temperature),
            DBThermostatConfiguration(threshold, name, latitude, longitude, location, followers)
        )
    }
}
