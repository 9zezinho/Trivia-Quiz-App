package com.example.performance

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * GenericDiffCallback is a reusable class that compares two lists of
 * objects (old and new lists) and tells the RecyclerView adapter
 * what has changed in the list
 */

class GenericDiffCallback<T> (
    private val oldList: List<T>,
    private val newList: List<T>,
    private val areItemsTheSame: (T, T) -> Boolean,
    private val areContentsTheSame: (T, T) -> Boolean
) : DiffUtil.Callback() {

    // Returns the size of old list
    override fun getOldListSize(): Int {
        return oldList.size
    }

    // Returns the size of new list
    override fun getNewListSize(): Int {
       return newList.size
    }

    /**
     * Compares whether the items at the old and new positions are
     * same and returns a Boolean
     *
     * @param oldItemPosition is the pos of old Item
     * @param newItemPosition is the pos of new Item
     */
    override fun areItemsTheSame(oldItemPosition: Int,
                                 newItemPosition: Int): Boolean {
        return areItemsTheSame(oldList[oldItemPosition],
            newList[newItemPosition])
    }

    /**
     * Compares whether the contents of the items at the old
     * and new positions are same and returns a Boolean
     *
     * @param oldItemPosition is the pos of old Item
     * @param newItemPosition is the pos of new Item
     */
    override fun areContentsTheSame(oldItemPosition: Int,
                                    newItemPosition: Int): Boolean {
        return areContentsTheSame(oldList[oldItemPosition],
            newList[newItemPosition])
    }
}

/**
 * A utility function to apply the diff callback and
 * update the RecycleView adapter
 *
 * @param oldList is the old list
 * @param newList is the new list
 * @param adapter the RecyclerView adapter that should be notified of updates
 * @param areContentsTheSame checks if the contents are same
 * @param areItemsTheSame checks if the items are same
 */
fun <T> applyDiff(
    oldList: List<T>,
    newList: List<T>,
    adapter: RecyclerView.Adapter<*>,
    areItemsTheSame: (T, T) -> Boolean,
    areContentsTheSame: (T, T) -> Boolean
) {
    val diffCallback =
        GenericDiffCallback(oldList, newList, areItemsTheSame, areContentsTheSame)
    val diffResult = DiffUtil.calculateDiff(diffCallback)
    diffResult.dispatchUpdatesTo(adapter) // notify adapter of the changes

}