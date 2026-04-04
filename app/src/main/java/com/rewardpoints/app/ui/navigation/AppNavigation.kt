package com.rewardpoints.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rewardpoints.app.ui.components.AmbientBackground
import com.rewardpoints.app.ui.components.glass.BottomNavItem
import com.rewardpoints.app.ui.components.glass.GlassBottomBar
import com.rewardpoints.app.ui.screen.achievements.AchievementsScreen
import com.rewardpoints.app.ui.screen.agent.AgentScreen
import com.rewardpoints.app.ui.screen.history.HistoryScreen
import com.rewardpoints.app.ui.screen.rewards.RewardsScreen
import com.rewardpoints.app.ui.screen.settings.SettingsScreen
import com.rewardpoints.app.ui.screen.stats.StatsScreen
import com.rewardpoints.app.ui.screen.status.StatusScreen
import com.rewardpoints.app.ui.screen.tasks.TasksScreen

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.STATUS

    val showBottomBar = currentRoute in listOf(
        Routes.STATUS, Routes.TASKS, Routes.REWARDS, Routes.AGENT, Routes.SETTINGS
    )

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
                modifier = Modifier.padding(paddingValues)
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
            }
        }
    }
}
