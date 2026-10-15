package com.example.nammanivasa.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammanivasa.R
import com.example.nammanivasa.ui.theme.ShapeExtraSmall
import com.example.nammanivasa.ui.theme.ShapeFull
import com.example.nammanivasa.ui.theme.ShapeLarge
import android.content.Context
import kotlinx.coroutines.launch

@Composable
private fun OnboardingPageOne() {
    FinanceHeroIllustration()
    Spacer(Modifier.height(14.dp))
    FinanceHeadline()
}

@Composable
private fun FinanceHeroIllustration() {
    // Trimmed hero asset (onboarding_finance_hero_trimmed.png, 1024x1300) has the
    // genuinely-empty pure-ivory outer bands removed while preserving the full
    // artwork — building, phone, document, coins, shadows and the blush background
    // glow. Shown edge-to-edge at the content width; ContentScale.Fit preserves its
    // aspect ratio (no stretching/cropping) and its ivory background blends seamlessly
    // with the screen, so there is no visible card or border. Top-aligned by the
    // caller, so the illustration sits directly below the header with the heading
    // immediately underneath.
    Image(
        painter = painterResource(R.drawable.onboarding_finance_hero_trimmed),
        contentDescription = "Apartment community with a finances dashboard on a smartphone",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FinanceHeadline() {
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Elegant serif display for the first two lines; the closing line carries the
        // bold burgundy emphasis. Steps down on compact screens to stay balanced.
        val size = if (maxWidth < 320.dp) 28.sp else 32.sp
        val lineHeight = size * 1.2f
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = buildAnnotatedString {
                    append("Your community's\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("money, made clear.")
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = size,
                lineHeight = lineHeight,
                color = primary
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "Understand collections, expenses\nand financial records \u2014 all in one place.",
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = DmSans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OnboardingPageTwo() {
    BubblyHeroIllustration()
    // Match Screen 1's hero→headline gap (14.dp) so the TOP of Screen 2's headline aligns
    // with Screen 1's. Both heroes are the same 1024x1300 asset and both headlines are now
    // three lines, so the earlier enlarged gap no longer applies — it pushed the headline
    // ~34.dp lower than Screen 1 and crowded the description down toward the footer page
    // indicators. The shared verticalScroll container keeps any remaining overflow reachable
    // on short screens (no clipping).
    Spacer(Modifier.height(14.dp))
    BubblyHeadline()
}

@Composable
private fun BubblyHeroIllustration() {
    // Bubbly hero artwork shown edge-to-edge at the content width, mirroring Screen 1.
    // ContentScale.Fit preserves the asset's aspect ratio (no stretching/cropping) and
    // its ivory background blends seamlessly with the screen, so there is no visible
    // card, border, or background behind it.
    Image(
        painter = painterResource(R.drawable.onboarding_bubbly_hero),
        contentDescription = "Bubbly, the community AI assistant",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BubblyHeadline() {
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Match Screen 1's serif headline with a bold burgundy closing line; steps down
        // on compact screens to stay balanced.
        val size = if (maxWidth < 320.dp) 28.sp else 32.sp
        val lineHeight = size * 1.2f
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your community.",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = size,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Normal,
                color = primary
            )
            Text(
                text = "Just ask.",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = size,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "Bubbly helps residents with instant answers and everyday requests.",
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = DmSans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OnboardingPageThree() {
    CommunityHeroIllustration()
    Spacer(Modifier.height(14.dp))
    CommunityHeadline()
}

@Composable
private fun CommunityHeroIllustration() {
    // Community hero artwork shown edge-to-edge at the content width, mirroring Screens 1
    // and 2. ContentScale.Fit preserves the asset's aspect ratio (no stretching/cropping)
    // and its ivory background blends seamlessly with the screen, so there is no visible
    // card, border, or background behind it.
    Image(
        painter = painterResource(R.drawable.onboarding_community_hero),
        contentDescription = "Apartment community connected through shared services and updates",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CommunityHeadline() {
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Match Screen 1/2's serif headline with a bold burgundy closing line; steps down
        // on compact screens to stay balanced.
        val size = if (maxWidth < 320.dp) 28.sp else 32.sp
        val lineHeight = size * 1.2f
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your community.",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = size,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Normal,
                color = primary
            )
            Text(
                text = "Made simpler.",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = size,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "Dues, notices, visitors, and support in one seamless app.",
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = DmSans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(8.dp))
}

private val OnboardingMargin = 24.dp

/**
 * Screen 1 typography. Playfair Display (elegant serif) for the headline and
 * DM Sans for the description, bundled locally under res/font (SIL Open Font
 * License 1.1). Static TTFs are used so they are safe on API 24+.
 */
private val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

private val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal)
)

/** Number of introductory pages: financial trust, Bubbly, community living. */
private const val OnboardingPageCount = 3

/** Logo lock-up in the onboarding header. The asset is 1017x786 px (~1.294:1). */
private val OnboardingLogoWidth = 52.dp
private val OnboardingLogoHeight = 40.dp

/** Space reserved for the Skip action when sizing the header wordmark. */
private val SkipActionWidth = 56.dp

/** Spoken description of the page indicator, so progress is announced as one unit. */
private fun PageIndicatorLabel(page: Int): String = "Page ${page + 1} of $OnboardingPageCount"

/** Destinations reachable from Screen 4 (Welcome): Create Account (5) and Log In (6). */
private enum class AuthDestination {
    Welcome,
    CreateAccount,
    LogIn,
    VerifyEmail,
    ForgotPassword
}

/** Screen 4 — Welcome: auth-flow hub with entry points to Create Account (5) and Log In (6). */
@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit,
    onLogIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Burgundy status strip keeps the brand colour behind the system status bar.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
        )

        // Brand area: logo, headline and tagline, centred in the leftover space.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = OnboardingMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.namma_nivasa_logo_burgundy),
                contentDescription = "Namma Nivasa logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 140.dp, height = 108.dp)
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Welcome to Namma Nivasa",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = PlayfairDisplay,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your community and your assistant, together in one place. Create an account or log in to get started.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = DmSans,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Action footer: member auth entry points.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OnboardingMargin)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onCreateAccount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(ShapeFull),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Create Account", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLogIn,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(ShapeFull),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Log In", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "For registered society members only.",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DmSans,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Owns Screen 4's auth destination so its entry points are wired while the flow is built. */
@Composable
fun WelcomeAuthFlow(onSignedIn: (String) -> Unit, modifier: Modifier = Modifier) {
    var destinationIndex by rememberSaveable { mutableIntStateOf(AuthDestination.Welcome.ordinal) }
    var verifyEmailAddress by rememberSaveable { mutableStateOf("") }
    var forgotPasswordEmail by rememberSaveable { mutableStateOf("") }
    val destination = AuthDestination.values().getOrElse(destinationIndex) { AuthDestination.Welcome }
    when (destination) {
        AuthDestination.Welcome -> WelcomeScreen(
            onCreateAccount = { destinationIndex = AuthDestination.CreateAccount.ordinal },
            onLogIn = { destinationIndex = AuthDestination.LogIn.ordinal },
            modifier = modifier
        )
        AuthDestination.CreateAccount -> CreateAccountScreen(
            viewModel = CreateAccountViewModel(),
            onVerifyEmail = { email ->
                verifyEmailAddress = email
                destinationIndex = AuthDestination.VerifyEmail.ordinal
            },
            onLogIn = { destinationIndex = AuthDestination.LogIn.ordinal },
            modifier = modifier
        )
        AuthDestination.LogIn -> LogInScreen(
            viewModel = LogInViewModel(context = LocalContext.current.applicationContext),
            onSignedIn = onSignedIn,
            onRequireEmailVerification = { email ->
                verifyEmailAddress = email
                destinationIndex = AuthDestination.VerifyEmail.ordinal
            },
            onCreateAccount = { destinationIndex = AuthDestination.CreateAccount.ordinal },
            onForgotPassword = { email ->
                forgotPasswordEmail = email
                destinationIndex = AuthDestination.ForgotPassword.ordinal
            },
            onBack = { destinationIndex = AuthDestination.Welcome.ordinal },
            modifier = modifier
        )
        AuthDestination.VerifyEmail -> VerifyEmailScreen(
            email = verifyEmailAddress,
            onVerified = { onSignedIn(verifyEmailAddress) },
            onBackToLogIn = { destinationIndex = AuthDestination.LogIn.ordinal },
            modifier = modifier
        )
        AuthDestination.ForgotPassword -> ForgotPasswordScreen(
            initialEmail = forgotPasswordEmail,
            onBackToLogin = { destinationIndex = AuthDestination.LogIn.ordinal },
            modifier = modifier
        )
    }
}



@Composable
private fun OnboardingHeader(showSkip: Boolean, onSkip: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OnboardingMargin, vertical = 12.dp)
    ) {
        // Reserve room for the logo and (when visible) the Skip action so the app
        // name steps down in size before it is ever clipped on a narrow screen.
        val reservedWidth = OnboardingLogoWidth + 10.dp +
            if (showSkip) SkipActionWidth else 0.dp
        val availableForName = (maxWidth - reservedWidth).coerceAtLeast(0.dp)

        // 28sp matches the approved Home hero wordmark; smaller steps keep the
        // single-line lock-up intact on compact devices.
        val nameSize = when {
            availableForName >= 200.dp -> 28.sp
            availableForName >= 172.dp -> 25.sp
            availableForName >= 148.dp -> 22.sp
            else -> 20.sp
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.namma_nivasa_logo_burgundy),
                contentDescription = "Namma Nivasa logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = OnboardingLogoWidth, height = OnboardingLogoHeight)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Namma",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = nameSize,
                    lineHeight = nameSize * 1.1f,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Nivasa",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = nameSize,
                    lineHeight = nameSize * 1.1f,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (showSkip) {
                Spacer(Modifier.width(4.dp))
                TextButton(
                    onClick = onSkip,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFooter(
    page: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onDotClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = OnboardingMargin).padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicator. Announced as a whole so assistive tech reads progress
        // rather than three unrelated dots.
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.semantics { contentDescription = PageIndicatorLabel(page) }
        ) {
            repeat(OnboardingPageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == page) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        .clickable { onDotClick(index) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (page > 0) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(ShapeFull),
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                ) {
                    Text("Back", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(ShapeFull),
                modifier = Modifier.weight(if (page > 0) 2f else 1f).heightIn(min = 52.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (page == 2) "Get Started" else "Next",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPageCount })
    val coroutineScope = rememberCoroutineScope()
    val lastPage = OnboardingPageCount - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Burgundy status-bar strip fills the top system inset so the OS status
        // icons sit on burgundy, matching the approved reference.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
        )
        OnboardingHeader(showSkip = pagerState.currentPage < lastPage, onSkip = onFinish)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OnboardingMargin)
            ) {
                when (pageIndex) {
                    0 -> OnboardingPageOne()
                    1 -> OnboardingPageTwo()
                    else -> OnboardingPageThree()
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        OnboardingFooter(
            page = pagerState.currentPage,
            onBack = {
                if (pagerState.currentPage > 0) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            onNext = {
                if (pagerState.currentPage < lastPage) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            },
            onDotClick = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )
    }
}
