package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.collectAsState()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isNotificationEnabled by remember { mutableStateOf(true) }

    // Category selection state
    var selectedCategory by remember { mutableStateOf("Personal") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Date/Time pick state
    val currentCalendar = Calendar.getInstance()
    var year by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var day by remember { mutableIntStateOf(currentCalendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableIntStateOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(currentCalendar.get(Calendar.MINUTE)) }

    var hasSelectedDate by remember { mutableStateOf(false) }
    var hasSelectedTime by remember { mutableStateOf(false) }

    // If categories load, pre-fill if empty
    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categories[0].name
        }
    }

    // Dynamic date formatted string
    val dateText = if (hasSelectedDate) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(cal.time)
    } else {
        "Select Trigger Date"
    }

    // Dynamic time formatted string
    val timeText = if (hasSelectedTime) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    } else {
        "Select Trigger Time"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("create_back_button")
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
            // Form Headers & Fields
            Text(
                text = "What would you like to be reminded of?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                placeholder = { Text("Enter title here") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_reminder_title_input")
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Additional details") },
                placeholder = { Text("e.g. bring grocery bags, files...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_reminder_notes_input")
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
                        .testTag("create_reminder_category_select")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedCategory, fontWeight = FontWeight.Medium)
                        FaIcon(
                            iconId = R.drawable.ic_fa_folder,
                            contentDescription = "Category Selector icon",
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

            // Date Selection Button
            val datePickerDialog = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    year = selectedYear
                    month = selectedMonth
                    day = selectedDayOfMonth
                    hasSelectedDate = true
                },
                year, month, day
            ).apply {
                // Prevent past dates
                datePicker.minDate = System.currentTimeMillis() - 1000
            }

            Surface(
                onClick = { datePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_date_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_calendar,
                        contentDescription = "Calendar Picker",
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = dateText, fontWeight = FontWeight.Bold, color = if (hasSelectedDate) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Time Selection Button
            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                    hasSelectedTime = true
                },
                hour, minute, false
            )

            Surface(
                onClick = { timePickerDialog.show() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_time_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_clock,
                        contentDescription = "Time Picker",
                        size = 20.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = timeText, fontWeight = FontWeight.Bold, color = if (hasSelectedTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Notifications Toggle Row
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
                        contentDescription = "Notification Bell",
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
                    modifier = Modifier.testTag("notification_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title is required.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!hasSelectedDate || !hasSelectedTime) {
                        Toast.makeText(context, "Please select both date and time.", Toast.LENGTH_SHORT).show()
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

                    if (chosenCalendar.timeInMillis <= System.currentTimeMillis()) {
                        Toast.makeText(context, "Past date and time is not allowed.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addReminder(
                        title = title,
                        notes = notes,
                        category = selectedCategory,
                        dateTimeMillis = chosenCalendar.timeInMillis,
                        isNotificationEnabled = isNotificationEnabled,
                        onSuccess = {
                            Toast.makeText(context, "Reminder created successfully.", Toast.LENGTH_SHORT).show()
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
                    .testTag("save_reminder_button")
            ) {
                Text("Create Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
