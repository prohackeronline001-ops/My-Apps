package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String,
    val category: String,
    val dateTimeMillis: Long,
    val isCompleted: Boolean = false,
    val isNotificationEnabled: Boolean = true
)
