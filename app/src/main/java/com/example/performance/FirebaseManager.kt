package com.example.performance


import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * FirebaseManager is a utility class responsible for interacting with Firebase
 * Realtime Database.It provides various methods to interact with user-related
 * data, such as saving data, fetching user history, points, level, lifelines,
 * and coins as well as updating values in Firebase database. This class is
 * responsible to centralize Firebase operations and make them reusable and
 * easy to access.
 */

private var logcatTag = "FirebaseM"
object FirebaseManager {

    // Instance to access the Firebase Realtime Database
    private val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("path-to-firebase-real-time-database")
    }

    // Root reference
    val rootReference: DatabaseReference
        get() = firebaseDatabase.reference

    // User reference
    val userReference: DatabaseReference
        get() = firebaseDatabase.reference.child("users")

    // Function to save data to specific user's path in database
    fun <T> saveData(
        userID: String,
        path: String,
        data: T,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
         userReference.child(userID).child(path).setValue(data)
             .addOnSuccessListener {
                 onSuccess()
             }.addOnFailureListener{e -> onFailure(e)}
    }

    // Function to fetch the user's session history from the database
    fun fetchSessionHistory(
        userID: String,
        onSuccess: (sessions: List<Session>) -> Unit,
        onFailure: (Exception) -> Unit)
    {
        val sessionList = mutableListOf<Session>()

        // Fetch session for specific user
        userReference.child(userID).child("sessions").get()
            .addOnSuccessListener { results ->

                for (sessionResult in results.children) {
                    val sessionID = sessionResult.key
                    val timeStamp = sessionResult.child("timeStamp")
                        .value as? Long?: 0L
                    val wins = sessionResult.child("wins")
                        .value as? Long?: 0L
                    val loss = sessionResult.child("loss")
                        .value as? Long?: 0L

                    val questions = mutableListOf<Question>()
                    // Fetch questions in the session
                    for (questionResult in sessionResult
                        .child("questions").children) {
                        val questionId = questionResult.child("questionId")
                            .value as? String?: ""
                        val questionText = questionResult.child("questionText")
                            .value as? String?: ""
                        val correctAnswer = questionResult.child("correctAnswer")
                            .value as? String?: ""
                        val userResponse = questionResult.child("userResponse")
                            .value as? String?: ""
                        questions.add(Question(questionId, questionText,
                            correctAnswer, userResponse))
                    }
                    sessionList.add(Session(sessionID!!, timeStamp,
                        questions, wins, loss))
                }
                onSuccess(sessionList)
            }.addOnFailureListener { e ->
                onFailure(e)
            }
    }

    // Function to fetch a user's total points and current level
    fun fetchUserPointsAndLevel(
        userID: String,
        onSuccess: (
            username: String,
            totalPoints: Long,
            currentLevel: Long) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        userReference.child(userID).child("sessions").get()
            .addOnSuccessListener { results ->
                var totalPoints: Long = 0

                // calculate total point from all sessions
                for (sessionResult in results.children) {
                    val points = sessionResult.child("points")
                        .value as? Long?: 0L
                    totalPoints += points
                }

                // fetch user's username and level
                userReference.child(userID).get()
                    .addOnSuccessListener { levelResult ->
                        val username = levelResult.child("username")
                            .value as? String?: ""
                        val currentLevel = levelResult.child("level")
                            .value as? Long?: 0L
                        onSuccess( username, totalPoints, currentLevel)

                    }.addOnFailureListener { e -> onFailure(e) }
            }.addOnFailureListener { e -> onFailure(e) }
    }

    // Function to update user's total points and levle
    fun updateUserPointsAndLevel(
        userID: String,
        totalPoints: Long,
        currentLevel: Long,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Map to update new values
        val updates = mapOf(
            "level" to currentLevel,
            "totalPoints" to totalPoints
        )
        userReference.child(userID).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener{e -> onFailure(e)}
    }

    // Function to fetch user's lifelines(fifty-fifty and skip)
    fun fetchUserLife(
        userID: String,
        onSuccess:(
            fiftyFifty: Long,
            skip: Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userReference.child(userID).get()
            .addOnSuccessListener { result ->
                val fiftyFiftyLifeLine = result.child("fifty")
                    .value as? Long?: 0L
                Log.i(logcatTag,"fifty: $fiftyFiftyLifeLine")
                val skipLifeLine = result.child("skip")
                    .value as? Long?: 0L
                onSuccess(fiftyFiftyLifeLine,skipLifeLine)
            }.addOnFailureListener{e ->
                onFailure(e)
            }
    }

    // Function to update a user's lifelines
    fun updateUserLifelines(userID: String,
        fiftyFifty: Long,
        skip: Long, onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Map to update new values
        val updates = mapOf(
            "fifty" to fiftyFifty,
            "skip" to skip
        )
        userReference.child(userID).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }


    // Function to fetch user's coins
    fun fetchUserCoins(
        userID: String,
        onSuccess: (coins: Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userReference.child(userID).get()
            .addOnSuccessListener { result ->
                val coins = result.child("coins")
                    .value as? Long?: 0L
                onSuccess(coins)
        }.addOnFailureListener { e -> onFailure(e)}
    }

    // Function to update user's coin balance
    fun updateUserCoins(
        userID: String,
        newCoins: Long,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Map to update new values
        val updates = mapOf(
            "coins" to newCoins
        )
        userReference.child(userID).updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Function to fetch user's 5-minute points
    fun fetch5minPoints(
        userID: String,
        onSuccess:(current5minPoints: Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userReference.child(userID).get()
            .addOnSuccessListener { result ->
                val current5minPoints = result.child("current5minPoints")
                    .value as? Long?: 0L
                onSuccess(current5minPoints)
            }.addOnFailureListener { e -> onFailure(e)}
    }

    // Function to save user's reminder time
    fun saveUserReminder(userID: String, hour: Int, minute: Int,
                         onSuccess: () -> Unit,
                         onFailure: (Exception) -> Unit){
        val reminderData = mapOf(
            "hour" to hour,
            "minute" to minute
        )
        userReference.child(userID).child("reminder")
            .setValue(reminderData).addOnSuccessListener {
                onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Function to fetch user's reminder time
    fun fetchUserReminder(
        userID: String,
        onSuccess: (Long, Long) -> Unit,
        onFailure: (Exception) -> Unit){

        userReference.child(userID).child("reminder").get()
            .addOnSuccessListener { result ->
                val hour = result.child("hour")
                    .value as? Long?: 0L
                val minute = result.child("minute")
                    .value as? Long?: 0L
                onSuccess(hour,minute)
            }.addOnFailureListener { e -> onFailure(e) }
    }

    // Function to delete a user's reminder
    fun deleteUserReminder(
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit) {

        userReference.child(userID).child("reminder").removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}