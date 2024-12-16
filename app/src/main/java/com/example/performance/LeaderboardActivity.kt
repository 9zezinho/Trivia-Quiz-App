package com.example.performance

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.performance.adapter.TabsPageAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * LeaderboardActivity displays the leaderboard of users, allowing
 * them to view rankings in different categories (e.g., level ranking,
 * every 5-minute ranking). It uses a ViewPager2 with tabs to switch
 * between different ranking categories.
 */
class LeaderboardActivity: AppCompatActivity() {

    private var logcatTag = "BoardTag" //for debugging

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Set up toolbar for teh leaderboard screen
        val mToolbar = findViewById<MaterialToolbar>(R.id.leaderboard_toolbar)
        setSupportActionBar(mToolbar)

        // Set up ViewPager adapter to display fragments
        viewPager.adapter = TabsPageAdapter(this)

        // Attach TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) {
            tab, position ->
            tab.text =
                if (position == 0) {
                    "Level Ranking" //Level-based ranking
                } else {
                    "Every 5 Min Ranking" //Time-based ranking
                }
        }.attach()

        // Handle the click event of the navigation button on toolbar
        mToolbar.setNavigationOnClickListener {
            Log.i(logcatTag, "Navigation Icon(Up btn) clicked")
            finish()
        }
    }

    // Inflate the menu for the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.leaderboard_toolbarlayout, menu)
        return super.onCreateOptionsMenu(menu)
    }
}