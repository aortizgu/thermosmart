package com.aortiz.android.thermosmart

import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.OperationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class ThermosmartFirebaseMessagingService : FirebaseMessagingService() {

    val repository: ThermostatRepository by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: ${remoteMessage?.from}")
        remoteMessage?.data?.let {
            Timber.d("Message data payload: " + remoteMessage.data)
        }
        remoteMessage?.notification?.let {
            Timber.d("Message Notification Body: ${it.body}")
            sendNotification(it.body!!)
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")
        repository.updateDeviceToken(token)
    }

    private fun sendNotification(messageBody: String) {
//        val notificationManager = ContextCompat.getSystemService(
//            applicationContext,
//            NotificationManager::class.java
//        ) as NotificationManager
//        notificationManager.sendNotification(messageBody, applicationContext)
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