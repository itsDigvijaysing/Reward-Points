package dev.statup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.statup.app.ui.navigation.AppNavigation
import dev.statup.app.ui.theme.StatUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() must run BEFORE super.onCreate() to swap the splash theme
        // for the main theme cleanly. The splash itself is described in Theme.StatUp.Splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatUpTheme {
                // AppNavigation gates on the onboarding flag and handles the post-onboarding
                // notification-permission request.
                AppNavigation()
            }
        }
    }
}
