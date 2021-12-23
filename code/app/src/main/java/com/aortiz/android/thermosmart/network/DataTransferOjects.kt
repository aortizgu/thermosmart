package com.aortiz.android.thermosmart.network

data class WeatherData(
    val icon: String
)

data class MainData(
    val temp: Float
)

data class OpenWeatherResponseData(
    val name: String,
    val weather: List<WeatherData>,
    val main: MainData
)
