package com.example.performance


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

/**
 * This class manages the functionality for selecting and unlocking
 * categories in the app.It interacts with Firebase to check the user's
 * progress (level and coins), unlocks categories based on conditions,
 * and provides feedback to the user via dialogs and SnackBars.
 */

class CategoriesActivity: AppCompatActivity() {

    private lateinit var books: MaterialButton
    private lateinit var film: MaterialButton
    private lateinit var music: MaterialButton
    private lateinit var tv: MaterialButton
    private lateinit var comics: MaterialButton
    private lateinit var videoGames: MaterialButton
    private lateinit var musicalsTheatres: MaterialButton
    private lateinit var boardGame: MaterialButton
    private lateinit var anime: MaterialButton
    private lateinit var cartoonAnimation: MaterialButton
    private lateinit var generalKnw: MaterialButton
    private lateinit var gadgets: MaterialButton
    private lateinit var maths: MaterialButton
    private lateinit var comp: MaterialButton
    private lateinit var mythology: MaterialButton
    private lateinit var sports: MaterialButton
    private lateinit var geo: MaterialButton
    private lateinit var history: MaterialButton
    private lateinit var politics: MaterialButton
    private lateinit var art: MaterialButton
    private lateinit var celeb: MaterialButton
    private lateinit var animals: MaterialButton
    private lateinit var vehicles: MaterialButton
    private lateinit var scienceAndNature: MaterialButton
    private lateinit var categoryToolbar: MaterialToolbar

    private var auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    private var logcatTag="CategoriesTag"

    // To pass the selected category to the Choose Activity
    private lateinit var selectedCategory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        //Retrieve the id's
        books = findViewById(R.id.booksBtn)
        film = findViewById(R.id.filmBtn)
        music = findViewById(R.id.musicBtn)
        tv = findViewById(R.id.tvBtn)
        comics = findViewById(R.id.comicsBtn)
        videoGames = findViewById(R.id.videoGamesBtn)
        musicalsTheatres = findViewById(R.id.musicalsTheatersBtn)
        boardGame = findViewById(R.id.boardGamesBtn)
        anime = findViewById(R.id.jpnAnimeBtn)
        cartoonAnimation = findViewById(R.id.cartoonBtn)
        generalKnw = findViewById(R.id.generalKnowledgeBtn)
        gadgets = findViewById(R.id.gadgetsBtn)
        maths = findViewById(R.id.mathsBtn)
        comp = findViewById(R.id.compBtn)
        mythology = findViewById(R.id.mythologyBtn)
        sports = findViewById(R.id.sportsBtn)
        geo = findViewById(R.id.geoBtn)
        history = findViewById(R.id.historyBtn)
        politics = findViewById(R.id.politicsBtn)
        art = findViewById(R.id.artBtn)
        celeb = findViewById(R.id.celebBtn)
        animals = findViewById(R.id.animalsBtn)
        vehicles = findViewById(R.id.vehiclesBtn)
        scienceAndNature = findViewById(R.id.scienceAndNatureBtn)
        categoryToolbar = findViewById(R.id.categoriesToolBar)


        //Click Listeners
        val categoriesButton = listOf(books,film,music,tv,
            comics,videoGames, musicalsTheatres,boardGame,
            anime,cartoonAnimation,generalKnw,gadgets, maths,
            comp,mythology,sports,geo, history, politics,art,
            celeb,animals,
            vehicles,scienceAndNature)
        books.setOnClickListener {
            selectOption(books,categoriesButton,
            "Entertainment: Books") }
        film.setOnClickListener { selectOption(film, categoriesButton,
            "Entertainment: Film") }
        music.setOnClickListener{ selectOption(music,categoriesButton,
            "Entertainment: Music") }
        tv.setOnClickListener{selectOption(tv, categoriesButton,
            "Entertainment: Television") }
        comics.setOnClickListener{
            selectOption(comics, categoriesButton,
            "Entertainment: Comics") }
        videoGames.setOnClickListener {
            selectOption(videoGames, categoriesButton,
            "Entertainment: Video Games") }
        musicalsTheatres.setOnClickListener{
            selectOption(musicalsTheatres, categoriesButton,
            "Entertainment: Musicals & Theatres")}
        boardGame.setOnClickListener {
            selectOption(boardGame, categoriesButton,
            "Entertainment: Board Games") }
        anime.setOnClickListener {
            selectOption(anime, categoriesButton,
            "Entertainment: Japanese Anime & Manga") }
        cartoonAnimation.setOnClickListener {
            selectOption(cartoonAnimation,
            categoriesButton, "Entertainment: Cartoon & Animations") }
        generalKnw.setOnClickListener {
            selectOption(generalKnw, categoriesButton,
            "General Knowledge") }
        gadgets.setOnClickListener {
            selectOption(gadgets, categoriesButton,
            "Science: Gadgets") }
        maths.setOnClickListener {
            selectOption(maths, categoriesButton,
            "Science: Mathematics") }
        comp.setOnClickListener {
            selectOption(comp, categoriesButton,
            "Science: Computers") }
        mythology.setOnClickListener {
            selectOption(mythology, categoriesButton,
            "Mythology") }
        sports.setOnClickListener {
            selectOption(sports, categoriesButton,
            "Sports") }
        geo.setOnClickListener {
            selectOption(geo, categoriesButton,
            "Geography") }
        history.setOnClickListener {
            selectOption(history, categoriesButton,
            "History") }
        politics.setOnClickListener {
            selectOption(politics, categoriesButton,
            "Politics") }
        art.setOnClickListener {
            selectOption(art, categoriesButton,
            "Art") }
        celeb.setOnClickListener {
            selectOption(celeb, categoriesButton,
            "Celebrities") }
        animals.setOnClickListener {
            selectOption(animals, categoriesButton,
            "Animals") }
        vehicles.setOnClickListener {
            selectOption(vehicles, categoriesButton,
            "Vehicles") }
        scienceAndNature.setOnClickListener {
            selectOption(scienceAndNature, categoriesButton,
            "Science & Nature") }


