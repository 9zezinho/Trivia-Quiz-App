package com.example.performance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.koushikdutta.ion.Ion
import org.json.JSONObject
import android.text.Html
import com.google.firebase.database.DatabaseReference
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView

/**
 * This activity handles the core functionality of the quiz gameplay. It
 * fetches questions from the Open Trivia Database API, parses the data
 * and dynamically displays the questions and multiple-choice answers to
 * the user. Uses the Firebase Real time Database to manager user data.
 */

class QuizActivity: AppCompatActivity() {

    private var logcatTag="QuizActivityTag"

    private lateinit var questionTextView: MaterialTextView
    private lateinit var multipleChoiceOptions: RadioGroup
    private lateinit var option1: MaterialRadioButton
    private lateinit var option2: MaterialRadioButton
    private lateinit var option3: MaterialRadioButton
    private lateinit var option4: MaterialRadioButton
    private lateinit var trueFalseOptions: LinearLayoutCompat
    private lateinit var trueButton: MaterialButton
    private lateinit var falseButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var finishButton: MaterialButton
    private lateinit var correctTextView: MaterialTextView
    private lateinit var wrongTextView: MaterialTextView
    private lateinit var correctAnsTextView: MaterialTextView
    private lateinit var noResultsLayout: LinearLayoutCompat
    private lateinit var showNoResults: MaterialTextView
    private lateinit var responseCode: MaterialTextView
    private lateinit var goBackButton: MaterialButton
    private lateinit var fiftyButton: MaterialButton
    private lateinit var skipButton: MaterialButton
    private lateinit var lifeline: MaterialButton
    private lateinit var lifelineLayout: LinearLayoutCompat
    private lateinit var fiftyLeft : MaterialTextView
    private lateinit var skipLeft : MaterialTextView

    private lateinit var questionList: List<JSONObject>
    private lateinit var database: DatabaseReference
    private var sessionID: String = ""

    private var currentQuestionIndex: Int =  0
    private var correctAnsCount: Long =  0
    private var wrongAnsCount: Long = 0
    private var sessionPoints: Long = 0
    private var fiftyFiftyLifeline: Long = 0
    private var skipLifeline: Long = 0
    private var correctStreak: Long = 0
    private var highestStreak: Long = 0
    private var coinsEarned: Long = 0

    private lateinit var lifelineManager: LifelineManager

    private var isFiftyFiftyUsed: Boolean = false
    private var isAnswerSelected: Boolean = false
    private var isSkipUsed: Boolean = false

    private val auth = FirebaseAuthManager.authentication
    private val userID = auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        //Retrieving the data from the choose activity
        val difficulty = intent.getStringExtra("difficulty") ?: ""
        val questionType = intent.getStringExtra("type")?: ""
        val noOfQuestion = intent.getStringExtra("amount")?: ""
        val selectedCategory = intent.getStringExtra("category")?: ""

        //Initialize id's
        questionTextView = findViewById(R.id.questionTextView)
        multipleChoiceOptions = findViewById(R.id.multipleChoiceOptions)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        trueFalseOptions = findViewById(R.id.trueFalseOptions)
        trueButton = findViewById(R.id.trueBtn)
        falseButton = findViewById(R.id.falseBtn)
        nextButton = findViewById(R.id.nextBtn)
        finishButton = findViewById(R.id.finishBtn)
        correctTextView= findViewById(R.id.correct)
        wrongTextView = findViewById(R.id.wrong)
        correctAnsTextView = findViewById(R.id.correctAnsTextView)
        noResultsLayout = findViewById(R.id.noResultsLyt)
        showNoResults = findViewById(R.id.showNoResultsText)
        responseCode = findViewById(R.id.responseCodeText)
        goBackButton = findViewById(R.id.goBackBtn)
        fiftyButton = findViewById(R.id.fifty)
        skipButton = findViewById(R.id.skipBtn)
        lifeline = findViewById(R.id.lifelineBtn)
        lifelineLayout = findViewById(R.id.lifelineLayout)
        fiftyLeft = findViewById(R.id.fiftyFiftyLeft)
        skipLeft = findViewById(R.id.skipLeft)

        // Initialize Firebase Database reference
        database = FirebaseManager.rootReference

        fetchUserLifeLines()

        fetchQuestions(difficulty,questionType,noOfQuestion,selectedCategory)

