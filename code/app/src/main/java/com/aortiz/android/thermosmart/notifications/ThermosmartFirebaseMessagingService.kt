package com.aortiz.android.thermosmart.notifications

import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.OperationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class ThermosmartFirebaseMessagingService : FirebaseMessagingService() {

    private val repository: ThermostatRepository by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: " + remoteMessage.data)
            sendNotification(remoteMessage.data)
        } else {
            Timber.e("Message data payload null or empty")
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        repository.updateDeviceToken(token)
    }

    private fun sendNotification(mutableMap: MutableMap<String, String>) {
        if (mutableMap.containsKey("id")
            && mutableMap.containsKey("name")
            && mutableMap.containsKey("state")
            && mutableMap.containsKey("system")
        ) {
            val id = mutableMap.getOrDefault("id", "")
            val name = mutableMap.getOrDefault("name", "")
            val state = mutableMap.getOrDefault("state", "")
            val system = mutableMap.getOrDefault("system", "")
            if (id.isEmpty() || name.isEmpty() || state.isEmpty() || system.isEmpty()) {
                Timber.e("invalid parameters, cannot be empty")
                return
            }
            if (state != "true" && state != "false") {
                Timber.e("invalid state parameter $state")
                return
            }
            if (system != "watering" && system != "boiler") {
                Timber.e("invalid system parameter $system")
                return
            }
            val systemString =
                if (system == "watering") getString(R.string.watering_system) else getString(R.string.boiler_system)
            val stateString = if (system == "watering") {
                if (state == "true") getString(R.string.system_on_m) else getString(R.string.system_off_m)
            } else {
                if (state == "true") getString(R.string.system_on_f) else getString(R.string.system_off_f)
            }
            val title = getString(R.string.notification_title, systemString, stateString)
            val body = if (system == "watering") {
                if (state == "true") getString(
                    R.string.notification_body_watering_active,
                    name
                ) else getString(R.string.notification_body_watering_inactive, name)
            } else {
                if (state == "true") getString(
                    R.string.notification_body_boiler_active,
                    name
                ) else getString(R.string.notification_body_boiler_inactive, name)
            }
            sendNotification(
                applicationContext,
                title,
                body,
                id
            )
        } else {
            Timber.e("missing keys received")
        }
    }

    companion object {
        fun refreshToken(cb: (result: OperationResult<String>) -> Unit) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    task.result?.let { token ->
                        Timber.i("Fetching FCM registration token success $token")
                        cb(OperationResult.Success(token))
                        return@OnCompleteListener
                    }
                } else {
                    Timber.i("Fetching FCM registration token failed ${task.exception}")
                    cb(
                        OperationResult.Error(
                            Exception("Error ${task.exception}")
                        )
                    )
                    return@OnCompleteListener
                }
            })
        }
    }
}