package com.example.performance

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

/**
 * This class is responsible for  displaying a user's historical performance in the app.
 * This activity retrieves session data from Firebase, displays it in a RecyclerView,
 * and updates various UI elements, including a pie chart and accuracy stats.
 * It also shows total wins and losses, and updates accuracy based on the
 * user's quiz performance.
 */

class HistoryActivity : AppCompatActivity() {

    private var logcatTag = "HistoryActivityTag" //for debugging

    private val auth = FirebaseAuthManager.authentication

    private lateinit var winsValue: MaterialTextView
    private lateinit var lossValue: MaterialTextView
    private lateinit var progressBar: ProgressBar
    private lateinit var accuracyValue: MaterialTextView
    private lateinit var historyView: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var historyAdapter: HistoryAdapter

    // List to hold session data
    private val sessions = mutableListOf<Session>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize UI components
        historyView = findViewById(R.id.recycleView)
        winsValue = findViewById(R.id.totalWinsValue)
        lossValue = findViewById(R.id.totalLossValue)
        progressBar = findViewById(R.id.accuracyProgressBar)
        accuracyValue = findViewById(R.id.accuracyValue)
        pieChart = findViewById(R.id.pieChart)

        // Set up the RecyclerView adapter for displaying session data
        historyAdapter = HistoryAdapter(sessions)
        historyView.layoutManager =
            LinearLayoutManager(this)
        historyView.adapter = historyAdapter

        // Get teh user ID from Firebase Authentication
        val userID = auth.currentUser!!.uid

        // Fetch session history for the user from Firebase
        fetchSessionHistory(userID)

        // Set up toolbar and it's back navigation
        val mToolbar = findViewById<MaterialToolbar>(R.id.history_toolbar)
        setSupportActionBar(mToolbar)
        mToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * This function is helps to retrieve the session data from a
     * database(Firebase Realtime Database) and update the RecycleView
     * with fetched data.
     * @param userID is the user ID of the current user
     */
    private fun fetchSessionHistory(userID: String) {

        FirebaseManager.fetchSessionHistory(userID, {
            fetchedSession ->
            Log.i(logcatTag, "Total session fetched: $fetchedSession")

            // copy current session list
            val oldSessions = ArrayList(sessions)
            // Clear the previous session
            sessions.clear()
            sessions.addAll(fetchedSession)

            // Apply DiffUtil to update the session list
            applyDiff(oldSessions, fetchedSession, historyAdapter,
                {oldItem, newItem -> oldItem.sessionID == newItem.sessionID},
                {oldItem, newItem -> oldItem == newItem})

            updateTotalWinsAndLoss()
            updatePieChartAndAccuracy()
        }, {e ->
            Log.e(logcatTag, "Error fetching session history: ${e.message}")
        })

    }

    /**
     * Calculates and updates the total wins and losses across all sessions.
     * Display the results in teh appropriate TextView
     */
    private fun updateTotalWinsAndLoss(){
        var totalWins: Long = 0
        var totalLoss: Long = 0

        //Sum up wins and losses for each session
        for(session in sessions){
            totalWins += session.wins
            totalLoss += session.loss
        }
        // Display the total wins for all sessions
        winsValue.text = totalWins.toString()
        lossValue.text = totalLoss.toString()
    }

    /**
     * Updates the pie chart and displays the accuracy of the user's performance.
     * The pie chart shows the proportion of correct (wins) and
     * incorrect (losses) answers
     */
    private fun updatePieChartAndAccuracy(){
        var totalWins: Long = 0
        var totalLoss: Long = 0

        //Sum up wins and losses
        for (session in sessions){
            totalWins += session.wins
            totalLoss += session.loss
        }

        Log.i(logcatTag, "Total Wins: $totalWins, Total Loss: $totalLoss")
        // Arraylist to hold the wins and loss
        val entries = ArrayList<PieEntry>()
        if (totalWins > 0) {
            entries.add(PieEntry(totalWins.toFloat(), "Correct"))
        }
        if(totalLoss > 0) {
            entries.add(PieEntry(totalLoss.toFloat(), "Incorrect"))
        }

        //Create a PieDataSet from the entries
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(getColor(R.color.teal_700),
            getColor(R.color.pumpkin))
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.setUsePercentValues(true)
        pieChart.setHoleColor(getColor(R.color.white))
        pieChart.data = data
        pieChart.animateY(1500)

        //remove description
        pieChart.description.isEnabled = false

        // Set center text
        pieChart.centerText = "Quiz Analysis"
        pieChart.setCenterTextSize(16f)

        //refresh pie chart
        pieChart.invalidate()
        updateAccuracy(totalWins, totalLoss)
    }

    /**
     * Calculates and updates the accuracy based on the total wins and losses.
     * Displays the accuracy percentage in the appropriate TextView
     * and progress bar.
     * @param totalLoss is the total loss
     * @param totalWins is the total wins so far
     */
    private fun updateAccuracy(totalWins: Long, totalLoss: Long) {
        val totalGames = totalWins + totalLoss
        if (totalGames > 0){
            val accurate = (totalWins.toFloat()/totalGames) * 100
            accuracyValue.text = getString(R.string.accuracy_value, accurate.toInt())
            progressBar.progress = accurate.toInt()
        }
    }
}