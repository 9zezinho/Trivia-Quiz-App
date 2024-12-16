package com.example.performance

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log

/**
 * This class is to handle the notification triggers at scheduled times.
 * It sends a notification to the user and reschedules itself for the
 * next day
 */
class NotificationReceiver : BroadcastReceiver() {

    private var logcatTag = "NotificationReceiverTag" // Tag for logging purpose

    override fun onReceive(context: Context, intent: Intent?) {

        //create instance of NotificationHelper to send notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.sendNotification()

        // Reschedule the notification for the next day
        rescheduleNotification(context, intent)
    }

    /**
     * Reschedules the notification to be sent at the same time the next day
     * Uses AlarmManager to schedule the next trigger time.
     * @param context is context from which the alarm is being set
     * @param intent is the intent passed along with the alarm,
     *              used to trigger teh same broadcast.
     */
    private fun rescheduleNotification(context: Context,intent:Intent?) {
        // Set the notification for the next day
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)  // Add one day to the current day
        }

        // Get AlarmManager system service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent!!,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Schedule the alarm for the next day (setExactAndAllowWhileIdle is used for better accuracy)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // catch exception if the app does not have permission to set alarm
            Log.e(logcatTag,
                "Permission denied for scheduling exact alarms", e)
        }
    }
}