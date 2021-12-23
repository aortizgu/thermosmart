package com.aortiz.android.thermosmart.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.aortiz.android.thermosmart.database.DBThermostat
import com.aortiz.android.thermosmart.database.local.SharedPreferencesDatabase
import com.aortiz.android.thermosmart.database.realtime.FirebaseDatabaseLiveData
import com.aortiz.android.thermosmart.database.realtime.RTDatabase
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.network.Network
import com.aortiz.android.thermosmart.utils.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class ThermostatRepository(
    private val spdb: SharedPreferencesDatabase,
    private val rtdb: RTDatabase,
    val openweatherApiKey: String
) {

    private var _cityName = MutableLiveData<String?>()
    val cityName: LiveData<String?>
        get() = _cityName

    private var _exteriorTemp = MutableLiveData<Double?>()
    val exteriorTemp: LiveData<Double?>
        get() = _exteriorTemp

    private var _exteriorImage = MutableLiveData<String?>()
    val exteriorImage: LiveData<String?>
        get() = _exteriorImage

    private suspend fun loadWeather(lat: Double, lon: Double) {
        withContext(Dispatchers.Default) {
            val data = Network.openWeather.getWheater(lat, lon, openweatherApiKey).await()
            _cityName.postValue(data.name)
            _exteriorTemp.postValue(data.main.temp.toDouble())
            if (!data.weather.isNullOrEmpty()) {
                val imageUrl = Network.getImageUrl(data.weather.first().icon)
                _exteriorImage.postValue(imageUrl)
            }
        }
    }

    suspend fun loadWeatherData(lat: Double, lon: Double) {
        try {
            loadWeather(lat, lon)
        } catch (e: HttpException) {
            Timber.e("HttpException ${e.message()}")
        } catch (e: SocketException) {
            Timber.e("SocketException ${e.message}")
        } catch (e: SocketTimeoutException) {
            Timber.e("SocketTimeoutException ${e.message}")
        } catch (e: UnknownHostException) {
            Timber.e("UnknownHostException ${e.message}")
        }
    }

    fun getThermostatLiveData(thermostatId: String): FirebaseDatabaseLiveData<DBThermostat> {
        return rtdb.getThermostatLiveData(thermostatId)
    }

    fun getUserThermostatListLiveData(): LiveData<List<Thermostat>>{
        return rtdb.getUserThermostatListLiveData().map { list ->
            Timber.i("list size ${list.size}")
            list.map{ item ->
                Timber.i("item id ${item.id}")
                item.asDomainModel()
            }
        }
    }

    fun getThermostat(thermostatId: String, cb: (result: OperationResult<DBThermostat>) -> Unit) {
        return rtdb.getThermostat(thermostatId, cb)
    }

    fun setThermostatConfig(thermostat: Thermostat) {
        val dbObject = thermostat.asDBThermostat()
        rtdb.setThermostatConfig(dbObject.id, dbObject.configuration)
    }

    fun followThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        rtdb.followThermostat(id, cb)
    }

    fun unfollowThermostat(id: String, cb: (result: OperationResult<String>) -> Unit) {
        rtdb.unfollowThermostat(id, cb)
    }

    fun updateDeviceToken(token: String) {
        rtdb.updateDeviceToken(token)
    }

    fun load() {
        rtdb.load()
    }

    fun getShowInFahrenheitConfig(): Boolean {
        return spdb.sharedPreferencesDao.getShowInFahrenheitConfig()
    }

    fun setShowInFahrenheitConfig(value: Boolean) {
        spdb.sharedPreferencesDao.setShowInFahrenheitConfig(value)
    }

    fun unloadWeatherData() {
        _cityName.postValue(null)
        _exteriorTemp.postValue(null)
        _exteriorImage.postValue(null)
    }

}