package com.example.performance

import org.json.JSONArray
import org.json.JSONObject

/**
 * LifelineManager is responsible for managing lifelines in a quiz game,
 * specifically the '50-50' lifeline. It helps in randomly choosing one
 * of the incorrect answers to eliminate, leaving only two possible
 * answers: the correct one and one incorrect answer
 */
class LifelineManager(
    private val questionList: List<JSONObject>,
    private var currentQuestionIndex: Int
){
    /**
     * Function to selects random incorrect answer from the list
     * of incorrect answers and returns shuffled list containing
     * the correct and one randomly selected incorrect answer
     * @param correctAnswer is teh correct answer to current question
     * @param incorrectAns is the incorrect answer to current question
     * @return shuffled list containing correct and incorrect answer.
     */
    fun useFiftyFifty(correctAnswer: String, incorrectAns: JSONArray)
            : List<String>{

        val randomIndex = (0 until incorrectAns.length()).random()
        val randomIncorrectAns = incorrectAns.getString(randomIndex)

        return listOf(correctAnswer, randomIncorrectAns).shuffled()
    }

}