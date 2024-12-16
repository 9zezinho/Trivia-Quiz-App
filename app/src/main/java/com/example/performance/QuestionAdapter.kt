package com.example.performance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

/**
 * This class adapts a list of Question objects to be displayed in a
 * RecyclerView. It uses DiffUtil to efficiently update the list.
 */

class QuestionAdapter (private var questions: MutableList<Question>)
    : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    private val  logcatTag = "QAdapter" //for debugging

    // ViewHOlder class to hold the view for each question item.
    inner class QuestionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val questionTextView: MaterialTextView =
            itemView.findViewById(R.id.questionText)
        val correctAnsTextView: MaterialTextView =
            itemView.findViewById(R.id.correctAnswer)
        val userResponseTextView: MaterialTextView =
            itemView.findViewById(R.id.userResponse)
    }

    // Create a new ViewHolder fore each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
        val v = view.inflate(R.layout.item_question,parent,false)
        return QuestionViewHolder(v)
    }

    // Bind data to the views in each ViewHolder
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        Log.i(logcatTag,"Q: $question")
        holder.questionTextView.text = holder.itemView.context
            .getString(R.string.q, question.questionText)
        holder.correctAnsTextView.text = holder.itemView.context
            .getString(R.string.correct_ans, question.correctAnswer)
        holder.userResponseTextView.text = holder.itemView.context
            .getString(R.string.your_ans_history, question.userResponse)
    }

    // Returns the current item count in list
    override fun getItemCount(): Int {
        return questions.size
    }

    // Function to get list of all questions
    fun updateQuestions(newQuestions: List<Question>) {

        // Apply DiffUtil to update the session list
        applyDiff(
            oldList = questions,
            newList = newQuestions,
            adapter = this,
            areItemsTheSame = {oldQuestion, newQuestion ->
                oldQuestion.questionId == newQuestion.questionId},
            areContentsTheSame = {oldQuestion, newQuestion ->
                oldQuestion == newQuestion}
        )
        questions = newQuestions.toMutableList()
    }
}