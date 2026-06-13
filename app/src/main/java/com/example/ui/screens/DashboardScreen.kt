package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Category
import com.example.data.Reminder
import com.example.ui.DashboardStats
import com.example.ui.ReminderViewModel
import com.example.ui.components.FaIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReminderViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToCompleted: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val reminders by viewModel.filteredReminders.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf("#4A5568") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Simple Reminder",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCompleted,
                        modifier = Modifier.testTag("completed_nav_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_check_double,
                            contentDescription = "Completed Reminders",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_nav_button")
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_cog,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("add_reminder_fab")
                    .navigationBarsPadding()
            ) {
                FaIcon(
                    iconId = R.drawable.ic_fa_plus,
                    contentDescription = "Add Reminder",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search title, details, category...") },
                leadingIcon = {
                    FaIcon(
                        iconId = R.drawable.ic_fa_search,
                        contentDescription = "Search icon",
                        size = 18.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar")
            )

            // Dashboard Grid (Stats Cards)
            StatsGrid(stats = stats)

            Spacer(modifier = Modifier.height(12.dp))

            // Screen Subtitle & Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    FaIcon(
                        iconId = R.drawable.ic_fa_plus,
                        contentDescription = "Add custom category",
                        size = 14.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 14.sp)
                }
            }

            // Categories horizontal selection list
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { viewModel.selectCategoryFilter(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat.name,
                        onClick = { viewModel.selectCategoryFilter(cat.name) },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color(android.graphics.Color.parseColor(cat.colorHex))
                                    )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder List Title
            Text(
                text = if (selectedFilter != null) "$selectedFilter Reminders" else "Active Reminders",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Dynamic Active Reminders List
            val activeReminders = reminders.filter { !it.isCompleted }

            if (activeReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_bell,
                            contentDescription = "No reminders item",
                            size = 48.dp,
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active reminders found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(activeReminders, key = { it.id }) { reminder ->
                        ReminderListItem(
                            reminder = reminder,
                            colorHex = categories.find { it.name.equals(reminder.category, ignoreCase = true) }?.colorHex ?: "#718096",
                            onCardClick = { onNavigateToDetails(reminder.id) },
                            onCheckToggle = { viewModel.toggleReminderCompletion(reminder) }
                        )
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        val colorOptions = listOf("#FF4B5C", "#3182CE", "#D69E2E", "#38A169", "#805AD5", "#E53E3E", "#319795")
        
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Create Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_category_name_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Tag Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colorOptions) { hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { newCategoryColor = hex }
                                    .padding(4.dp)
                            ) {
                                if (newCategoryColor == hex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.createCategory(newCategoryName, newCategoryColor)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_category_confirm_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsGrid(stats: DashboardStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatsCard(
                title = "TOTAL",
                count = stats.total,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconId = R.drawable.ic_fa_bell,
                showIconBg = true,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "TODAY",
                count = stats.today,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconId = R.drawable.ic_fa_clock,
                showIconBg = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatsCard(
                title = "UPCOMING",
                count = stats.upcoming,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconId = R.drawable.ic_fa_calendar,
                showIconBg = false,
                borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "DONE",
                count = stats.completed,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconId = R.drawable.ic_fa_check_double,
                showIconBg = false,
                borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
    iconId: Int,
    showIconBg: Boolean,
    borderStroke: BorderStroke? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = borderStroke,
        modifier = modifier.height(112.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = count.toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    color = contentColor
                )
                if (showIconBg) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(6.dp)
                    ) {
                        FaIcon(
                            iconId = iconId,
                            contentDescription = null,
                            size = 18.dp,
                            tint = contentColor
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(6.dp)
                    ) {
                        FaIcon(
                            iconId = iconId,
                            contentDescription = null,
                            size = 14.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderListItem(
    reminder: Reminder,
    colorHex: String,
    onCardClick: () -> Unit,
    onCheckToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    val isOverdue = remember(reminder.dateTimeMillis) {
        reminder.dateTimeMillis < System.currentTimeMillis()
    }

    val formattedDate = remember(reminder.dateTimeMillis) {
        val sdf = SimpleDateFormat("EEE, MMM dd 'at' hh:mm a", Locale.getDefault())
        sdf.format(Date(reminder.dateTimeMillis))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("reminder_item_${reminder.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onCheckToggle() },
                modifier = Modifier
                    .testTag("reminder_checkbox_${reminder.id}")
                    .padding(end = 6.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = reminder.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (reminder.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = reminder.notes,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = reminder.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FaIcon(
                            iconId = R.drawable.ic_fa_clock,
                            contentDescription = "Alarm icon",
                            size = 11.dp,
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (reminder.isNotificationEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else Color.Transparent
                    )
                    .padding(8.dp)
            ) {
                FaIcon(
                    iconId = R.drawable.ic_fa_bell,
                    contentDescription = if (reminder.isNotificationEnabled) "Notification enabled" else "Notification disabled",
                    size = 16.dp,
                    tint = if (reminder.isNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }
        }
    }
}
