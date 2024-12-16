package com.example.performance

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder


/**
 * MyService is a Service that plays background music when started.
 * The music is played on a loop until the service is destroyed.
 * This service uses a `MediaPlayer` to handle audio playback.
 *
 * Credits:
 * Music Title: Happy And Joyful Children https://www.patreon.com/posts/62276355
 * Released by: Oleg Mazur https://soundcloud.com/fm_freemusic
 * Music promoted by https://www.chosic.com/free-music/all/
 * Creative Commons CC BY 3.0
 * https://creativecommons.org/licenses/by/3.0/
 */

class MyService : Service(){

    private var backgroundMusic: MediaPlayer? = null

    /**
     * This function initializes and starts the background music.
     * It ensures that the music plays on loop
     */
    private fun playMusic(){
        backgroundMusic = MediaPlayer.create(this,R.raw.background_music)

        //check if music is already playing
        if(!backgroundMusic!!.isPlaying){
            backgroundMusic!!.start()
            backgroundMusic!!.isLooping = true
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Method is called when the service starts
    override fun onStartCommand(intent: Intent?,
                                flags: Int, startId: Int): Int {
        playMusic()
        return START_STICKY // service will restart if killed by system
    }

    // Method is called when teh service is destroyed
    override fun onDestroy(){
        backgroundMusic!!.stop()
        backgroundMusic!!.release() // Frees system resources
        backgroundMusic = null  // To prevent accidental reuse
        super.onDestroy()
    }
}