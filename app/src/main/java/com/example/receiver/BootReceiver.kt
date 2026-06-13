package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.util.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val database = AppDatabase.getDatabase(context, this)
                    val dao = database.reminderDao()
                    
                    // Retrieve future uncompleted reminders
                    val reminders = dao.getAllReminders().first()
                    val now = System.currentTimeMillis()

                    for (reminder in reminders) {
                        if (!reminder.isCompleted && reminder.dateTimeMillis > now) {
                            NotificationScheduler.scheduleReminder(context, reminder)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
