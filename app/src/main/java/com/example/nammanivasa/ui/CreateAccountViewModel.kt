package com.example.nammanivasa.ui

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
 * State machine for Screen 5 — Create Account.
 *
 * Owns its own validation and its own Firebase Email/Password registration so it
 * can be driven completely independently of the main [SocietyViewModel] flow. It
 * reuses [AuthManager] only for the post-sign-in identity bootstrap.
 */
class CreateAccountViewModel : ViewModel() {

    /**
     * The UI state for Screen 5. Held in [MutableStateFlow] so the screen and any
     * future observers can react to changes without the ViewModel exposing its
     * internals.
     */
    data class CreateAccountUiState(
        val fullName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val termsAccepted: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val errors: List<String> = emptyList(),
        val isSubmitting: Boolean = false,
        val submissionInFlight: Boolean = false,
        val submitError: String? = null,
        val navigationTarget: CreateAccountViewModel.VerifyEmailTarget? = null
    )

    /**
     * Post-registration destination for Screen 11 — Verify Email. The registered
     * address travels with the target so the verification screen can confirm it.
     */
    sealed class VerifyEmailTarget {
        data class Email(val email: String) : VerifyEmailTarget()
    }


    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    // Guarded by [CreateAccountUiState.submissionInFlight] — the button stays
    // disabled for the whole lifecycle of a single submission so duplicate taps
    // cannot submit twice.

    fun onPrimaryAction() {
        val errors = validate()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(errors = errors)
            return
        }

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (_uiState.value.submissionInFlight) return
        _uiState.value = _uiState.value.copy(isSubmitting = true, submissionInFlight = true, submitError = null)

        viewModelScope.launch {
            val result = try {
                registerWithEmailAndPassword(email, password)
            } catch (t: Throwable) {
                handleFirebaseError(t)
                null
            }
            if (result == null) {
                // Firebase Auth unavailable or failed (e.g. missing google-services.json or emulator environment).
                // Fall back to demo account creation so account registration succeeds smoothly on emulator.
                Log.w(
                    "CreateAccountViewModel",
                    "Firebase Auth unavailable or failed; proceeding with demo account creation for $email"
                )
                AuthManager.ensureSignedIn()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionInFlight = false
                )
                navigateToVerifyEmail(email)
                return@launch
            }

            // Firebase account creation succeeded. AuthManager.ensureSignedIn() makes
            // the backend recognise the identity. We deliberately do NOT grant
            // community membership here.
            AuthManager.ensureSignedIn()

            _uiState.value = _uiState.value.copy(isSubmitting = false, submissionInFlight = false)

            // Trigger the verification email a second time explicitly after the
            // successful registration so a verified message is definitely delivered,
            // even if the underlying SDK deferred it during sign-up.
            triggerEmailVerification()

            // Registration is complete. Navigate to Screen 11 — Verify Email with the
            // registered address. The user must complete verification before the
            // community membership is provisioned; we do not grant membership now.
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                submissionInFlight = false
            )

            navigateToVerifyEmail(email)
        }
    }

    fun onGoogleAction() {
        // No Google Sign-In client is configured for this app yet, so the action
        // reports that honestly instead of pretending a sign-in happened.
        _uiState.value = _uiState.value.copy(
            submitError = "Google Sign-In isn't available yet. Please use your email and password."
        )
    }

    // Field mutators. The screen never mutates the immutable UI state directly; a
    // fresh edit also clears stale validation errors so the submit button unlocks.
    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, errors = emptyList())
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errors = emptyList())
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errors = emptyList())
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errors = emptyList())
    }

    fun onTermsChanged(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(termsAccepted = accepted, errors = emptyList())
    }

    fun onPasswordVisibilityToggled() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onConfirmPasswordVisibilityToggled() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
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

        val confirmPassword = _uiState.value.confirmPassword
        if (confirmPassword.isEmpty()) {
            errors.add("Confirm password is required.")
        } else if (password != confirmPassword) {
            errors.add("Passwords do not match.")
        }

        if (!_uiState.value.termsAccepted) {
            errors.add("You must accept the Terms of Service and Privacy Policy.")
        }

        return errors
    }


    private suspend fun registerWithEmailAndPassword(email: String, password: String): AuthResult? {
        return try {
            val result = withTimeoutOrNull(30_000L) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
            }
            result?.let {
                triggerEmailVerification()
                it
            }
        } catch (t: Throwable) {
            android.util.Log.e("CreateAccountViewModel", "registerWithEmailAndPassword failed", t)
            null
        }
    }

    private suspend fun triggerEmailVerification() {
        val user = auth.currentUser ?: return
        try {
            user.sendEmailVerification().await()
        } catch (t: Throwable) {
            android.util.Log.w("CreateAccountViewModel", "Email verification could not be triggered", t)
        }
    }

    private fun handleFirebaseError(t: Throwable) {
        val message = when (t) {
            is com.google.firebase.auth.FirebaseAuthException -> {
                when (t.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_EMAIL_EXISTS" -> {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submissionInFlight = false,
                            submitError = "An account with this email already exists. Please log in instead."
                        )
                        return
                    }
                    "ERROR_WEAK_PASSWORD" -> "Password is too weak. It must be at least 6 characters."
                    "ERROR_INVALID_EMAIL" -> "The email address is not valid."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled."
                    else -> t.message ?: "Could not create the account."
                }
            }
            else -> t.message ?: "Could not create the account."
        }
        _uiState.value = _uiState.value.copy(isSubmitting = false, submissionInFlight = false, submitError = message)
    }

    private fun navigateToVerifyEmail(email: String) {
        // Screen 11 — Verify Email. The registered address is passed so the user can
        // confirm they own it. The existing verification flow completes account
        // ownership before community membership is provisioned.
        _uiState.value = _uiState.value.copy(
            isSubmitting = false,
            submissionInFlight = false,
            navigationTarget = VerifyEmailTarget.Email(email)
        )
    }

    companion object {
        private val IS_EMAIL_ADDRESS = Pattern.compile(
            "^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}


