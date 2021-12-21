package com.aortiz.android.thermosmart

import android.app.Application
import com.aortiz.android.thermosmart.authentication.AuthenticationViewModel
import com.aortiz.android.thermosmart.config.AppConfigViewModel
import com.aortiz.android.thermosmart.database.local.SharedPreferencesDatabase
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.thermostat.config.ThermostatConfigViewModel
import com.aortiz.android.thermosmart.thermostat.detail.ThermostatDetailViewModel
import com.aortiz.android.thermosmart.thermostat.list.ThermostatListViewModel
import com.aortiz.android.thermosmart.thermostat.save.ThermostatSaveViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class MyApp : Application() {

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
            viewModel {
                ThermostatSaveViewModel(
                    get()
                )
            }
            viewModel {
                ThermostatDetailViewModel(
                    get()
                )
            }
            viewModel {
                ThermostatConfigViewModel(
                    get()
                )
            }

            single { SharedPreferencesDatabase(get()) }
            single { ThermostatRepository() }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}