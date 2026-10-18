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

        val realUid = currentUser?.uid?.takeIf { it.isNotBlank() } ?: "11111111-1111-4111-8111-111111111111"
        val realEmail = currentUser?.email.orEmpty().ifBlank { "suda.raghu@gmail.com" }
        val realName = currentUser?.displayName.orEmpty().ifBlank {
            realEmail.split("@").firstOrNull()?.replace(".", " ")?.replaceFirstChar { it.titlecase() } ?: "Raghu Suda"
        }

        val authorizedRequest = if (BuildConfig.DEBUG) {
            originalRequest.newBuilder()
                .header("Authorization", if (idToken != null && idToken.isNotBlank()) "Bearer $idToken" else "Bearer test-token")
                .header("X-Test-User", realUid)
                .header("X-User-Email", realEmail)
                .header("X-User-Name", realName)
                .build()
        } else if (idToken != null && idToken.isNotBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $idToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authorizedRequest)
    }
}
