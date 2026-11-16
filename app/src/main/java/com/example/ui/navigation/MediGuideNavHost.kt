package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalMediStrings
import com.example.ui.MainAppUiState
import com.example.ui.components.MediTopBar
import com.example.ui.screens.assistant.AssistantScreen
import com.example.ui.screens.care.CareModulesScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.medications.MedicationsScreen
import com.example.ui.screens.profiles.ProfilesSecurityScreen
import kotlinx.serialization.Serializable

@Serializable object RouteToday
@Serializable object RouteMedicines
@Serializable object RouteCare
@Serializable object RouteAssistant
@Serializable object RouteProfiles

private data class NavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MediGuideShell(
    mainState: MainAppUiState,
    onSelectProfile: (Long) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onTestLockScreen: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val strings = LocalMediStrings.current
    var openAddMedicineTrigger by rememberSaveable { mutableStateOf(false) }

    val navItems = listOf(
        NavItem(RouteToday, strings.navToday, Icons.Filled.Home, Icons.Outlined.Home, "nav_today"),
        NavItem(RouteMedicines, strings.navMedicines, Icons.Filled.Medication, Icons.Outlined.Medication, "nav_medicines"),
        NavItem(RouteCare, strings.navCare, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_care"),
        NavItem(RouteAssistant, strings.navAssistant, Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety, "nav_assistant"),
        NavItem(RouteProfiles, strings.navProfiles, Icons.Filled.Person, Icons.Outlined.Person, "nav_profiles")
    )

    val cycleLanguage: () -> Unit = {
        val entries = AppLanguage.entries
        val next = entries[(entries.indexOf(mainState.language) + 1) % entries.size]
        onSelectLanguage(next)
    }

    val navigateToTab: (Any) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    MediTopBar(
                        activeProfile = mainState.activeProfile,
                        currentLanguage = mainState.language,
                        onCycleLanguage = cycleLanguage,
                        onProfileClick = { navigateToTab(RouteProfiles) }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = RouteToday
                        ) {
                            composable<RouteToday> {
                                DashboardScreen(
                                    onNavigateToAddMedicine = {
                                        openAddMedicineTrigger = true
                                        navigateToTab(RouteMedicines)
                                    }
                                )
                            }
                            composable<RouteMedicines> {
                                MedicationsScreen(
                                    openAddDialogInitially = openAddMedicineTrigger,
                                    onDismissInitialAdd = { openAddMedicineTrigger = false }
                                )
                            }
                            composable<RouteCare> {
                                CareModulesScreen()
                            }
                            composable<RouteAssistant> {
                                AssistantScreen()
                            }
                            composable<RouteProfiles> {
                                ProfilesSecurityScreen(
                                    mainState = mainState,
                                    onSelectProfile = onSelectProfile,
                                    onSelectLanguage = onSelectLanguage,
                                    onToggleBiometricLock = onToggleBiometricLock,
                                    onTestLockScreen = onTestLockScreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
