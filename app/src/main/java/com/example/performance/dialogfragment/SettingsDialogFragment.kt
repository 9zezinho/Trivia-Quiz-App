package com.example.performance.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.performance.MyService
import com.example.performance.R
import com.google.android.material.button.MaterialButton

/**
 * This class manages the music state(whether music is playing
 * or stopped) by allowing the user to mute or un mute the music.
 * It interacts with the background music service(MyService) to start
 * or stop the music.
 */

class SettingsDialogFragment: DialogFragment() {

    private var isMusicPlaying: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the dialog layout
        val dialogView = layoutInflater
            .inflate(R.layout.settings_dialog, null)

        val muteButton: MaterialButton =
            dialogView.findViewById(R.id.muteMusicBtn)
        val unMuteButton: MaterialButton=
            dialogView.findViewById(R.id.unMuteMusicBtn)

        // Load current state of music
        isMusicPlaying = loadMusicState()
        if (isMusicPlaying) {
            // If music is playing show the mute button and hide unMute button
            muteButton.visibility = View.VISIBLE
            unMuteButton.visibility = View.GONE
        } else {
            muteButton.visibility = View.GONE
            unMuteButton.visibility = View.VISIBLE
        }

        // Set up listener for the mute button
        muteButton.setOnClickListener{
            stopMusicService()
            isMusicPlaying = false
            muteButton.visibility = View.GONE
            unMuteButton.visibility = View.VISIBLE
            saveMusicState(isMusicPlaying)
        }

        // Set up listener for unMute button
        unMuteButton.setOnClickListener{
            startMusicService()
            isMusicPlaying = true
            unMuteButton.visibility = View.GONE
            muteButton.visibility = View.VISIBLE
            saveMusicState(isMusicPlaying)
        }

        // Create and return AlertDialog
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") {
                dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    // Start Music Service
    private fun startMusicService(){
        val serviceIntent = Intent(requireContext(), MyService::class.java)
        requireContext().startService(serviceIntent)
    }

    //Stop Music Service
    private fun stopMusicService(){
        val serviceIntent = Intent(requireContext(), MyService::class.java)
        requireContext().stopService(serviceIntent)
    }

    // Save the music state in SharedPreferences
    private fun saveMusicState(isPlaying: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs",
            Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isMusicPlaying", isPlaying)
        editor.apply()
    }

    // Load the music from the SharedPreferences
    private fun loadMusicState(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs",
            Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isMusicPlaying", false)
    }
}