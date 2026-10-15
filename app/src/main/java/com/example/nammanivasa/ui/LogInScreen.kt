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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
 * Screen 6 — Log In.
 *
 * Email Address, Password with visibility toggle, "Forgot Password?" action,
 * Log In button, Continue with Google, "Create Account" link, and back
 * navigation — mirroring the Screen 5 (Create Account) design with Inter typography.
 */
@Composable
fun LogInScreen(
    viewModel: LogInViewModel,
    onSignedIn: (String) -> Unit,
    onRequireEmailVerification: (String) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    var navigationHandled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isSignedIn, uiState.requiresEmailVerification, uiState.signedInEmail, navigationHandled) {
        if (!uiState.isSignedIn) return@LaunchedEffect
        if (navigationHandled) return@LaunchedEffect
        navigationHandled = true
        if (uiState.requiresEmailVerification) {
            onRequireEmailVerification(uiState.signedInEmail.ifBlank { uiState.email.trim() })
        } else {
            onSignedIn(uiState.signedInEmail.ifBlank { uiState.email.trim() })
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
                        viewModel.onBack()
                        onBack()
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
                text = "Log In",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = InterFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Welcome back! Log in to continue with Namma Nivasa.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable content: form + actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Email Address
                NnTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email Address",
                    placeholder = "e.g., jane@example.com",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                    singleLine = true,
                    isError = uiState.errors.any { it.contains("email", ignoreCase = true) }
                )

                // Password
                NnTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    placeholder = "Your password",
                    leadingIcon = Icons.Filled.Lock,
                    trailingIcon = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    onTrailingIconClick = viewModel::onPasswordVisibilityToggled,
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.errors.any { it.contains("Password", ignoreCase = true) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            onForgotPassword(uiState.email.trim())
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary action: Log In
                NnPrimaryButton(
                    text = "Log In",
                    onClick = viewModel::onPrimaryAction,
                    enabled = uiState.errors.isEmpty(),
                    loading = uiState.isSubmitting
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary action: Continue with Google
                NnSecondaryButton(
                    text = "Continue with Google",
                    onClick = viewModel::onGoogleAction,
                    enabled = !uiState.isSubmitting,
                    iconPainter = painterResource(id = R.drawable.ic_google_logo)
                )

                // Inline status
                NnFormErrorSummary(errors = uiState.errors, submitError = uiState.submitError)

                // "New to Namma Nivasa? Create Account" link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New to Namma Nivasa?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onCreateAccount)
                    )
                }
            }
        }
    }
}
