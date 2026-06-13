package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Category
import com.example.data.Reminder
import com.example.data.ReminderRepository
import com.example.util.NotificationScheduler
import com.example.util.SettingsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardStats(
    val total: Int = 0,
    val today: Int = 0,
    val upcoming: Int = 0,
    val completed: Int = 0
)

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository
    private val settingsHelper = SettingsHelper(application)

    val allReminders: StateFlow<List<Reminder>>
    val allCategories: StateFlow<List<Category>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _appThemeState = MutableStateFlow(settingsHelper.theme)
    val appThemeState = _appThemeState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ReminderRepository(database.reminderDao())
        
        allReminders = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Combined filtered reminders
    val filteredReminders: StateFlow<List<Reminder>> = combine(
        allReminders,
        _searchQuery,
        _selectedCategoryFilter
    ) { reminders, query, filter ->
        reminders.filter { reminder ->
            val matchQuery = query.isEmpty() ||
                    reminder.title.contains(query, ignoreCase = true) ||
                    reminder.notes.contains(query, ignoreCase = true) ||
                    reminder.category.contains(query, ignoreCase = true)
            
            val matchFilter = filter == null || reminder.category.equals(filter, ignoreCase = true)
            
            matchQuery && matchFilter
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive stats combining
    val stats: StateFlow<DashboardStats> = allReminders.combine(filteredReminders) { all, _ ->
        val now = System.currentTimeMillis()
        val calendarNow = Calendar.getInstance()
        
        var total = 0
        var today = 0
        var upcoming = 0
        var completed = 0

        for (reminder in all) {
            if (reminder.isCompleted) {
                completed++
            } else {
                total++
                
                // Today check
                val calReminder = Calendar.getInstance().apply { timeInMillis = reminder.dateTimeMillis }
                val isToday = calReminder.get(Calendar.YEAR) == calendarNow.get(Calendar.YEAR) &&
                        calReminder.get(Calendar.DAY_OF_YEAR) == calendarNow.get(Calendar.DAY_OF_YEAR)
                
                if (isToday) {
                    today++
                }

                // Upcoming check
                if (reminder.dateTimeMillis > now) {
                    upcoming++
                }
            }
        }

        DashboardStats(total = total, today = today, upcoming = upcoming, completed = completed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    // Reminders CRUD
    fun addReminder(
        title: String,
        notes: String,
        category: String,
        dateTimeMillis: Long,
        isNotificationEnabled: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("Title is required.")
            return
        }
        if (dateTimeMillis <= System.currentTimeMillis()) {
            onError("Past date and time is not allowed.")
            return
        }

        viewModelScope.launch {
            try {
                val newReminder = Reminder(
                    title = title,
                    notes = notes,
                    category = category,
                    dateTimeMillis = dateTimeMillis,
                    isCompleted = false,
                    isNotificationEnabled = isNotificationEnabled
                )
                val id = repository.insertReminder(newReminder)
                val insertedReminder = newReminder.copy(id = id.toInt())
                
                if (isNotificationEnabled) {
                    NotificationScheduler.scheduleReminder(getApplication(), insertedReminder)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Could not save reminder.")
            }
        }
    }

    fun updateReminderDetails(
        id: Int,
        title: String,
        notes: String,
        category: String,
        dateTimeMillis: Long,
        isNotificationEnabled: Boolean,
        isCompleted: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) {
            onError("Title is required.")
            return
        }
        if (dateTimeMillis <= System.currentTimeMillis() && !isCompleted) {
            onError("Past date and time is not allowed for active reminders.")
            return
        }

        viewModelScope.launch {
            try {
                val updatedReminder = Reminder(
                    id = id,
                    title = title,
                    notes = notes,
                    category = category,
                    dateTimeMillis = dateTimeMillis,
                    isCompleted = isCompleted,
                    isNotificationEnabled = isNotificationEnabled
                )
                repository.updateReminder(updatedReminder)
                
                // Maintain schedules
                NotificationScheduler.cancelReminder(getApplication(), id)
                if (isNotificationEnabled && !isCompleted && dateTimeMillis > System.currentTimeMillis()) {
                    NotificationScheduler.scheduleReminder(getApplication(), updatedReminder)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Could not update reminder.")
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            NotificationScheduler.cancelReminder(getApplication(), reminder.id)
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.updateReminder(updated)
            
            NotificationScheduler.cancelReminder(getApplication(), reminder.id)
            if (!updated.isCompleted && updated.isNotificationEnabled && updated.dateTimeMillis > System.currentTimeMillis()) {
                NotificationScheduler.scheduleReminder(getApplication(), updated)
            }
        }
    }

    // Categories
    fun createCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, colorHex = colorHex))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Theme Settings
    fun updateTheme(newTheme: String) {
        settingsHelper.theme = newTheme
        _appThemeState.value = newTheme
    }

    fun getNotificationPreferences(): Pair<Boolean, Boolean> {
        return Pair(settingsHelper.isSoundEnabled, settingsHelper.isVibrationEnabled)
    }

    fun updateNotificationPrefs(sound: Boolean, vibration: Boolean) {
        settingsHelper.isSoundEnabled = sound
        settingsHelper.isVibrationEnabled = vibration
    }
}
