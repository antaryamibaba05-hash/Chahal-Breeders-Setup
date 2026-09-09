package com.highflyerpro.tracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash", null)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Pigeons : Screen("pigeons", "Pigeons", Icons.Default.People)
    object Flight : Screen("flight", "Flights", Icons.Default.FlightTakeoff)
    object Manage : Screen("manage", "Manage", Icons.Default.GridView)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)

    object Leaderboard : Screen("leaderboard", "Standings", Icons.Default.EmojiEvents)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Breeding : Screen("breeding", "Breeding", Icons.Default.Favorite)
    object Health : Screen("health", "Health", Icons.Default.MedicalServices)
    object Nutrition : Screen("nutrition", "Feed", Icons.Default.Restaurant)
    object Reminders : Screen("reminders", "Alerts", Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Groups : Screen("groups", "Groups", null)
    object TrashBin : Screen("trash", "Trash Bin", null)
    object ArchiveCenter : Screen("archive", "Archive Center", null)
    object HallOfFame : Screen("halloffame", "Hall of Fame", null)
    object Reports : Screen("reports", "Reports & PDF", null)
    object OnboardingWelcome : Screen("onboarding_welcome", "Welcome", null)
    object PermissionsSetup : Screen("permissions_setup", "Setup Permissions", null)
    object PermissionsManagement : Screen("permissions_management", "Permissions & Access", null)
    object DataAndBackupSettings : Screen("data_backup_settings", "Data & Backup", null)

    // Parameterized routes
    object AddPigeon : Screen("pigeons/add", "Add Pigeon", null)
    object EditPigeon : Screen("pigeons/edit/{pigeonId}", "Edit Pigeon", null) {
        fun createRoute(pigeonId: String) = "pigeons/edit/$pigeonId"
    }
    object PigeonDetail : Screen("pigeons/detail/{pigeonId}", "Pigeon Profile", null) {
        fun createRoute(pigeonId: String) = "pigeons/detail/$pigeonId"
    }
    object PedigreeTree : Screen("pigeons/pedigree/{pigeonId}", "Pedigree Tree", null) {
        fun createRoute(pigeonId: String) = "pigeons/pedigree/$pigeonId"
    }

    object StartFlight : Screen("flight/start", "Release Flight", null)
    object LiveTracker : Screen("flight/live/{sessionId}", "Live Flight", null) {
        fun createRoute(sessionId: String) = "flight/live/$sessionId"
    }
    object FlightDetail : Screen("flight/detail/{sessionId}", "Flight Results", null) {
        fun createRoute(sessionId: String) = "flight/detail/$sessionId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Pigeons,
    Screen.Flight,
    Screen.Manage,
    Screen.More
)
