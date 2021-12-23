package com.aortiz.android.thermosmart.notifications

import android.content.Intent
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.thermostat.MainActivity
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
        Timber.d("From: ${remoteMessage?.from}")

        if (!remoteMessage.data.isNullOrEmpty()) {
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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("title", mutableMap["title"])
        intent.putExtra("body", mutableMap["body"])
        intent.putExtra("thermostat", mutableMap["thermostat"])
        NotificationJobIntentService.enqueueWork(this, intent)
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