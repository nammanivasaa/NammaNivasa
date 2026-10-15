package com.example.nammanivasa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammanivasa.ui.theme.InterFontFamily
import com.example.nammanivasa.ui.theme.Spacing

/**
 * Primary call-to-action button — the burgundy pill used across the approved
 * onboarding and authentication screens (Get Started, Create Account, Log In,
 * Forgot Password, Verify Email).
 *
 * Consolidates what was previously copy-pasted into each auth screen: a 56.dp
 * button with 16.dp corner radius, bold 17.sp Inter label and an inline loading
 * spinner. Provide [loading] to swap the label area for a spinner while keeping
 * the button height stable and disabling interaction.
 */
@Composable
fun NnPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight),
        enabled = enabled && !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

/**
 * Secondary (outlined) action button — the burgundy-outlined button paired with
 * [NnPrimaryButton] on the authentication screens (Continue with Google,
 * Resend Verification Email, Back).
 *
 * Supports an optional leading [icon] (e.g. the Google account icon) and a
 * [loading] spinner, matching the primary button's affordances.
 */
@Composable
fun NnSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconPainter: Painter? = null
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(Spacing.buttonHeight),
        enabled = enabled && !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else if (icon != null) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Consolidated field-validation / submission message summary.
 *
 * Every authentication screen rendered the same thing: a list of validation
 * errors plus an optional submission-level error, joined into a bulleted block
 * via [NnErrorState]. This keeps that presentation in one place so the screens
 * no longer each rebuild it.
 */
@Composable
fun NnFormErrorSummary(
    errors: List<String>,
    submitError: String?,
    modifier: Modifier = Modifier
) {
    val messages = buildList {
        addAll(errors)
        submitError?.let { add(it) }
    }
    if (messages.isNotEmpty()) {
        NnErrorState(
            message = messages.joinToString("\n") { "• $it" },
            modifier = modifier.fillMaxWidth()
        )
    }
}
