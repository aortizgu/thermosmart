package com.aortiz.android.thermosmart.database.realtime

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.database.*
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class RTDatabase(context: Context) {

    private var database: FirebaseDatabase =
        Firebase.database(context.getString(R.string.db_url))
        //Firebase.database(context.getString(R.string.emulator_db_url))

    companion object {
        const val ROOT_REFERENCE = "root"
        const val DEVICES_REFERENCE = "devices"
        const val USERS_REFERENCE = "users"
        const val NAME_REFERENCE = "name"
        const val CONFIG_REFERENCE = "configuration"
        const val STATUS_REFERENCE = "status"
        const val LOCATION_REFERENCE = "location"
        const val LAST_WATERING_ACTIVATION_REFERENCE = "lastWateringActivation"
        const val FOLLOWERS_REFERENCE = "followers"
        const val AUTOMATIC_ACTIVATION_ENABLED_REFERENCE = "automaticActivationEnabled"
        const val BOILER_REFERENCE = "boiler"
        const val WATERING_REFERENCE = "watering"
        const val THRESHOLD_REFERENCE = "threshold"
        const val TOKEN_REFERENCE = "token"
        const val ID_REFERENCE = "id"
    }

    init {
        database.setPersistenceEnabled(true)
    }

    fun load() {
        checkUser()
    }

    private fun checkUser() {
        Firebase.auth.currentUser?.uid?.let { uid ->
            database.getReference("$ROOT_REFERENCE/$USERS_REFERENCE/$uid/$ID_REFERENCE")
                .setValue(uid)
                .addOnSuccessListener {
                    Timber.i("checkUser: updated")
                }.addOnFailureListener {
                    Timber.e("checkUser: error $it")
                }
        }
    }

    fun getThermostatLiveData(thermostatId: String): FirebaseDatabaseLiveData<DBThermostat> {
        return FirebaseDatabaseLiveData(
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$thermostatId"),
            DBThermostat::class.java
        )
    }

    fun getUserThermostatListLiveData(): LiveData<List<DBThermostat>> {
        return FirebaseDatabaseLiveData(
            database.getReference("$ROOT_REFERENCE"),
            DBSnapShot::class.java
        ).map { snapShot ->
            val uid = Firebase.auth.currentUser?.uid
            if (uid != null) {
                val deviceList = snapShot.devices?.values?.toList()
                if (deviceList != null) {
                    return@map deviceList.filter { device ->
                        val deviceFollowers = device.configuration?.followers
                        if (deviceFollowers != null) {
                            return@filter deviceFollowers.contains(uid)
                        } else {
                            Timber.e("not valid deviceFollowers")
                            return@filter false
                        }
                    }
                } else {
                    Timber.e("not valid deviceList")
                    return@map emptyList()
                }
            } else {
                Timber.e("not valid Firebase.auth.currentUser")
                return@map emptyList()
            }
        }
    }

    fun getThermostat(thermostatId: String, cb: (result: OperationResult<DBThermostat>) -> Unit) {
        database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$thermostatId").get()
            .addOnSuccessListener { dataSnapshot ->
                Timber.d("getThermostat: success")
                var thermostat = dataSnapshot.getValue(DBThermostat::class.java)
                if (thermostat != null) {
                    cb(OperationResult.Success(thermostat))
                } else {
                    Timber.e("getThermostat: null thermostat")
                    cb(
                        OperationResult.Error(
                            Exception("null thermostat")
                        )
                    )
                }
            }.addOnFailureListener {
                Timber.e("getThermostat: Error getting data $it")
                cb(
                    OperationResult.Error(
                        Exception("Error getting data")
                    )
                )
            }
    }

    fun setThermostatConfig(
        id: String?,
        configuration: DBThermostatConfiguration?,
        cb: (result: OperationResult<String>) -> Unit
    ) {
        database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE")
            .setValue(configuration).addOnSuccessListener {
                Timber.i("setThermostatConfig: data saved")
                cb(OperationResult.Success("data saved"))
                return@addOnSuccessListener
            }.addOnFailureListener {
                Timber.e("setThermostatConfig: Error saving data $it")
                cb(
                    OperationResult.Error(
                        Exception("Error $it"),
                        ERROR.UNKNOWN
                    )
                )
                return@addOnFailureListener
            }
    }

    fun followThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("followThermostat: $id for user $userId")
            val reference =
                database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE")
            reference.get()
                .addOnSuccessListener addOnSuccessListener1@{ dataSnapshot ->
                    if (dataSnapshot.hasChild(id)) {
                        var devicesList = dataSnapshot.child(id).child(CONFIG_REFERENCE)
                            .child(FOLLOWERS_REFERENCE).getValue<ArrayList<String>>() ?: ArrayList()
                        if (!devicesList.contains(userId)) {
                            devicesList.add(userId)
                            reference.child(id).child(CONFIG_REFERENCE).child(FOLLOWERS_REFERENCE)
                                .setValue(devicesList)
                                .addOnSuccessListener {
                                    cb(OperationResult.Success("Followed"))
                                    return@addOnSuccessListener
                                }.addOnFailureListener {
                                    Timber.e("followThermostat: Error getting data $it")
                                    cb(
                                        OperationResult.Error(
                                            Exception("Error $it"),
                                            ERROR.UNKNOWN
                                        )
                                    )
                                    return@addOnFailureListener
                                }
                        } else {
                            Timber.e("followThermostat: Already Following")
                            cb(
                                OperationResult.Error(
                                    Exception("Already Following"),
                                    ERROR.ALREADY_FOLLOWING
                                )
                            )
                            return@addOnSuccessListener1
                        }
                    } else {
                        Timber.e("followThermostat: Invalid device")
                        cb(
                            OperationResult.Error(
                                Exception("Invalid Device"),
                                ERROR.INVALID_DEVICE
                            )
                        )
                        return@addOnSuccessListener1
                    }
                }.addOnFailureListener {
                    Timber.e("Error getting data $it")
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("fetchThermostats: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun unfollowThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("unfollowThermostat: $id for user $userId")
            val reference =
                database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$FOLLOWERS_REFERENCE")
            reference.get()
                .addOnSuccessListener addOnSuccessListener1@{ dataSnapshot ->
                    var devicesList = dataSnapshot.getValue<ArrayList<String>>() ?: ArrayList()
                    if (devicesList.contains(userId)) {
                        devicesList.remove(userId)
                        reference
                            .setValue(devicesList)
                            .addOnSuccessListener {
                                cb(OperationResult.Success("Unfollowed"))
                                return@addOnSuccessListener
                            }.addOnFailureListener {
                                Timber.e("unfollowThermostat: Error getting data $it")
                                cb(
                                    OperationResult.Error(
                                        Exception("Error $it"),
                                        ERROR.UNKNOWN
                                    )
                                )
                                return@addOnFailureListener
                            }
                    } else {
                        Timber.e("followThermostat: Not Following")
                        cb(
                            OperationResult.Error(
                                Exception("Not Following device"),
                                ERROR.NOT_FOLLOWING
                            )
                        )
                        return@addOnSuccessListener1
                    }
                }.addOnFailureListener {
                    Timber.e("Error getting data $it")
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("fetchThermostats: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun updateDeviceToken(token: String) {
        Firebase.auth.currentUser?.uid?.let { uid ->
            database.getReference("$ROOT_REFERENCE/$USERS_REFERENCE/$uid/$TOKEN_REFERENCE")
                .setValue(token)
                .addOnSuccessListener {
                    Timber.d("updateDeviceToken: updated")
                }.addOnFailureListener {
                    Timber.d("updateDeviceToken: error $it")
                }
        }
    }

    fun setControllerName(
        id: String,
        name: String,
        cb: (result: OperationResult<String>) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("changeThermostatName: new name $name, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$NAME_REFERENCE")
                .setValue(name)
                .addOnSuccessListener {
                    cb(OperationResult.Success("Updated"))
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerName: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerLocation(
        id: String,
        location: DBLocationConfiguration,
        cb: (result: OperationResult<String>) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerLocation: new location $location, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$LOCATION_REFERENCE")
                .setValue(location).addOnSuccessListener {
                    cb(OperationResult.Success("data saved"))
                    return@addOnSuccessListener
                }.addOnFailureListener {
                    cb(
                        OperationResult.Error(
                            Exception("Error $it"),
                            ERROR.UNKNOWN
                        )
                    )
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerLocation: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerBoilerAutomaticActivation(id: String, checked: Boolean, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerBoilerAutomaticActivation: new state $checked, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$BOILER_REFERENCE/$AUTOMATIC_ACTIVATION_ENABLED_REFERENCE")
                .setValue(checked)
                .addOnSuccessListener {
                    cb(OperationResult.Success("Updated"))
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerBoilerAutomaticActivation: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerWateringAutomaticActivation(id: String, checked: Boolean, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerWateringAutomaticActivation: new state $checked, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$WATERING_REFERENCE/$AUTOMATIC_ACTIVATION_ENABLED_REFERENCE")
                .setValue(checked)
                .addOnSuccessListener {
                    cb(OperationResult.Success("Updated"))
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerWateringAutomaticActivation: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerBoilerThreshold(id: String, threshold: Double, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerBoilerThreshold: new threshold $threshold, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$BOILER_REFERENCE/$THRESHOLD_REFERENCE")
                .setValue(threshold)
                .addOnSuccessListener {
                    cb(OperationResult.Success("Updated"))
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    cb(OperationResult.Error(Exception("Error $it"), ERROR.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerBoilerAutomaticActivation: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerWateringConfig(id: String, wateringConfig: DBWateringConfiguration, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerWateringConfig: new wateringConfig $wateringConfig, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$WATERING_REFERENCE")
                .setValue(wateringConfig).addOnSuccessListener {
                    cb(OperationResult.Success("data saved"))
                    return@addOnSuccessListener
                }.addOnFailureListener {
                    cb(
                        OperationResult.Error(
                            Exception("Error $it"),
                            ERROR.UNKNOWN
                        )
                    )
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerWateringConfig: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }

    fun setControllerLastWateringActivation(id: String, epochSecond: Long, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("setControllerLastWateringActivation: new lastWateringActivation $epochSecond, for id $id")
            database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$STATUS_REFERENCE/$LAST_WATERING_ACTIVATION_REFERENCE")
                .setValue(epochSecond).addOnSuccessListener {
                    cb(OperationResult.Success("data saved"))
                    return@addOnSuccessListener
                }.addOnFailureListener {
                    cb(
                        OperationResult.Error(
                            Exception("Error $it"),
                            ERROR.UNKNOWN
                        )
                    )
                    return@addOnFailureListener
                }
        } else {
            Timber.d("setControllerWateringConfig: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR.INVALID_USER))
        }
    }
}