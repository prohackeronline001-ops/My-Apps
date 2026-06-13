package com.example.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allCategories: Flow<List<Category>> = reminderDao.getAllCategories()

    suspend fun getReminderById(id: Int): Reminder? {
        return reminderDao.getReminderById(id)
    }

    fun getReminderByIdFlow(id: Int): Flow<Reminder?> {
        return reminderDao.getReminderByIdFlow(id)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Int) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun insertCategory(category: Category): Long {
        return reminderDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        reminderDao.deleteCategory(category)
    }
}
