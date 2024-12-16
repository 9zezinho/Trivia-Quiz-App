package com.example.performance

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.util.Log
import android.widget.ImageView
import com.example.performance.dialogfragment.CartDialogFragment
import com.example.performance.dialogfragment.ReminderDialogFragment
import com.example.performance.dialogfragment.SettingsDialogFragment
import com.example.performance.dialogfragment.UserNameDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlin.math.pow

/**
 * HomeActivity is the main screen for the application, where users interact
 * with various features such as playing the quiz, viewing their history,
 * checking their points, and managing settings. It serves as the central hub
 * of the app. The activity interacts with Firebase to fetch and update user
 * data like points, level, lifelines and coins.
 */

class HomeActivity : AppCompatActivity(){
    private lateinit var playButton: MaterialButton
    private lateinit var historyButton: MaterialButton
    private lateinit var timerButton: MaterialButton
    private lateinit var aboutButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var setReminderTextView: MaterialTextView
    private lateinit var points: MaterialTextView
    private lateinit var level: MaterialTextView
    private lateinit var leaderBoardButton: MaterialButton
    private lateinit var fiftyFiftyTextView: MaterialTextView
    private lateinit var skipTextView: MaterialTextView
    private lateinit var categoriesButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var shoppingCart: MaterialButton
    private lateinit var coinText: MaterialTextView
    private lateinit var profileImage: ImageView

    private var logcatTag = "HomeTag" // for debugging


    private var auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    private val REQUEST_CODE_POST_NOTIFICATIONS = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        playButton = findViewById(R.id.playBtn)
        historyButton = findViewById(R.id.home_page_historyBtn)
        timerButton = findViewById(R.id.timerBtn)
        aboutButton = findViewById(R.id.aboutBtn)
        logoutButton = findViewById(R.id.logoutBtn)
        setReminderTextView = findViewById(R.id.setReminder)
        points = findViewById(R.id.points)
        level = findViewById(R.id.homeLevel)
        leaderBoardButton = findViewById(R.id.leaderboardBtn)
        fiftyFiftyTextView = findViewById(R.id.fiftyFifty)
        skipTextView = findViewById(R.id.skip)
        categoriesButton = findViewById(R.id.categoriesBtn)
        settingsButton = findViewById(R.id.settingsBtn)
        shoppingCart = findViewById(R.id.cart)
        coinText = findViewById(R.id.coinText)
        profileImage = findViewById(R.id.profilePic)

        //Check for the first time user
        val homeUsername = findViewById<MaterialTextView>(R.id.homeUsername)
        checkForFirstTimeUser(userID, homeUsername)

        // Start Timer Service
        val timerServiceIntent =
            Intent(this, TimerService::class.java)
        startService(timerServiceIntent)

        // Set up click listeners for various buttons in the activity
        logoutButton.setOnClickListener{logoutUser()}

        // Navigation for quiz play
        playButton.setOnClickListener {
            val intent = Intent(this,ChooseActivity:: class.java)
            startActivity(intent)
        }

        // Navigation to history screen
        historyButton.setOnClickListener {
            intent = Intent(this, HistoryActivity:: class.java)
            startActivity(intent)
        }

        // Request notification permission if needed
        checkAndReqNotificationPermission()

        // Display the reminder dialog on timer button
        timerButton.setOnClickListener{
            val dialog = ReminderDialogFragment()
            dialog.show(supportFragmentManager, "ReminderDialog")
        }

        // Get the points and level from Firebase
        fetchUserPointsAndLevel()

        // Fetch lifeline points from Firebase using FirebaseManager object
        fetchUserLifeLines()

        // Fetch user coins from the Firebase using FirebaseManager object
        fetchUserCoins()