        // initialize the unlocked categories
        initializeCategories()


        categoryToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Function that handles the selection of a category
     * button and manages category unlocking
     * @param button is the category selected
     * @param allButtons is the list of categories button
     * @param selectedOption is the selected category in String
     */
    private fun selectOption(button: Button, allButtons: List<Button>,
                             selectedOption: String) {
        // Reset all buttons to default color
        val defaultColor = ContextCompat.getColor(this,R.color.teal_700)
        allButtons.forEach { it.setBackgroundColor(defaultColor) }

        // Check if the category is unlocked
        checkCategoryUnlockStatus(selectedOption) {isUnlocked ->
            if(isUnlocked){
                button.setBackgroundColor(getColor(R.color.pumpkin))
                selectedCategory = selectedOption
                saveSelectedCategory(selectedCategory)
            } else {
                // If the category is unlocked , show the unlock dialog
                showUnlockCategoryDialog(selectedOption)
            }
        }

    }

    /**
     * Function to initialize a category as unlocked
     * in the Firebase database
     */
    private fun initializeCategories(){
        val defaultCategories = listOf("Entertainment: Books",
            "General Knowledge") //everyone has access to this category

        for (category in defaultCategories) {
            FirebaseManager.userReference.child(userID)
                .child("unlockedCategories")
                .child(category)
                .setValue(true)
                .addOnSuccessListener {
                    Log.i(logcatTag, "Category initialized as unlocked")
                }.addOnFailureListener{ e ->
                    Log.e(logcatTag,
                        "Failed to initialize category: ${e.message}")
                }
        }
    }

    /**
     * Function to check if a specific category is unlocked by querying
     * in Firebase Database
     * @param category is the name of Category
     * @param onResult return value based on the status of category
     */
    private fun checkCategoryUnlockStatus(
        category: String,
        onResult:(Boolean) -> Unit) {
        FirebaseManager.userReference.child(userID).get()
            .addOnSuccessListener { result ->
                //get all unlocked categories as a set
                val unlockedCategories =
                    result.child("unlockedCategories")
                    .children.mapNotNull { it.key }.toSet()

                val isUnlocked =
                    isCategoryUnlocked( category, unlockedCategories)
                onResult(isUnlocked) // Return unlock status of category
            }
    }

    // Helper function to check if a category is unlocked categories set
    private fun isCategoryUnlocked (
        category: String, unlockedCategories: Set<String>
    ): Boolean {
        // Returns true if category is unlocked
        return unlockedCategories.contains(category)
    }

    /**
     * Returns the cost (in coins) to unlock a specific category
     * @param category is the chosen category by the user
     */
    private fun getCategoryUnlockCost(category: String): Long {
        return CategoryData.categoryUnlockCost[category] ?: 100L
    }

    // Show a dialog prompting user to unlock a category
    private fun showUnlockCategoryDialog(category: String) {
        val categoryUnlockCost = getCategoryUnlockCost(category)
        val requiredLvl =
            CategoryData.levelRequirements[category] ?: 100L // level requirement
        val rootView = findViewById<View>(android.R.id.content)

        val dialog = AlertDialog.Builder(this)
            .setMessage("To unlock $category, you need to:\n" +
                    "- Reach Level $requiredLvl\n" +
                    "- Spend $categoryUnlockCost coins\n\n" +
                    "Do you want to proceed?")
            .setPositiveButton("Unlock") { _, _ ->
                // Unlock category if user press "Unlock"
                unlockCategoryWithCoins(category,rootView)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    /**
     * Function that handles the actual unlocking of the category with the coins and
     * level check
     * @param category is the category that user selected
     * @param view is the view to display message using SnackBar
     */
    private fun unlockCategoryWithCoins(category: String, view: View){
        FirebaseManager.userReference.child(userID).get()
            .addOnSuccessListener { result ->
                val userLvl = result.child("level")
                    .value as? Long?: 0L
                val currentCoins = result.child("coins")
                    .value as? Long?: 0L
                val categoryUnlockCost = getCategoryUnlockCost(category)
                val requiredLvl =
                    CategoryData.levelRequirements[category]?: 100 //lvl requirement

                // Check if the user meets the level requirements
                if(userLvl < requiredLvl) {
                    Snackbar.make(view,
                        "Level $requiredLvl required to unlock $category",
                        Snackbar.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Check if the user has enough coins
                if (currentCoins >= categoryUnlockCost) {
                    val newCoins = currentCoins - categoryUnlockCost
                    FirebaseManager.updateUserCoins(userID, newCoins, {
                        FirebaseManager.userReference
                            .child(userID)
                            .child("unlockedCategories")
                            .child(category)
                            .setValue(true)
                            .addOnSuccessListener {
                                displayMessage(view, "$category unlocked!!")
                                saveSelectedCategory(category)
                            }
                            .addOnFailureListener { e ->
                                displayMessage(view,
                                    "Failed to unlock $category: ${e.message}")}
                    }, {e ->
                        Log.e(logcatTag,
                            "Error updating coins to Firebase: ${e.message}")
                    })
                }else {
                    displayMessage(view,
                        "Not enough coins to unlock $category")}
            }
    }

    // Displaying message using SnackBar
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Function to extract the selected option
    private fun saveSelectedCategory(category: String) {
        val sharedPrefs = getSharedPreferences("CategoryPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString("selectedCategory", category)
        editor.apply()
        Log.i(logcatTag, "Selected category: $category")
    }
}