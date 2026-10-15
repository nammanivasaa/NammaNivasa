package com.example.nammanivasa.data.api

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Ensures the app has a Firebase identity before the first API call goes out.
 *
 * The backend verifies Firebase ID tokens (see [FirebaseAuthInterceptor]), so an
 * anonymous sign-in gives the Retrofit client a bearer token to attach. Sign-in is
 * strictly best-effort: when Firebase is not configured, Play services is missing,
 * or the device is offline, the app continues without a session and the repository
 * layer falls back to local demo data. Nothing here may ever crash the app.
 */
object AuthManager {

    private const val TAG = "AuthManager"
    private const val SIGN_IN_TIMEOUT_MS = 10_000L

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    suspend fun ensureSignedIn() {
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) return

            val result = withTimeoutOrNull(SIGN_IN_TIMEOUT_MS) {
                auth.signInAnonymously().await()
            }
            if (result == null) {
                Log.w(TAG, "Anonymous sign-in timed out; continuing without a session")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Anonymous sign-in failed; continuing without a session", t)
        }
    }
}
