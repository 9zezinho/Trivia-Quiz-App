package com.example.performance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView

/**
 * ChooseActivity class is responsible for setting up the user interface
 * for selecting quiz settings like the number of questions, difficulty
 * level, question type, and category. It acts as the main interface for
 * configuring the quiz before starting it. The user can choose various
 * options like difficulty(easy, medium, hard). question type(true/false,
 * multiple-choice), and category.
 */
class ChooseActivity: AppCompatActivity() {

    private lateinit var numberOfQuestion: EditText
    private lateinit var easyButton: MaterialButton
    private lateinit var medButton: MaterialButton
    private lateinit var hardButton: MaterialButton
    private lateinit var anyLevelButton: MaterialButton
    private lateinit var trueFalseButton: MaterialButton
    private lateinit var multipleChoiceButton: MaterialButton
    private lateinit var selectCategoryButton: MaterialButton
    private lateinit var anyQuestionTypeButton: MaterialButton
    private lateinit var startButton: MaterialButton
    private lateinit var categoryTextView: MaterialTextView
    private lateinit var chooseToolbar: MaterialToolbar

    private var logcatTag="ChooseTag" //for debugging

    private var selectedCategory: String? = null
    private var selectedDifficulty: String? = null
    private var selectedQuestionType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        //Retrieve the id's
        numberOfQuestion = findViewById(R.id.editTextQuestionNumber)
        easyButton = findViewById(R.id.easyBtn)
        medButton = findViewById(R.id.mediumBtn)
        hardButton = findViewById(R.id.hardBtn)
        anyLevelButton = findViewById(R.id.anyLevelBtn)
        trueFalseButton = findViewById(R.id.trueFalseBtn)
        multipleChoiceButton = findViewById(R.id.multipleChoiceBtn)
        selectCategoryButton = findViewById(R.id.selectCategoryBtn)
        anyQuestionTypeButton = findViewById(R.id.anyQuestionTypeBtn)
        startButton = findViewById(R.id.startBtn)
        categoryTextView = findViewById(R.id.categorySelectedView)
        chooseToolbar = findViewById(R.id.chooseToolbar)

        clearSelectedCategory() //clear any previously selected category

        //Set up listeners for the select category button
         selectCategoryButton.setOnClickListener {
             val intent = Intent(this,
                 CategoriesActivity::class.java)
             startActivity(intent)
         }

        // Set up toolbar navigation(back button)
        chooseToolbar.setNavigationOnClickListener { finish() }


        // Set up listeners for difficulty button
        val difficultyButtons = listOf(easyButton,medButton,
            hardButton,anyLevelButton)
        easyButton.setOnClickListener { selectOption(easyButton,
            difficultyButtons,"easy","difficulty") }
        medButton.setOnClickListener { selectOption(medButton,
            difficultyButtons,"medium", "difficulty") }
        hardButton.setOnClickListener { selectOption(hardButton,
            difficultyButtons,"hard", "difficulty") }
        anyLevelButton.setOnClickListener { selectOption(anyLevelButton,
            difficultyButtons,"", "difficulty") }

        // Set up listeners for question type buttons
        val questionTypeButtons = listOf(trueFalseButton,
            multipleChoiceButton, anyQuestionTypeButton)
        trueFalseButton.setOnClickListener {
            selectOption(trueFalseButton,
            questionTypeButtons, "boolean","questionType") }
        multipleChoiceButton.setOnClickListener {
            selectOption(multipleChoiceButton,
            questionTypeButtons,"multiple","questionType") }
        anyQuestionTypeButton.setOnClickListener {
            selectOption(anyQuestionTypeButton,
            questionTypeButtons, "", "questionType") }

        // Set up listener for the start quiz button
        startButton.setOnClickListener {view ->
            Log.i(logcatTag, "Start Clicked")

            // Make sure no of question 1-50
            val numberText = numberOfQuestion.text.toString()
            closeKeyboard()
            val intValue = numberText.toInt()
            if(intValue < 51){
                startQuiz()
            } else {
                displayMessage(view, getString(R.string.high_number))
            }
        }
    }

    // called when activity is resumed, to update the selected category text
    override fun onResume() {
        super.onResume()
        selectedCategory = getSelectedCategory()
        categoryTextView.text = getString(R.string.selected_category_text, selectedCategory)
    }

    // Helper function to retrieve the selected category from shared prefs
    private fun getSelectedCategory(): String? {
        val sharedPrefs = getSharedPreferences("CategoryPrefs",
            Context.MODE_PRIVATE)
        return sharedPrefs.getString("selectedCategory",
            "No category selected")
    }

    // Function to clear any selected category from shared prefs
    private fun clearSelectedCategory(){
        val sharedPrefs = getSharedPreferences("CategoryPrefs",
            Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove("selectedCategory")
        editor.apply()
    }

    // Function to handle the selection of options
    private fun selectOption(button: Button, allButtons: List<Button>,
                             selectedOption:String, optionType: String){

        //Reset all buttons to default color
        val defaultColor = ContextCompat.getColor(this,R.color.teal_700)
        allButtons.forEach{it.setBackgroundColor(defaultColor)}

        // Highlight the selected button
        button.setBackgroundColor(getColor(R.color.pumpkin))
        when(optionType) {
            "difficulty" -> selectedDifficulty = selectedOption
            "questionType" -> selectedQuestionType = selectedOption
        }
    }

    // Passing the selected data to next Activity
    private fun startQuiz(){
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("difficulty",selectedDifficulty)
        intent.putExtra("type",selectedQuestionType)
        intent.putExtra("amount", numberOfQuestion.text.toString())
        intent.putExtra("category",selectedCategory)
        startActivity(intent)
    }

    // Display message using SnackBar
    private fun displayMessage(view: View, msgTxt : String){
        val snackBar = Snackbar.make(view, msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    //Helper function to close keyboard
    private fun closeKeyboard() {
        val view = this.currentFocus
        if(view != null){
            val x = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            x.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}