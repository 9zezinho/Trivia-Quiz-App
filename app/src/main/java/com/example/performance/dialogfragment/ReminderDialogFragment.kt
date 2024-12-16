package com.example.performance.dialogfragment

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.performance.FirebaseAuthManager
import com.example.performance.FirebaseManager
import com.example.performance.NotificationReceiver
import com.example.performance.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

/**
 * This class is a DialogFragment that allows users to set a daily
 * reminder for a specific time of the day. It uses time picker to
 * select the reminder time and saves to Firebase. This class is
 * also responsible for allowing the user to cancel or clear the
 * reminder
 */
class ReminderDialogFragment: DialogFragment() {
    private lateinit var set: MaterialButton
    private lateinit var cancel: MaterialButton
    private lateinit var setReminderText: MaterialTextView
    private lateinit var cancelText: MaterialTextView

    private var auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    private var logcatTag = "Reminder" //for debugging

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Inflate the dialog view
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog,
            null)
        set = dialogView.findViewById(R.id.setBtn)
        cancel = dialogView.findViewById(R.id.cancelBtn)
        setReminderText = dialogView.findViewById(R.id.reminderText)
        cancelText = dialogView.findViewById(R.id.cancelText)

        checkAndRequestExactAlarmPermission() //checks for the permission

        restoreReminderTime() //restore any previously set reminder

        // Set a click listener for the "Set" button
        set.setOnClickListener {showTimePicker() }

        // Set a click listener for the "Cancel" button
        cancel.setOnClickListener { cancelReminder() }

        // Create and return teh dialog with the custom view
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") {
                dialog,_ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * This method checks and requests permission to schedule exact alarms
     * for devices running on Android 13
     */
    private fun checkAndRequestExactAlarmPermission() {
        val alarmManager = requireContext().
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent =
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent) //launch permission request screen
                } catch (e: ActivityNotFoundException) {
                    Log.e(logcatTag,
                        "Exact Alarm Permission screen not found", e)
                }
            }
        }
    }

    //Display Message using SnackBar
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    /**
     * Show a time picker dialog to allow the user to select
     * teh reminder time
     */
    private fun showTimePicker() {
        // Build Material Time Picker with 24hr format
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Select Notification Time")
            .build()

        // Display MaterialTimePicker in screen
        picker.show(parentFragmentManager, "timePicker")

        // Capture the time once users selects it
        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMin = picker.minute

            Log.i(logcatTag, "hr,min: $selectedHour,$selectedMin")

            //Save the reminder to Firebase
            FirebaseManager.saveUserReminder(userID,
                selectedHour, selectedMin, {
                    Log.i(logcatTag, "Reminder saved to Firebase")
                }, {e ->
                    Log.e(logcatTag, "Failed to save reminder: ${e.message}")
                })

            // Save the selected time to SharedPref
            val sharedPref =
                requireContext().getSharedPreferences("ReminderPrefs",
                    Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("hour", selectedHour)
                putInt("min", selectedMin)
                apply()
            }

            //Schedule notification with selected hour and minute
            scheduleDailyNotification(selectedHour,selectedMin)
        }
    }

    /**
     * This function schedules a daily notification based on teh
     * selected time
     * @param hour is the Hour
     * @param minute is the Minute
     */
    private fun scheduleDailyNotification(hour: Int, minute: Int) {
        val alarmManager = requireContext()
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        Log.i(logcatTag, "Scheduling alarm for $hour:$minute")

        // Set the reminder using Calendar object
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // If the time is before the current time, schedule for the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set repeating alarm for daily notification
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, //wake up device to trigger alarm
            calendar.timeInMillis,
            pendingIntent
        )
        Log.i(logcatTag, "Alarm set for ${calendar.time}")

        val formattedTime = String.format("%02d:%02d", hour, minute)

        setReminderText.text =
            getString(R.string.daily_reminder_text, formattedTime)
        displayMessage(set, "Reminder Set!!")

    }

    /**
     * This function retrieves the stored reminder time from Firebase
     * and updates the UI
     */
    private fun restoreReminderTime(){
        FirebaseManager.fetchUserReminder(userID, {
            hour, minute ->
            if (hour !=-1L && minute != -1L){
                val formattedTime = String.format("%02d:%02d", hour, minute)
                setReminderText.text =
                    getString(R.string.daily_reminder_text, formattedTime)
            } else {
                Log.i(logcatTag, "No new reminder set")
            }
        }, {e ->
            Log.e(logcatTag, "Failed to fetch reminder: ${e.message}")
        })
    }

    /**
     * This function cancels the reminder by removing the scheduled alarm
     */
    private fun cancelReminder(){
        val alarmManager =
            requireContext().
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        Log.i(logcatTag, "Reminder cancelled")

        // Remove reminder from Firebase
        FirebaseManager.deleteUserReminder(userID,{
            Log.i(logcatTag, "Reminder deleted from Firebase")
        }, {e ->
            Log.e(logcatTag, "Failed to delete reminder form Firebase: ${e.message}")
        })

        // Clear the saved time from SharedPref
        val sharedPref = requireContext().getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            clear()
            apply()
        }

        setReminderText.text = getString(R.string.no_reminder_set)
        displayMessage(cancel, "Reminder Cancelled!!")
    }
}