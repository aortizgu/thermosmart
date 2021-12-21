package com.aortiz.android.thermosmart.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Thermostat(
    var name: String?,
    var active: Boolean?,
    var location: String?,
    var temperature: Double?,
    var threshold: Double?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is Thermostat && id != other.id
    }
}