        lifeline.setOnClickListener {
            lifelineLayout.visibility = View.VISIBLE
        }
    }


    /**
     * Fetches trivia questions from the Open Trivia Database API based on
        the provided parameters.
     *
     * @param difficulty The difficulty level of the questions(e.g., easy, medium, hard)
     * @param questionType The type of questions to fetch (multiple or boolean)
     * @param noOfQuestion Number of questions to fetch.
     * @param category The category oif the questions(e.g., film, books, anime, maths)
     *
     * This function makes an HTTP request to the Open trivia Database API using the
        Ion Library.
     */
    private fun fetchQuestions(difficulty: String, questionType: String,
                               noOfQuestion: String, category: String) {
        val categoryId = CategoryData.categoryMap[category] ?: ""
        Log.i(logcatTag, "Fetching questions with params - difficulty: $difficulty," +
                " questionType: $questionType, " +
                "amount: $noOfQuestion, categoryId: $categoryId")

        val url = "https://opentdb.com/api.php"

        // Using ion library to add query to the url to get questions
        Ion.with(this)
            .load(url)
            .addQuery("amount",noOfQuestion)
            .addQuery("category", categoryId)
            .addQuery("difficulty", difficulty)
            .addQuery("type",questionType)
            .asString()
            .setCallback{ ex, result ->
                if (ex != null){
                    Log.e(logcatTag, "Error fetching data", ex)
                }
                if (result != null){
                    Log.i(logcatTag, "Questions fetched successfully")
                    parseTriviaQuestions(result)
                }
            }

    }

    // Function to parse the fetched questions from API
    private fun parseTriviaQuestions(response: String){
        try {
            val jsonResponse = JSONObject(response)
            val responseCode = jsonResponse.getInt("response_code")
            val questionsArray = jsonResponse.getJSONArray("results")

            if(responseCode == 0){
                Log.i(logcatTag, "Total questions: ${questionsArray.length()}")

                questionList = List(questionsArray.length()) {
                    questionsArray.getJSONObject(it)

                }
                Log.i(logcatTag, "Parsed questions successfully, first question: " +
                        "${questionList[0]}")
                //Save the session to Firebase and Display question
                saveSession()
                displayQuestion()
            } else {
                Log.i(logcatTag, "No question received. Response code: $responseCode")
                showNoResultsLayout()
            }
        } catch (ex: Exception) {
            Log.e(logcatTag, "Error parsing JSON response", ex)
        }
    }

    // Function to display question type depending on user's preferences
    private fun displayQuestion(){

        //Initialize lifeline Manager
        lifelineManager = LifelineManager(questionList, currentQuestionIndex)

        val questionObj = questionList[currentQuestionIndex]

        // Convert HTML formatted text with HTML tags to normal Strings
        val questionText = Html.fromHtml(questionObj.getString("question"),
            Html.FROM_HTML_MODE_LEGACY).toString()
        val questionType = questionObj.getString("type")

        //Reset the fifty-fifty state for new question
        isFiftyFiftyUsed = false
        isSkipUsed = false

        //Reset for next question
        isAnswerSelected = false

        // Set the question text
        questionTextView.text = questionText

        // Display the lifelines UI to user
        fiftyLeft.text = getString(R.string.left, fiftyFiftyLifeline)
        skipLeft.text = getString(R.string.left, skipLifeline)


        if(questionType == "boolean") {
            //Show true/false buttons
            trueFalseOptions.visibility = View.VISIBLE
            multipleChoiceOptions.visibility = View.GONE

            trueButton.setOnClickListener {
                trueButton.setBackgroundColor(getColor(R.color.pumpkin))
                isAnswerSelected = true
                checkAnswer("True") }

            falseButton.setOnClickListener {
                falseButton.setBackgroundColor(getColor(R.color.pumpkin))
                isAnswerSelected = true
                checkAnswer("False") }

            fiftyButton.setOnClickListener {
                displayMessage(fiftyButton,
                    "You cannot use this lifeline in this type of questions")
            }

        } else if (questionType == "multiple") {
            // Show multiple options
            trueFalseOptions.visibility = View.GONE
            multipleChoiceOptions.visibility = View.VISIBLE

            // Convert HTML formatted text with HTML tags to normal Strings
            val correctAnswer = Html.fromHtml(questionObj
                .getString("correct_answer"),
                Html.FROM_HTML_MODE_LEGACY).toString()
            val incorrectAnswers =
                questionObj.getJSONArray("incorrect_answers")
            val allAnswers = mutableListOf<String>().apply {
                add(correctAnswer)
                for (i in 0 until incorrectAnswers.length()) {

                    // Convert HTML formatted text with HTML tags to normal Strings
                    add(Html.fromHtml(incorrectAnswers.getString(i),
                        Html.FROM_HTML_MODE_LEGACY).toString())
                }
                shuffle() // Randomize answer order
            }

            //Display answers in Radio buttons
            option1.text = allAnswers[0]
            option2.text = allAnswers[1]
            option3.text = allAnswers[2]
            option4.text = allAnswers[3]

            //Listener to handle answer selection
            option1.setOnClickListener {
                isAnswerSelected = true
                checkAnswer(option1.text.toString()) }
            option2.setOnClickListener {
                isAnswerSelected = true
                checkAnswer(option2.text.toString()) }
            option3.setOnClickListener {
                isAnswerSelected = true
                checkAnswer(option3.text.toString()) }
            option4.setOnClickListener {
                isAnswerSelected = true
                checkAnswer(option4.text.toString()) }

            // Set onClick listener for Fifty-Fifty button
            fiftyButton.setOnClickListener {
                if (fiftyFiftyLifeline > 0 && !isFiftyFiftyUsed) {

                    val reducedOptions =
                        lifelineManager.useFiftyFifty(correctAnswer,
                        incorrectAnswers)

                    multipleChoiceOptions.visibility = View.VISIBLE
                    option1.text = reducedOptions[0]
                    option2.text = reducedOptions[1]
                    option3.visibility = View.GONE
                    option4.visibility = View.GONE

                    option1.setOnClickListener {
                        isAnswerSelected = true
                        checkAnswer(option1.text.toString()) }
                    option2.setOnClickListener {
                        isAnswerSelected = true
                        checkAnswer(option2.text.toString()) }

                    fiftyFiftyLifeline-- //decrement on use
                    updateLifeLinesToFirebase()

                    // update UI
                    fiftyLeft.text =
                        getString(R.string.left, fiftyFiftyLifeline)
                    isFiftyFiftyUsed = true
                } else {
                    displayMessage(fiftyButton, "No Fifty-Fifty LifeLine left")
                }
                lifelineLayout.visibility = View.GONE
            }
        }
        skipButton.setOnClickListener {
            if (skipLifeline > 0) {
                if (currentQuestionIndex == questionList.size -1) {
                    lastQuestionSkipped()
                } else {
                    currentQuestionIndex++ //next question
                    displayMessage(skipButton, "You have skipped a Question")
                    resetQuestionState()
                    displayQuestion()
                }
                skipLifeline-- //decrement on use
                updateLifeLinesToFirebase()

                skipLeft.text = getString(R.string.left, skipLifeline)
            } else {
                displayMessage(skipButton, "No Skip LifeLine left")
            }
            lifelineLayout.visibility = View.GONE
        }
    }

    // Function to check Answer selected by user
    private fun checkAnswer(selectedAnswer: String){
        Log.i(logcatTag, "Check Ans called with: $selectedAnswer")

        val questionObj = questionList[currentQuestionIndex]
        val correctAnswer = Html.fromHtml(
            questionList[currentQuestionIndex].getString("correct_answer"),
            Html.FROM_HTML_MODE_LEGACY).toString()

        // Convert HTML formatted text with HTML tags to normal Strings
        val questionText = Html.fromHtml(questionObj.getString("question"),
            Html.FROM_HTML_MODE_LEGACY).toString()
        val difficulty = questionObj.getString("difficulty")
        val type = questionObj.getString("type")


        val questionPoints = calculatePoints(difficulty,type,correctStreak)
        val questionCoins = calculateCoins(difficulty,type, correctStreak)

        // In case of correct answer
        if (selectedAnswer == correctAnswer) {
            correctTextView.visibility = View.VISIBLE
            correctTextView.text = getString(R.string.correct)
            correctTextView.setBackgroundColor(getColor(R.color.teal_700))
            correctAnsCount++ //increment
            correctStreak++ //increment streak

            sessionPoints += questionPoints
            coinsEarned += questionCoins

            if (correctStreak > highestStreak) {
                highestStreak = correctStreak
            }

            Log.i(logcatTag, "Correct Answer count: $correctAnsCount")
        } else {
            wrongTextView.visibility = View.VISIBLE
            wrongTextView.text = getString(R.string.incorrect_ans)
            wrongTextView.setBackgroundColor(getColor(R.color.orange))
            correctAnsTextView.visibility = View.VISIBLE
            correctAnsTextView.text =
                getString(R.string.correct_ans, correctAnswer)
            wrongAnsCount++
            correctStreak = 0 //Reset streak

            Log.i(logcatTag, "Incorrect Answer count: $wrongAnsCount")
        }
        //Save questions details to session in Firebase
        saveQuestionToSession(questionText,correctAnswer,selectedAnswer)
        checkLastQuestion()
    }

    // Function to check the last question
    private fun checkLastQuestion() {
        //Reset for next question
        isFiftyFiftyUsed = false

        //Check the last question.
        if (currentQuestionIndex == questionList.size-1) {
            nextButton.visibility = View.GONE
            finishButton.visibility = View.VISIBLE

            finishButton.setOnClickListener {
                Log.i(logcatTag, "Finish Button Pressed")
                lastQuestionSkipped()
            }
        } else {
            //Move to the next question
            nextButton.setOnClickListener {
                Log.i(logcatTag, "Next button Clicked")

                lifelineLayout.visibility = View.GONE

                if (!isAnswerSelected) {
                    displayMessage(nextButton, "Please select answer before proceeding")
                    return@setOnClickListener
                }
                resetQuestionState()
                currentQuestionIndex++ //next question
                displayQuestion()
            }
        }
    }

    // Function to check if the last question was skipped
    private fun lastQuestionSkipped() {
        nextButton.visibility = View.GONE
        finishButton.visibility = View.VISIBLE

        //Update session results to Firebase
        updateSessionWithFinalResults()
        upload5MinPointsToFirebase()

        // Pass the correct and incorrect answers count to quiz completed activity
        val intent = Intent(this, QuizCompletedActivity::class.java)
        Log.i(logcatTag, "$correctAnsCount")
        Log.i(logcatTag,"$wrongAnsCount")

        intent.putExtra("correctAnsCount", correctAnsCount)
        intent.putExtra("wrongAnsCount", wrongAnsCount)
        intent.putExtra("points", sessionPoints)
        intent.putExtra("streak", highestStreak)
        intent.putExtra("coins", coinsEarned)
        startActivity(intent)
    }

    // Function to reset Question state
    private fun resetQuestionState(){
        option1.visibility = View.VISIBLE
        option2.visibility = View.VISIBLE
        option3.visibility = View.VISIBLE
        option4.visibility = View.VISIBLE

        // Reset the color
        val defaultColor = ContextCompat.getColor(this,R.color.teal_700)
        trueButton.setBackgroundColor(defaultColor)
        falseButton.setBackgroundColor(defaultColor)

        // Clear the options
        multipleChoiceOptions.clearCheck()

        // Visibility check
        correctTextView.visibility = View. GONE
        wrongTextView.visibility = View. GONE
        correctAnsTextView.visibility = View. GONE
    }

    // Save session to the Firebase
    private fun saveSession() {
        // Create a uniques session ID based on timeStamp
        sessionID = database.child("users").child(userID)
            .child("sessions")
            .push().key ?: System.currentTimeMillis().toString()
        Log.i(logcatTag, "Session ID here: $sessionID")

        // Session object
        val session = Session (
            sessionID = sessionID,
            timeStamp = System.currentTimeMillis(),
            wins = correctAnsCount,
            loss = wrongAnsCount,
            points = sessionPoints
        )

        // Path to save the session data
        val path = "sessions/$sessionID"

        // Save the session to Firebase
        FirebaseManager.saveData(
            userID = userID,
            path = path,
            data = session,
            onSuccess = {
                Log.i(logcatTag, "New Session saved with ID: ${session.sessionID}")
            }, onFailure = {e ->
                Log.e(logcatTag, "Error saving session: ${e.message}")
            }
        )
    }

    // Function to save the question Details
    private fun saveQuestionToSession(
        questionText: String, correctAns: String,
        userResponse: String) {

        // Generate unique question ID
        val questionID = "questions_${System.currentTimeMillis()}"
        Log.i(logcatTag, "QuestionID: $questionID")

        // Question object using the data class
        val questionData = Question(
            questionId = questionID,
            questionText = questionText,
            correctAnswer = correctAns,
            userResponse = userResponse
        )
        Log.i(logcatTag, "Question data passed")

        // Path for saving the question
        val path = "sessions/$sessionID/questions/$questionID"

        // FirebaseManager to save the question
        FirebaseManager.saveData(
            userID = userID,
            path = path,
            data = questionData,
            onSuccess = {
                Log.i(logcatTag, "Question successfully saved to Firebase")
            }, onFailure = {e ->
                Log.e(logcatTag, "Error saving the Question ${e.message}")
            }
        )
    }


    //Upload 5min Points to Firebase and reset 5min counter
    private fun upload5MinPointsToFirebase() {
        FirebaseManager.fetch5minPoints(
            userID = userID,
            onSuccess = { current5minPoints ->
                Log.i(logcatTag, "Current 5min Points: $current5minPoints")

                val new5MinPoints = current5minPoints + sessionPoints
                FirebaseManager.saveData(
                    userID = userID,
                    path = "current5minPoints",
                    data = new5MinPoints,
                    onSuccess = {
                        Log.i(logcatTag, "5min Points updated")
                    }, onFailure = {e ->
                        Log.e(logcatTag, "Error updating 5min Points: ${e.message}")
                    }
                )
            }, onFailure = {e ->
                Log.e(logcatTag, "Error Fetching 5min Points: ${e.message}")
            }
        )
    }


    private fun updateSessionWithFinalResults(){
        // Update Wins
        FirebaseManager.saveData(
            userID = userID,
            path = "sessions/$sessionID/wins",
            data = correctAnsCount,
            onSuccess = {Log.i(logcatTag, "Wins updated: $correctAnsCount")},
            onFailure = {e -> Log.e(logcatTag, "Error updating wins: ${e.message}")}
        )

        // Update Losses
        FirebaseManager.saveData(
            userID = userID,
            path = "sessions/$sessionID/loss",
            data = wrongAnsCount,
            onSuccess = {Log.i(logcatTag, "Loss updated: $wrongAnsCount")},
            onFailure = {e -> Log.e(logcatTag, "Error updating loss: ${e.message}")}
        )

        //Update points
        FirebaseManager.saveData(
            userID = userID,
            path = "sessions/$sessionID/points",
            data = sessionPoints,
            onSuccess = {Log.i(logcatTag, "Points updated: $sessionPoints")},
            onFailure = {e -> Log.e(logcatTag, "Error updating loss: ${e.message}")}
        )
    }

    // Displaying message using SnackBar
    private fun displayMessage(view: View, msgTxt : String) {
        val snackBar = Snackbar.make(view,msgTxt, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    // Functions to handle no Results scenario
    private fun showNoResultsLayout() {
        noResultsLayout.visibility = View.VISIBLE
        nextButton.visibility = View.GONE

        // Close the current activity and go back to previous one
        goBackButton.setOnClickListener { finish() }
    }


    //Calculate Points
    private fun calculatePoints(difficulty: String, type: String, streak: Long): Long {
        // Base points based on difficulty level
        val basePoints = when(difficulty){
            "easy" -> 5
            "medium" -> 10
            "hard" -> 15
            else ->  0
        }
        // Type multiplier
        val typeMultiplier = when(type) {
            "boolean" -> 1.0
            "multiple" -> 1.5
            "" -> 1.3
            else -> 1.0
        }
        // Streak Multiplier
        val streakMultiplier = when {
            streak < 3 -> 1.0
            streak in 3 .. 5 -> 1.2
            streak in 6 .. 9 -> 1.5
            else -> 2.0
        }
        return (basePoints * typeMultiplier * streakMultiplier).toLong()
    }

    //Calculate Coins
    private fun calculateCoins(difficulty: String, type: String, streak: Long): Long {
        // Base coins based on difficulty level
        val basePoints = when(difficulty){
            "easy" -> 1
            "medium" -> 2
            "hard" -> 3
            else ->  0
        }
        // Type multiplier
        val typeMultiplier = when(type) {
            "boolean" -> 1.0
            "multiple" -> 1.2
            "" -> 1.3
            else -> 1.0
        }
        // Streak Multiplier
        val streakMultiplier = when {
            streak < 3 -> 1.0
            streak in 3 .. 5 -> 1.1
            streak in 6 .. 9 -> 1.2
            else -> 1.5
        }
        return (basePoints * typeMultiplier * streakMultiplier).toLong()
    }

    // Fetch initial lifeline counts
    private fun fetchUserLifeLines() {
        FirebaseManager.fetchUserLife(userID,
            onSuccess = {
                    fiftyFifty, skip ->
                Log.i(logcatTag, "50-50: $fiftyFifty, Skip: $skip")
                fiftyFiftyLifeline = fiftyFifty
                skipLifeline = skip
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to fetch lifelines: ${e.message}")
            })
    }

    // Update lifelines counts to Firebase
    private fun updateLifeLinesToFirebase() {
        FirebaseManager.updateUserLifelines(
            userID = userID,
            fiftyFifty = fiftyFiftyLifeline,
            skip = skipLifeline,
            onSuccess = {
                Log.i(logcatTag, "New fifty and Skip LifeLine updated")
            }, onFailure = {e ->
                Log.e(logcatTag, "Failed to update the lifelines: ${e.message}")
            }
        )
    }
}