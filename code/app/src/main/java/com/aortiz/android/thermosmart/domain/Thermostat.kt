package com.aortiz.android.thermosmart.domain

import android.os.Parcelable
import com.aortiz.android.thermosmart.database.*
import kotlinx.parcelize.Parcelize

@Parcelize
class Thermostat(
    val id: String?,
    var configuration: Configuration,
    var status: Status,
) : Parcelable {

    @Parcelize
    class Configuration(
        var heating: Heating,
        var watering: Watering,
        var location: Location,
        var name: String?,
        private var followers: ArrayList<String>?,
    ) : Parcelable {
        @Parcelize
        class Heating(
            var automaticActivationEnabled: Boolean?,
            var threshold: Double?,
        ) : Parcelable {
            fun asDBHeatingConfiguration(): DBHeatingConfiguration {
                return DBHeatingConfiguration(
                    automaticActivationEnabled,
                    threshold
                )
            }
        }

        @Parcelize
        class Watering(
            var activationHour: Int? = null,
            var automaticActivationEnabled: Boolean? = null,
            var durationMinute: Int? = null,
            var frequencyDay: Int? = null,
        ) : Parcelable {
            fun asDBWateringConfiguration(): DBWateringConfiguration {
                return DBWateringConfiguration(
                    activationHour,
                    automaticActivationEnabled,
                    durationMinute,
                    frequencyDay
                )
            }
        }

        @Parcelize
        class Location(
            var latitude: Double?,
            var longitude: Double?,
            var location: String?,
        ) : Parcelable {
            fun asDBLocationConfiguration(): DBLocationConfiguration {
                return DBLocationConfiguration(
                    latitude,
                    longitude,
                    location
                )
            }
        }

        fun asDBConfiguration(): DBThermostatConfiguration {
            return DBThermostatConfiguration(
                heating.asDBHeatingConfiguration(),
                watering.asDBWateringConfiguration(),
                location.asDBLocationConfiguration(),
                name,
                followers
            )
        }
    }

    @Parcelize
    class Status(
        var esp8266: ESP8266,
        var outputs: Outputs,
        var lastWateringActivation: Int?,
        var nextWateringActivation: Int?,
        var temperature: Double?
    ) : Parcelable {
        fun asDBThermostatStatus(): DBThermostatStatus {
            return DBThermostatStatus(
                esp8266.asDBESP8266(),
                outputs.asDBOutputs(),
                lastWateringActivation,
                nextWateringActivation,
                temperature
            )
        }

        @Parcelize
        class ESP8266(
            var boilerState: Boolean?,
            var wateringState: Boolean?,
            var heartbeat: Int?,
            var num: Int?,
            var period: Int?,
        ) : Parcelable {
            fun asDBESP8266(): DBESP8266 {
                return DBESP8266(
                    boilerState,
                    wateringState,
                    heartbeat,
                    num,
                    period
                )
            }
        }

        @Parcelize
        class Outputs(
            var boiler: Boolean?,
            var watering: Boolean?
        ) : Parcelable {
            fun asDBOutputs(): DBOutputs {
                return DBOutputs(
                    boiler,
                    watering
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Thermostat && id != other.id
    }

    fun asDBThermostat(): DBThermostat {
        return DBThermostat(
            id,
            status.asDBThermostatStatus(),
            configuration.asDBConfiguration()
        )
    }
}
