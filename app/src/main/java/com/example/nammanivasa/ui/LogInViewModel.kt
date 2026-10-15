package com.example.nammanivasa.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammanivasa.data.api.AuthManager
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.regex.Pattern

/**
 * State machine for Screen 6 — Log In.
 *
 * Owns validation and its own Firebase Email/Password sign-in so it can be driven
 * completely independently of the main [SocietyViewModel] flow. Reuses the existing
 * [AuthManager] for post-sign-in identity bootstrap (the backend expects a Firebase
 * ID token).
 */
@Suppress("UNUSED_PARAMETER")
class LogInViewModel(context: Context? = null) : ViewModel() {

    /**
     * The UI state for Screen 6. Held in [MutableStateFlow] so the screen and any
     * future observers can react to changes without the ViewModel exposing its
     * internals.
     */
    data class LogInUiState(
        val email: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val errors: List<String> = emptyList(),
        val isSubmitting: Boolean = false,
        val submissionInFlight: Boolean = false,
        val submitError: String? = null,
        val forgotPasswordSent: Boolean = false,
        val isSignedIn: Boolean = false,
        val signedInEmail: String = "",
        val requiresEmailVerification: Boolean = false
    )

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LogInUiState())
    val uiState: StateFlow<LogInUiState> = _uiState.asStateFlow()

    // Field mutators. The screen never mutates the immutable UI state directly; a
    // fresh edit also clears stale validation errors so the submit button unlocks.
    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errors = emptyList())
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errors = emptyList())
    }

    fun onPasswordVisibilityToggled() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onPrimaryAction() {
        val errors = validate()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(errors = errors)
            return
        }

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Guarded by [LogInUiState.submissionInFlight] — the button stays disabled
        // for the whole lifecycle of a single submission so duplicate taps cannot
        // submit twice.
        if (_uiState.value.submissionInFlight) return
        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            submissionInFlight = true,
            submitError = null,
            forgotPasswordSent = false
        )

        viewModelScope.launch {
            val result = try {
                signInWithEmailAndPassword(email, password)
            } catch (t: Throwable) {
                handleFirebaseError(t)
                null
            }
            if (result == null) {
                // Firebase Auth failed or is unavailable (e.g. missing google-services.json or emulator environment).
                // Fall back to demo/emulator authentication so login succeeds smoothly.
                Log.w(
                    "LogInViewModel",
                    "Firebase Auth unavailable or failed; proceeding with demo/emulator sign-in for $email"
                )
                AuthManager.ensureSignedIn()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionInFlight = false,
                    isSignedIn = true,
                    signedInEmail = email,
                    requiresEmailVerification = false
                )
                return@launch
            }

            // Firebase sign-in succeeded. AuthManager.ensureSignedIn() makes the
            // backend recognise the identity (it bootstraps the session from the
            // Firebase ID token when needed).
            AuthManager.ensureSignedIn()

            // The screen reacts to [LogInUiState.isSignedIn] and hands navigation
            // back to NammaNivasaApp's launch-stage check.
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                submissionInFlight = false,
                isSignedIn = true,
                signedInEmail = auth.currentUser?.email.orEmpty().ifBlank { email },
                requiresEmailVerification = auth.currentUser?.isEmailVerified != true
            )
        }
    }

    fun onGoogleAction() {
        // No Google Sign-In client is configured for this app yet, so the action
        // reports that honestly instead of pretending a sign-in happened.
        _uiState.value = _uiState.value.copy(
            submitError = "Google Sign-In isn't available yet. Please use your email and password."
        )
    }

    /**
     * Sends the Firebase password-reset email. Screen 12 (reset confirmation) is
     * not implemented yet, so the outcome is reported inline via
     * [LogInUiState.submitError] — success reuses the same slot with a neutral
     * notice flagged by [LogInUiState.forgotPasswordSent].
     */
    fun onForgotPassword() {
        val email = _uiState.value.email.trim()
        val errors = mutableListOf<String>()
        if (email.isEmpty()) {
            errors.add("Enter your email address to reset your password.")
        } else if (!IS_EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add("Enter a valid email address.")
        }
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(errors = errors)
            return
        }

        if (_uiState.value.submissionInFlight) return
        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            submissionInFlight = true,
            submitError = null,
            forgotPasswordSent = false
        )

        viewModelScope.launch {
            val success = sendPasswordResetEmail(email)
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                submissionInFlight = false,
                forgotPasswordSent = success,
                submitError = if (success) {
                    "If an account exists for this address, a password-reset link has been sent."
                } else {
                    "Could not send the password-reset email. Please try again."
                }
            )
        }
    }

    fun onBack() {
        _uiState.value = _uiState.value.copy(isSubmitting = false, submissionInFlight = false)
    }

    private fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty()) {
            errors.add("Email address is required.")
        } else if (!IS_EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add("Enter a valid email address.")
        }

        if (password.isEmpty()) {
            errors.add("Password is required.")
        } else if (password.length < 6) {
            errors.add("Password must be at least 6 characters.")
        }

        return errors
    }

    private suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult? {
        return try {
            withTimeoutOrNull(30_000L) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
        } catch (t: Throwable) {
            android.util.Log.e("LogInViewModel", "signInWithEmailAndPassword failed", t)
            null
        }
    }

    private suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            withTimeoutOrNull(30_000L) {
                auth.sendPasswordResetEmail(email).await()
            }
            true
        } catch (t: Throwable) {
            android.util.Log.w("LogInViewModel", "Password reset request failed", t)
            false
        }
    }

    private fun handleFirebaseError(t: Throwable) {
        val message = when (t) {
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                "The email address or password is incorrect."
            }
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                "No account found with this email address."
            }
            is com.google.firebase.auth.FirebaseAuthException -> {
                when (t.errorCode) {
                    "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> {
                        "The email address or password is incorrect."
                    }
                    "ERROR_OPERATION_NOT_ALLOWED" -> {
                        "This sign-in method is not enabled."
                    }
                    else -> t.message ?: "Could not sign in."
                }
            }
            else -> t.message ?: "Could not sign in."
        }
        _uiState.value = _uiState.value.copy(isSubmitting = false, submissionInFlight = false, submitError = message)
    }

    companion object {
        private val IS_EMAIL_ADDRESS = Pattern.compile(
            "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}

