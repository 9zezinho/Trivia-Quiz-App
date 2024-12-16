package com.example.performance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.util.Date

/**
 * This clas is designed to efficiently handle teh display of sessions
 * with a list of questions and allows users to expand or collapse each session
 * to view or hide the questions.
 */
class HistoryAdapter(private val sessions: MutableList<Session>)
    : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> () {
        private var logcatTag = "HAdapter" //for debugging

    // Track expanded session positions
    private val expandedSessions = mutableSetOf<Int>()

    // ViewHolder class to represent each item(session) in the RecyclerView
    inner class  HistoryViewHolder(itemView: View)
        :RecyclerView.ViewHolder(itemView) {
        val sessionTimeStamp: MaterialTextView =
            itemView.findViewById(R.id.sessionTimestamp)
        val questionsRecyclerView: RecyclerView =
            itemView.findViewById(R.id.questionsRecyclerView)
        val expandButton: MaterialButton =
            itemView.findViewById(R.id.expandBtn)
        val collapseButton: MaterialButton =
            itemView.findViewById(R.id.collapseBtn)
    }

    // Creates a new ViewHolder instance when a new item is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        : HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
        val v = view.inflate(R.layout.item_session,parent,false)
        return HistoryViewHolder(v)
    }

    // Binds data(session) to ViewHolder for specified position in the list
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val session = sessions[position]
        val formattedTimeStamp = Date(session.timeStamp).toString()
        holder.sessionTimeStamp.text = holder.itemView.context
            .getString(R.string.session_timeStamp, formattedTimeStamp)

        //Check if the session is expanded and adjust visibility of questionsRecyclerView
        val isExpanded = expandedSessions.contains(position)
        holder.questionsRecyclerView.visibility =
                if (isExpanded) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        // Set up QAdapter only if not already set
        if(holder.questionsRecyclerView.adapter == null) {
            holder.questionsRecyclerView.adapter =
                QuestionAdapter(session.questions.toMutableList())
            holder.questionsRecyclerView.layoutManager =
                LinearLayoutManager(holder.itemView.context)
        } else {
            // update the data in the adapter if RecycleView already has an adapter
            (holder.questionsRecyclerView.adapter as QuestionAdapter)
                .updateQuestions(session.questions)

        }

        // Show (expand/collapse) based on whether teh session is expanded or collapsed
        holder.questionsRecyclerView.visibility =
            if (isExpanded){
                View.VISIBLE
            } else {
                View.GONE
            }
        holder.expandButton.visibility =
                if (isExpanded) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        holder.collapseButton.visibility =
                if(isExpanded) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        // Set click listener on expandTextView to toggle the session
        holder.expandButton.setOnClickListener{
            expandedSessions.add(position)
            Log.i(logcatTag,"Expand Clicked at position: $position")
            notifyItemChanged(position)
        }

        // Set click listener on collapse~TextView to toggle the session
        holder.collapseButton.setOnClickListener{
            expandedSessions.remove(position)
            Log.i(logcatTag,"Collapse clicked")
            notifyItemChanged(position)
        }
    }

    // Returns the total number of items(sessions) in the list
    override fun getItemCount(): Int {
        Log.i(logcatTag,"Item Count: ${sessions.size}")
        return sessions.size
    }
}