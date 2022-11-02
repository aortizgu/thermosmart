package com.aortiz.android.thermosmart.notifications

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
        if (mutableMap.containsKey("title")
            && mutableMap.containsKey("body")
            && mutableMap.containsKey("thermostat")
        ) {
            val title = mutableMap.getOrDefault("title", "")
            val body = mutableMap.getOrDefault("body", "")
            val thermostatId = mutableMap.getOrDefault("thermostat", "")
            sendNotification(
                applicationContext,
                title,
                body,
                thermostatId
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