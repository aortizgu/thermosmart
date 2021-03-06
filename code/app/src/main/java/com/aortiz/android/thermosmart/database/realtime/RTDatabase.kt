package com.aortiz.android.thermosmart.database.realtime

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.database.DBSnapShot
import com.aortiz.android.thermosmart.database.DBThermostat
import com.aortiz.android.thermosmart.database.DBThermostatConfiguration
import com.aortiz.android.thermosmart.utils.ERROR_CODE
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
        const val CONFIG_REFERENCE = "configuration"
        const val FOLLOWERS_REFERENCE = "followers"
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

    fun getUserThermostatListLiveData(): LiveData<List<DBThermostat>>{
        return FirebaseDatabaseLiveData(
            database.getReference("$ROOT_REFERENCE"),
            DBSnapShot::class.java
        ).map { snapShot ->
            val uid = Firebase.auth.currentUser?.uid
            if (uid != null) {
                val deviceList = snapShot.devices?.values?.toList()
                if (deviceList != null){
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

    fun setThermostatConfig(id: String?, configuration: DBThermostatConfiguration?) {
        database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE")
            .setValue(configuration).addOnSuccessListener {
                Timber.i("setThermostatConfig: data saved")
            }.addOnFailureListener {
                Timber.e("setThermostatConfig: Error saving data $it")
            }
    }

    fun followThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("followThermostat: $id for user $userId")
            val reference =
                database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE")
            reference.get()
                .addOnSuccessListener { dataSnapshot ->
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
                                            ERROR_CODE.UNKNOWN
                                        )
                                    )
                                    return@addOnFailureListener
                                }
                        } else {
                            Timber.e("followThermostat: Already Following")
                            cb(
                                OperationResult.Error(
                                    Exception("Already Following"),
                                    ERROR_CODE.ALREADY_FOLLOWING
                                )
                            )
                            return@addOnSuccessListener
                        }
                    } else {
                        Timber.e("followThermostat: Invalid device")
                        cb(
                            OperationResult.Error(
                                Exception("Invalid Device"),
                                ERROR_CODE.INVALID_DEVICE
                            )
                        )
                        return@addOnSuccessListener
                    }
                }.addOnFailureListener {
                    Timber.e("Error getting data $it")
                    cb(OperationResult.Error(Exception("Error $it"), ERROR_CODE.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("fetchThermostats: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR_CODE.INVALID_USER))
        }
    }

    fun unfollowThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            Timber.d("unfollowThermostat: $id for user $userId")
            val reference =
                database.getReference("$ROOT_REFERENCE/$DEVICES_REFERENCE/$id/$CONFIG_REFERENCE/$FOLLOWERS_REFERENCE")
            reference.get()
                .addOnSuccessListener { dataSnapshot ->
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
                                        ERROR_CODE.UNKNOWN
                                    )
                                )
                                return@addOnFailureListener
                            }
                    } else {
                        Timber.e("followThermostat: Not Following")
                        cb(
                            OperationResult.Error(
                                Exception("Not Following device"),
                                ERROR_CODE.NOT_FOLLOWING
                            )
                        )
                        return@addOnSuccessListener
                    }
                }.addOnFailureListener {
                    Timber.e("Error getting data $it")
                    cb(OperationResult.Error(Exception("Error $it"), ERROR_CODE.UNKNOWN))
                    return@addOnFailureListener
                }
        } else {
            Timber.d("fetchThermostats: cannot get user")
            cb(OperationResult.Error(Exception("Error getting user"), ERROR_CODE.INVALID_USER))
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
}