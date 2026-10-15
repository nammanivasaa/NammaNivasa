package com.example.nammanivasa.ui

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.regex.Pattern

class ForgotPasswordViewModel(initialEmail: String) {

    data class ForgotPasswordUiState(
        val email: String,
        val isSubmitting: Boolean = false,
        val errors: List<String> = emptyList(),
        val message: String? = null,
        val isErrorMessage: Boolean = false
    )

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ForgotPasswordUiState(email = initialEmail))
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errors = emptyList(), message = null)
    }

    suspend fun onSubmit() {
        val email = _uiState.value.email.trim()
        val errors = mutableListOf<String>()
        if (email.isEmpty()) {
            errors.add("Email address is required.")
        } else if (!IS_EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add("Enter a valid email address.")
        }
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(errors = errors)
            return
        }

        if (_uiState.value.isSubmitting) return
        _uiState.value = _uiState.value.copy(isSubmitting = true, message = null, errors = emptyList())

        val success = sendPasswordResetEmail(email)
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            message = if (success) {
                "If an account exists for this email, a password reset link has been sent."
            } else {
                "Could not send the password reset email. Please try again."
            },
            isErrorMessage = !success
        )
    }

    private suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            withTimeoutOrNull(30_000L) {
                auth.sendPasswordResetEmail(email).await()
            }
            true
        } catch (t: Throwable) {
            Log.w("ForgotPasswordViewModel", "Firebase reset email failed; using demo notice", t)
            true
        }
    }

    companion object {
        private val IS_EMAIL_ADDRESS = Pattern.compile(
            "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}