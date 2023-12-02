package com.aortiz.android.thermosmart.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aortiz.android.thermosmart.R
import timber.log.Timber


private const val NOTIFICATION_CHANNEL_ID =
    "com.google.firebase.messaging.default_notification_channel_id_0"
const val STATE_TRUE = "true"
const val STATE_FALSE = "false"
const val SYSTEM_WATERING = "watering"
const val SYSTEM_BOILER = "boiler"

fun sendNotification(
    context: Context,
    title: String,
    body: String,
    thermostatId: String,
    system: String
) {
    Timber.i("sendNotification")
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        NotificationActivity.newIntent(context.applicationContext, thermostatId)
    val notificationPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        // Add the intent, which inflates the back stack
        addNextIntentWithParentStack(intent)
        // Get the PendingIntent containing the entire back stack
        getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(if (system == SYSTEM_BOILER) R.drawable.ic_stat_name else R.drawable.ic_stat_water)
        .setLargeIcon(
            BitmapFactory.decodeResource(
                context.resources,
                if (system == SYSTEM_BOILER) R.drawable.ic_heating else R.drawable.ic_water_hose
            )
        )
        .setContentTitle(title)
        .setContentText(body)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)

    val intId = polynomialRollingHash(thermostatId+system)
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