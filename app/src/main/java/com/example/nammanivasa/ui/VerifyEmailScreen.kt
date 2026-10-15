package com.example.nammanivasa.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammanivasa.ui.components.NnErrorState
import com.example.nammanivasa.ui.components.NnPrimaryButton
import com.example.nammanivasa.ui.components.NnSecondaryButton
import com.example.nammanivasa.ui.theme.InterFontFamily
import com.example.nammanivasa.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerified: () -> Unit,
    onBackToLogIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember(email) { VerifyEmailViewModel(initialEmail = email) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) onVerified()
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
                IconButton(onClick = onBackToLogIn) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            }

            Text(
                text = "Verify Email",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = InterFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We sent a verification link to:",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            NnPrimaryButton(
                text = "I Have Verified",
                onClick = { scope.launch { viewModel.checkVerificationStatus() } },
                loading = uiState.isChecking,
                enabled = !uiState.isResending
            )

            Spacer(modifier = Modifier.height(12.dp))

            NnSecondaryButton(
                text = "Resend Verification Email",
                onClick = { scope.launch { viewModel.resendVerificationEmail() } },
                enabled = !uiState.isChecking
            )

            if (!uiState.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                NnErrorState(
                    message = uiState.message.orEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            NnSecondaryButton(
                text = "Back to Log In",
                onClick = onBackToLogIn
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
