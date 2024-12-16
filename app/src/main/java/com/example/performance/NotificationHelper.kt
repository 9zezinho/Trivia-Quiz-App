package com.example.performance


import android.app.NotificationChannel
import android.content.Context
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth

/**
 * This class is a utility class for creating and managing notification
 * channels and sending notifications to user.It is responsible for
 * creating a notification, displaying it to user, and launching
 * the correct activity based on whether user is logged in or not.
 */
class NotificationHelper (private val context: Context){

    // constants for channel ID and notification ID
    companion object {
        const val  CHANNEL_ID_ONE = "NOTIFICATION_CHANNEL_ID_ONE"
        const val NOTIFICATION_ID = 1
    }

    init{
        createNotificationChannel()
    }

    //This function creates a notification channel for daily reminders
    private fun createNotificationChannel(){
        // Channel for daily Reminder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //channel properties such as name and descriptions
            val name = "Daily Notification Channel"
            val descriptionText = "Channel for daily notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

           // create notification channel with above properties
            val channel = NotificationChannel(
                CHANNEL_ID_ONE, name, importance).apply {
                description = descriptionText
            }

            // get NotificationManger tro create notification channel
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * This function sends a notification to a user. The notification informs the
     * user about their daily reminder. Depending on whether the user is logged in or
     * not , it opens the appropriate activity
     */
    fun sendNotification() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        //Determine which activity to open based on the login state
        val targetActivity = if(currentUser != null) {
            // User is logged in, open HomeActivity
            HomeActivity::class.java
        } else {
            // User is not logged in, open MainActivity
            MainActivity::class.java
        }

        //Create an intent to open appropriate activity
        val activityIntent = Intent(context, targetActivity)
        activityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Pending Intent to trigger the NotificationReceiver when notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to open the HomeActivity when the notification is clicked
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ONE)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Daily Reminder")
            .setContentText("Don't forget to get your streak today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Get the NotificationManger to actually send teh notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        // Send the notification with the specified ID
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}