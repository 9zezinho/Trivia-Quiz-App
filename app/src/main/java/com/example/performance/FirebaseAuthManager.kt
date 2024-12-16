package com.example.performance

import com.google.firebase.auth.FirebaseAuth

/**
 * This object serves as a wrapper around the FirebaseAuth
 * instance to streamline authentication tasks in the app.
 * It follows Singleton pattern, ensuring that only one
 * FirebaseAuth instance is used across the entire application for
 * user authentication.
 */
object FirebaseAuthManager {
    val authentication: FirebaseAuth = FirebaseAuth.getInstance()
}