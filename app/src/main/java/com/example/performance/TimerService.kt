package com.example.performance

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * This class is a background service that manages a centralized timer for
 * resetting user points every 5 minutes. It integrates with Firebase to
 * coordinate resets across all users and awards prizes to the top three
 * during each 5 minute interval
 */

class TimerService : Service() {

    private val logcatTag = "TimerService" //for debugging
    private lateinit var database: DatabaseReference
    private var handler: Handler = Handler(Looper.getMainLooper())
    private val resetInterval: Long = 300000// 5 Min

    private var isResetInProgress = false

    override fun onCreate() {
        super.onCreate()

        database = FirebaseManager.userReference
        centralizedTimer()
        observeTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int,
                                startId: Int): Int {
        return START_STICKY // service runs continuously in background
    }

    override fun onDestroy() {
        super.onDestroy()
        // remove any scheduled task when the service is destroyed
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // NO binding as this is run in background
    }

    // Function to ensure a centralized timer is set in Firebase
    private fun centralizedTimer() {
        database
            .child("nextReset")
            .addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentTime = System.currentTimeMillis()
                    val nextReset = snapshot.value as? Long

                    if (nextReset == null || currentTime > nextReset) {
                        val newResetTime = currentTime + resetInterval
                        database.child("nextReset").setValue(newResetTime)

                        // Convert to readable format
                        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()).format(newResetTime)
                        Log.i(logcatTag, "Reset timer initialized at: $formattedTime")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(logcatTag, "Failed to access timer node: ${error.message}")
                }
            })
    }

    // Function to observe changes to the "nextReset" timeStamp in Firebase
    private fun observeTimer() {
        database
            .child("nextReset").addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val nextReset = snapshot.value as? Long ?: 0L
                    val currentTime = System.currentTimeMillis()

                    val timeUntilReset = nextReset - currentTime
                    if (timeUntilReset > 0 && !isResetInProgress) {
                        scheduleReset(timeUntilReset)
                        Log.i(logcatTag, "Schedule Reset")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(logcatTag, "Error observing the timer: ${error.message}")
                }
            })
    }

    // Schedule a local reset based on the centralized timer
    private fun scheduleReset( timeUntilReset: Long) {
        handler.postDelayed({
            if(!isResetInProgress){
                resetFiveMinPoints()
                Log.i(logcatTag, "Points reset successfully")
            }
        }, timeUntilReset)
    }

    /**
     * This function Resets "current5minPoints" for all users in Firebase
     * to 0 and schedules the next reset. Awards prizes to the top 3
     * based on their points
     */
    private fun resetFiveMinPoints() {
        if (isResetInProgress) {
            Log.i(logcatTag, "Reset already in progress, skipping")
            return
        }
        isResetInProgress = true

        // Call identifyAndAwardTop3 before resetting points
        identifyAndAwardTop3()

        database.get().addOnSuccessListener { results ->
            for(users in results.children) {
                val userID = users.key.toString()
                val resetPoints = 0

                //Update the new value of current5minPoints for everyone
                FirebaseManager.saveData(
                    userID = userID,
                    path = "current5minPoints",
                    data = resetPoints,
                    onSuccess = {
                        Log.i(logcatTag, "New 5min Points updated")},

                    onFailure = {e ->
                        Log.e(logcatTag, "Error resetting points: ${e.message}")
                        isResetInProgress = false
                    }
                )
            }
            val nextResetValue = results.child("nextReset").value as? Long?: 0L

            // Update the global timer
            val newResetTime =  nextResetValue + resetInterval
            database.child("nextReset")
                .setValue(newResetTime)
            isResetInProgress = false
        }.addOnFailureListener { e ->
            Log.e(logcatTag, "Error resting points for all users: ${e.message}")
        }
    }

    // Function to identify top 3 users with highest "current5minPoints"
    private fun  identifyAndAwardTop3(){
        database.get().addOnSuccessListener { result ->
            val usersWithPoints = mutableListOf<Pair<String,Long>>()

            for (userResult in result.children) {
                val userID = userResult.key.toString()
                val current5minPoints = userResult.child("current5minPoints")
                    .value as? Long?: 0L

                usersWithPoints.add(Pair(userID, current5minPoints))
            }
            val topUsers = usersWithPoints
                .sortedByDescending { it.second } //returns new list
                .take(3) //takes top 3
                .filter { it.second > 0 } // filter out users with 0 points
                .map {it.first} //maps to user IDs

            awardPrizes(topUsers)
        }.addOnFailureListener { e ->
            Log.e(logcatTag, "Failed to fetch users: ${e.message}")
        }
    }

    // Function to award coins to top 3 users based on their rank
    private fun awardPrizes(topUsers: List<String>) {
        topUsers.forEachIndexed { index, userID ->
            val prize = when (index){
                0 -> 5 //first place
                1 -> 3 //second place
                2 -> 1 //third place
                else -> 0
            }

            FirebaseManager.fetchUserCoins(
                userID = userID,
                onSuccess = {currentCoins ->
                    val newCoins = currentCoins + prize
                    FirebaseManager.updateUserCoins(
                        userID = userID,
                        newCoins = newCoins,
                        onSuccess = {
                        Log.i(logcatTag, "Awarded $prize coins to user $userID")
                        }, onFailure = {e ->
                            Log.e(logcatTag, "Failed to update coins for user $userID: ${e.message}")
                        }
                    )
                }, onFailure = {e ->
                    Log.e(logcatTag, "Failed to fetch coins for user $userID: ${e.message}")
                }
            )
        }
    }
}