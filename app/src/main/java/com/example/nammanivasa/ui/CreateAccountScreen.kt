package com.example.nammanivasa.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.nammanivasa.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammanivasa.ui.components.NnFormErrorSummary
import com.example.nammanivasa.ui.components.NnPrimaryButton
import com.example.nammanivasa.ui.components.NnSecondaryButton
import com.example.nammanivasa.ui.components.NnTextField
import com.example.nammanivasa.ui.theme.InterFontFamily
import com.example.nammanivasa.ui.theme.Primary

/**
 * Screen 5 — Create Account.
 *
 * Full Name, Email Address, Password, Confirm Password, password visibility
 * toggle, Terms of Service + Privacy Policy agreement, Create Account button,
 * Continue with Google, "Already have an account?" link, and back navigation.
 */
@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onVerifyEmail: (String) -> Unit,
    onLogIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    LaunchedEffect(uiState.navigationTarget) {
        val target = uiState.navigationTarget
        if (target is CreateAccountViewModel.VerifyEmailTarget.Email) {
            onVerifyEmail(target.email)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Burgundy status-bar strip keeps the brand color behind the system status bar.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            // Back navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        (view.context as? ComponentActivity)?.finish()
                        onLogIn()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            }

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = InterFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Set up your account to get started with Namma Nivasa.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                CreateAccountForm(
                    fullName = uiState.fullName,
                    email = uiState.email,
                    password = uiState.password,
                    confirmPassword = uiState.confirmPassword,
                    termsAccepted = uiState.termsAccepted,
                    isPasswordVisible = uiState.isPasswordVisible,
                    isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
                    errors = uiState.errors,
                    isSubmitting = uiState.isSubmitting,
                    submitError = uiState.submitError,
                    onFullNameChange = viewModel::onFullNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTermsChanged = viewModel::onTermsChanged,
                    onPasswordVisibilityToggled = viewModel::onPasswordVisibilityToggled,
                    onConfirmPasswordVisibilityToggled = viewModel::onConfirmPasswordVisibilityToggled,
                    onPrimaryAction = viewModel::onPrimaryAction,
                    onGoogleAction = viewModel::onGoogleAction
                )

                // "Already have an account?" link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onLogIn)
                    )
                }
            }
        }
    }
}

/**
 * The form layout for Screen 5: name, email, password, confirm password,
 * visibility toggles, terms agreement, and both action buttons.
 */
@Composable
private fun CreateAccountForm(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    errors: List<String>,
    isSubmitting: Boolean,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsChanged: (Boolean) -> Unit,
    onPasswordVisibilityToggled: () -> Unit,
    onConfirmPasswordVisibilityToggled: () -> Unit,
    submitError: String?,
    onPrimaryAction: () -> Unit,
    onGoogleAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Full Name
        NnTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Full Name",
            placeholder = "e.g., Jane Doe",
            leadingIcon = Icons.Filled.Person,
            singleLine = true,
            isError = errors.any { it.contains("Full Name", ignoreCase = true) }
        )

        // Email Address
        NnTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email Address",
            placeholder = "e.g., jane@example.com",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            singleLine = true,
            isError = errors.any { it.contains("email", ignoreCase = true) }
        )

        // Password
        NnTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "At least 6 characters",
            leadingIcon = Icons.Filled.Lock,
            trailingIcon = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            onTrailingIconClick = onPasswordVisibilityToggled,
            singleLine = true,
            isError = errors.any { it.contains("Password", ignoreCase = true) }
        )

        // Confirm Password
        NnTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            placeholder = "Re-enter your password",
            leadingIcon = Icons.Filled.Lock,
            trailingIcon = if (isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            onTrailingIconClick = onConfirmPasswordVisibilityToggled,
            singleLine = true,
            isError = errors.any { it.contains("Confirm password", ignoreCase = true) }
        )

        // Terms of Service + Privacy Policy
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = onTermsChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = Primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "By creating an account you are agreeing to the terms.",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Primary action: Create Account
        NnPrimaryButton(
            text = "Create Account",
            onClick = onPrimaryAction,
            enabled = errors.isEmpty(),
            loading = isSubmitting
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Secondary action: Continue with Google
        NnSecondaryButton(
            text = "Continue with Google",
            onClick = onGoogleAction,
            enabled = !isSubmitting,
            iconPainter = painterResource(id = R.drawable.ic_google_logo)
        )

        // Error summary
        NnFormErrorSummary(errors = errors, submitError = submitError)
    }
}
