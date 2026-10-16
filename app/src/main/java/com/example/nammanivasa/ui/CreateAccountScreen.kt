package com.example.nammanivasa.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.nammanivasa.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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
 * First Name, Last Name, Email Address, Country Code + Mobile Number, Password, Confirm Password,
 * password visibility toggle, Terms of Service + Privacy Policy agreement, Create Account button,
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
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    countryCode = uiState.countryCode,
                    mobileNumber = uiState.mobileNumber,
                    email = uiState.email,
                    password = uiState.password,
                    confirmPassword = uiState.confirmPassword,
                    termsAccepted = uiState.termsAccepted,
                    isPasswordVisible = uiState.isPasswordVisible,
                    isConfirmPasswordVisible = uiState.isConfirmPasswordVisible,
                    errors = uiState.errors,
                    isSubmitting = uiState.isSubmitting,
                    submitError = uiState.submitError,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onCountryCodeChange = viewModel::onCountryCodeChange,
                    onMobileNumberChange = viewModel::onMobileNumberChange,
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
 * The form layout for Screen 5: first name, last name, email, mobile number, password, confirm password,
 * visibility toggles, terms agreement, and both action buttons.
 */
@Composable
private fun CreateAccountForm(
    firstName: String,
    lastName: String,
    countryCode: String,
    mobileNumber: String,
    email: String,
    password: String,
    confirmPassword: String,
    termsAccepted: Boolean,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    errors: List<String>,
    isSubmitting: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onCountryCodeChange: (String) -> Unit,
    onMobileNumberChange: (String) -> Unit,
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
        // First Name & Last Name (Side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                NnTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = "First Name",
                    placeholder = "",
                    leadingIcon = Icons.Filled.Person,
                    singleLine = true,
                    isError = errors.any { it.contains("First name", ignoreCase = true) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                NnTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = "Last Name",
                    placeholder = "",
                    singleLine = true,
                    isError = errors.any { it.contains("Last name", ignoreCase = true) }
                )
            }
        }

        // Email Address
        NnTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email Address",
            placeholder = "",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            singleLine = true,
            isError = errors.any { it.contains("email", ignoreCase = true) }
        )

        // Mobile Number with Country Code Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(140.dp)) {
                CountryCodeDropdown(
                    selectedCode = countryCode,
                    onCodeSelected = onCountryCodeChange
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                NnTextField(
                    value = mobileNumber,
                    onValueChange = onMobileNumberChange,
                    label = "Mobile Number",
                    placeholder = "",
                    leadingIcon = Icons.Filled.Phone,
                    keyboardType = KeyboardType.Phone,
                    singleLine = true,
                    isError = errors.any { it.contains("mobile number", ignoreCase = true) }
                )
            }
        }

        // Password
        NnTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "At least 8 characters",
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

@Composable
private fun CountryCodeDropdown(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sheetOpen by remember { mutableStateOf(false) }
    val options = listOf(
        "🇮🇳 +91",
        "🇺🇸 +1",
        "🇬🇧 +44",
        "🇦🇪 +971",
        "🇸🇬 +65",
        "🇦🇺 +61",
        "🇸🇦 +966",
        "➕ Other"
    )

    Column(modifier = modifier) {
        Text(
            text = "Code",
            style = MaterialTheme.typography.labelLarge,
            fontFamily = InterFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = { sheetOpen = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFF5F0EE),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCode.ifBlank { "🇮🇳 +91" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = InterFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Primary)
            }
        }

        if (sheetOpen) {
            CountryCodeBottomSheet(
                options = options,
                selectedOption = selectedCode,
                onSelected = {
                    onCodeSelected(it)
                    sheetOpen = false
                },
                onDismiss = { sheetOpen = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryCodeBottomSheet(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Select Country Code",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(Modifier.height(12.dp))
            options.forEach { option ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(option) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFF5F0EE) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = InterFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Primary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
