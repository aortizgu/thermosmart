package com.aortiz.android.thermosmart.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.TaskStackBuilder
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aortiz.android.thermosmart.R
import timber.log.Timber

private const val NOTIFICATION_CHANNEL_ID =
    "com.google.firebase.messaging.default_notification_channel_id_0"

fun sendNotification(context: Context, title: String, body: String, thermostatId: String) {
    Timber.i("sendNotification")
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intId = polynomialRollingHash(thermostatId)
    if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    val intent =
        DetailNotificationActivity.newIntent(context.applicationContext, thermostatId)
    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(DetailNotificationActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder.getPendingIntent(intId, FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
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

    with(NotificationManagerCompat.from(context)) {
        notify(intId, builder.build())
    }
}

fun polynomialRollingHash(str: String): Int {

    // P and M
    val p = 31
    val m = (1e9 + 9).toInt()
    var powerOfP = 1
    var hashVal = 0

    // Loop to calculate the hash value
    // by iterating over the elements of String
    for (i in 0 until str.length) {
        hashVal = (hashVal + (str[i] -
                'a' + 1) * powerOfP) % m
        powerOfP = powerOfP * p % m
    }
    return hashVal
}