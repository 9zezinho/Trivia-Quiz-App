package com.example.performance.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.performance.FirebaseAuthManager
import com.example.performance.FirebaseManager
import com.example.performance.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

/**
 * This class is responsible for providing a dialog to change the username
 * of the currently authenticated user. It validates new username, checks
 * if it already exits, and updates Firebase if the change is successful
 */

class UserNameDialogFragment: DialogFragment() {

    // lambda function to be invoked when dialog is dismissed
    lateinit var onDismissAction: (() -> Unit)
    private var logcatTag = "UserName"// for debugging

    private var auth = FirebaseAuthManager.authentication

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the dialog layout
        val dialogView =
            layoutInflater.inflate(R.layout.username_dialog, null)

        // Initialize the UI components
        val changeButton: MaterialButton =
            dialogView.findViewById(R.id.changeUsernameBtn)
        val editUsername: EditText =
            dialogView.findViewById(R.id.usernameEditText)

        // Set up listener for the "Change Username" button
        changeButton.setOnClickListener{

            // Get teh new username entered by the user.
            val newUsername = editUsername.text.toString()
            val userID = auth.currentUser!!.uid

            //Check if the username is not empty
            if (newUsername.isEmpty()) {
                displayMessage(changeButton, "Username cannot be empty")
                return@setOnClickListener
            }

            // Check if the entered username already exists in the Firebase Database
            FirebaseManager.userReference
                .orderByChild("username").equalTo(newUsername)
                .get()
                .addOnSuccessListener { result ->
                    if (result.exists()) { // if the username is already taken
                        displayMessage(changeButton,
                            "Username is already taken")
                    } else {
                        val sharedPref = requireActivity()
                            .getSharedPreferences("UserPrefs",
                            Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("USERNAME_KEY",newUsername)
                        editor.apply()

                        // Save teh username to Firebase under the current user's ID
                        FirebaseManager.saveData(userID,
                            path = "username",
                            data = newUsername,
                            onSuccess = {
                                displayMessage(changeButton,
                                    "Username changed successfully")
                                closeKeyboard()
                            }, onFailure = {e ->
                                Log.e(logcatTag,
                                    "Failed to change username: ${e.message}")
                            })
                    }
                }.addOnFailureListener { e ->
                    Log.e(logcatTag,
                        "Error checking username: ${e.message}")
                }
        }

        // Create and return the AlertDialog
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") {
                dialog, _ ->
                dialog.dismiss() //dismiss dialog when "Close" is clicked
            }
            .create()
    }

    // Helper function to close keyboard
    private fun closeKeyboard() {
        val view = dialog?.currentFocus
        if(view != null){
            val x = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            x.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // Displaying message using SnackBar
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Override the onDismiss method to execute the action when the dialog is closed
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction.invoke() //callback when the dialog is dismissed
    }

}