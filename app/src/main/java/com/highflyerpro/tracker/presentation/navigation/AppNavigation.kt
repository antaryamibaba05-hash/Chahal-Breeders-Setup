package com.highflyerpro.tracker.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.highflyerpro.tracker.presentation.screens.analytics.AnalyticsScreen
import com.highflyerpro.tracker.presentation.screens.archive.ArchiveCenterScreen
import com.highflyerpro.tracker.presentation.screens.breeding.BreedingScreen
import com.highflyerpro.tracker.presentation.screens.flight.FlightDetailScreen
import com.highflyerpro.tracker.presentation.screens.flight.FlightListScreen
import com.highflyerpro.tracker.presentation.screens.flight.LiveFlightTrackerScreen
import com.highflyerpro.tracker.presentation.screens.flight.StartFlightScreen
import com.highflyerpro.tracker.presentation.screens.groups.GroupsScreen
import com.highflyerpro.tracker.presentation.screens.halloffame.HallOfFameScreen
import com.highflyerpro.tracker.presentation.screens.health.HealthScreen
import com.highflyerpro.tracker.presentation.screens.home.HomeScreen
import com.highflyerpro.tracker.presentation.screens.leaderboard.LeaderboardScreen
import com.highflyerpro.tracker.presentation.screens.manage.ManageScreen
import com.highflyerpro.tracker.presentation.screens.more.MoreScreen
import com.highflyerpro.tracker.presentation.screens.nutrition.NutritionScreen
import com.highflyerpro.tracker.presentation.screens.pedigree.PedigreeScreen
import com.highflyerpro.tracker.presentation.screens.pigeons.AddEditPigeonScreen
import com.highflyerpro.tracker.presentation.screens.pigeons.PigeonDetailScreen
import com.highflyerpro.tracker.presentation.screens.pigeons.PigeonsScreen
import com.highflyerpro.tracker.presentation.screens.reminders.RemindersScreen
import com.highflyerpro.tracker.presentation.screens.reports.ReportsScreen
import com.highflyerpro.tracker.presentation.screens.settings.DataAndBackupSettingsScreen
import com.highflyerpro.tracker.presentation.screens.settings.PermissionsManagementScreen
import com.highflyerpro.tracker.presentation.screens.settings.PermissionsSetupScreen
import com.highflyerpro.tracker.presentation.screens.settings.SettingsScreen
import com.highflyerpro.tracker.presentation.screens.splash.OnboardingWelcomeScreen
import com.highflyerpro.tracker.presentation.screens.splash.SplashScreen
import com.highflyerpro.tracker.presentation.screens.trash.TrashBinScreen
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Pigeons.route,
        Screen.Flight.route,
        Screen.Manage.route,
        Screen.More.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen.icon != null) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToWelcome = {
                        navController.navigate(Screen.OnboardingWelcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding Welcome (First Launch)
            composable(Screen.OnboardingWelcome.route) {
                OnboardingWelcomeScreen(
                    viewModel = viewModel,
                    onStartFreshSelected = {
                        navController.navigate(Screen.PermissionsSetup.route)
                    },
                    onRestoreCompleted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // Permissions Setup
            composable(Screen.PermissionsSetup.route) {
                PermissionsSetupScreen(
                    viewModel = viewModel,
                    onFinishSetup = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PermissionsSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPigeons = { navController.navigate(Screen.Pigeons.route) },
                    onNavigateToStartFlight = { navController.navigate(Screen.StartFlight.route) },
                    onNavigateToLiveTracker = { sessionId ->
                        navController.navigate(Screen.LiveTracker.createRoute(sessionId))
                    },
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    },
                    onNavigateToBreeding = { navController.navigate(Screen.Breeding.route) },
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) },
                    onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Pigeons Directory
            composable(Screen.Pigeons.route) {
                PigeonsScreen(
                    viewModel = viewModel,
                    onNavigateToAddPigeon = { navController.navigate(Screen.AddPigeon.route) },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Add Pigeon
            composable(Screen.AddPigeon.route) {
                AddEditPigeonScreen(
                    viewModel = viewModel,
                    pigeonIdToEdit = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Pigeon
            composable(
                route = Screen.EditPigeon.route,
                arguments = listOf(navArgument("pigeonId") { type = NavType.StringType })
            ) { backStack ->
                val pigeonId = backStack.arguments?.getString("pigeonId") ?: ""
                AddEditPigeonScreen(
                    viewModel = viewModel,
                    pigeonIdToEdit = pigeonId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pigeon Detail / Profile
            composable(
                route = Screen.PigeonDetail.route,
                arguments = listOf(navArgument("pigeonId") { type = NavType.StringType })
            ) { backStack ->
                val pigeonId = backStack.arguments?.getString("pigeonId") ?: ""
                PigeonDetailScreen(
                    viewModel = viewModel,
                    pigeonId = pigeonId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.EditPigeon.createRoute(id))
                    },
                    onNavigateToPigeonDetail = { id ->
                        navController.navigate(Screen.PigeonDetail.createRoute(id))
                    }
                )
            }

            // Pedigree Tree
            composable(
                route = Screen.PedigreeTree.route,
                arguments = listOf(navArgument("pigeonId") { type = NavType.StringType })
            ) { backStack ->
                val pigeonId = backStack.arguments?.getString("pigeonId") ?: ""
                PedigreeScreen(
                    pigeonId = pigeonId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPigeonDetail = { id ->
                        navController.navigate(Screen.PigeonDetail.createRoute(id))
                    }
                )
            }

            // Flight Central
            composable(Screen.Flight.route) {
                FlightListScreen(
                    viewModel = viewModel,
                    onNavigateToStartFlight = { navController.navigate(Screen.StartFlight.route) },
                    onNavigateToLiveTracker = { sessionId ->
                        navController.navigate(Screen.LiveTracker.createRoute(sessionId))
                    },
                    onNavigateToFlightDetail = { sessionId ->
                        navController.navigate(Screen.FlightDetail.createRoute(sessionId))
                    }
                )
            }

            // Start Flight
            composable(Screen.StartFlight.route) {
                StartFlightScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onFlightStarted = { sessionId ->
                        navController.navigate(Screen.LiveTracker.createRoute(sessionId)) {
                            popUpTo(Screen.Flight.route)
                        }
                    }
                )
            }

            // Live Flight Tracker
            composable(
                route = Screen.LiveTracker.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStack ->
                val sessionId = backStack.arguments?.getString("sessionId") ?: ""
                LiveFlightTrackerScreen(
                    viewModel = viewModel,
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() },
                    onFlightCompleted = {
                        navController.navigate(Screen.FlightDetail.createRoute(sessionId)) {
                            popUpTo(Screen.Flight.route)
                        }
                    }
                )
            }

            // Flight Detail
            composable(
                route = Screen.FlightDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStack ->
                val sessionId = backStack.arguments?.getString("sessionId") ?: ""
                FlightDetailScreen(
                    viewModel = viewModel,
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Manage Hub
            composable(Screen.Manage.route) {
                ManageScreen(
                    onNavigateToBreeding = { navController.navigate(Screen.Breeding.route) },
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) },
                    onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToArchive = { navController.navigate(Screen.ArchiveCenter.route) },
                    onNavigateToTrash = { navController.navigate(Screen.TrashBin.route) },
                    onNavigateToGroups = { navController.navigate(Screen.Groups.route) }
                )
            }

            // More Hub
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onNavigateToHallOfFame = { navController.navigate(Screen.HallOfFame.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Leaderboard / Standings
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(
                    viewModel = viewModel,
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Analytics
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Groups
            composable(Screen.Groups.route) {
                GroupsScreen(
                    viewModel = viewModel,
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Breeding Center
            composable(Screen.Breeding.route) {
                BreedingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPigeonClick = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Health Care
            composable(Screen.Health.route) {
                HealthScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Nutrition & Feed Stock
            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Reminders & Tags
            composable(Screen.Reminders.route) {
                RemindersScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Trash Bin
            composable(Screen.TrashBin.route) {
                TrashBinScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Archive Center
            composable(Screen.ArchiveCenter.route) {
                ArchiveCenterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Hall of Fame
            composable(Screen.HallOfFame.route) {
                HallOfFameScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPigeonDetail = { pigeonId ->
                        navController.navigate(Screen.PigeonDetail.createRoute(pigeonId))
                    }
                )
            }

            // Reports Center
            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Permissions Management
            composable(Screen.PermissionsManagement.route) {
                PermissionsManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Data & Backup Settings
            composable(Screen.DataAndBackupSettings.route) {
                DataAndBackupSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDataAndBackup = { navController.navigate(Screen.DataAndBackupSettings.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.PermissionsManagement.route) }
                )
            }
        }
    }
}
