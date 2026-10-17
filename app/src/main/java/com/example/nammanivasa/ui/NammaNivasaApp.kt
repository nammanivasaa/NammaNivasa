package com.example.nammanivasa.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nammanivasa.data.api.AuthManager
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.example.nammanivasa.R
import com.example.nammanivasa.data.model.BillItem
import com.example.nammanivasa.data.model.NoticeItem
import com.example.nammanivasa.data.repository.FakeNammaNivasaRepository
import com.example.nammanivasa.data.repository.HybridNammaNivasaRepository
import com.example.nammanivasa.data.repository.RealNammaNivasaRepository
import com.example.nammanivasa.data.repository.RepoCreatePaymentRequest
import com.example.nammanivasa.data.repository.RepoError
import com.example.nammanivasa.data.repository.RepoResult
import com.example.nammanivasa.domain.model.PermissionCode
import com.example.nammanivasa.domain.model.RoleCode
import com.example.nammanivasa.domain.model.RoleDefinitions
import com.example.nammanivasa.ui.components.NnEmptyState
import com.example.nammanivasa.data.model.CommunityMembershipInfo
import com.example.nammanivasa.data.repository.userMessage
import com.example.nammanivasa.ui.components.NnErrorState
import com.example.nammanivasa.ui.components.NnInlineError
import com.example.nammanivasa.ui.components.NnLoadingState
import com.example.nammanivasa.ui.components.NnPrimaryButton
import com.example.nammanivasa.ui.components.NnSecondaryButton
import com.example.nammanivasa.ui.components.NnSectionHeader
import com.example.nammanivasa.ui.components.NnStatusBadge
import com.example.nammanivasa.ui.components.NnTextField
import com.example.nammanivasa.ui.components.nnSection
import com.example.nammanivasa.ui.components.StatusType
import com.example.nammanivasa.ui.theme.BrandGradientEnd
import com.example.nammanivasa.ui.theme.BrandGradientStart
import com.example.nammanivasa.ui.theme.DmSans
import com.example.nammanivasa.ui.theme.InterFontFamily
import com.example.nammanivasa.ui.theme.NotificationDot
import com.example.nammanivasa.ui.theme.PlayfairDisplay
import com.example.nammanivasa.ui.theme.PresenceActive
import com.example.nammanivasa.ui.theme.Primary
import com.example.nammanivasa.ui.theme.PrimaryDark
import com.example.nammanivasa.ui.theme.ShapeFull
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** The four primary destinations shown in the bottom navigation bar. */
private enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Payments("Payments", Icons.Filled.Payments),
    Community("Community", Icons.Filled.Groups),
    More("More", Icons.Filled.MoreHoriz)
}

/** Full-screen, reusable sub-screens that can be opened from any tab. */
private enum class SubScreen {
    None,
    FinancialAccess,
    PaymentVerification,
    NoticeManagement,
    MaintenanceRequest,
    ManagementDashboard
}

/** Launch stage: branded splash, onboarding intro, Welcome (Screen 4 auth entry), then main tabs. */
private enum class LaunchStage {
    Splash,
    Onboarding,
    Welcome,
    CommunityOnboarding,
    PendingApproval,
    Main
}

private enum class UserCommunityStatus {
    NoCommunity,
    RequestPending,
    AlreadyMember
}

private enum class ResidentRoleOption(val label: String) {
    Owner("Owner"),
    Tenant("Tenant"),
    FamilyMember("Family Member")
}

private data class UserCommunityStatusResolution(
    val status: UserCommunityStatus? = null,
    val error: RepoError? = null
)

private enum class CommunityOnboardingRoute {
    JoinFind,
    JoinApartment,
    JoinRole,
    JoinReview,
    CreateCommunity
}

/**
 * A community shown in the join flow, sourced from the discovery endpoint.
 *
 * [location] is an explicit "unavailable" placeholder — the `communities`
 * table has no location column — because Screens 8–10 render this field.
 * Tower/flat/member counts are deliberately absent: none can be derived from
 * authoritative community data.
 */
private data class DiscoverableCommunity(
    val communityId: String,
    val name: String,
    val location: String,
    val communityType: String? = null
)

// TODO(backend): The community-join flow needs a real unit-structure source —
// per-community towers / floors / flats from the API — before release. The
// backend's community record currently exposes only id / name / status / member
// count, so the picker options below are placeholder demo values. They are
// centralized here (instead of scattered inline lists) so the whole set can be
// swapped for API-driven data in one place once the schema exists. They must not
// ship as if they were real production data.
private val PlaceholderTowers = listOf("Tower A", "Tower B", "Tower C")
private val PlaceholderFloors = listOf(
    "Ground Floor",
    "1st Floor",
    "2nd Floor",
    "3rd Floor",
    "4th Floor",
    "5th Floor"
)
private val PlaceholderFlats = listOf("A-101", "A-203", "A-404", "B-302", "C-501")
private val PlaceholderPhases = listOf("Phase 1", "Phase 2", "Sector A", "Sector B")
private val PlaceholderVillas = listOf("Villa 101", "Villa 102", "Villa 201", "Plot 12", "Plot 45")

private data class FindCommunitiesUiState(
    val isLoading: Boolean = true,
    val communities: List<DiscoverableCommunity> = emptyList(),
    val error: String? = null
)

private const val LaunchPrefsName = "launch_prefs"
private const val OnboardingDoneKey = "onboarding_done"

@Composable
private fun BrandedSplashScreen() {
    var playAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playAnimation = true
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (playAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "splash_logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (playAnimation) 1f else 0.86f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (playAnimation) 0f else 10f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_logo_offset"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (playAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 320, easing = FastOutSlowInEasing),
        label = "splash_title_alpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (playAnimation) 0f else 14f,
        animationSpec = tween(durationMillis = 560, delayMillis = 320, easing = FastOutSlowInEasing),
        label = "splash_title_offset"
    )

    val captionAlpha by animateFloatAsState(
        targetValue = if (playAnimation) 0.92f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 620, easing = FastOutSlowInEasing),
        label = "splash_caption_alpha"
    )
    val captionOffsetY by animateFloatAsState(
        targetValue = if (playAnimation) 0f else 16f,
        animationSpec = tween(durationMillis = 560, delayMillis = 620, easing = FastOutSlowInEasing),
        label = "splash_caption_offset"
    )

    val loopTransition = rememberInfiniteTransition(label = "splash_logo_loop")
    val logoPulseScale by loopTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_logo_pulse"
    )
    val logoBobOffsetY by loopTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_logo_bob"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(BrandGradientStart, BrandGradientEnd)
                )
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.namma_nivasa_logo_white),
                contentDescription = "Namma Nivasa logo",
                modifier = Modifier
                    .offset(y = (logoOffsetY + logoBobOffsetY).dp)
                    .size(124.dp)
                    .graphicsLayer {
                        alpha = logoAlpha
                        scaleX = logoScale * logoPulseScale
                        scaleY = logoScale * logoPulseScale
                    }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Namma Nivasa",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = titleOffsetY.dp)
                    .graphicsLayer { alpha = titleAlpha }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart community living, simplified.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = captionOffsetY.dp)
                    .graphicsLayer { alpha = captionAlpha }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NnTopBar(communityName: String, onRefresh: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    // Premium brand gradient used sparingly for the prominent app bar.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(BrandGradientStart, BrandGradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Namma Nivasa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = communityName,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

/**
 * A slim, burgundy status-bar-height strip shown on the Home tab. It carries no
 * content — its only job is to keep the brand colour flowing behind the system
 * status bar while the permanent architectural hero (in the scrollable content)
 * passes beneath it. This lets the hero read as one continuous burgundy header
 * without duplicating the branding that [NnTopBar] provides on the other tabs.
 */
@Composable
private fun NnHomeStatusBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(BrandGradientStart, BrandGradientEnd)
                )
            )
    )
}

