package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: Int,
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.allReminders.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val reminder = remember(reminders, reminderId) {
        reminders.find { it.id == reminderId }
    }

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

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isCompleted by remember { mutableStateOf(false) }

    // Category selection state
    var selectedCategory by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Date/Time pick state
    var year by remember { mutableIntStateOf(2026) }
    var month by remember { mutableIntStateOf(0) }
    var day by remember { mutableIntStateOf(1) }
    var hour by remember { mutableIntStateOf(12) }
    var minute by remember { mutableIntStateOf(0) }

    var hasInitialized by remember { mutableStateOf(false) }

    // Initialize values exactly once based on the loaded reminder
    if (!hasInitialized) {
        title = reminder.title
        notes = reminder.notes
        isNotificationEnabled = reminder.isNotificationEnabled
        isCompleted = reminder.isCompleted
        selectedCategory = reminder.category

        val cal = Calendar.getInstance().apply { timeInMillis = reminder.dateTimeMillis }
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        hasInitialized = true
    }

    // Dynamic date formatted string
    val dateText = remember(year, month, day) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(cal.time)
    }

    // Dynamic time formatted string
    val timeText = remember(hour, minute) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_back_button")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Modify reminder configs",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_reminder_title_input")
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_reminder_notes_input")
            )

            // Category Selector
            Text(
                text = "Select Category",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { dropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_reminder_category_select")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedCategory, fontWeight = FontWeight.Medium)
                        FaIcon(
                            iconId = R.drawable.ic_fa_folder,
                            contentDescription = "Category icon",
                            size = 18.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategory = cat.name
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${cat.name}")
                        )
                    }
                }
            }

            // Schedule triggers
            Text(
                text = "Reminder Schedule",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            // Date Picker Dialog
            val datePickerDialog = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    year = selectedYear
                    month = selectedMonth
                    day = selectedDayOfMonth
                },
                year, month, day
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
            }

            Surface(
                onClick = { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_select_date_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_calendar,
                        contentDescription = "Change Date",
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = dateText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                },
                hour, minute, false
            )

            Surface(
                onClick = { timePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_select_time_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_clock,
                        contentDescription = "Change Time",
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = timeText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Notifications Switch Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_bell,
                        contentDescription = "Alarm status",
                        tint = MaterialTheme.colorScheme.primary,
                        size = 20.dp
                    )
                    Column {
                        Text("Schedule Alarm Notification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Receive alert when this reminder is due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isNotificationEnabled,
                    onCheckedChange = { isNotificationEnabled = it },
                    modifier = Modifier.testTag("edit_notification_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title is required.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val chosenCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (chosenCalendar.timeInMillis <= System.currentTimeMillis() && !isCompleted) {
                        Toast.makeText(context, "Past date and time is not allowed for active reminders.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.updateReminderDetails(
                        id = reminder.id,
                        title = title,
                        notes = notes,
                        category = selectedCategory,
                        dateTimeMillis = chosenCalendar.timeInMillis,
                        isNotificationEnabled = isNotificationEnabled,
                        isCompleted = isCompleted,
                        onSuccess = {
                            Toast.makeText(context, "Reminder updated successfully.", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_edits_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
