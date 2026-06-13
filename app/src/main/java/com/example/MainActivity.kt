package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.example.ui.components.FaIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.ReminderViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Notification permission is recommended to trigger precise reminder alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request runtime notification permission on startup (Android 13+)
        checkRuntimeNotificationPermissions()

        setContent {
            val viewModel: ReminderViewModel = viewModel()
            val themeSetting by viewModel.appThemeState.collectAsState()

            // Resolve dark theme based on preferences or system defaults
            val darkTheme = when (themeSetting) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                // Handle deep-link reminder trigger if opened via active system notification click
                LaunchedEffect(intent) {
                    val targetReminderId = intent?.getIntExtra("NOTIFICATION_REMINDER_ID", -1) ?: -1
                    if (targetReminderId != -1) {
                        navController.navigate("reminder_details/$targetReminderId")
                    }
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val topLevelRoutes = listOf("dashboard", "categories", "completed_reminders", "settings")
                val showBottomBar = currentRoute in topLevelRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "dashboard",
                                    onClick = {
                                        if (currentRoute != "dashboard") {
                                            navController.navigate("dashboard") {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        FaIcon(
                                            iconId = R.drawable.ic_fa_folder,
                                            contentDescription = "Dashboard",
                                            size = 20.dp,
                                            tint = if (currentRoute == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    label = { Text("Dashboard") },
                                    modifier = Modifier.testTag("nav_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "categories",
                                    onClick = {
                                        if (currentRoute != "categories") {
                                            navController.navigate("categories") {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        FaIcon(
                                            iconId = R.drawable.ic_fa_tag,
                                            contentDescription = "Categories",
                                            size = 20.dp,
                                            tint = if (currentRoute == "categories") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    label = { Text("Categories") },
                                    modifier = Modifier.testTag("nav_categories")
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "completed_reminders",
                                    onClick = {
                                        if (currentRoute != "completed_reminders") {
                                            navController.navigate("completed_reminders") {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        FaIcon(
                                            iconId = R.drawable.ic_fa_check_double,
                                            contentDescription = "Completed",
                                            size = 20.dp,
                                            tint = if (currentRoute == "completed_reminders") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    label = { Text("Completed") },
                                    modifier = Modifier.testTag("nav_completed")
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        if (currentRoute != "settings") {
                                            navController.navigate("settings") {
                                                popUpTo("dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        FaIcon(
                                            iconId = R.drawable.ic_fa_cog,
                                            contentDescription = "Settings",
                                            size = 20.dp,
                                            tint = if (currentRoute == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    label = { Text("Settings") },
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            // Dashboard Home Screen
                            composable("dashboard") {
                                // Intercept back presses on Home to execute custom exit protection
                                ExitProtectionHandler()

                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToCreate = { navController.navigate("create_reminder") },
                                    onNavigateToDetails = { id -> navController.navigate("reminder_details/$id") },
                                    onNavigateToCompleted = { navController.navigate("completed_reminders") },
                                    onNavigateToSettings = { navController.navigate("settings") }
                                )
                            }

                            // Categories Screen
                            composable("categories") {
                                CategoriesScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Create Reminder Screen
                            composable("create_reminder") {
                                CreateReminderScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Reminder Details Screen
                            composable(
                                route = "reminder_details/{reminderId}",
                                arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("reminderId") ?: -1
                                ReminderDetailsScreen(
                                    reminderId = id,
                                    viewModel = viewModel,
                                    onNavigateToEdit = { editId -> navController.navigate("edit_reminder/$editId") },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Edit Reminder Screen
                            composable(
                                route = "edit_reminder/{reminderId}",
                                arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("reminderId") ?: -1
                                EditReminderScreen(
                                    reminderId = id,
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Completed Reminders History Screen
                            composable("completed_reminders") {
                                CompletedRemindersScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // Global Application Settings Screen
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkRuntimeNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Composable
    private fun ExitProtectionHandler() {
        val context = LocalContext.current
        var backPressTimestamp by remember { mutableLongStateOf(0L) }
        var showExitDialog by remember { mutableStateOf(false) }

        BackHandler(enabled = true) {
            val now = System.currentTimeMillis()
            if (now - backPressTimestamp < 2000L) {
                // Second back press within 2 seconds: Trigger M3 confirmation dialog
                showExitDialog = true
            } else {
                // First back press: Record time and display instructions Toast
                backPressTimestamp = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit Application") },
                text = { Text("Are you sure you want to exit Simple Reminder?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            finish() // Exit application
                        },
                        modifier = Modifier.testTag("exit_confirm_yes")
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false },
                        modifier = Modifier.testTag("exit_confirm_no")
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}
