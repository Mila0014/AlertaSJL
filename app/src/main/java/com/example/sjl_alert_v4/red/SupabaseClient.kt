package com.example.sjl_alert_v4.red

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    // ─────────────────────────────────────────────────────────────────────
    // Configura tu URL de Supabase y tu Anon Key
    // Puedes encontrarlos en: Settings -> API de tu dashboard de Supabase
    // ─────────────────────────────────────────────────────────────────────
    private const val SUPABASE_URL = "https://mywcznncxuplsbzifjab.supabase.co/"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im15d2N6bm5jeHVwbHNiemlmamFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk5MzgwMDUsImV4cCI6MjA5NTUxNDAwNX0.u-AHPM0FWkPoCTM2dXn9JLOxVpRKbFHxjT4Wg_foeS8"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
    }

    val api: SupabaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApiService::class.java)
    }
}
