package com.rewardpoints.app.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rewardpoints.app.data.local.datastore.UserPreferences
import com.rewardpoints.app.ui.components.AmbientBackground
import com.rewardpoints.app.ui.components.LocalHapticsEnabled
import com.rewardpoints.app.ui.components.glass.BottomNavItem
import com.rewardpoints.app.ui.components.glass.GlassBottomBar
import com.rewardpoints.app.ui.components.glass.LocalHazeState
import com.rewardpoints.app.ui.components.glass.hazeSourceOrFallback
import dev.chrisbanes.haze.rememberHazeState
import com.rewardpoints.app.ui.screen.achievements.AchievementsScreen
import com.rewardpoints.app.ui.screen.agent.AgentScreen
import com.rewardpoints.app.ui.screen.history.HistoryScreen
import com.rewardpoints.app.ui.screen.legal.PrivacyPolicyScreen
import com.rewardpoints.app.ui.screen.onboarding.OnboardingScreen
import com.rewardpoints.app.ui.screen.rewards.RewardsScreen
import com.rewardpoints.app.ui.screen.settings.SettingsScreen
import com.rewardpoints.app.ui.screen.stats.StatsScreen
import com.rewardpoints.app.ui.screen.status.StatusScreen
import com.rewardpoints.app.ui.screen.tasks.TasksScreen
import com.rewardpoints.app.ui.theme.BackgroundBase
import org.koin.compose.koinInject

private val bottomNavItems = listOf(
    BottomNavItem("Status", Icons.Outlined.Shield, Routes.STATUS),
    BottomNavItem("Tasks", Icons.Outlined.TaskAlt, Routes.TASKS),
    BottomNavItem("Rewards", Icons.Outlined.CardGiftcard, Routes.REWARDS),
    BottomNavItem("Agent", Icons.Outlined.SmartToy, Routes.AGENT),
    BottomNavItem("Settings", Icons.Outlined.Settings, Routes.SETTINGS)
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // First-run gate: show onboarding until the flag is set. `null` = the flag is still loading,
    // so we render a neutral dark frame (never a flash of onboarding for already-onboarded users).
    val userPreferences = koinInject<UserPreferences>()
    val onboarded by produceState<Boolean?>(initialValue = null, userPreferences) {
        userPreferences.onboardingComplete.collect { value = it }
    }
    // Expose the Haptic Feedback preference app-wide so action handlers can tick on confirm.
    val hapticsEnabled by userPreferences.hapticFeedback.collectAsStateWithLifecycle(initialValue = true)

    CompositionLocalProvider(LocalHapticsEnabled provides hapticsEnabled) {
        when (onboarded) {
            null -> Box(modifier = Modifier.fillMaxSize().background(BackgroundBase))
            false -> OnboardingScreen()
            else -> MainShell(navController)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainShell(navController: NavHostController) {
    // Ask for notification permission here (after onboarding), not over the intro.
    RequestNotificationPermissionOnce()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.STATUS

    // Scaffold's bottomBar slot reserves a FIXED height for content padding — it never
    // accounts for the IME, so when the keyboard opens over AgentScreen's chat input, that
    // fixed reservation and the keyboard height fight each other (wrong gap either way).
    // Hiding the bar while the keyboard is visible removes the fixed competitor entirely,
    // leaving AgentScreen's own imePadding() as the only thing sizing the bottom space.
    val imeVisible = WindowInsets.isImeVisible
    val showBottomBar = currentRoute in listOf(
        Routes.STATUS, Routes.TASKS, Routes.REWARDS, Routes.AGENT, Routes.SETTINGS
    ) && !imeVisible

    // Single HazeState shared across the whole shell — content is the "source", glass
    // primitives (cards, bottom bar) are "effects" that sample the source at blur time.
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState) {
    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackground()

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    GlassBottomBar(
                        items = bottomNavItems,
                        selectedRoute = currentRoute,
                        onItemClick = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo(Routes.STATUS) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.STATUS,
                modifier = Modifier
                    .padding(paddingValues)
                    // Without this, descendants that read WindowInsets directly (e.g.
                    // AgentScreen's imePadding()) don't know paddingValues already reserved
                    // the bottom-bar's height, so they stack the full inset on top of it —
                    // a permanent gap the size of the bottom bar between content and the IME.
                    .consumeWindowInsets(paddingValues)
                    .hazeSourceOrFallback(),
                // Subtle fade-through on route changes for a more fluid feel.
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 16 } },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(160)) }
            ) {
                composable(Routes.STATUS) {
                    StatusScreen(navController = navController)
                }
                composable(Routes.TASKS) {
                    TasksScreen(navController = navController)
                }
                composable(Routes.REWARDS) {
                    RewardsScreen(navController = navController)
                }
                composable(Routes.AGENT) {
                    AgentScreen(navController = navController)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(navController = navController)
                }
                composable(Routes.HISTORY) {
                    HistoryScreen(navController = navController)
                }
                composable(Routes.FULL_STATS) {
                    StatsScreen(navController = navController)
                }
                composable(Routes.ACHIEVEMENTS) {
                    AchievementsScreen(navController = navController)
                }
                composable(Routes.PRIVACY_POLICY) {
                    PrivacyPolicyScreen(navController = navController)
                }
            }
        }
    }
    }  // CompositionLocalProvider
}

/**
 * Asks for POST_NOTIFICATIONS once on Android 13+. Decline is fine — the Notifier re-checks
 * permission before every notify call, so refusal silently disables notifications. No UI shows
 * if the permission isn't needed (API < 33) or is already granted.
 */
@Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result ignored — Notifier re-checks before each notify call */ }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
