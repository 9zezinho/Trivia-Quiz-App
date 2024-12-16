package com.example.performance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * This class displays the results of a completed quiz session
 * It shows the number of correct and incorrect answers, total
 * points scored, the user's highest streak and the coins earned
 * during the session.
 */

class QuizCompletedActivity: AppCompatActivity() {

    private var logcatTag = "CompletedTag" //for debugging
    private lateinit var totalCorrectAns: MaterialTextView
    private lateinit var totalWrongAns: MaterialTextView
    private lateinit var homeButton: MaterialButton
    private lateinit var tryAgainButton: MaterialButton
    private lateinit var points: MaterialTextView
    private lateinit var streak: MaterialTextView
    private lateinit var coins: MaterialTextView

    private var auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quizcompleted)

        // Retrieve the correct and incorrect no of Questions
        val correctAns = intent.getLongExtra("correctAnsCount", 0)
        val wrongAns = intent.getLongExtra("wrongAnsCount",0)
        val sessionPoints = intent.getLongExtra("points",0)
        val highestStreak = intent.getLongExtra("streak", 0)
        val coinsEarned = intent.getLongExtra("coins", 0)
        Log.i(logcatTag, "Correct: $correctAns & Incorrect: $wrongAns" +
                " & Session Points: $sessionPoints")
        Log.i(logcatTag, "Streak: $highestStreak & Coins: $coinsEarned")

        totalCorrectAns = findViewById(R.id.totalCorrectAns)
        totalWrongAns = findViewById(R.id.totalWrongAns)
        homeButton = findViewById(R.id.homeBtn)
        tryAgainButton = findViewById(R.id.tryAgainBtn)
        points = findViewById(R.id.pointsScored)
        streak = findViewById(R.id.highestStreak)
        coins = findViewById(R.id.coinsEarned)

        totalCorrectAns.text = getString(R.string.total_correct, correctAns)
        totalWrongAns.text = getString(R.string.total_wrong, wrongAns)
        points.text = getString(R.string.scored_text, sessionPoints)
        streak.text = getString(R.string.streak, highestStreak)
        coins.text = getString(R.string.coins_earned_text, coinsEarned)

        // Fetch the coins and update with new earned ones
        FirebaseManager.fetchUserCoins(userID,
            onSuccess = {
                    initialCoins ->
                val newCoins = initialCoins + coinsEarned
                Log.i(logcatTag, "Current Coins: $initialCoins")

                // Update teh user's total coins in Firebase
                FirebaseManager.updateUserCoins(userID,
                    newCoins,
                    onSuccess = {
                        Log.i(logcatTag, "Successfully Coins Updated")
                    }, onFailure = {e ->
                        Log.e(logcatTag, "Failed to update coins: ${e.message}")
                    })
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to fetch coins: ${e.message}")
            }
        )

        // Set the onClick listener for the home button
        homeButton.setOnClickListener {
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Set the onClick listener for the tryAgain Button
        tryAgainButton.setOnClickListener {
            intent = Intent(this, ChooseActivity::class.java)
            startActivity(intent)
        }
    }
}