@Composable
private fun NnBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NammaNivasaApp() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val previousLightBars = controller?.isAppearanceLightStatusBars
            controller?.isAppearanceLightStatusBars = false
            onDispose {
                controller?.isAppearanceLightStatusBars = previousLightBars ?: true
            }
        }
    }

    val context = LocalContext.current
    val launchPrefs = remember(context) {
        context.getSharedPreferences(LaunchPrefsName, Activity.MODE_PRIVATE)
    }
    // Development flow: always preview branded splash + onboarding on launch.
    // Production routing (signed-in / onboarding-complete checks) can be restored later.
    var stage by rememberSaveable {
        mutableStateOf(LaunchStage.Splash)
    }
    var signedInEmail by rememberSaveable { mutableStateOf("") }
    var userStatusLookupError by rememberSaveable { mutableStateOf<String?>(null) }
    var communityRoute by rememberSaveable { mutableStateOf(CommunityOnboardingRoute.JoinFind) }
    if (stage == LaunchStage.Splash) {
        BrandedSplashScreen()
        LaunchedEffect(Unit) {
            val startTime = System.currentTimeMillis()
            val nextStage = if (AuthManager.isLoggedIn() || FirebaseAuth.getInstance().currentUser != null) {
                val user = FirebaseAuth.getInstance().currentUser
                var isUserValid = true
                if (user != null) {
                    try {
                        withTimeoutOrNull(5000L) {
                            user.reload().await()
                        }
                    } catch (t: Throwable) {
                        FirebaseAuth.getInstance().signOut()
                        isUserValid = false
                    }
                }

                if (!isUserValid || FirebaseAuth.getInstance().currentUser == null) {
                    signedInEmail = ""
                    Toast.makeText(context, "Your session has ended. Please sign in again.", Toast.LENGTH_LONG).show()
                    LaunchStage.Welcome
                } else {
                    signedInEmail = user?.email.orEmpty().ifBlank { "user@nammanivasa.com" }
                    val resolution = resolveUserCommunityStatus(context)
                    if (resolution.error is RepoError.Unauthenticated) {
                        FirebaseAuth.getInstance().signOut()
                        signedInEmail = ""
                        Toast.makeText(context, "Your session has ended. Please sign in again.", Toast.LENGTH_LONG).show()
                        LaunchStage.Welcome
                    } else {
                        userStatusLookupError = resolution.error?.userMessage()
                        when (resolution.status) {
                            UserCommunityStatus.NoCommunity -> LaunchStage.CommunityOnboarding
                            UserCommunityStatus.RequestPending -> LaunchStage.PendingApproval
                            UserCommunityStatus.AlreadyMember -> LaunchStage.Main
                            null -> LaunchStage.CommunityOnboarding
                        }
                    }
                }
            } else if (!launchPrefs.getBoolean(OnboardingDoneKey, false)) {
                LaunchStage.Onboarding
            } else {
                LaunchStage.Welcome
            }

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 1_800) {
                delay(1_800 - elapsed)
            }
            communityRoute = CommunityOnboardingRoute.JoinFind
            stage = nextStage
        }
        return
    }
    if (stage == LaunchStage.Onboarding) {
        OnboardingScreen(
            onFinish = {
                launchPrefs.edit().putBoolean(OnboardingDoneKey, true).apply()
                stage = LaunchStage.Welcome
            }
        )
        return
    }
    if (stage == LaunchStage.Welcome) {
        WelcomeAuthFlow(
            onSignedIn = { email ->
                signedInEmail = email
                userStatusLookupError = null
                stage = LaunchStage.Splash
            }
        )
        return
    }
    if (stage == LaunchStage.PendingApproval) {
        PendingApprovalScreen(
            userEmail = signedInEmail,
            onRefreshStatus = { stage = LaunchStage.Splash },
            onBackToWelcome = { stage = LaunchStage.Welcome }
        )
        return
    }
    val appContext = LocalContext.current.applicationContext
    val viewModel: SocietyViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SocietyViewModel.createDefault(appContext) }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (stage == LaunchStage.CommunityOnboarding) {
        CommunityOnboardingFlowScreen(
            route = communityRoute,
            statusWarning = userStatusLookupError,
            viewModel = viewModel,
            onStartJoin = { communityRoute = CommunityOnboardingRoute.JoinFind },
            onStartCreate = { communityRoute = CommunityOnboardingRoute.CreateCommunity },
            onOpenJoinApartment = { communityRoute = CommunityOnboardingRoute.JoinApartment },
            onOpenJoinRole = { communityRoute = CommunityOnboardingRoute.JoinRole },
            onOpenJoinReview = { communityRoute = CommunityOnboardingRoute.JoinReview },
            onBackToWelcome = {
                signedInEmail = ""
                communityRoute = CommunityOnboardingRoute.JoinFind
                stage = LaunchStage.Welcome
            },
            onJoinSubmitted = { stage = LaunchStage.PendingApproval },
            onCommunityCreated = { stage = LaunchStage.Main }
        )
        return
    }

    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var subScreen by remember { mutableStateOf(SubScreen.None) }

    BackHandler(enabled = stage == LaunchStage.Main) {
        when {
            subScreen != SubScreen.None -> subScreen = SubScreen.None
            selectedTab != AppTab.Home -> selectedTab = AppTab.Home
            else -> (context as? Activity)?.finish()
        }
    }

    // The Home tab is headed by a permanent burgundy architectural hero (rather
    // than the shared branded top app bar). On Home a slim status-bar strip keeps
    // the brand colour behind the system bars while the hero scrolls beneath it;
    // the other tabs and all full-screen sub-screens keep the [NnTopBar].
    val isHomeMain = selectedTab == AppTab.Home && subScreen == SubScreen.None

    val openSub: (SubScreen) -> Unit = { subScreen = it }
    val closeSub: () -> Unit = { subScreen = SubScreen.None }
    val onNavigateTab: (AppTab) -> Unit = { tab ->
        selectedTab = tab
        subScreen = SubScreen.None
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // No community chosen yet: every community-scoped read is skipped, so the tabs
    // would otherwise look inexplicably empty. Ask for a selection instead.
    if (uiState.needsCommunitySelection && subScreen == SubScreen.None) {
        NoCommunityPrompt(
            communities = uiState.communities,
            onSelect = viewModel::selectCommunity,
            onOpenCommunities = {
                selectedTab = AppTab.More
                subScreen = SubScreen.None
            }
        )
        return
    }

    // A failure to load the profile or the community list blocks everything else.
    // uiState is a delegated property, so capture the error locally to allow the
    // smart cast below.
    val loadError = uiState.error
    if (loadError != null && subScreen == SubScreen.None) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                NnTopBar(
                    communityName = uiState.activeCommunity?.name ?: "Namma Nivasa",
                    onRefresh = { viewModel.refresh() },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                NnErrorState(
                    message = loadError.userMessage(),
                    onRetry = { viewModel.refresh() }
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isHomeMain) {
                NnHomeStatusBar()
            } else {
                NnTopBar(
                    communityName = uiState.activeCommunity?.name
                        ?: uiState.userProfile?.fullName
                        ?: "Namma Nivasa",
                    onRefresh = { viewModel.refresh() },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            NnBottomBar(
                selected = selectedTab,
                onSelect = {
                    selectedTab = it
                    subScreen = SubScreen.None
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .then(
                    if (isHomeMain) {
                        Modifier
                    } else {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    }
                )
        ) {
            // Slim, always-visible notice while the repository is serving local demo
            // data, so fabricated content is never mistaken for backend data.
            if (uiState.usingDemoData) {
                NnDemoDataBanner()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (subScreen) {
                    SubScreen.FinancialAccess -> FinancialAccessScreen(
                        grants = uiState.financialAccessGrants.data,
                        isSubmitting = uiState.isSubmitting,
                        submitError = uiState.submitError,
                        ownerUserId = uiState.userProfile?.id,
                        onCreateGrant = viewModel::createFinancialAccessGrant,
                        onRevokeGrant = viewModel::revokeFinancialAccessGrant,
                        onCancel = closeSub
                    )

                    SubScreen.PaymentVerification -> PaymentVerificationScreen(
                        payments = uiState.myPayments.data,
                        isSubmitting = uiState.isSubmitting,
                        submitError = uiState.submitError,
                        onVerify = viewModel::verifyPayment,
                        onReject = viewModel::rejectPayment,
                        onCancel = closeSub
                    )

                    SubScreen.NoticeManagement -> NoticeManagementScreen(
                        notices = uiState.notices.data,
                        isSubmitting = uiState.isSubmitting,
                        submitError = uiState.submitError,
                        onCreateNotice = viewModel::createNotice,
                        onPublishNotice = viewModel::publishNotice,
                        onCancel = closeSub
                    )

                    SubScreen.MaintenanceRequest -> MaintenanceRequestScreen(
                        requests = uiState.maintenanceRequests.data,
                        isSubmitting = uiState.isSubmitting,
                        submitError = uiState.submitError,
                        onCreateRequest = viewModel::createMaintenanceRequest,
                        onCancel = closeSub
                    )

                    SubScreen.ManagementDashboard -> ManagementDashboardScreen(
                        visitors = uiState.visitors.data,
                        payments = uiState.myPayments.data,
                        notices = uiState.notices.data,
                        maintenanceRequests = uiState.maintenanceRequests.data,
                        isLoading = uiState.isLoading,
                        onApproveVisitor = viewModel::approveVisitor,
                        onRejectVisitor = viewModel::rejectVisitor,
                        onVerifyPayment = viewModel::verifyPayment,
                        onRejectPayment = viewModel::rejectPayment,
                        onCancel = closeSub
                    )

                    SubScreen.None -> when (selectedTab) {
                        AppTab.Home -> HomeTab(uiState, viewModel, openSub, onNavigateTab)
                        AppTab.Payments -> PaymentsTab(uiState, viewModel, openSub)
                        AppTab.Community -> CommunityTab(uiState, openSub)
                        AppTab.More -> MoreTab(uiState, viewModel, openSub)
                    }
                }
            }
        }
    }
}

private suspend fun resolveUserCommunityStatus(context: android.content.Context): UserCommunityStatusResolution {
    val repository = RealNammaNivasaRepository(context.applicationContext)
    val result = try {
        withTimeout(4000) {
            repository.getCommunities()
        }
    } catch (e: Exception) {
        RepoResult.Failure(RepoError.Unexpected(e.message))
    }
    return when (result) {
        is RepoResult.Success -> {
            val memberships = result.data
            UserCommunityStatusResolution(
                status = when {
                    memberships.any { it.membershipStatus.equals("ACTIVE", ignoreCase = true) } -> UserCommunityStatus.AlreadyMember
                    memberships.any {
                        it.membershipStatus.equals("PENDING", ignoreCase = true) ||
                            it.membershipStatus.equals("REQUEST_PENDING", ignoreCase = true)
                    } -> UserCommunityStatus.RequestPending
                    else -> UserCommunityStatus.NoCommunity
                }
            )
        }
        is RepoResult.Failure -> UserCommunityStatusResolution(error = result.error)
    }
}

@Composable
private fun UserStatusLookupErrorScreen(
    message: String?,
    onRetry: () -> Unit,
    onBackToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't check your community status",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message ?: "Please try again.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Retry")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBackToWelcome, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun PendingApprovalScreen(
    userEmail: String,
    onRefreshStatus: () -> Unit,
    onBackToWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.namma_nivasa_logo_burgundy),
                contentDescription = "Namma Nivasa",
                modifier = Modifier.height(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Request Pending",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your community access request is pending approval.",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (userEmail.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = InterFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(32.dp))
            NnPrimaryButton(
                text = "Refresh Status",
                onClick = onRefreshStatus
            )
            Spacer(Modifier.height(12.dp))
            NnSecondaryButton(
                text = "Back",
                onClick = onBackToWelcome
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CommunityOnboardingFlowScreen(
    route: CommunityOnboardingRoute,
    statusWarning: String?,
    viewModel: SocietyViewModel,
    onStartJoin: () -> Unit,
    onStartCreate: () -> Unit,
    onOpenJoinApartment: () -> Unit,
    onOpenJoinRole: () -> Unit,
    onOpenJoinReview: () -> Unit,
    onBackToWelcome: () -> Unit,
    onJoinSubmitted: () -> Unit,
    onCommunityCreated: () -> Unit
) {
    var selectedCommunityId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTower by rememberSaveable { mutableStateOf(PlaceholderTowers.first()) }
    var selectedFloor by rememberSaveable { mutableStateOf(PlaceholderFloors.first()) }
    var selectedFlat by rememberSaveable { mutableStateOf(PlaceholderFlats.first()) }
    var selectedPhase by rememberSaveable { mutableStateOf(PlaceholderPhases.first()) }
    var selectedVilla by rememberSaveable { mutableStateOf(PlaceholderVillas.first()) }
    var selectedResidentRole by rememberSaveable { mutableStateOf(ResidentRoleOption.Owner.name) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var joinConfirmed by rememberSaveable { mutableStateOf(false) }
    var communitiesReloadTrigger by rememberSaveable { mutableStateOf(0) }

    val context = LocalContext.current
    val findCommunitiesState by produceState(
        initialValue = FindCommunitiesUiState(),
        key1 = context.applicationContext,
        key2 = communitiesReloadTrigger
    ) {
        val repository = RealNammaNivasaRepository(context.applicationContext)
        value = FindCommunitiesUiState(isLoading = true)
        when (val result = repository.discoverCommunities("")) {
            is RepoResult.Success -> {
                value = FindCommunitiesUiState(
                    isLoading = false,
                    communities = result.data.map { community ->
                        DiscoverableCommunity(
                            communityId = community.id,
                            name = community.name,
                            location = "Location unavailable",
                            communityType = when {
                                community.name.contains("Villa", ignoreCase = true) || community.name.contains("Gated", ignoreCase = true) -> "Villa Layout"
                                community.name.contains("Standalone", ignoreCase = true) || community.name.contains("Building", ignoreCase = true) -> "Standalone Building"
                                community.name.contains("Tower", ignoreCase = true) || community.name.contains("Residency", ignoreCase = true) || community.name.contains("Apartment", ignoreCase = true) -> "Apartment Complex"
                                else -> null
                            }
                        )
                    }
                )
            }
            is RepoResult.Failure -> {
                value = FindCommunitiesUiState(
                    isLoading = false,
                    error = result.error.userMessage()
                )
            }
        }
    }

    val filteredCommunities = findCommunitiesState.communities.filter {
        val query = searchQuery.trim()
        query.isNotBlank() && it.name.contains(query, ignoreCase = true)
    }
    val selectedCommunity = filteredCommunities.find { it.communityId == selectedCommunityId }

    when (route) {
        CommunityOnboardingRoute.JoinFind -> JoinCommunityFindScreen(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isLoading = findCommunitiesState.isLoading,
            errorMessage = statusWarning?.takeIf { it.isNotBlank() } ?: findCommunitiesState.error,
            communities = filteredCommunities,
            selectedCommunityId = selectedCommunityId,
            onSelectCommunity = { selectedCommunityId = it },
            onBack = onBackToWelcome,
            onReload = { communitiesReloadTrigger += 1 },
            onRegisterCommunity = onStartCreate,
            onContinue = {
                if (selectedCommunity != null) {
                    joinConfirmed = false
                    onOpenJoinApartment()
                }
            }
        )

        CommunityOnboardingRoute.JoinApartment -> if (selectedCommunity != null) JoinCommunityApartmentScreen(
            community = selectedCommunity,
            selectedTower = selectedTower,
            selectedFloor = selectedFloor,
            selectedFlat = selectedFlat,
            selectedPhase = selectedPhase,
            selectedVilla = selectedVilla,
            onTowerChanged = { selectedTower = it },
            onFloorChanged = { selectedFloor = it },
            onFlatChanged = { selectedFlat = it },
            onPhaseChanged = { selectedPhase = it },
            onVillaChanged = { selectedVilla = it },
            onBack = { onStartJoin() },
            onContinue = { onOpenJoinRole() }
        ) else JoinCommunityFindScreen(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isLoading = findCommunitiesState.isLoading,
            errorMessage = findCommunitiesState.error,
            communities = filteredCommunities,
            selectedCommunityId = selectedCommunityId,
            onSelectCommunity = { selectedCommunityId = it },
            onBack = onBackToWelcome,
            onReload = { communitiesReloadTrigger += 1 },
            onRegisterCommunity = onStartCreate,
            onContinue = {
                if (selectedCommunity != null) {
                    joinConfirmed = false
                    onOpenJoinApartment()
                }
            }
        )

        CommunityOnboardingRoute.JoinRole -> if (selectedCommunity != null) JoinCommunityRoleScreen(
            community = selectedCommunity,
            selectedTower = selectedTower,
            selectedFloor = selectedFloor,
            selectedFlat = selectedFlat,
            selectedPhase = selectedPhase,
            selectedVilla = selectedVilla,
            selectedRole = ResidentRoleOption.valueOf(selectedResidentRole),
            onRoleSelected = { selectedResidentRole = it.name },
            onBack = { onOpenJoinApartment() },
            onContinue = { onOpenJoinReview() }
        ) else JoinCommunityFindScreen(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isLoading = findCommunitiesState.isLoading,
            errorMessage = findCommunitiesState.error,
            communities = filteredCommunities,
            selectedCommunityId = selectedCommunityId,
            onSelectCommunity = { selectedCommunityId = it },
            onBack = onBackToWelcome,
            onReload = { communitiesReloadTrigger += 1 },
            onRegisterCommunity = onStartCreate,
            onContinue = {
                if (selectedCommunity != null) {
                    joinConfirmed = false
                    onOpenJoinApartment()
                }
            }
        )

        CommunityOnboardingRoute.JoinReview -> if (selectedCommunity != null) JoinCommunityReviewScreen(
            community = selectedCommunity,
            selectedTower = selectedTower,
            selectedFloor = selectedFloor,
            selectedFlat = selectedFlat,
            selectedPhase = selectedPhase,
            selectedVilla = selectedVilla,
            selectedRole = ResidentRoleOption.valueOf(selectedResidentRole),
            confirmed = joinConfirmed,
            onConfirmationChanged = { joinConfirmed = it },
            onBack = { onOpenJoinRole() },
            onSubmit = onJoinSubmitted
        ) else JoinCommunityFindScreen(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isLoading = findCommunitiesState.isLoading,
            errorMessage = findCommunitiesState.error,
            communities = filteredCommunities,
            selectedCommunityId = selectedCommunityId,
            onSelectCommunity = { selectedCommunityId = it },
            onBack = onBackToWelcome,
            onReload = { communitiesReloadTrigger += 1 },
            onRegisterCommunity = onStartCreate,
            onContinue = {
                if (selectedCommunity != null) {
                    joinConfirmed = false
                    onOpenJoinApartment()
                }
            }
        )

        CommunityOnboardingRoute.CreateCommunity -> CreateCommunityStarterScreen(
            onBack = onStartJoin,
            onCreated = onCommunityCreated,
            viewModel = viewModel
        )
    }
}

@Composable
private fun JoinCommunityFindScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    communities: List<DiscoverableCommunity>,
    selectedCommunityId: String?,
    onSelectCommunity: (String) -> Unit,
    onBack: () -> Unit,
    onReload: () -> Unit,
    onRegisterCommunity: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
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
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            CommunityOnboardingProgressIndicator(currentStep = 1)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Find Your Community",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Search by community name.",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                placeholder = { Text("Search by community name", fontFamily = InterFontFamily) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = Color(0xFFF5F0EE),
                    unfocusedContainerColor = Color(0xFFF5F0EE),
                    errorContainerColor = Color(0xFFF5F0EE)
                )
            )
            Spacer(Modifier.height(12.dp))

            if (query.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Search Results", style = MaterialTheme.typography.titleMedium, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (communities.isEmpty()) {
                    item {
                        if (query.isBlank()) {
                            NnEmptyState(
                                icon = Icons.Filled.Search,
                                title = "Find Your Community",
                                message = "Type your apartment or society name above to search."
                            )
                        } else {
                            NnEmptyState(
                                icon = Icons.Filled.Apartment,
                                title = "No Communities Found",
                                message = "We couldn't find any communities matching \"$query\".",
                                actionText = "Can't find your community? Register",
                                onActionClick = onRegisterCommunity
                            )
                        }
                    }
                } else {
                    items(items = communities, key = { it.communityId }) { item ->
                        val selected = item.communityId == selectedCommunityId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCommunity(item.communityId) },
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.placeholder),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 60.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(item.location, style = MaterialTheme.typography.bodyMedium, fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NnSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                NnPrimaryButton(
                    text = "Continue  →",
                    onClick = onContinue,
                    enabled = communities.any { it.communityId == selectedCommunityId },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CommunityOnboardingProgressIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Find Community", "Apartment", "Role", "Review")
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val stepNum = index + 1
                val active = stepNum <= currentStep
                val current = stepNum == currentStep
                ProgressNode(label = stepNum.toString(), active = active, current = current)
                if (index < steps.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        color = if (stepNum < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, label ->
                val stepNum = index + 1
                val active = stepNum == currentStep
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressNode(label: String, active: Boolean, current: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                CircleShape
            )
            .border(
                width = if (current) 2.dp else 1.dp,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 13.sp,
            fontFamily = InterFontFamily,
            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
private fun JoinCommunityApartmentScreen(
    community: DiscoverableCommunity,
    selectedTower: String,
    selectedFloor: String,
    selectedFlat: String,
    selectedPhase: String,
    selectedVilla: String,
    onTowerChanged: (String) -> Unit,
    onFloorChanged: (String) -> Unit,
    onFlatChanged: (String) -> Unit,
    onPhaseChanged: (String) -> Unit,
    onVillaChanged: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val communityType = community.communityType?.lowercase() ?: ""
    val isStandalone = communityType.contains("standalone") || communityType.contains("single")
    val isVilla = communityType.contains("villa") || communityType.contains("gated")
    val isApartment = communityType.contains("apartment") || communityType.contains("complex") || communityType.contains("tower")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            CommunityOnboardingProgressIndicator(currentStep = 2)
            Spacer(Modifier.height(20.dp))
            Text(
                text = when {
                    isVilla -> "Select your villa or plot"
                    isStandalone -> "Select your floor and flat"
                    else -> "Select your apartment"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(community.name, style = MaterialTheme.typography.titleLarge, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(community.location, style = MaterialTheme.typography.bodyMedium, fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (community.communityType != null) {
                        Spacer(Modifier.height(4.dp))
                        Text("Type: ${community.communityType}", style = MaterialTheme.typography.bodySmall, fontFamily = InterFontFamily, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            when {
                isStandalone -> {
                    // Standalone Building: omit block/tower selection entirely, present Floor Selection and Flat/Unit Selection directly
                    RoleDropdown(
                        title = "Floor Selection",
                        selectedLabel = selectedFloor,
                        options = PlaceholderFloors,
                        onOptionSelected = onFloorChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Flat / Unit Selection",
                        selectedLabel = selectedFlat,
                        options = PlaceholderFlats,
                        onOptionSelected = onFlatChanged
                    )
                }
                isVilla -> {
                    // Villa Layout: present Phase/Sector and Villa/Plot selection
                    RoleDropdown(
                        title = "Phase / Sector",
                        selectedLabel = selectedPhase,
                        options = PlaceholderPhases,
                        onOptionSelected = onPhaseChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Villa / Plot Selection",
                        selectedLabel = selectedVilla,
                        options = PlaceholderVillas,
                        onOptionSelected = onVillaChanged
                    )
                }
                isApartment -> {
                    // Apartment Complex: present Block/Tower and Flat selection (and Floor Selection)
                    RoleDropdown(
                        title = "Block / Tower",
                        selectedLabel = selectedTower,
                        options = PlaceholderTowers,
                        onOptionSelected = onTowerChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Floor Selection",
                        selectedLabel = selectedFloor,
                        options = PlaceholderFloors,
                        onOptionSelected = onFloorChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Flat Selection",
                        selectedLabel = selectedFlat,
                        options = PlaceholderFlats,
                        onOptionSelected = onFlatChanged
                    )
                }
                else -> {
                    // Unspecified configuration: fall back gracefully to a flexible unit selector
                    RoleDropdown(
                        title = "Tower / Block",
                        selectedLabel = selectedTower,
                        options = PlaceholderTowers,
                        onOptionSelected = onTowerChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Floor Selection",
                        selectedLabel = selectedFloor,
                        options = PlaceholderFloors,
                        onOptionSelected = onFloorChanged
                    )
                    Spacer(Modifier.height(16.dp))
                    RoleDropdown(
                        title = "Flat / Unit Selection",
                        selectedLabel = selectedFlat,
                        options = PlaceholderFlats,
                        onOptionSelected = onFlatChanged
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NnSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                NnPrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun JoinCommunityRoleScreen(
    community: DiscoverableCommunity,
    selectedTower: String,
    selectedFloor: String,
    selectedFlat: String,
    selectedPhase: String,
    selectedVilla: String,
    selectedRole: ResidentRoleOption,
    onRoleSelected: (ResidentRoleOption) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val communityType = community.communityType?.lowercase() ?: ""
    val isStandalone = communityType.contains("standalone") || communityType.contains("single")
    val isVilla = communityType.contains("villa") || communityType.contains("gated")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            CommunityOnboardingProgressIndicator(currentStep = 3)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Your relationship to this unit",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    isVilla -> "${community.name} • $selectedPhase • $selectedVilla"
                    isStandalone -> "${community.name} • $selectedFloor • $selectedFlat"
                    else -> "${community.name} • $selectedTower • $selectedFloor • $selectedFlat"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            ResidentRoleOption.entries.forEach { role ->
                val isSelected = selectedRole == role
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onRoleSelected(role) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFF5F0EE) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(role.label, style = MaterialTheme.typography.titleMedium, fontFamily = InterFontFamily, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = when (role) {
                                    ResidentRoleOption.Owner -> "I own this unit."
                                    ResidentRoleOption.Tenant -> "I rent and live in this unit."
                                    ResidentRoleOption.FamilyMember -> "I am a family member of the owner."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NnSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                NnPrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun JoinCommunityReviewScreen(
    community: DiscoverableCommunity,
    selectedTower: String,
    selectedFloor: String,
    selectedFlat: String,
    selectedPhase: String,
    selectedVilla: String,
    selectedRole: ResidentRoleOption,
    confirmed: Boolean,
    onConfirmationChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val communityType = community.communityType?.lowercase() ?: ""
    val isStandalone = communityType.contains("standalone") || communityType.contains("single")
    val isVilla = communityType.contains("villa") || communityType.contains("gated")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            CommunityOnboardingProgressIndicator(currentStep = 4)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Review and submit",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EE))
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReviewRow(label = "Community", value = community.name)
                    ReviewRow(label = "Location", value = community.location)
                    if (community.communityType != null) {
                        ReviewRow(label = "Type", value = community.communityType)
                    }
                    when {
                        isVilla -> {
                            ReviewRow(label = "Phase / Sector", value = selectedPhase)
                            ReviewRow(label = "Villa / Plot", value = selectedVilla)
                        }
                        isStandalone -> {
                            ReviewRow(label = "Floor", value = selectedFloor)
                            ReviewRow(label = "Flat / Unit", value = selectedFlat)
                        }
                        else -> {
                            ReviewRow(label = "Tower", value = selectedTower)
                            ReviewRow(label = "Floor", value = selectedFloor)
                            ReviewRow(label = "Flat", value = selectedFlat)
                        }
                    }
                    ReviewRow(label = "Role", value = selectedRole.label)
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfirmationChanged(!confirmed) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (confirmed) 1.5.dp else 1.dp,
                    color = if (confirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (confirmed) Color(0xFFF5F0EE) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (confirmed) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        contentDescription = null,
                        tint = if (confirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "I confirm that the above apartment and role details are correct.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NnSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                NnPrimaryButton(
                    text = "Submit Request",
                    onClick = onSubmit,
                    enabled = confirmed,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = InterFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private const val OTHER_CITY = "➕ Other (Type custom city/town...)"
private const val OTHER_AREA = "➕ Other (Type custom area...)"

private data class AreaPin(
    val area: String,
    val pinCode: String
)

private val locationHierarchy: Map<String, Map<String, List<AreaPin>>> = mapOf(
    "Andhra Pradesh" to mapOf(
        "Visakhapatnam" to listOf(AreaPin("MVP Colony", "530017"), AreaPin("Gajuwaka", "530026"), AreaPin("Madhurawada", "530041")),
        "Vijayawada" to listOf(AreaPin("Benz Circle", "520010"), AreaPin("Moghalrajpuram", "520010")),
        "Tirupati" to listOf(AreaPin("KT Road", "517501"), AreaPin("MR Palle", "517502"))
    ),
    "Arunachal Pradesh" to mapOf(
        "Itanagar" to listOf(AreaPin("Ganga Market", "791111"), AreaPin("E Sector", "791111")),
        "Naharlagun" to listOf(AreaPin("Model Village", "791110"))
    ),
    "Assam" to mapOf(
        "Guwahati" to listOf(AreaPin("GS Road", "781005"), AreaPin("Zoo Road", "781024"), AreaPin("Dispur", "781006")),
        "Silchar" to listOf(AreaPin("Tarapur", "788003"), AreaPin("Ambicapatty", "788004"))
    ),
    "Bihar" to mapOf(
        "Patna" to listOf(AreaPin("Boring Road", "800001"), AreaPin("Kankarbagh", "800020"), AreaPin("Bailey Road", "800014")),
        "Gaya" to listOf(AreaPin("AP Colony", "823001"), AreaPin("Civil Lines", "823001"))
    ),
    "Chhattisgarh" to mapOf(
        "Raipur" to listOf(AreaPin("Shankar Nagar", "492007"), AreaPin("Pandri", "492004")),
        "Bhilai" to listOf(AreaPin("Sector 6", "490006"), AreaPin("Civic Centre", "490006"))
    ),
    "Goa" to mapOf(
        "Panaji" to listOf(AreaPin("Miramar", "403001"), AreaPin("Caranzalem", "403002")),
        "Margao" to listOf(AreaPin("Fatorda", "403602"), AreaPin("Borda", "403601"))
    ),
    "Gujarat" to mapOf(
        "Ahmedabad" to listOf(AreaPin("Navrangpura", "380009"), AreaPin("Satellite", "380015"), AreaPin("SG Highway", "380054")),
        "Surat" to listOf(AreaPin("Adajan", "395009"), AreaPin("Vesu", "395007")),
        "Vadodara" to listOf(AreaPin("Alkapuri", "390007"), AreaPin("Gotri", "390021"))
    ),
    "Haryana" to mapOf(
        "Gurugram" to listOf(AreaPin("DLF Phase 1", "122002"), AreaPin("Cyber City", "122002"), AreaPin("Sohna Road", "122018"), AreaPin("Golf Course Road", "122002")),
        "Faridabad" to listOf(AreaPin("Sector 15", "121007"), AreaPin("Sector 21C", "121001")),
        "Panchkula" to listOf(AreaPin("Sector 5", "134109"), AreaPin("Sector 20", "134116"))
    ),
    "Himachal Pradesh" to mapOf(
        "Shimla" to listOf(AreaPin("Mall Road", "171001"), AreaPin("Chotta Shimla", "171002")),
        "Dharamshala" to listOf(AreaPin("McLeod Ganj", "176219"), AreaPin("Kotwali Bazaar", "176215"))
    ),
    "Jharkhand" to mapOf(
        "Ranchi" to listOf(AreaPin("Main Road", "834001"), AreaPin("Kanke Road", "834008")),
        "Jamshedpur" to listOf(AreaPin("Bistupur", "831001"), AreaPin("Sakchi", "831001"))
    ),
    "Karnataka" to mapOf(
        "Bengaluru" to listOf(
            AreaPin("Indiranagar", "560038"),
            AreaPin("Koramangala", "560095"),
            AreaPin("Whitefield", "560066"),
            AreaPin("Jayanagar", "560041"),
            AreaPin("HSR Layout", "560102"),
            AreaPin("Marathahalli", "560037"),
            AreaPin("Electronic City", "560100"),
            AreaPin("Hebbal", "560024"),
            AreaPin("JP Nagar", "560078")
        ),
        "Mysuru" to listOf(
            AreaPin("Gokulam", "570002"),
            AreaPin("Vijayanagar", "570017"),
            AreaPin("Saraswathipuram", "570009"),
            AreaPin("Kuvempunagar", "570023")
        ),
        "Hubballi-Dharwad" to listOf(AreaPin("Vidyanagar", "580021"), AreaPin("Gokul Road", "580030")),
        "Mangaluru" to listOf(AreaPin("Kadur", "575002"), AreaPin("Bejai", "575004"))
    ),
    "Kerala" to mapOf(
        "Kochi" to listOf(AreaPin("Marine Drive", "682031"), AreaPin("Kadavanthra", "682020"), AreaPin("Kakkanad", "682030")),
        "Thiruvananthapuram" to listOf(AreaPin("Kowdiar", "695003"), AreaPin("Technopark", "695581")),
        "Kozhikode" to listOf(AreaPin("Mavoor Road", "673004"), AreaPin("Tali", "673002"))
    ),
    "Madhya Pradesh" to mapOf(
        "Indore" to listOf(AreaPin("Vijay Nagar", "452010"), AreaPin("Palasia", "452001"), AreaPin("Saket", "452018")),
        "Bhopal" to listOf(AreaPin("Arera Colony", "462016"), AreaPin("MP Nagar", "462011"))
    ),
    "Maharashtra" to mapOf(
        "Mumbai" to listOf(
            AreaPin("Andheri West", "400053"),
            AreaPin("Bandra West", "400050"),
            AreaPin("Powai", "400076"),
            AreaPin("Worli", "400018"),
            AreaPin("Juhu", "400049"),
            AreaPin("Lower Parel", "400013")
        ),
        "Pune" to listOf(
            AreaPin("Hinjewadi", "411057"),
            AreaPin("Koregaon Park", "411001"),
            AreaPin("Baner", "411045"),
            AreaPin("Wakad", "411057"),
            AreaPin("Viman Nagar", "411014")
        ),
        "Nagpur" to listOf(AreaPin("Dharampeth", "440010"), AreaPin("Civil Lines", "440001")),
        "Thane" to listOf(AreaPin("Ghodbunder Road", "400615"), AreaPin("Majiwada", "400601")),
        "Nashik" to listOf(AreaPin("College Road", "422005"), AreaPin("Indira Nagar", "422009"))
    ),
    "Manipur" to mapOf("Imphal" to listOf(AreaPin("Thangal Bazar", "795001"), AreaPin("Paona Bazar", "795001"))),
    "Meghalaya" to mapOf("Shillong" to listOf(AreaPin("Police Bazar", "793001"), AreaPin("Laitumkhrah", "793003"))),
    "Mizoram" to mapOf("Aizawl" to listOf(AreaPin("Zarkawt", "796001"), AreaPin("Chanmari", "796007"))),
    "Nagaland" to mapOf("Kohima" to listOf(AreaPin("PR Hill", "797001")), "Dimapur" to listOf(AreaPin("Circular Road", "797112"))),
    "Odisha" to mapOf(
        "Bhubaneswar" to listOf(AreaPin("Jayadev Vihar", "751013"), AreaPin("Saheed Nagar", "751007"), AreaPin("Patia", "751024")),
        "Cuttack" to listOf(AreaPin("Cantonment Road", "753001"))
    ),
    "Punjab" to mapOf(
        "Ludhiana" to listOf(AreaPin("Sarabha Nagar", "141001"), AreaPin("Model Town", "141002")),
        "Amritsar" to listOf(AreaPin("Ranjit Avenue", "143001"), AreaPin("Mall Road", "143001")),
        "Mohali" to listOf(AreaPin("Sector 70", "160071"), AreaPin("Phase 3B2", "160059"))
    ),
    "Rajasthan" to mapOf(
        "Jaipur" to listOf(AreaPin("Malviya Nagar", "302017"), AreaPin("Vaishali Nagar", "302021"), AreaPin("C Scheme", "302001")),
        "Udaipur" to listOf(AreaPin("Fatehpura", "313001"), AreaPin("Hiran Magri", "313002")),
        "Jodhpur" to listOf(AreaPin("Sardarpura", "342003"), AreaPin("Ratanada", "342011"))
    ),
    "Sikkim" to mapOf("Gangtok" to listOf(AreaPin("MG Marg", "737101"), AreaPin("Deorali", "737102"))),
    "Tamil Nadu" to mapOf(
        "Chennai" to listOf(
            AreaPin("T. Nagar", "600017"),
            AreaPin("Anna Nagar", "600040"),
            AreaPin("Velachery", "600042"),
            AreaPin("Adyar", "600020"),
            AreaPin("Nungambakkam", "600034")
        ),
        "Coimbatore" to listOf(
            AreaPin("RS Puram", "641002"),
            AreaPin("Peelamedu", "641004"),
            AreaPin("Saibaba Colony", "641011")
        ),
        "Madurai" to listOf(AreaPin("Anna Nagar", "625020"), AreaPin("KK Nagar", "625020"))
    ),
    "Telangana" to mapOf(
        "Hyderabad" to listOf(
            AreaPin("Gachibowli", "500032"),
            AreaPin("HITEC City", "500081"),
            AreaPin("Jubilee Hills", "500033"),
            AreaPin("Banjara Hills", "500034"),
            AreaPin("Madhapur", "500081"),
            AreaPin("Kondapur", "500084")
        ),
        "Warangal" to listOf(AreaPin("Hanamkonda", "506001"), AreaPin("Kazipet", "506003"))
    ),
    "Tripura" to mapOf("Agartala" to listOf(AreaPin("Banamalipur", "799001"), AreaPin("Agartala Bazar", "799001"))),
    "Uttar Pradesh" to mapOf(
        "Noida" to listOf(AreaPin("Sector 62", "201309"), AreaPin("Sector 18", "201301"), AreaPin("Sector 137", "201305")),
        "Lucknow" to listOf(AreaPin("Gomti Nagar", "226010"), AreaPin("Hazratganj", "226001"), AreaPin("Indira Nagar", "226016")),
        "Kanpur" to listOf(AreaPin("Swaroop Nagar", "208002"), AreaPin("Civil Lines", "208001")),
        "Ghaziabad" to listOf(AreaPin("Indirapuram", "201014"), AreaPin("Vaishali", "201010")),
        "Varanasi" to listOf(AreaPin("Lanka", "221005"), AreaPin("Sigra", "221002"))
    ),
    "Uttarakhand" to mapOf(
        "Dehradun" to listOf(AreaPin("Rajpur Road", "248001"), AreaPin("Vasant Vihar", "248006")),
        "Haridwar" to listOf(AreaPin("Ranipur", "249407"), AreaPin("Kankhal", "249408"))
    ),
    "West Bengal" to mapOf(
        "Kolkata" to listOf(AreaPin("Salt Lake", "700091"), AreaPin("New Town", "700156"), AreaPin("Ballygunge", "700019"), AreaPin("Park Street", "700016")),
        "Howrah" to listOf(AreaPin("Shibpur", "711102"), AreaPin("Salkia", "711106")),
        "Siliguri" to listOf(AreaPin("Bidhan Road", "734001"), AreaPin("Sevoke Road", "734001"))
    ),
    "Andaman and Nicobar Islands" to mapOf("Port Blair" to listOf(AreaPin("Aberdeen Bazar", "744101"))),
    "Chandigarh" to mapOf("Chandigarh" to listOf(AreaPin("Sector 17", "160017"), AreaPin("Sector 35", "160035"), AreaPin("Sector 22", "160022"))),
    "Dadra and Nagar Haveli and Daman and Diu" to mapOf("Daman" to listOf(AreaPin("Nani Daman", "396210")), "Silvassa" to listOf(AreaPin("Tokarkhada", "396230"))),
    "Delhi" to mapOf(
        "New Delhi" to listOf(AreaPin("Connaught Place", "110001"), AreaPin("Vasant Vihar", "110057"), AreaPin("Dwarka", "110075"), AreaPin("Saket", "110017")),
        "North Delhi" to listOf(AreaPin("Model Town", "110009"), AreaPin("Pitampura", "110034")),
        "South Delhi" to listOf(AreaPin("Greater Kailash", "110048"), AreaPin("Hauz Khas", "110016")),
        "West Delhi" to listOf(AreaPin("Janakpuri", "110058"), AreaPin("Rajouri Garden", "110027")),
        "East Delhi" to listOf(AreaPin("Mayur Vihar", "110091"), AreaPin("Preet Vihar", "110092"))
    ),
    "Jammu and Kashmir" to mapOf(
        "Srinagar" to listOf(AreaPin("Lal Chowk", "190001"), AreaPin("Rajbagh", "190008")),
        "Jammu" to listOf(AreaPin("Gandhi Nagar", "180004"), AreaPin("Trikuta Nagar", "180012"))
    ),
    "Ladakh" to mapOf("Leh" to listOf(AreaPin("Main Bazaar", "194101"))),
    "Lakshadweep" to mapOf("Kavaratti" to listOf(AreaPin("Kavaratti Island", "682555"))),
    "Puducherry" to mapOf("Puducherry" to listOf(AreaPin("White Town", "605001"), AreaPin("Lawspet", "605008")))
)

private fun lookupPincode(pin: String): Triple<String, String, String>? {
    val cleanPin = pin.trim()
    if (cleanPin.length != 6) return null
    for ((st, cities) in locationHierarchy) {
        for ((ct, areas) in cities) {
            for (ap in areas) {
                if (ap.pinCode == cleanPin) {
                    return Triple(st, ct, ap.area)
                }
            }
        }
    }
    return null
}

@Composable
private fun CreateCommunityStarterScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: SocietyViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var currentStep by rememberSaveable { mutableStateOf(1) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(currentStep) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // Step 1: Basic Society Details & Location
    var communityName by rememberSaveable { mutableStateOf("") }
    var societyType by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var selectedCity by rememberSaveable { mutableStateOf("") }
    var customCity by rememberSaveable { mutableStateOf("") }
    var selectedArea by rememberSaveable { mutableStateOf("") }
    var customArea by rememberSaveable { mutableStateOf("") }
    var pinCode by rememberSaveable { mutableStateOf("") }
    var streetAddress by rememberSaveable { mutableStateOf("") }

    // Step 2: Structure Configuration
    var blockNames by rememberSaveable { mutableStateOf("") }
    var floorsPerTower by rememberSaveable { mutableStateOf("") }
    var flatsPerFloor by rememberSaveable { mutableStateOf("") }
    var numberingStyle by rememberSaveable { mutableStateOf("Standard (101, 102, 103...)") }

    var standaloneFloors by rememberSaveable { mutableStateOf("") }

    var phaseNames by rememberSaveable { mutableStateOf("") }
    var villaPrefix by rememberSaveable { mutableStateOf("") }
    var startingNumber by rememberSaveable { mutableStateOf("") }
    var totalVillaCount by rememberSaveable { mutableStateOf("") }

    var builderBuildingNames by rememberSaveable { mutableStateOf("") }
    var builderFloors by rememberSaveable { mutableStateOf("") }
    var unitPositionStyle by rememberSaveable { mutableStateOf("") }

    // Auto-generate flat inventory list under the hood
    val allGeneratedFlats = remember(
        societyType,
        blockNames,
        floorsPerTower,
        flatsPerFloor,
        numberingStyle,
        standaloneFloors,
        phaseNames,
        villaPrefix,
        startingNumber,
        totalVillaCount,
        builderBuildingNames,
        builderFloors,
        unitPositionStyle
    ) {
        buildList {
            when (societyType) {
                "Apartment Complex / Multi-Tower" -> {
                    val blocks = if (blockNames.isNotBlank()) blockNames.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
                    val floors = floorsPerTower.toIntOrNull() ?: 0
                    val perFloor = flatsPerFloor.toIntOrNull() ?: 0
                    val isGroundNumeric = numberingStyle.contains("Ground", ignoreCase = true)
                    for (block in blocks) {
                        for (floor in 1..floors) {
                            for (unit in 1..perFloor) {
                                val floorLabel = if (isGroundNumeric && floor == 1) "Ground Floor" else "Floor $floor"
                                val unitCode = if (isGroundNumeric && floor == 1) {
                                    "G%02d".format(unit)
                                } else {
                                    val effectiveFloor = if (isGroundNumeric) floor - 1 else floor
                                    "%d%02d".format(effectiveFloor, unit)
                                }
                                add(Triple(block, floorLabel, "$block-$unitCode"))
                            }
                        }
                    }
                }
                "Standalone / Single Apartment Building" -> {
                    val floors = standaloneFloors.toIntOrNull() ?: 0
                    val perFloor = flatsPerFloor.toIntOrNull() ?: 0
                    val isGroundNumeric = numberingStyle.contains("Ground", ignoreCase = true)
                    val block = "Main Building"
                    for (floor in 1..floors) {
                        for (unit in 1..perFloor) {
                            val floorLabel = if (isGroundNumeric && floor == 1) "Ground Floor" else "Floor $floor"
                            val unitCode = if (isGroundNumeric && floor == 1) {
                                "G%02d".format(unit)
                            } else {
                                val effectiveFloor = if (isGroundNumeric) floor - 1 else floor
                                "%d%02d".format(effectiveFloor, unit)
                            }
                            add(Triple(block, floorLabel, "Flat $unitCode"))
                        }
                    }
                }
                "Gated Community / Villa Layout" -> {
                    val phases = if (phaseNames.isNotBlank()) phaseNames.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
                    val prefix = villaPrefix.ifBlank { "Villa" }
                    val startNum = startingNumber.toIntOrNull() ?: 1
                    val totalVillas = totalVillaCount.toIntOrNull() ?: 0
                    if (phases.isNotEmpty() && totalVillas > 0) {
                        val villasPerPhase = maxOf(1, totalVillas / phases.size)
                        var currentVilla = startNum
                        for (phase in phases) {
                            val countInPhase = minOf(villasPerPhase, totalVillas - (currentVilla - startNum))
                            repeat(countInPhase) {
                                val villaNo = currentVilla++
                                add(Triple(phase, "Independent Villa", "$prefix $villaNo"))
                            }
                        }
                        while (size < totalVillas) {
                            val villaNo = currentVilla++
                            add(Triple(phases.firstOrNull() ?: "Phase 1", "Independent Villa", "$prefix $villaNo"))
                        }
                    }
                }
                "Builder Floor Society" -> {
                    val buildings = if (builderBuildingNames.isNotBlank()) builderBuildingNames.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
                    val floors = builderFloors.toIntOrNull() ?: 0
                    val isFrontRear = unitPositionStyle.contains("Front", ignoreCase = true)
                    val isLeftRight = unitPositionStyle.contains("Left", ignoreCase = true)
                    for (building in buildings) {
                        for (floor in 1..floors) {
                            val positions = when {
                                isFrontRear -> listOf("Front (F)", "Rear (R)")
                                isLeftRight -> listOf("Left (L)", "Right (R)")
                                unitPositionStyle.isNotBlank() -> listOf("Unit 1", "Unit 2")
                                else -> emptyList()
                            }
                            for (pos in positions) {
                                add(Triple(building, "Floor $floor", "$building - ${floor}${pos.take(1)}"))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    val activeUnitCodes = remember(allGeneratedFlats) {
        allGeneratedFlats.map { it.third }
    }

    // Step 3: Founding Admin Details & Flat Assignment (Auto-filled from Auth/Profile)
    val uiState by viewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(SocietyUiState()) }
    val profile = uiState.userProfile
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }

    var adminFirstName by rememberSaveable { mutableStateOf("") }
    var adminLastName by rememberSaveable { mutableStateOf("") }
    var signedInEmail by rememberSaveable { mutableStateOf("") }
    var adminMobile by rememberSaveable { mutableStateOf("") }
    var adminRole by rememberSaveable { mutableStateOf("") }
    var adminFlat by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(profile, currentUser) {
        val targetName = profile?.fullName?.takeIf { it.isNotBlank() } ?: currentUser?.displayName.orEmpty().ifBlank { "Admin User" }
        val nameParts = targetName.trim().split(" ").filter { it.isNotBlank() }
        if (adminFirstName.isBlank()) adminFirstName = nameParts.firstOrNull() ?: "Admin"
        if (adminLastName.isBlank()) adminLastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "User"

        val targetEmail = profile?.email?.takeIf { it.isNotBlank() } ?: currentUser?.email.orEmpty().ifBlank { "admin@nammanivasa.com" }
        if (signedInEmail.isBlank()) signedInEmail = targetEmail

        val targetPhone = profile?.phone?.takeIf { it.isNotBlank() } ?: currentUser?.phoneNumber.orEmpty()
        if (adminMobile.isBlank()) adminMobile = targetPhone
    }

    LaunchedEffect(activeUnitCodes) {
        if (adminFlat.isNotBlank() && adminFlat !in activeUnitCodes) {
            adminFlat = ""
        }
    }

    val statesList = locationHierarchy.keys.toList()
    val citiesMap: Map<String, List<AreaPin>> = if (state.isNotBlank()) locationHierarchy[state] ?: emptyMap() else emptyMap()
    val citiesList: List<String> = if (state.isNotBlank()) citiesMap.keys.toList() + OTHER_CITY else emptyList()
    val areaPins: List<AreaPin> = if (selectedCity.isNotBlank() && selectedCity != OTHER_CITY) (citiesMap[selectedCity] ?: emptyList()) else emptyList()
    val areaNames: List<String> = if (selectedCity.isNotBlank()) {
        if (selectedCity != OTHER_CITY) (areaPins.map { it.area } + OTHER_AREA) else listOf(OTHER_AREA)
    } else emptyList()

    var effectiveCity = if (selectedCity == OTHER_CITY) customCity else selectedCity
    var effectiveArea = if (selectedArea == OTHER_AREA) customArea else selectedArea

    LaunchedEffect(pinCode) {
        val cleanPin = pinCode.trim()
        if (cleanPin.length == 6) {
            val detected = lookupPincode(cleanPin)
            if (detected != null) {
                state = detected.first
                selectedCity = detected.second
                selectedArea = detected.third
            }
        }
    }

    val canNext1 = true
    val canNext2 = true
    val canNext3 = true
    val canRegister = true

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Register Community",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = InterFontFamily,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Set up your society details, structure, and founding admin in 4 simple steps.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = InterFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            // Step Indicator at Top (4 Steps)
            CommunityRegistrationProgressIndicator(currentStep = currentStep)
            Spacer(Modifier.height(24.dp))

            when (currentStep) {
                1 -> {
                    // Step 1: Basic Society Details & Location
                    Text(
                        text = "1. Basic Society Details & Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    NnTextField(
                        value = communityName,
                        onValueChange = { communityName = it },
                        label = "Society / Community Name",
                        placeholder = "",
                        singleLine = true
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleDropdown(
                        title = "Society Type",
                        selectedLabel = societyType,
                        placeholder = "Select...",
                        options = listOf(
                            "Apartment Complex / Multi-Tower",
                            "Standalone / Single Apartment Building",
                            "Gated Community / Villa Layout",
                            "Builder Floor Society"
                        ),
                        onOptionSelected = { societyType = it }
                    )
                    Spacer(Modifier.height(14.dp))
                    NnTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = "PIN Code (6 digits)",
                        placeholder = "",
                        singleLine = true,
                        keyboardType = KeyboardType.Number
                    )

                    if (state.isNotBlank() && effectiveCity.isNotBlank() && effectiveArea.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(
                                        text = "Location Auto-Detected",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = InterFontFamily
                                    )
                                    Text(
                                        text = "$effectiveArea, $effectiveCity, $state ($pinCode)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    NnTextField(
                        value = streetAddress,
                        onValueChange = { streetAddress = it },
                        label = "Street Address / Landmark",
                        placeholder = "",
                        singleLine = false
                    )
                }
                2 -> {
                    // Step 2: Structure Configuration
                    Text(
                        text = "2. Structure Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (societyType) {
                            "Apartment Complex / Multi-Tower" -> "Configure towers, floors, flats per floor, and numbering style."
                            "Standalone / Single Apartment Building" -> "Configure floors, flats per floor, and numbering style."
                            "Gated Community / Villa Layout" -> "Configure phases, villa prefix, starting number, and total villas."
                            "Builder Floor Society" -> "Configure building names, floors, and unit position naming."
                            else -> "Configure your building structure."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    when (societyType) {
                        "Apartment Complex / Multi-Tower" -> {
                            NnTextField(
                                value = blockNames,
                                onValueChange = { blockNames = it },
                                label = "Tower / Block Names",
                                placeholder = "",
                                singleLine = true
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = floorsPerTower,
                                        onValueChange = { floorsPerTower = it },
                                        label = "Floors per Tower",
                                        placeholder = "",
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = flatsPerFloor,
                                        onValueChange = { flatsPerFloor = it },
                                        label = "Flats per Floor",
                                        placeholder = "",
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            RoleDropdown(
                                title = "Flat Numbering Style",
                                selectedLabel = numberingStyle,
                                placeholder = "Select...",
                                options = listOf(
                                    "Standard (101, 102, 103...)",
                                    "Ground + Numeric (G01, G02, 101...)"
                                ),
                                onOptionSelected = { numberingStyle = it }
                            )
                        }
                        "Standalone / Single Apartment Building" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = standaloneFloors,
                                        onValueChange = { standaloneFloors = it },
                                        label = "Total Floors",
                                        placeholder = "",
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = flatsPerFloor,
                                        onValueChange = { flatsPerFloor = it },
                                        label = "Flats per Floor",
                                        placeholder = "",
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            RoleDropdown(
                                title = "Flat Numbering Style",
                                selectedLabel = numberingStyle,
                                placeholder = "Select...",
                                options = listOf(
                                    "Standard (101, 102, 103...)",
                                    "Ground + Numeric (G01, G02, 101...)"
                                ),
                                onOptionSelected = { numberingStyle = it }
                            )
                        }
                        "Gated Community / Villa Layout" -> {
                            NnTextField(
                                value = phaseNames,
                                onValueChange = { phaseNames = it },
                                label = "Phase / Sector Names",
                                placeholder = "",
                                singleLine = true
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = villaPrefix,
                                        onValueChange = { villaPrefix = it },
                                        label = "Villa Prefix",
                                        placeholder = "",
                                        singleLine = true
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    NnTextField(
                                        value = startingNumber,
                                        onValueChange = { startingNumber = it },
                                        label = "Starting Number",
                                        placeholder = "",
                                        singleLine = true,
                                        keyboardType = KeyboardType.Number
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            NnTextField(
                                value = totalVillaCount,
                                onValueChange = { totalVillaCount = it },
                                label = "Total Villa Count",
                                placeholder = "",
                                singleLine = true,
                                keyboardType = KeyboardType.Number
                            )
                        }
                        "Builder Floor Society" -> {
                            NnTextField(
                                value = builderBuildingNames,
                                onValueChange = { builderBuildingNames = it },
                                label = "Building / Plot Names",
                                placeholder = "",
                                singleLine = true
                            )
                            Spacer(Modifier.height(14.dp))
                            NnTextField(
                                value = builderFloors,
                                onValueChange = { builderFloors = it },
                                label = "Floors per Building",
                                placeholder = "",
                                singleLine = true,
                                keyboardType = KeyboardType.Number
                            )
                            Spacer(Modifier.height(14.dp))
                            RoleDropdown(
                                title = "Unit Position Naming",
                                selectedLabel = unitPositionStyle,
                                placeholder = "Select...",
                                options = listOf(
                                    "Front / Rear (1F, 1R)",
                                    "Left / Right (1L, 1R)",
                                    "Unit 1 / Unit 2"
                                ),
                                onOptionSelected = { unitPositionStyle = it }
                            )
                        }
                    }

                    if (activeUnitCodes.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Units will be automatically generated (${activeUnitCodes.size} units configured).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // Step 3: Founding Admin Details & Flat Assignment
                    Text(
                        text = "3. Founding Admin Details & Flat Assignment",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Provide founding admin details and select your assigned flat/villa.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            NnTextField(
                                value = adminFirstName,
                                onValueChange = { adminFirstName = it },
                                label = "First Name",
                                placeholder = "",
                                singleLine = true
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            NnTextField(
                                value = adminLastName,
                                onValueChange = { adminLastName = it },
                                label = "Last Name",
                                placeholder = "",
                                singleLine = true
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    NnTextField(
                        value = signedInEmail,
                        onValueChange = {},
                        label = "Email ID",
                        placeholder = "",
                        singleLine = true,
                        readOnly = true
                    )
                    Spacer(Modifier.height(14.dp))
                    NnTextField(
                        value = adminMobile,
                        onValueChange = { adminMobile = it },
                        label = "Mobile Number",
                        placeholder = "",
                        singleLine = true,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleDropdown(
                        title = "Role in Society",
                        selectedLabel = adminRole,
                        placeholder = "Select...",
                        options = listOf(
                            "President",
                            "Vice-President",
                            "Secretary",
                            "Joint Secretary",
                            "Treasurer",
                            "Joint Treasurer",
                            "Executive Committee Member",
                            "Resident Owner",
                            "Tenant"
                        ),
                        onOptionSelected = { adminRole = it }
                    )
                    Spacer(Modifier.height(14.dp))
                    RoleDropdown(
                        title = "Select Your Flat / Villa",
                        selectedLabel = adminFlat,
                        placeholder = "Select...",
                        options = if (activeUnitCodes.isNotEmpty()) activeUnitCodes else listOf("Complete Step 2 first"),
                        searchable = true,
                        onOptionSelected = { adminFlat = it }
                    )
                }
                4 -> {
                    // Step 4: Review & Confirm
                    Text(
                        text = "4. Review & Confirm",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Review all details below before registering and launching your community.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Society & Location Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ReviewRow(label = "Society Name", value = communityName)
                            ReviewRow(label = "Society Type", value = societyType)
                            ReviewRow(label = "State", value = state)
                            ReviewRow(label = "City", value = effectiveCity)
                            ReviewRow(label = "Area / Locality", value = effectiveArea)
                            ReviewRow(label = "PIN Code", value = pinCode)
                            ReviewRow(label = "Street Address", value = streetAddress)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Structure & Active Flat Count",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ReviewRow(label = "Structure Summary", value = when (societyType) {
                                "Apartment Complex / Multi-Tower" -> "$blockNames ($floorsPerTower floors, $flatsPerFloor flats/floor)"
                                "Standalone / Single Apartment Building" -> "Standalone Building ($standaloneFloors floors, $flatsPerFloor flats/floor)"
                                "Gated Community / Villa Layout" -> "$phaseNames ($totalVillaCount villas)"
                                "Builder Floor Society" -> "$builderBuildingNames ($builderFloors floors)"
                                else -> "Standard Structure"
                            })
                            ReviewRow(label = "Total Units", value = "${activeUnitCodes.size} Units")
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0EE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Founding Admin Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ReviewRow(label = "Admin Name", value = "$adminFirstName $adminLastName")
                            ReviewRow(label = "Email ID", value = signedInEmail)
                            ReviewRow(label = "Mobile Number", value = adminMobile)
                            ReviewRow(label = "Role in Society", value = adminRole)
                            ReviewRow(label = "Assigned Flat", value = adminFlat)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NnSecondaryButton(
                    text = "Back",
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (currentStep > 1) {
                            currentStep -= 1
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (currentStep < 4) {
                    NnPrimaryButton(
                        text = "Next ➔",
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            when (currentStep) {
                                1 -> if (canNext1) currentStep += 1 else Toast.makeText(context, "Please complete all basic society details & location.", Toast.LENGTH_SHORT).show()
                                2 -> if (canNext2) currentStep += 1 else Toast.makeText(context, "Please configure building structure and numbering style.", Toast.LENGTH_SHORT).show()
                                3 -> if (canNext3) currentStep += 1 else Toast.makeText(context, "Please complete admin details and select your flat.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        if (!uiState.submitError.isNullOrBlank()) {
                            Text(
                                text = uiState.submitError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        NnPrimaryButton(
                            text = "Register",
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (viewModel != null) {
                                    viewModel.registerCommunity(
                                        name = communityName.ifBlank { "My Community" },
                                        societyType = societyType.ifBlank { "Apartment Complex" },
                                        state = state.ifBlank { "Karnataka" },
                                        city = effectiveCity.ifBlank { "Bengaluru" },
                                        area = effectiveArea.ifBlank { "HSR Layout" },
                                        pinCode = pinCode.ifBlank { "560102" },
                                        streetAddress = streetAddress.ifBlank { "Main Road" },
                                        buildingName = adminFlat.ifBlank { "Tower A" },
                                        floors = floorsPerTower.toIntOrNull() ?: standaloneFloors.toIntOrNull() ?: builderFloors.toIntOrNull() ?: 10,
                                        flatNumber = adminFlat.ifBlank { "A-101" },
                                        onSuccess = { onCreated() }
                                    )
                                } else {
                                    onCreated()
                                }
                            },
                            loading = uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CommunityRegistrationProgressIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf("1. Details", "2. Structure", "3. Admin", "4. Review")
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val stepNum = index + 1
                val active = stepNum <= currentStep
                val current = stepNum == currentStep
                ProgressNode(label = stepNum.toString(), active = active, current = current)
                if (index < steps.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        color = if (stepNum < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            steps.forEachIndexed { index, label ->
                val stepNum = index + 1
                val active = stepNum == currentStep
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NnBottomSheetSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    searchable: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else options.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            if (searchable || options.size > 8) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    singleLine = true,
                    placeholder = { Text("Search $title...", fontFamily = InterFontFamily) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = Color(0xFFF5F0EE),
                        unfocusedContainerColor = Color(0xFFF5F0EE),
                        errorContainerColor = Color(0xFFF5F0EE)
                    )
                )
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (filteredOptions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No options found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                } else {
                    items(filteredOptions) { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOptionSelected(option)
                                    onDismissRequest()
                                },
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
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleDropdown(
    title: String,
    selectedLabel: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    searchable: Boolean = false
) {
    var sheetOpen by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = InterFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                sheetOpen = true
            },
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
                val textToShow = selectedLabel.ifBlank { placeholder.ifBlank { "Select option..." } }
                val textColor = if (selectedLabel.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                Text(textToShow, style = MaterialTheme.typography.bodyLarge, fontFamily = InterFontFamily, color = textColor)
                Icon(Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (sheetOpen) {
            NnBottomSheetSelector(
                title = title,
                options = options,
                selectedOption = selectedLabel,
                onOptionSelected = onOptionSelected,
                onDismissRequest = { sheetOpen = false },
                searchable = searchable || options.size > 8
            )
        }
    }
}



/**
 * Slim, non-blocking notice shown while the repository is serving local demo data
 * (backend unreachable, or no backend account for this user). It exists so that
 * fabricated content is never mistaken for verified backend data.
 */
@Composable
private fun NnDemoDataBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Demo data \u2014 backend not connected. Changes stay on this device.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Shown when the user has no active community. Without it, every community-scoped
 * read is skipped and the tabs would render as unexplained empty lists.
 */
@Composable
private fun NoCommunityPrompt(
    communities: List<CommunityMembershipInfo>,
    onSelect: (String) -> Unit,
    onOpenCommunities: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Apartment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose a community",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your bills, notices and visitors appear once a community is selected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (communities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(communities) { membership ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(membership.communityId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = membership.communityId,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = membership.roles.joinToString().ifBlank { "Resident" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            NnStatusBadge(
                                text = membership.membershipStatus,
                                status = statusTypeFrom(membership.membershipStatus)
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onOpenCommunities) { Text("View my communities") }
        }
    }
}

/**
 * Resolves the effective [PermissionCode] set for the signed-in user's active
 * community membership using the shared domain role catalog. The backend remains
 * authoritative; this only drives what the UI chooses to surface.
 */
private fun effectivePermissions(state: SocietyUiState): Set<PermissionCode> {
    val activeId = state.activeCommunityId
    val membership = state.communities.firstOrNull { it.communityId == activeId }
        ?: state.userProfile?.activeCommunities?.firstOrNull { it.communityId == activeId }
        ?: state.userProfile?.activeCommunities?.firstOrNull()
    val roles = membership?.roles ?: emptyList()
    return roles.mapNotNull { roleName ->
        RoleCode.entries.firstOrNull { it.name.equals(roleName, ignoreCase = true) }
    }.flatMap { RoleDefinitions.defaultPermissionsFor(it) }.toSet()
}

/** True when the user holds [code] in their active community. */
private fun SocietyUiState.hasPermission(code: PermissionCode): Boolean =
    effectivePermissions(this).contains(code)

/** True when the user is entitled to see personal financial records (bills/payments). */
private fun SocietyUiState.canViewPersonalFinance(): Boolean =
    hasPermission(PermissionCode.READ_PERSONAL_FINANCIAL_RECORDS)

/** True when the user may raise maintenance requests. */
private fun SocietyUiState.canCreateMaintenance(): Boolean =
    hasPermission(PermissionCode.CREATE_MAINTENANCE_REQUEST)

/** True when the user may approve visitors at the gate. */
private fun SocietyUiState.canApproveVisitors(): Boolean =
    hasPermission(PermissionCode.APPROVE_VISITOR)

/** Human-friendly role label for the active membership, e.g. "Owner", "Tenant". */
private fun SocietyUiState.primaryRoleLabel(): String {
    val activeId = activeCommunityId
    val membership = communities.firstOrNull { it.communityId == activeId }
        ?: userProfile?.activeCommunities?.firstOrNull { it.communityId == activeId }
        ?: userProfile?.activeCommunities?.firstOrNull()
    val role = membership?.roles?.firstOrNull()?.let { name ->
        RoleCode.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    } ?: return "Resident"
    return role.name.lowercase().split('_')
        .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
}

/**
 * The permanent Home hero — Namma Nivasa's signature architectural header, shown
 * on every visit (not a one-time onboarding screen). A full-bleed burgundy gradient
 * with a curved lower edge that dissolves into the warm-white content below. The
 * top row carries a large "Namma Nivasa" wordmark with the Bubbly sparkle and a
 * notification bell (with unread dot). Below it, the resident's circular profile
 * picture — their photo when the profile provides one, otherwise a typographic
 * monogram, never a fabricated photo — sits beside the active community shown
 * uppercase with a switcher chevron, a time-aware greeting, the resident's full
 * name, and the unit line ("Tower B • Flat 1004") with the membership role in a
 * pill. A subtle abstract building silhouette — a decorative motif — is blended
 * into the lower-right. Every value derives from the authenticated profile and
 * selected community; nothing is hardcoded.
 */
@Composable
private fun NnSignatureWelcome(
    state: SocietyUiState,
    onSwitchCommunity: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenBubbly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullName = state.userProfile?.fullName?.trim().orEmpty()
    val greeting = remember { greetingForNow() }
    val communityName = state.activeCommunity?.name
    val roleLabel = state.primaryRoleLabel()
    val unitLabel = unitLabelFor(
        buildingName = state.userProfile?.buildingName,
        flatNumber = state.userProfile?.flatNumber
    )
    val canSwitch = state.communities.size > 1
    var menuOpen by remember { mutableStateOf(false) }

    // A single gentle lower curve: the burgundy dips a touch lower at the centre,
    // letting it melt into the warm-white content. The depth scales with width so
    // the treatment stays responsive across screen sizes. Implemented directly via
    // [Shape] so the curve is derived from the measured size.
    val heroShape = remember {
        object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                val path = Path().apply {
                    val w = size.width
                    val h = size.height
                    val dip = w * 0.045f
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h - dip)
                    quadraticTo(w * 0.5f, h + dip, 0f, h - dip)
                    close()
                }
                return Outline.Generic(path)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(heroShape)
            .background(
                brush = Brush.linearGradient(
                    listOf(BrandGradientStart, BrandGradientEnd)
                )
            )
    ) {
        // Abstract architectural silhouette blended into the lower-right. Decorative
        // only, and deliberately low-contrast so it reads as texture — not as a real
        // community photograph, which we never fabricate.
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.End),
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(
                22.dp to 64.dp,
                16.dp to 104.dp,
                28.dp to 84.dp,
                18.dp to 132.dp,
                24.dp to 72.dp,
                20.dp to 96.dp
            ).forEach { (buildingWidth, buildingHeight) ->
                Box(
                    modifier = Modifier
                        .size(width = buildingWidth, height = buildingHeight)
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 44.dp)
        ) {
            // Wordmark + Bubbly sparkle / notification actions. The bell carries a
            // small unread dot as in the design.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Namma Nivasa",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenBubbly, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Bubbly",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onOpenNotifications, modifier = Modifier.size(40.dp)) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            // Unread notification indicator.
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NotificationDot)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile row: the resident's circular picture beside the active
            // community (uppercase, with a switcher chevron), a time-aware
            // greeting, the full name, and the unit line with the role pill.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NnProfileAvatar(fullName = fullName)

                Column(modifier = Modifier.weight(1f)) {
                    // Active community. The chevron opens the switcher when the
                    // resident belongs to several communities.
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (canSwitch) {
                                Modifier.clickable { menuOpen = true }
                            } else {
                                Modifier
                            }
                        ) {
                            Text(
                                text = (communityName ?: "Namma Nivasa").uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.4.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = if (canSwitch) "Switch community" else null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (canSwitch) {
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                state.communities.forEach { membership ->
                                    val isActive = membership.communityId == state.activeCommunityId
                                    DropdownMenuItem(
                                        text = { Text(membership.communityId.take(8)) },
                                        trailingIcon = {
                                            if (isActive) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Active"
                                                )
                                            }
                                        },
                                        onClick = {
                                            menuOpen = false
                                            onSwitchCommunity(membership.communityId)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    if (fullName.isNotBlank()) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Unit line ("Tower B • Flat 1004") with the membership role
                    // pill. Parts hidden when the profile does not provide them.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (unitLabel != null) {
                            Icon(
                                imageVector = Icons.Filled.Apartment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = unitLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (roleLabel.isNotBlank()) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Circular resident profile picture for the Home hero. Shows a typographic
 * monogram on a light disc — a photo is never fabricated. A white
 * ring separates it from the burgundy header, and a small green dot at the
 * lower edge signals an active profile, as in the design.
 */
@Composable
private fun NnProfileAvatar(
    fullName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(78.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsFor(fullName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Primary
            )
        }
        // Presence indicator.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .clip(CircleShape)
                .background(PresenceActive)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}

/** Time-aware greeting ("Good morning" / "Good afternoon" / "Good evening"). */
private fun greetingForNow(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

/** "Tower B • Flat 1004" from the resident's unit details; null when unknown. */
private fun unitLabelFor(buildingName: String?, flatNumber: String?): String? {
    val building = buildingName?.trim()?.takeIf { it.isNotBlank() }
    val flat = flatNumber?.trim()?.takeIf { it.isNotBlank() }?.let { "Flat $it" }
    return listOfNotNull(building, flat).takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

/** Two-letter monogram ("RS") used when the profile has no photo. */
private fun initialsFor(fullName: String): String {
    val words = fullName.trim().split(' ').filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(2)
        else -> "${words.first().first()}${words.last().first()}"
    }.uppercase()
}

/**
 * A tappable list row that opens one of the reusable full-screen destinations.
 */
@Composable
private fun NnQuickAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Renders loading / error states for a tab before its content is shown.
 */
@Composable
private fun TabStateHost(
    isLoading: Boolean,
    error: RepoError?,
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        isLoading -> NnLoadingState()
        error != null -> NnErrorState(message = error.userMessage(), onRetry = onRetry)
        else -> content()
    }
}


/**
 * Home tab — the resident's daily landing experience.
 *
 * Structure (in order): the permanent burgundy architectural hero; a
 * permission-aware "Your attention" summary of real, actionable items;
 * four quick actions; a horizontal carousel of scheduled community notices;
 * and a restrained Bubbly
 * entry point. Every figure comes from the repository and respects the active
 * membership's permissions. No society-wide financial totals are shown, and no
 * data is fabricated.
 */
@Composable
private fun HomeTab(
    uiState: SocietyUiState,
    viewModel: SocietyViewModel,
    openSub: (SubScreen) -> Unit,
    onNavigateTab: (AppTab) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Shared with the hero's sparkle action so it can open the Bubbly entry point
    // below and scroll to it — real navigation to existing functionality.
    var bubblyExpanded by remember { mutableStateOf(false) }

    TabStateHost(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = { viewModel.refresh() }
    ) {
        val me = uiState.userProfile?.id
        val canFinance = uiState.canViewPersonalFinance()
        val canMaintenance = uiState.canCreateMaintenance()
        val canVisitors = uiState.canApproveVisitors()

        // Each section keeps its own status, so a failed request degrades only its
        // own row instead of blanking the whole tab. A failed financial read is
        // deliberately NOT treated as "nothing due".
        val unpaidBills = if (canFinance) {
            uiState.bills.data.filter { !it.status.equals("Paid", true) }
        } else {
            emptyList()
        }
        val awaitingVerification = if (canFinance) {
            uiState.myPayments.data.filter { it.payerUserId == me && it.status.equals("PENDING", true) }
        } else {
            emptyList()
        }
        val pendingVisitors = if (canVisitors) {
            uiState.visitors.data.filter { it.status.equals("Pending", true) }
        } else {
            emptyList()
        }
        val openMaintenance = if (canMaintenance) {
            uiState.maintenanceRequests.data.filter {
                it.createdByUserId == me &&
                    (it.status.equals("OPEN", true) || it.status.equals("IN_PROGRESS", true))
            }
        } else {
            emptyList()
        }

        // Published notices with real schedules power the "Happening" carousel;
        // soonest-first, capped so Home stays scannable.
        val upcomingNotices = uiState.notices.data
            .filter { it.status.equals("PUBLISHED", true) }
            .sortedBy { it.startsAt ?: it.createdAt }
            .take(6)

        // Surface a failed section on Home rather than silently reporting zero.
        // A failed financial read must never read as "nothing due".
        val homeError = when {
            canFinance && uiState.bills.status.error != null -> uiState.bills.status.error
            canFinance && uiState.myPayments.status.error != null -> uiState.myPayments.status.error
            canVisitors && uiState.visitors.status.error != null -> uiState.visitors.status.error
            canMaintenance && uiState.maintenanceRequests.status.error != null ->
                uiState.maintenanceRequests.status.error
            else -> null
        }

        val attentionTotal =
            unpaidBills.size + awaitingVerification.size + pendingVisitors.size + openMaintenance.size

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // Top padding must stay 0: the hero is the first item and has to sit
            // flush under the burgundy status-bar strip. Any top gap lets the
            // Scaffold's background show through as a thin white line between
            // the strip and the hero.
            contentPadding = PaddingValues(top = 0.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                NnSignatureWelcome(
                    state = uiState,
                    onSwitchCommunity = viewModel::selectCommunity,
                    onOpenNotifications = { onNavigateTab(AppTab.Community) },
                    onOpenBubbly = {
                        bubblyExpanded = true
                        scope.launch {
                            val lastIndex = listState.layoutInfo.totalItemsCount - 1
                            if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
                        }
                    }
                )
            }

            // --- Your attention (grouped, compact rows) ---
            item {
                NnSectionHeader(
                    title = "Your attention",
                    actionText = if (attentionTotal > 0) "See all" else null,
                    onActionClick = if (attentionTotal > 0) {
                        { onNavigateTab(AppTab.Payments) }
                    } else {
                        null
                    }
                )
            }

            if (attentionTotal == 0) {
                item { NnCaughtUpCard() }
            } else {
                item {
                    NnAttentionGroup(
                        paymentsCount = unpaidBills.size + awaitingVerification.size,
                        paymentsSubtitle = paymentsSubtitle(unpaidBills.size, awaitingVerification.size),
                        paymentsUrgent = unpaidBills.any { it.status.equals("Urgent", true) },
                        visitorsCount = pendingVisitors.size,
                        visitorsSubtitle = pendingVisitors.firstOrNull()
                            ?.let { "${it.name} • ${it.purpose}" },
                        maintenanceCount = openMaintenance.size,
                        maintenanceSubtitle = openMaintenance.firstOrNull()?.title,
                        onPayments = { onNavigateTab(AppTab.Payments) },
                        onVisitors = { openSub(SubScreen.ManagementDashboard) },
                        onMaintenance = { openSub(SubScreen.MaintenanceRequest) }
                    )
                }
            }

            // A failed section is reported on Home too, so an outage is never
            // presented as "all caught up".
            if (homeError != null) {
                item {
                    NnInlineError(
                        message = homeError.userMessage(),
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // --- Quick actions ---
            item {
                NnSectionHeader(title = "Quick actions")
            }
            item {
                NnQuickActionsRow(
                    canFinance = canFinance,
                    canMaintenance = canMaintenance,
                    canVisitors = canVisitors,
                    onPayDues = { onNavigateTab(AppTab.Payments) },
                    onRaiseRequest = { openSub(SubScreen.MaintenanceRequest) },
                    onManageVisitors = { openSub(SubScreen.ManagementDashboard) },
                    onViewNotices = { onNavigateTab(AppTab.Community) }
                )
            }

            // --- Happening in your community (scheduled notices carousel) ---
            if (upcomingNotices.isNotEmpty()) {
                item {
                    NnSectionHeader(
                        title = "Happening in your community",
                        actionText = "See all",
                        onActionClick = { onNavigateTab(AppTab.Community) }
                    )
                }
                item {
                    NnCommunityEvents(
                        notices = upcomingNotices,
                        onClick = { onNavigateTab(AppTab.Community) }
                    )
                }
            }

            // --- Bubbly assistant entry point ---
            item {
                NnBubblyEntry(
                    expanded = bubblyExpanded,
                    onExpandedChange = { bubblyExpanded = it },
                    onRaiseRequest = { openSub(SubScreen.MaintenanceRequest) },
                    onPayDues = { onNavigateTab(AppTab.Payments) },
                    onViewNotices = { onNavigateTab(AppTab.Community) },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/** Builds an accurate, non-misleading payments subtitle from real counts. */
private fun paymentsSubtitle(unpaid: Int, awaiting: Int): String = when {
    unpaid > 0 && awaiting > 0 ->
        "$unpaid to pay • $awaiting awaiting confirmation"
    unpaid > 0 ->
        if (unpaid == 1) "1 bill to pay" else "$unpaid bills to pay"
    awaiting > 0 ->
        if (awaiting == 1) "1 awaiting confirmation" else "$awaiting awaiting confirmation"
    else -> "Nothing due"
}


/**
 * Calm empty state shown when the resident has nothing pending. Uses a soft,
 * tonal surface with a subtle border rather than a colored card.
 */
@Composable
private fun NnCaughtUpCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "You're all caught up",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "No pending actions right now.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * The "Your attention" group: a single white inset surface containing compact,
 * premium rows for each actionable category. Rows carry a consistent icon
 * container, a concise title, an accurate status line, and a count only when
 * real data supports it. Only categories with real, permitted items are shown,
 * so the group never contains empty filler. A single urgent item is emphasised
 * via a burgundy count so not everything looks equally urgent.
 */
@Composable
private fun NnAttentionGroup(
    paymentsCount: Int,
    paymentsSubtitle: String,
    paymentsUrgent: Boolean,
    visitorsCount: Int,
    visitorsSubtitle: String?,
    maintenanceCount: Int,
    maintenanceSubtitle: String?,
    onPayments: () -> Unit,
    onVisitors: () -> Unit,
    onMaintenance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            if (paymentsCount > 0) {
                NnAttentionRow(
                    icon = Icons.Filled.Payments,
                    title = "Payments",
                    subtitle = paymentsSubtitle,
                    count = paymentsCount,
                    emphasize = paymentsUrgent,
                    onClick = onPayments
                )
            }
            if (visitorsCount > 0) {
                if (paymentsCount > 0) NnAttentionDivider()
                NnAttentionRow(
                    icon = Icons.Filled.Groups,
                    title = "Visitors",
                    subtitle = visitorsSubtitle ?: "Approvals waiting",
                    count = visitorsCount,
                    emphasize = false,
                    onClick = onVisitors
                )
            }
            if (maintenanceCount > 0) {
                if (paymentsCount > 0 || visitorsCount > 0) NnAttentionDivider()
                NnAttentionRow(
                    icon = Icons.Filled.Build,
                    title = "Maintenance",
                    subtitle = maintenanceSubtitle ?: "Request in progress",
                    count = maintenanceCount,
                    emphasize = false,
                    onClick = onMaintenance
                )
            }
        }
    }
}

/** A hairline divider between attention rows inside the [NnAttentionGroup]. */
@Composable
private fun NnAttentionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 70.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}

/**
 * A single compact attention row: a soft-rose rounded icon container with a
 * burgundy icon, a concise title and status line, an optional count badge, and a
 * chevron affordance. Touch target meets accessibility minimums.
 */
@Composable
private fun NnAttentionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    emphasize: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                // Truncation keeps long subtitles from colliding with the count
                // badge or the chevron on narrower screens.
                .padding(end = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        NnCountBadge(count = count, emphasize = emphasize)
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A small count badge. Emphasised (urgent) counts are filled burgundy; otherwise
 * a restrained soft-rose pill. The number is always paired with the row's text so
 * meaning never depends on colour alone.
 */
@Composable
private fun NnCountBadge(count: Int, emphasize: Boolean, modifier: Modifier = Modifier) {
    val bg = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val fg = if (emphasize) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(ShapeFull))
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}


/**
 * A compact, evenly-spaced row of quick-action tiles. Actions are permission-gated
 * so the resident only ever sees entries they can actually use. Uses a consistent
 * tonal circular icon treatment with a short label beneath.
 */
@Composable
private fun NnQuickActionsRow(
    canFinance: Boolean,
    canMaintenance: Boolean,
    canVisitors: Boolean,
    onPayDues: () -> Unit,
    onRaiseRequest: () -> Unit,
    onManageVisitors: () -> Unit,
    onViewNotices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canFinance) {
            NnQuickActionTile(
                icon = Icons.Filled.Payments,
                label = "Pay dues",
                onClick = onPayDues,
                modifier = Modifier.weight(1f)
            )
        }
        if (canMaintenance) {
            NnQuickActionTile(
                icon = Icons.Filled.Build,
                label = "Request",
                onClick = onRaiseRequest,
                modifier = Modifier.weight(1f)
            )
        }
        if (canVisitors) {
            NnQuickActionTile(
                icon = Icons.Filled.Groups,
                label = "Visitors",
                onClick = onManageVisitors,
                modifier = Modifier.weight(1f)
            )
        }
        NnQuickActionTile(
            icon = Icons.Filled.Campaign,
            label = "Notices",
            onClick = onViewNotices,
            modifier = Modifier.weight(1f)
        )
    }
}

/** A single compact quick-action tile: tonal circular icon over a short label. */
@Composable
private fun NnQuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            // A shared fixed height keeps all four quick-action tiles aligned,
            // even when one permission-gated label wraps to two lines.
            .height(112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Horizontally scrolling strip of scheduled community notices on Home. Each card
 * shows a brand-tinted cover with a calendar badge (month + day parsed from the
 * notice's real schedule, never fabricated), the notice title, and the time
 * window when the data provides one. The cover deliberately uses the brand
 * gradient instead of stock photos — community imagery is never fabricated.
 */
@Composable
private fun NnCommunityEvents(
    notices: List<NoticeItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notices, key = { it.id }) { notice ->
            NnEventCard(notice = notice, onClick = onClick)
        }
    }
}

/** One event-style card in the Home "Happening" carousel. */
@Composable
private fun NnEventCard(
    notice: NoticeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheduleIso = notice.startsAt ?: notice.createdAt
    val badge = remember(scheduleIso) { eventBadgeParts(scheduleIso) }
    val timeLabel = remember(notice.startsAt, notice.endsAt) {
        eventTimeRange(notice.startsAt, notice.endsAt)
    }

    Card(
        modifier = modifier
            .width(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            // Brand cover with the calendar badge overlaid top-start.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(BrandGradientStart, BrandGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(56.dp)
                )
                if (badge != null) {
                    val (month, day) = badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Primary
                            )
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (timeLabel != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** ISO-8601 pattern matching repository timestamps, always suffixed "Z" (UTC). */
private const val PATTERN_ISO_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"

/**
 * Parses an ISO-8601 UTC timestamp (e.g. "2024-01-15T10:00:00Z") into a [Date],
 * returning null when unparseable so we never show a fabricated date.
 */
private fun parseIsoUtc(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    return try {
        SimpleDateFormat(PATTERN_ISO_UTC, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(iso)
    } catch (e: ParseException) {
        null
    }
}

/**
 * Uppercase month + day parts for an event card's calendar badge, rendered in
 * the device's local timezone. Null when the timestamp is unparseable.
 */
private fun eventBadgeParts(iso: String?): Pair<String, String>? {
    val date = parseIsoUtc(iso) ?: return null
    val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase()
    val day = SimpleDateFormat("d", Locale.getDefault()).format(date)
    return month to day
}

/** "6:00 PM - 9:00 PM" from the notice schedule; null when either end is unknown. */
private fun eventTimeRange(startIso: String?, endIso: String?): String? {
    val start = parseIsoUtc(startIso) ?: return null
    val end = parseIsoUtc(endIso) ?: return null
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return "${format.format(start)} - ${format.format(end)}"
}


/**
 * Bubbly — Namma Nivasa's contextual assistant entry point. Deliberately NOT a
 * bottom-navigation tab and NOT a fake AI: it is a compact, elegant launcher on a
 * pale-rose surface that expands to reveal real, functional shortcuts to existing
 * screens. It never bypasses backend permissions — consequential actions still go
 * through their normal authorized destinations.
 */
@Composable
private fun NnBubblyEntry(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRaiseRequest: () -> Unit,
    onPayDues: () -> Unit,
    onViewNotices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "bubblyChevronRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ask Bubbly",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "How can I help you today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse assistant" else "Expand assistant",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NnBubblyShortcut(
                        icon = Icons.Filled.Build,
                        label = "Raise a maintenance request",
                        onClick = onRaiseRequest
                    )
                    NnBubblyShortcut(
                        icon = Icons.Filled.Payments,
                        label = "Pay my dues",
                        onClick = onPayDues
                    )
                    NnBubblyShortcut(
                        icon = Icons.Filled.Campaign,
                        label = "Read community notices",
                        onClick = onViewNotices
                    )
                }
            }
        }
    }
}

/** A single tappable shortcut row inside the [NnBubblyEntry] launcher. */
@Composable
private fun NnBubblyShortcut(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


/**
 * Payments tab: outstanding bills, recent payments and financial tooling.
 */
@Composable
private fun PaymentsTab(
    uiState: SocietyUiState,
    viewModel: SocietyViewModel,
    openSub: (SubScreen) -> Unit
) {
    TabStateHost(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                NnSectionHeader(title = "Bills")
            }

            nnSection(
                section = uiState.bills,
                emptyIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                emptyTitle = "No bills",
                // Shown only after a successful response confirms there are none.
                emptyMessage = "You have no outstanding bills right now.",
                onRetry = { viewModel.refresh() }
            ) { bills ->
                items(bills) { bill ->
                    NnBillCard(
                        bill = bill,
                        isSubmitting = uiState.isSubmitting,
                        onPay = {
                            viewModel.createPayment(
                                RepoCreatePaymentRequest(
                                    billId = bill.id,
                                    amount = bill.amount.filter { it.isDigit() || it == '.' }
                                )
                            )
                        }
                    )
                }
            }

            item {
                NnSectionHeader(title = "Payments")
            }

            nnSection(
                section = uiState.myPayments,
                emptyIcon = Icons.Filled.Payments,
                emptyTitle = "No payments",
                emptyMessage = "Your submitted payments will show up here.",
                onRetry = { viewModel.refresh() }
            ) { payments ->
                items(payments) { payment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${payment.currency} ${payment.amount}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bill: ${payment.billId ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            NnStatusBadge(
                                text = payment.status,
                                status = statusTypeFrom(payment.status)
                            )
                        }
                    }
                }
            }

            item {
                NnSectionHeader(title = "Financial tools")
            }
            item {
                NnQuickAction(
                    icon = Icons.Filled.Lock,
                    title = "Financial Access",
                    subtitle = "Control who can view your financial records",
                    onClick = { openSub(SubScreen.FinancialAccess) }
                )
            }
            item {
                NnQuickAction(
                    icon = Icons.Filled.Payments,
                    title = "Verify Payments",
                    subtitle = "Review and verify pending payments",
                    onClick = { openSub(SubScreen.PaymentVerification) }
                )
            }
        }
    }
}


/** Card showing a single bill with an inline "Pay" action. */
@Composable
private fun NnBillCard(
    bill: BillItem,
    isSubmitting: Boolean,
    onPay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Due ${bill.due}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = bill.amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NnStatusBadge(text = bill.status, status = statusTypeFrom(bill.status))
                Button(
                    onClick = onPay,
                    enabled = !isSubmitting
                ) {
                    Text("Pay")
                }
            }
        }
    }
}

/** Maps a backend status string onto the shared [StatusType] badge style. */
private fun statusTypeFrom(status: String): StatusType = when (status.uppercase()) {
    "PAID", "ACTIVE", "RESOLVED", "PUBLISHED", "APPROVED" -> StatusType.Success
    "PENDING", "DRAFT", "OPEN", "IN_PROGRESS", "PARTIALLY_PAID" -> StatusType.Pending
    "OVERDUE", "REJECTED", "REVOKED", "VOID", "CANCELLED" -> StatusType.Error
    else -> StatusType.Info
}


/**
 * Community tab: notices feed, tickets, visitors and community tooling.
 */
@Composable
private fun CommunityTab(
    uiState: SocietyUiState,
    openSub: (SubScreen) -> Unit
) {
    TabStateHost(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRetry = { openSub(SubScreen.None) }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                NnSectionHeader(title = "Notices")
            }

            if (uiState.notices.data.isEmpty()) {
                item {
                    NnEmptyState(
                        icon = Icons.Filled.Notifications,
                        title = "No notices",
                        message = "Community notices will appear here."
                    )
                }
            } else {
                items(uiState.notices.data) { notice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                NnStatusBadge(
                                    text = notice.status,
                                    status = statusTypeFrom(notice.status)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = notice.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                NnSectionHeader(title = "Service tickets")
            }

            if (uiState.tickets.data.isEmpty()) {
                item {
                    NnEmptyState(
                        icon = Icons.Filled.ConfirmationNumber,
                        title = "No tickets",
                        message = "Service tickets raised by the community show here."
                    )
                }
            } else {
                items(uiState.tickets.data) { ticket ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ticket.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${ticket.category} • ${ticket.priority} priority",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            NnStatusBadge(
                                text = ticket.status,
                                status = statusTypeFrom(ticket.status)
                            )
                        }
                    }
                }
            }


            item {
                NnSectionHeader(title = "Community tools")
            }
            item {
                NnQuickAction(
                    icon = Icons.Filled.Campaign,
                    title = "Manage Notices",
                    subtitle = "Create and publish community notices",
                    onClick = { openSub(SubScreen.NoticeManagement) }
                )
            }
            item {
                NnQuickAction(
                    icon = Icons.Filled.Build,
                    title = "Maintenance",
                    subtitle = "Raise a new maintenance request",
                    onClick = { openSub(SubScreen.MaintenanceRequest) }
                )
            }
            item {
                NnQuickAction(
                    icon = Icons.Filled.Dashboard,
                    title = "Management Dashboard",
                    subtitle = "Visitors, payments and draft notices",
                    onClick = { openSub(SubScreen.ManagementDashboard) }
                )
            }
        }
    }
}


/**
 * More tab: profile summary, community switcher, emergency contacts and tools.
 */
@Composable
private fun MoreTab(
    uiState: SocietyUiState,
    viewModel: SocietyViewModel,
    openSub: (SubScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            NnSectionHeader(title = "Profile")
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = uiState.userProfile?.fullName ?: "Guest",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.userProfile?.email
                                ?: uiState.userProfile?.phone
                                ?: "Signed in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            NnSectionHeader(title = "Your communities")
        }
        if (uiState.communities.isEmpty()) {
            item {
                NnEmptyState(
                    icon = Icons.Filled.Apartment,
                    title = "No communities",
                    message = "You are not part of any community yet."
                )
            }
        } else {
            items(uiState.communities) { membership ->
                val isActive = membership.communityId == uiState.activeCommunityId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.selectCommunity(membership.communityId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = membership.communityId,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = membership.roles.joinToString().ifBlank { "Resident" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NnStatusBadge(
                            text = membership.membershipStatus,
                            status = statusTypeFrom(membership.membershipStatus)
                        )
                    }
                }
            }
        }


        item {
            NnSectionHeader(title = "Emergency contacts")
        }
        if (uiState.emergencyContacts.data.isEmpty()) {
            item {
                // Distinguish "none recorded" from "could not load": only the former
                // gets the reassuring empty copy. Capture the error locally because
                // uiState is a delegated property and cannot be smart cast.
                val contactsError = uiState.emergencyContacts.status.error
                if (contactsError != null) {
                    NnErrorState(
                        message = contactsError.userMessage(),
                        onRetry = { viewModel.refresh() }
                    )
                } else {
                    NnEmptyState(
                        icon = Icons.Filled.Phone,
                        title = "No emergency contacts",
                        message = "Emergency contacts for your community will show here."
                    )
                }
            }
        } else {
            items(uiState.emergencyContacts.data) { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = contact.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = contact.phone,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            NnSectionHeader(title = "Settings")
        }
        item {
            NnQuickAction(
                icon = Icons.Filled.Lock,
                title = "Financial Access",
                subtitle = "Manage who can view financial records",
                onClick = { openSub(SubScreen.FinancialAccess) }
            )
        }
    }
}
}
