package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Reminder
import com.example.ui.ReminderViewModel
import com.example.ui.components.FaIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailsScreen(
    reminderId: Int,
    viewModel: ReminderViewModel,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.allReminders.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val reminder = remember(reminders, reminderId) {
        reminders.find { it.id == reminderId }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (reminder == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Reminder not found.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val categoryColorHex = remember(categories, reminder.category) {
        categories.find { it.name.equals(reminder.category, ignoreCase = true) }?.colorHex ?: "#718096"
    }
    val categoryColor = remember(categoryColorHex) {
        Color(android.graphics.Color.parseColor(categoryColorHex))
    }

    val formattedDate = remember(reminder.dateTimeMillis) {
        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        sdf.format(Date(reminder.dateTimeMillis))
    }

    val isOverdue = remember(reminder.dateTimeMillis, reminder.isCompleted) {
        reminder.dateTimeMillis < System.currentTimeMillis() && !reminder.isCompleted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("details_back_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_arrow_left,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(reminder.id) },
                        modifier = Modifier.testTag("edit_reminder_icon_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_pen,
                            contentDescription = "Edit Reminder",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("delete_reminder_icon_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_trash,
                            contentDescription = "Delete Reminder",
                            tint = MaterialTheme.colorScheme.error
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Status Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (reminder.isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else if (isOverdue) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FaIcon(
                        iconId = if (reminder.isCompleted) R.drawable.ic_fa_check_double else R.drawable.ic_fa_bell,
                        contentDescription = "Status Banner Icon",
                        tint = if (reminder.isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else if (isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        size = 28.dp
                    )
                    Column {
                        Text(
                            text = if (reminder.isCompleted) "Completed" else if (isOverdue) "Overdue" else "Active",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (reminder.isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else if (isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Text(
                            text = if (reminder.isCompleted) "This reminder has been checked off" else if (isOverdue) "The scheduled alarm time has passed!" else "Scheduled to notify you",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Central Card
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = reminder.category,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Title
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 32.sp
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Date & Time block
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_calendar,
                            contentDescription = "Date and Time icon",
                            tint = MaterialTheme.colorScheme.primary,
                            size = 20.dp
                        )
                        Column {
                            Text(
                                text = "Reminder Time",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Alarm Enabled status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FaIcon(
                            iconId = if (reminder.isNotificationEnabled) R.drawable.ic_fa_bell else R.drawable.ic_fa_bell, // Bell or crossed out, but bell is nice
                            contentDescription = "Notification Alarm status icon",
                            tint = if (reminder.isNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            size = 20.dp
                        )
                        Column {
                            Text(
                                text = "Notification Alarm",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (reminder.isNotificationEnabled) "Enabled" else "Disabled / Silenced",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (reminder.isNotificationEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Notes details panel if exist
                    if (reminder.notes.isNotEmpty()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = reminder.notes,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Toggle Completion Button
            Button(
                onClick = {
                    viewModel.toggleReminderCompletion(reminder)
                    val toastMsg = if (!reminder.isCompleted) "Reminder marked completed." else "Reminder restored to active."
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("toggle_complete_button")
            ) {
                FaIcon(
                    iconId = if (reminder.isCompleted) R.drawable.ic_fa_plus else R.drawable.ic_fa_check_double, // Rotate/Restore or Complete icon
                    contentDescription = null,
                    size = 18.dp,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (reminder.isCompleted) "Mark Incomplete / Restore" else "Mark Complete",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // Delete Confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReminder(reminder)
                        showDeleteDialog = false
                        Toast.makeText(context, "Reminder deleted.", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("delete_confirm_yes")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    modifier = Modifier.testTag("delete_confirm_no")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
