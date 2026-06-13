package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ReminderViewModel
import com.example.ui.components.FaIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activeTheme by viewModel.appThemeState.collectAsState()

    // Load sound and vibe configurations
    var soundEnabled by remember { mutableStateOf(true) }
    var vibeEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val prefs = viewModel.getNotificationPreferences()
        soundEnabled = prefs.first
        vibeEnabled = prefs.second
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_arrow_left,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme selection heading
            Text(
                text = "Application Theme",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // System Default Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Follow System Default", fontWeight = FontWeight.SemiBold)
                            Text("Sync app theme with Android settings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = activeTheme == "system",
                            onClick = { viewModel.updateTheme("system") },
                            modifier = Modifier.testTag("radio_theme_system")
                        )
                    }

                    Divider()

                    // Light Theme Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Forced Light", fontWeight = FontWeight.SemiBold)
                        RadioButton(
                            selected = activeTheme == "light",
                            onClick = { viewModel.updateTheme("light") },
                            modifier = Modifier.testTag("radio_theme_light")
                        )
                    }

                    Divider()

                    // Dark Theme Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Forced Dark", fontWeight = FontWeight.SemiBold)
                        RadioButton(
                            selected = activeTheme == "dark",
                            onClick = { viewModel.updateTheme("dark") },
                            modifier = Modifier.testTag("radio_theme_dark")
                        )
                    }
                }
            }

            // Notification preferences heading
            Text(
                text = "Sound & Vibrations",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Alarm Sound Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alarm Sound Enabled", fontWeight = FontWeight.SemiBold)
                            Text("Play ringtone when reminder is due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                viewModel.updateNotificationPrefs(soundEnabled, vibeEnabled)
                                Toast.makeText(context, "Sound preferences updated.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("switch_sound")
                        )
                    }

                    Divider()

                    // Alarm Vibration Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alarm Vibration Enabled", fontWeight = FontWeight.SemiBold)
                            Text("Vibrate device when reminder is due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = vibeEnabled,
                            onCheckedChange = {
                                vibeEnabled = it
                                viewModel.updateNotificationPrefs(soundEnabled, vibeEnabled)
                                Toast.makeText(context, "Vibration preferences updated.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("switch_vibration")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App specifications Card
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_info_circle,
                        contentDescription = "Info icon",
                        tint = MaterialTheme.colorScheme.primary,
                        size = 32.dp
                    )
                    Column {
                        Text(
                            text = "Simple Reminder",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Version 1.0.0 (Native Android production build)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Designed offline for secure on-device persistence and high reliability exact notification triggers.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
