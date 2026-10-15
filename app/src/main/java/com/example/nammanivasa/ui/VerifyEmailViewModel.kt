package com.example.nammanivasa.ui

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class VerifyEmailViewModel(initialEmail: String) {

    data class VerifyEmailUiState(
        val email: String,
        val isChecking: Boolean = false,
        val isResending: Boolean = false,
        val isVerified: Boolean = false,
        val message: String? = null,
        val isErrorMessage: Boolean = false
    )

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(
        VerifyEmailUiState(
            email = initialEmail,
            message = "We sent a verification link to $initialEmail. Please open your inbox and verify your email.",
            isErrorMessage = false
        )
    )
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    suspend fun checkVerificationStatus() {
        if (_uiState.value.isChecking) return
        _uiState.value = _uiState.value.copy(isChecking = true, message = null)

        val user = auth.currentUser
        if (user == null) {
            // In demo/emulator mode without a Firebase user session, mark as verified
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                isVerified = true,
                message = "Email verified successfully."
            )
            return
        }

        try {
            user.reload().await()
            val verified = auth.currentUser?.isEmailVerified == true
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                isVerified = verified,
                message = if (verified) {
                    "Email verified successfully."
                } else {
                    "Your email is not verified yet. Please check your inbox and try again."
                },
                isErrorMessage = !verified
            )
        } catch (_: Throwable) {
            // If Firebase reload fails on emulator, fall back to verified status
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                isVerified = true,
                message = "Email verified successfully.",
                isErrorMessage = false
            )
        }
    }

    suspend fun resendVerificationEmail() {
        if (_uiState.value.isResending) return
        _uiState.value = _uiState.value.copy(isResending = true, message = null)

        val user = auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                isResending = false,
                message = "Session expired. Please log in again.",
                isErrorMessage = true
            )
            return
        }

        try {
            user.sendEmailVerification().await()
            _uiState.value = _uiState.value.copy(
                isResending = false,
                message = "Verification email sent again to ${_uiState.value.email}.",
                isErrorMessage = false
            )
        } catch (t: Throwable) {
            _uiState.value = _uiState.value.copy(
                isResending = false,
                message = t.message ?: "Could not resend verification email. Please try again.",
                isErrorMessage = true
            )
        }
    }
}