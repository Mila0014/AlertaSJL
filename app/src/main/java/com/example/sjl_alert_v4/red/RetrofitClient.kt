package com.example.sjl_alert_v4.red

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ─────────────────────────────────────────────────────────────────────
    // PASO 2: Reemplaza esta URL con la URL de tu Azure App Service
    // Formato: https://TU-APP.azurewebsites.net/
    // ─────────────────────────────────────────────────────────────────────
    private const val BASE_URL = "https://alertasjl-avfpaae5befgh4be.canadacentral-01.azurewebsites.net/"

    val incidenciaApi: IncidenciaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IncidenciaApiService::class.java)
    }
}