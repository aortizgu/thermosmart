package com.aortiz.android.thermosmart.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.OperationResult
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class NotificationJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                NotificationJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        Timber.i("onHandleWork")
        val remindersLocalRepository: ThermostatRepository by inject()
        val title = intent?.extras?.getString("title")
        val body = intent?.extras?.getString("body")
        val thermostatId = intent?.extras?.getString("thermostat")
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            Timber.d("onHandleWork: $thermostatId $title $body")
            if (thermostatId != null && title != null && body != null) {
                remindersLocalRepository.getThermostat(thermostatId) {
                    if (it is OperationResult.Success && it.data != null) {
                        sendNotification(
                            this@NotificationJobIntentService,
                            title,
                            body,
                            it.data.asDomainModel()
                        )
                    } else {
                        Timber.e("invalid result")
                    }
                }
            } else {
                Timber.e("null value $thermostatId $title $body")
            }
        }
    }
}