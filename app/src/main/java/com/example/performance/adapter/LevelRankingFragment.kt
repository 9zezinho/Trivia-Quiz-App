package com.example.performance.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.performance.FirebaseAuthManager
import com.example.performance.FirebaseManager
import com.example.performance.R
import com.example.performance.RankingAdapter
import com.example.performance.RankingType
import com.example.performance.Rankings
import com.example.performance.applyDiff
import com.google.firebase.database.DatabaseReference

/**
 * This class represents the fragment that handles the display of the
 * leaderboard ranking based on the user's level
 */

class LevelRankingFragment: Fragment() {

    // Adapter and data list for RecyclerView
    private lateinit var  rankingAdapter: RankingAdapter
    private val rankings = mutableListOf<Rankings>()

    // Firebase References
    private lateinit var database: DatabaseReference
    private var auth = FirebaseAuthManager.authentication

    private var logcatTag = "LvlRank" //for debugging

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout
        val view = inflater.inflate(R.layout.fragment_leaderboard_ranking,
            container, false)

        // Set up RecyclerView for displaying rankings
        val recyclerView: RecyclerView =
            view.findViewById(R.id.recyclerViewTotalRanking)
        recyclerView.layoutManager = LinearLayoutManager(context)
        rankingAdapter = RankingAdapter(rankings)
        recyclerView.adapter = rankingAdapter

        fetchLevelRanking()

        return view // Return the inflated view
    }

    /**
     * Function to fetch the rankings data from Firebase
     */
    private fun fetchLevelRanking() {
        database = FirebaseManager.userReference

        // Get currently authenticated user
        val currentUser = auth.currentUser
        Log.i(logcatTag, "Current user uid: ${auth.currentUser!!.uid}")
        if (currentUser == null){
            Log.e(logcatTag, "User is not authenticated")
        }

        // Get the users details
        database.get().addOnSuccessListener { results ->
            val tempRankings = mutableListOf<Rankings>()

            for (pointsResults in results.children){
                val username = pointsResults
                    .child("username").value as? String?: ""
                val lvl = pointsResults
                    .child("level").value as? Long?: 0L
                val totalPoints = pointsResults
                    .child("totalPoints").value as? Long?: 0L
                val current5minPoints = pointsResults
                    .child("current5minPoints").value as? Long?: 0L

                val user = Rankings(RankingType.LEVEL, username, lvl,
                    totalPoints, current5minPoints)
                tempRankings.add(user)
            }
            //Sort the list based on user's level(descending order)
            rankings.clear() // clear list
            rankings.addAll(tempRankings.sortedByDescending {
                it.lvl }.take(10))// takes top 10 only

            // Apply DiffUtil to update RecyclerView with new ranking list
            applyDiff(rankings, tempRankings, rankingAdapter,
                { oldItem, newItem -> oldItem.userName == newItem.userName},
                { oldItem, newItem -> oldItem == newItem }
            )
        }.addOnFailureListener { e ->
            Log.e(logcatTag, "Failed to get the user's Total Points: ${e.message}")
        }
    }
}