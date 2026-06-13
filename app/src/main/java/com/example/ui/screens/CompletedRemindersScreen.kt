package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
fun CompletedRemindersScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.allReminders.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val completedList = remember(reminders) {
        reminders.filter { it.isCompleted }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("completed_back_button")
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
        ) {
            if (completedList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_check_double,
                            contentDescription = "No completed items",
                            size = 48.dp,
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No completed reminders found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(completedList, key = { it.id }) { reminder ->
                        val themeColorHex = categories.find { it.name.equals(reminder.category, ignoreCase = true) }?.colorHex ?: "#718096"
                        val formattedTime = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(reminder.dateTimeMillis))
                        
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("completed_item_${reminder.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Undo action button (Restore)
                                IconButton(
                                    onClick = {
                                        viewModel.toggleReminderCompletion(reminder)
                                        Toast.makeText(context, "Reminder restored.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("restore_button_${reminder.id}")
                                ) {
                                    FaIcon(
                                        iconId = R.drawable.ic_fa_plus, // Reused as Action Restore
                                        contentDescription = "Restore Reminder",
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 18.dp
                                    )
                                }

                                // Details column
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = reminder.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$formattedTime - ${reminder.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                // Delete permanently action button
                                IconButton(
                                    onClick = {
                                        viewModel.deleteReminder(reminder)
                                        Toast.makeText(context, "Reminder deleted permanently.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("delete_permanently_button_${reminder.id}")
                                ) {
                                    FaIcon(
                                        iconId = R.drawable.ic_fa_trash,
                                        contentDescription = "Delete permanently",
                                        tint = MaterialTheme.colorScheme.error,
                                        size = 18.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
