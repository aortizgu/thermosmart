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
        var boiler: Boiler,
        var watering: Watering,
        var location: Location,
        var name: String?,
        private var followers: ArrayList<String>?,
    ) : Parcelable {
        @Parcelize
        class Boiler(
            var automaticActivationEnabled: Boolean?,
            var threshold: Double?,
        ) : Parcelable {
            fun asDBBoilerConfiguration(): DBBoilerConfiguration {
                return DBBoilerConfiguration(
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
                boiler.asDBBoilerConfiguration(),
                watering.asDBWateringConfiguration(),
                location.asDBLocationConfiguration(),
                name,
                followers
            )
        }
    }

    @Parcelize
    class Status(
        private var esp8266: ESP8266,
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
            private var boilerState: Boolean?,
            private var wateringState: Boolean?,
            private var heartbeat: Int?,
            private var num: Int?,
            private var period: Int?,
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
