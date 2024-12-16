package com.example.performance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

/**
 * Adapter class for displaying the rankings in a RecyclerView.
 * It binds the ranking data to the views in the RecyclerView's
 * item layout.
 */

class RankingAdapter(private val rankings: List<Rankings>)
    : RecyclerView.Adapter<RankingAdapter.RankingViewHolder> (){

    private var logcatTag = "RAdapter" //for debugging
    inner class RankingViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {
        val rankPos: MaterialTextView =
            itemView.findViewById(R.id.rankPosition)
        val userPoints: MaterialTextView =
            itemView.findViewById(R.id.userPoints)
        val userLvl: MaterialTextView =
            itemView.findViewById(R.id.userLevel)
        val userName: MaterialTextView =
            itemView.findViewById(R.id.userName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RankingAdapter.RankingViewHolder {
        // Inflate the item ranking layout
        val view = LayoutInflater.from(parent.context)
        val v = view.inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(v)
    }

    /**
     * Binds data to the views in teh ViewHolder. It is called for each item
     * in the list.
     * @param holder is used to display the data
     * @param position is the position of the current item in the data list
     */
    override fun onBindViewHolder(holder: RankingAdapter.RankingViewHolder,
                                  position: Int) {
        val rank = rankings[position]
        holder.rankPos.text = holder.itemView.context
            .getString(R.string.rank_pos, position + 1)
        holder.userName.text = rank.userName

        holder.userLvl.text = holder.itemView.context
            .getString(R.string.user_lvl, rank.lvl)

        // Set the points based on ranking type(LEVEL or EVERY_5_MIN)
        if (rank.type == RankingType.LEVEL){
            holder.userPoints.text = holder.itemView.context
                .getString(R.string.user_points, rank.totalPoints)
        } else {
            holder.userPoints.text = holder.itemView.context
                .getString(R.string.user_points, rank.current5minPoints)
        }
    }

    // Returns the total number of items in the rankings list.
    override fun getItemCount(): Int {
        Log.i(logcatTag,"Ranking Size: ${rankings.size}")
        return rankings.size
    }

}