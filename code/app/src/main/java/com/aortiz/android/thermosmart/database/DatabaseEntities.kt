package com.aortiz.android.thermosmart.database

import com.aortiz.android.thermosmart.domain.Thermostat
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DBBoilerConfiguration(
    var automaticActivationEnabled: Boolean? = null,
    var threshold: Double? = null,
)

@IgnoreExtraProperties
data class DBWateringConfiguration(
    var activationHour: Int? = null,
    var automaticActivationEnabled: Boolean? = null,
    var durationMinute: Int? = null,
    var frequencyDay: Int? = null,
)

@IgnoreExtraProperties
data class DBLocationConfiguration(
    var lat: Double? = null,
    var lon: Double? = null,
    var location: String? = null,
)

@IgnoreExtraProperties
data class DBThermostatConfiguration(
    var boiler: DBBoilerConfiguration? = null,
    var watering: DBWateringConfiguration? = null,
    var location: DBLocationConfiguration? = null,
    var name: String? = null,
    var followers: ArrayList<String>? = null,
)

@IgnoreExtraProperties
class DBESP8266(
    var boilerState: Boolean? = false,
    var wateringState: Boolean? = false,
    var heartbeat: Int? = null,
    var num: Int? = null,
    var period: Int? = null,
)

@IgnoreExtraProperties
class DBOutputs(
    var boiler: Boolean? = null,
    var watering: Boolean? = null
)

@IgnoreExtraProperties
data class DBThermostatStatus(
    var esp8266: DBESP8266? = null,
    var outputs: DBOutputs? = null,
    var lastWateringActivation: Int? = null,
    var nextWateringActivation: Int? = null,
    var temperature: Double? = null
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
            Thermostat.Configuration(
                Thermostat.Configuration.Boiler(
                    configuration?.boiler?.automaticActivationEnabled,
                    configuration?.boiler?.threshold
                ),
                Thermostat.Configuration.Watering(
                    configuration?.watering?.activationHour,
                    configuration?.watering?.automaticActivationEnabled,
                    configuration?.watering?.durationMinute,
                    configuration?.watering?.frequencyDay,
                ),
                Thermostat.Configuration.Location(
                    configuration?.location?.lat,
                    configuration?.location?.lon,
                    configuration?.location?.location,
                ),
                configuration?.name,
                configuration?.followers
            ),
            Thermostat.Status(
                Thermostat.Status.ESP8266(
                    status?.esp8266?.boilerState,
                    status?.esp8266?.wateringState,
                    status?.esp8266?.heartbeat,
                    status?.esp8266?.num,
                    status?.esp8266?.period
                ),
                Thermostat.Status.Outputs(
                    status?.outputs?.boiler,
                    status?.outputs?.watering
                ),
                status?.lastWateringActivation,
                status?.nextWateringActivation,
                status?.temperature
            )
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
