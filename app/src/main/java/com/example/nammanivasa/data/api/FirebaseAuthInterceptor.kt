package com.example.nammanivasa.data.api

import android.util.Log
import com.example.nammanivasa.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class FirebaseAuthInterceptor : Interceptor {

    private val TAG = "FirebaseAuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for health/ready endpoints
        val path = originalRequest.url.encodedPath
        if (path == "/health" || path == "/ready") {
            return chain.proceed(originalRequest)
        }

        // Get current Firebase user and their ID token
        val currentUser = FirebaseAuth.getInstance().currentUser
        val idToken = try {
            if (currentUser != null) {
                // Use blocking await - this runs on OkHttp's background thread pool
                Tasks.await(currentUser.getIdToken(false)).token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Firebase ID token", e)
            null
        }

        // Add Authorization header if we have a token
        val authorizedRequest = if (idToken != null && idToken.isNotBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $idToken")
                .build()
        } else if (BuildConfig.DEBUG) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer test-token")
                .header("X-Test-User", "11111111-1111-4111-8111-111111111111")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authorizedRequest)
    }
}