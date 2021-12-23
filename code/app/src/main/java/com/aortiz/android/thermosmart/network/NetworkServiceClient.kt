package com.aortiz.android.thermosmart.network

import com.aortiz.android.thermosmart.network.ApiOpenWeatherService.Companion.BASE_URL
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiOpenWeatherService {

    companion object {
        const val WEATHER = "/data/2.5/weather"
        const val IMAGE = "/img/w/{icon}.png"
        const val BASE_URL = "https://api.openweathermap.org"
    }

    @GET(WEATHER)
    fun getWheater(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Deferred<OpenWeatherResponseData>

}

object Network {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val openWeather: ApiOpenWeatherService = retrofit.create(ApiOpenWeatherService::class.java)

    fun getImageUrl(icon: String): String {
        return "$BASE_URL/img/w/$icon.png"
    }
}