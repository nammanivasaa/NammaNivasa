package com.example.nammanivasa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammanivasa.ui.components.NnFormErrorSummary
import com.example.nammanivasa.ui.components.NnPrimaryButton
import com.example.nammanivasa.ui.components.NnTextField
import com.example.nammanivasa.ui.theme.InterFontFamily
import com.example.nammanivasa.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    initialEmail: String,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(initialEmail) { ForgotPasswordViewModel(initialEmail = initialEmail) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            }

            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = InterFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enter your account email and we’ll send you a reset link.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            NnPrimaryButton(
                text = "Send Reset Link",
                onClick = { scope.launch { viewModel.onSubmit() } },
                loading = uiState.isSubmitting
            )

            NnFormErrorSummary(errors = uiState.errors, submitError = uiState.message)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember your password?",
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
                    modifier = Modifier.clickable(onClick = onBackToLogin)
                )
            }
        }
    }
}
