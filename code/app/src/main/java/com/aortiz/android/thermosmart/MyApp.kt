package com.aortiz.android.thermosmart

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.aortiz.android.thermosmart.authentication.AuthenticationViewModel
import com.aortiz.android.thermosmart.config.AppConfigViewModel
import com.aortiz.android.thermosmart.database.local.SharedPreferencesDatabase
import com.aortiz.android.thermosmart.database.realtime.RTDatabase
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.thermostat.detail.ThermostatDetailViewModel
import com.aortiz.android.thermosmart.thermostat.list.ThermostatListViewModel
import com.aortiz.android.thermosmart.thermostat.selectlocation.ThermostatSelectLocationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class MyApp : Application() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val myModule = module {
            viewModel {
                AuthenticationViewModel(
                    get()
                )
            }
            viewModel {
                AppConfigViewModel(
                    get(),
                    get()
                )
            }
            viewModel {
                ThermostatListViewModel(
                    get(),
                    get()
                )
            }
            viewModel { parameters ->
                ThermostatDetailViewModel(
                    get(),
                    get(),
                    parameters.get()
                )
            }
            viewModel {
                ThermostatSelectLocationViewModel(
                    get(),
                    get()
                )
            }

            single { SharedPreferencesDatabase(get()) }
            single { ThermostatRepository(get(), get(), BuildConfig.OPEN_WEATHER_API_KEY) }
            single { RTDatabase(get()) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}