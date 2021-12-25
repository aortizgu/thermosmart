package com.aortiz.android.thermosmart.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.domain.Thermostat
import timber.log.Timber

private const val NOTIFICATION_CHANNEL_ID =
    "com.google.firebase.messaging.default_notification_channel_id_0"

fun sendNotification(context: Context, title: String, body: String, thermostat: Thermostat) {
    Timber.i("sendNotification")
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intId = polynomialRollingHash(thermostat.id?:"")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    val intent = DetailNotificationActivity.newIntent(context.applicationContext, thermostat)

    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(DetailNotificationActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(intId, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_name)
        .setLargeIcon(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_heater
            )
        )
        .setContentTitle(title)
        .setContentText(body)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(intId, notification)
}

fun polynomialRollingHash(str: String): Int {

    // P and M
    val p = 31
    val m = (1e9 + 9).toInt()
    var power_of_p = 1
    var hash_val = 0

    // Loop to calculate the hash value
    // by iterating over the elements of String
    for (i in 0 until str.length) {
        hash_val = (hash_val + (str[i] -
                'a' + 1) * power_of_p) % m
        power_of_p = power_of_p * p % m
    }
    return hash_val
}