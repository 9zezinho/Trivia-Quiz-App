package com.example.performance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * This class is used to show information about the game, such as
 * features and gameplay. It sets up a toolbar at the top of the screen
 * for easy navigation allowing users to go back to previous screen.
 * The layout for this activity would contain teh details and
 * description about the game's mechanics.
 */

class AboutActivity: AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Initialize the toolbar
        toolbar = findViewById(R.id.about_toolbar)
        setSupportActionBar(toolbar)

        // Set a click listener on the toolbar's navigation button (Back button)
        toolbar.setNavigationOnClickListener {
            finish()
        }

    }
}