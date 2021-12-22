package com.aortiz.android.thermosmart.database

import com.aortiz.android.thermosmart.domain.Thermostat
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DBThermostatConfiguration(
    var threshold: Double? = null,
    var name: String? = null,
    var lat: Double? = null,
    var lon: Double? = null,
    var location: String? = null,
    var followers: ArrayList<String>? = null,
)

@IgnoreExtraProperties
data class DBThermostatStatus(
    var active: Boolean? = false,
    var temperature: Double? = 0.0
)

@IgnoreExtraProperties
data class DBThermostat(
    var id: String? = "",
    var status: DBThermostatStatus? = DBThermostatStatus(),
    var configuration: DBThermostatConfiguration? = DBThermostatConfiguration()
) {
    fun asDomainModel(): Thermostat {
        return Thermostat(
            id,
            status?.active,
            status?.temperature,
            configuration?.name,
            configuration?.location,
            configuration?.threshold,
            configuration?.lat,
            configuration?.lon,
            configuration?.followers
        )
    }
}

@IgnoreExtraProperties
data class DBUser(
    var id: String? = null,
    var token: String? = null,
)

@IgnoreExtraProperties
data class DBSnapShot(
    var devices: HashMap<String, DBThermostat>? = null,
    var users: HashMap<String, DBUser>? = null,
)
