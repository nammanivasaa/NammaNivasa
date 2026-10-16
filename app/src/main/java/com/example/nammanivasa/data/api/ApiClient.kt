package com.example.nammanivasa.data.api

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.nammanivasa.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    private val baseUrl: String
        get() {
            val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu")
            return if (isEmulator) {
                "http://10.0.2.2:4000/"
            } else {
                "http://192.168.1.179:4000/"
            }
        }

    @Volatile
    private var INSTANCE: ApiService? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        val redacted = message
            .replace("Authorization: Bearer ", "Authorization: Bearer ***")
            .replace("\"password\":\"", "\"password\":\"***\"")
            .replace("\"privateKey\":\"", "\"privateKey\":\"***\"")
            .replace("\"evidenceRef\":\"", "\"evidenceRef\":\"***\"")
        Log.d("OkHttp", redacted)
    }.apply {
        // Detailed request/response bodies are for local debugging only. Release
        // builds must never log bodies: they can carry PII, bill amounts and other
        // sensitive payloads that the redaction below does not cover.
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(FirebaseAuthInterceptor())
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Suppress("UNUSED_PARAMETER")
    fun getApiService(context: Context): ApiService {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: retrofit.create(ApiService::class.java).also { INSTANCE = it }
        }
    }
}