        // Navigate to Leaderboard screen
        leaderBoardButton.setOnClickListener{
            intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        // Open Settings dialog
        settingsButton.setOnClickListener{
            val dialog = SettingsDialogFragment()
            dialog.show(supportFragmentManager, "SettingsDialog")
        }

        // Navigate to Categories
        categoriesButton.setOnClickListener{
            intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        // Open shopping cart dialog
        shoppingCart.setOnClickListener {
            val dialog = CartDialogFragment()
            dialog.show(supportFragmentManager, "CartDialog")
            dialog.onDismissAction = {
                // refresh coins and lifelines after cart interaction
                fetchUserCoins()
                fetchUserLifeLines()
            }
        }

        // Open user Profile dialog
        profileImage.setOnClickListener{
            val dialog = UserNameDialogFragment()
            dialog.show(supportFragmentManager, "UserNameDialog")
            dialog.onDismissAction = {
                FirebaseManager.fetchUserPointsAndLevel(
                    userID = userID,
                    onSuccess = { username, _, _ ->
                        homeUsername.text = username
                        Log.i(logcatTag, "£Fetched username: $username")
                    }, onFailure = {e ->
                        Log.e(logcatTag, "Failed to fetch username: ${e.message}")
                    }
                )
            }
        }

        // Open About screen on about button click
        aboutButton.setOnClickListener{
            val intent  = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Fetch updated coins, level, points and lifelines
        fetchUserCoins()
        fetchUserLifeLines()
        fetchUserPointsAndLevel()
    }

    // Log out User
    private fun logoutUser(){
        auth.signOut()
        displayMessage(logoutButton, getString(R.string.logout_successful))

        //Redirect back to MainActivity(login screen)
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //SnackBar to display the message
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Function to check and request notification permission for notification
    private fun checkAndReqNotificationPermission(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                    , REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Function to check if the user is logging in for first time
     * @param userID is the ID of the user
     * @param homeUsername is textview to view the username of user
     */
    private fun checkForFirstTimeUser(userID: String,
                                      homeUsername: MaterialTextView){
        val sharedPref =
            getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val isFirstTimeUser =
            sharedPref.getBoolean("IS_FIRST_TIME_USER", true)
        if(isFirstTimeUser) {
            val dialog = UserNameDialogFragment()
            dialog.show(supportFragmentManager, "UserNameDialog")
            dialog.onDismissAction = {
                FirebaseManager.fetchUserPointsAndLevel(
                    userID = userID,
                    onSuccess = { username, _, _ ->
                        homeUsername.text = username
                        Log.i(logcatTag, "£Fetched username: $username")
                    }, onFailure = {e ->
                        Log.e(logcatTag,
                            "Failed to fetch username: ${e.message}")
                    }
                )
            }

            // Mark user as not first-time after showing the dialog
            val editor = sharedPref.edit()
            editor.putBoolean("IS_FIRST_TIME_USER", false)
            editor.apply()
        } else {
            // Fetch username if it's not first time user
            FirebaseManager.fetchUserPointsAndLevel(
                userID = userID,
                onSuccess = { username, _, _ ->
                    homeUsername.text = username
                    Log.i(logcatTag, "£Fetched username: $username")
                }, onFailure = {e ->
                    Log.e(logcatTag,
                        "Failed to fetch username: ${e.message}")
                }
            )
        }
    }


    // Fetch and update the user's points and level from Firebase
    private fun fetchUserPointsAndLevel() {
        FirebaseManager.fetchUserPointsAndLevel(
            userID = userID,
            onSuccess = { _,totalPoints, currentLevel ->
                Log.i(logcatTag,
                    "£Fetched Points: $totalPoints, Current Level: $currentLevel")
                val (newLevel, remainingPoints) =
                    checkLevelUpPoints(totalPoints, currentLevel)

                // Update the user's points and level
                FirebaseManager.updateUserPointsAndLevel(
                    userID = userID,
                    currentLevel = newLevel,
                    totalPoints = remainingPoints,
                    onSuccess = {
                        level.text = getString(R.string.level, newLevel.toString())
                        points.text = getString(R.string.points, remainingPoints.toString())
                        Log.i(logcatTag, "User leveled up to level: $newLevel")
                    }, onFailure = {e ->
                        Log.e(logcatTag,
                            "Failed to update user level: ${e.message}")
                    }
                )
            }, onFailure = {e ->
                Log.e(logcatTag,
                    "Failed to fetch user points and level: ${e.message}")
            }
        )
    }

    // Calculate required points for the next level based on the current level
    private fun calculatePointsForLevel(
        level: Long,
        basePoints: Long = 20,
        difficultyFactor: Double = 1.2)
    : Int {
            return (basePoints * level.toDouble().pow(difficultyFactor)).toInt()
    }

    // Check if the user has enough points to level up and return the new level
    private fun checkLevelUpPoints(
        totalPoints: Long,
        currentLevel: Long,
        basePoints: Long = 20,
        difficultyFactor: Double = 1.2)
    : Pair <Long, Long> {
        val pointsForNextLevel = calculatePointsForLevel(
            currentLevel, basePoints, difficultyFactor)
        return if (totalPoints >= pointsForNextLevel) {
            val newLevel = currentLevel + 1
            val remainingPoints = totalPoints - pointsForNextLevel
            Pair(newLevel, remainingPoints)
        } else {
            Pair(currentLevel, totalPoints)
        }
    }

    // Fetch the LifeLines points.
    private fun fetchUserLifeLines() {
        FirebaseManager.fetchUserLife(userID,
            onSuccess = {
                    fiftyFifty, skip ->
                Log.i(logcatTag, "50-50: $fiftyFifty, Skip: $skip")
                fiftyFiftyTextView.text =
                    getString(R.string.fiftyFifty, fiftyFifty.toString())
                skipTextView.text =
                    getString(R.string.skip, skip.toString())
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to fetch lifelines: ${e.message}")
            })
    }

    // Fetch User coins
    private fun fetchUserCoins(){
        FirebaseManager.fetchUserCoins(userID,
            onSuccess = {
                    coins ->
                Log.i(logcatTag, "Coins: $coins")
                coinText.text = coins.toString()
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to fetch coins: ${e.message}")
            })
    }
}