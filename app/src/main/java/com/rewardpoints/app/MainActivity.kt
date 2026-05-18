package com.rewardpoints.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rewardpoints.app.ui.navigation.AppNavigation
import com.rewardpoints.app.ui.theme.StatUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() must run BEFORE super.onCreate() to swap the splash theme
        // for the main theme cleanly. The splash itself is described in Theme.StatUp.Splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatUpTheme {
                RequestNotificationPermissionOnce()
                AppNavigation()
            }
        }
    }
}

/**
 * Asks for POST_NOTIFICATIONS once per app launch on Android 13+. Decline is fine — the
 * Notifier checks permission before every notification, so refusal silently disables them.
 * No UI is shown if the permission isn't needed (API < 33) or is already granted.
 */
@androidx.compose.runtime.Composable
private fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result ignored — Notifier re-checks before each notify call */ }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
