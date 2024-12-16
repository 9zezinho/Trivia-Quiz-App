package com.example.performance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuthException

/**
 * MainActivity is responsible for handling user login and sign-up processes.
 * It uses Firebase Authentication to manage user authentication
 * (login, registration, etc.), and displays the appropriate messages using
 * SnackBars for successful or failed operations.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var greetingText: MaterialTextView
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton

    private lateinit var sEmail: String
    private lateinit var sPass: String

    private var auth = FirebaseAuthManager.authentication
    private var currentUser = auth.currentUser

    private var logCatTag = "RegTag" //for debugging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Retrieve the id
        emailEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginBtn)
        signUpButton = findViewById(R.id.signUpBtn)

        // Set onClick Listeners for the Sign-up and Login buttons
        signUpButton.setOnClickListener {  signUpClick() }
        loginButton.setOnClickListener {  loginClick() }
    }

    // Checks if the user is already logged in, if yes redirects to home activity
    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // user is already logged in
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to sign up new User in the Login Page
    private fun signUpClick(){
        Log.i(logCatTag,"Sign up Clicked")

        sEmail = emailEditText.text.toString()
        sPass = passwordEditText.text.toString()

        //Check for empty fields
        if(sEmail.isEmpty() || sPass.isEmpty()){
            closeKeyboard()
            displayMessage(signUpButton, getString(R.string.empty_field_registration))
            return
        }

        // Create a new user in Firebase Authentication
        auth.createUserWithEmailAndPassword(
            sEmail,sPass
        ).addOnCompleteListener(this) {task ->
            if(task.isSuccessful){
                closeKeyboard()
                saveData()
                displayMessage(signUpButton, getString(R.string.signup_successful))

                // Save the username in SharedPref for first-time user
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("USERNAME_KEY", sEmail)
                editor.putBoolean("IS_FIRST_TIME_USER", true)
                editor.apply()

            } else {
                // Handle errors
                val errorCode = (task.exception as FirebaseAuthException).errorCode

                if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                    closeKeyboard()
                    displayMessage(signUpButton,getString(R.string.register_while_logged_in))
                } else {
                    closeKeyboard()
                    displayMessage(signUpButton, getString(R.string.signup_failure))
                }
            }
        }


    }

    // Function to let the user login to app
    private fun loginClick(){
        Log.i(logCatTag, "Login Clicked")
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        //Check for empty fields
        if(email.isEmpty() || password.isEmpty()){
            closeKeyboard()
            displayMessage(signUpButton, getString(R.string.empty_field_registration))
            return
        }

        // Attempt to sign in the user with Firebase Authentication
        auth.signInWithEmailAndPassword(
            email,password)
            .addOnCompleteListener(this) {
                task ->
                if(task.isSuccessful){
                    closeKeyboard()
                    val userId = task.result?.user?.uid
                    if(userId != null){
                        displayMessage(loginButton, getString(R.string.login_successful))
                        updateUI()
                        val intent = Intent(this, HomeActivity:: class.java)
                        startActivity(intent)
                    } else {
                        Log.i(logCatTag, "User ID  is null")
                    }
                } else {
                    closeKeyboard()
                    displayMessage(loginButton,
                        getString(R.string.login_failure))
                }
            }
    }

    // SnackBar to display the message
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt,Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Helper function to close keyboard
    private fun closeKeyboard() {
        val view = this.currentFocus
        if(view != null){
            val x = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            x.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // Function to updates the UI with login status
    private fun updateUI() {
        Log.i(logCatTag, "IN update")

        currentUser = auth.currentUser
        val currentEmail = currentUser?.email

        greetingText = findViewById(R.id.greetingView)
        greetingText.text = getString(R.string.greeting_string, currentEmail)
    }

    // Function to save user's data to Firebase
    private fun saveData(){
        sEmail = emailEditText.text.toString()
        sPass = passwordEditText.text.toString()
        val userID = auth.currentUser!!.uid

        // Create a user model with the data
        val user = User(sEmail,sPass)

        // Save data to Firebase
        FirebaseManager.saveData(userID,
            path = "",
            data = user,
            onSuccess = {
                Log.i(logCatTag, "Data successfully saved to Firebase")
            }, onFailure = {e ->
                Log.e(logCatTag, "Data save failed. Ex: ${e.message}")
            }
        )
    }
}