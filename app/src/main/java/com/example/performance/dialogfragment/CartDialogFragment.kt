package com.example.performance.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.performance.FirebaseAuthManager
import com.example.performance.FirebaseManager
import com.example.performance.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DatabaseReference

/**
 * This class represents a dialog where the user can redeem lifelines
 * (50-50 and skip) using their in-game coins. It handles teh
 * displaying the current coin balance, allowing users to redeem
 * lifelines, updating the Firebase Database with the new coin
 * balance and lifeline count, showing appropriate messages to user
 */

class CartDialogFragment: DialogFragment() {

    private lateinit var coin: MaterialTextView
    private lateinit var buy5050: MaterialButton
    private lateinit var buySkip: MaterialButton

    private lateinit var database: DatabaseReference
    private var auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    // Callback to be invoked when the dialog is dismissed
    lateinit var onDismissAction: (() -> Unit)
    private var logcatTag = "Redeem" // for debugging

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Inflate the custom dialog
        val dialogView = layoutInflater.inflate(R.layout.cart_dialog, null)
        coin = dialogView.findViewById(R.id.coinText)
        buy5050 = dialogView.findViewById(R.id.buy5050)
        buySkip = dialogView.findViewById(R.id.buySkip)


        // Initialize Firebase Database reference
        database = FirebaseManager.userReference
        fetchCoin()

        // Set up click listener for the 50-50 lifeline purchase
        buy5050.setOnClickListener{
            val currentCoins = coin.text.toString().toLong()
            if (currentCoins >= 3) {
                //Fetch the user lifeline points
                FirebaseManager.fetchUserLife(
                    userID,
                    onSuccess = { fiftyFifty, skip ->
                        val new5050 = fiftyFifty + 1 // increment
                        val newCoins = currentCoins - 3 //decrement coins
                        //Update new Lifeline
                        FirebaseManager.updateUserLifelines(userID,
                            fiftyFifty =  new5050,
                            skip = skip, // don't change the skip value
                            onSuccess = {
                                displayMessage(buy5050,
                                    "50-50 lifeline Redeemed!")
                                //Update new user coins
                                FirebaseManager.updateUserCoins(userID,
                                    newCoins,
                                    onSuccess = {
                                        coin.text = newCoins.toString()
                                    }, onFailure = {e ->
                                        Log.i(logcatTag,
                                            "Failed to update coins: ${e.message}")
                                    })
                            }, onFailure = {e ->
                                Log.e(logcatTag,
                                    "Failed to redeem 50-50: ${e.message}")
                            }
                        )
                    }, onFailure = {e ->
                        Log.e(logcatTag,
                            "Failed to fetch lifelines: ${e.message}")
                    }
                )
            } else {
                displayMessage(buy5050, "Not enough coins!!")
            }
        }

        // Set up click listener for the Skip lifeline purchase
        buySkip.setOnClickListener{
            val currentCoins = coin.text.toString().toLong()
            if (currentCoins >= 5) {
                //Fetch the user lifeline points
                FirebaseManager.fetchUserLife(
                    userID,
                    onSuccess = { fiftyFifty, skip ->
                        val newSkip = skip + 1 // increment
                        val newCoins = currentCoins - 5 //decrement coins
                        //Update new Lifeline
                        FirebaseManager.updateUserLifelines(userID,
                            fiftyFifty = fiftyFifty, // don't change the value
                            skip = newSkip,
                            onSuccess = {
                                displayMessage(buySkip,
                                    "Skip lifeline Redeemed!")
                                //Update new user coins
                                FirebaseManager.updateUserCoins(userID,
                                    newCoins,
                                    onSuccess = {
                                        coin.text = newCoins.toString()
                                    }, onFailure = {e ->
                                        Log.i(logcatTag,
                                            "Failed to update coins: ${e.message}")
                                    })
                            }, onFailure = {e ->
                                Log.e(logcatTag,
                                    "Failed to redeem Skip: ${e.message}")
                            }
                        )
                    }, onFailure = {e ->
                        Log.e(logcatTag,
                            "Failed to fetch lifelines: ${e.message}")
                    }
                )
            } else {
                displayMessage(buySkip, "Not enough coins!!")
            }
        }

        // Create and return an AlertDialog with a Close button
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") {
                dialog,_ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * Fetch the user's current coin balance from Firebase
     */
    private fun fetchCoin(){
        FirebaseManager.fetchUserCoins(userID,
            onSuccess = {
                    coins ->
                Log.i(logcatTag, "Coins: $coins")
                coin.text = coins.toString() // update UI with fetched coin
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to fetch coins: ${e.message}")
            })
    }

    //Display Message using SnackBar
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Override the onDismiss method to execute the action when the dialog is closed
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissAction.invoke() // callback when the dialog is dismissed
    }

}