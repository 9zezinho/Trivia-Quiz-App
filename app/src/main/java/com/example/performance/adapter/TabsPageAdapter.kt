package com.example.performance.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * This is a Adapter class for managing the fragments in a
 * TabLayout with ViewPager2. This adapter is responsible
 * for populating teh tabs with fragments
 */
class TabsPageAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    // List of fragments
    private val fragments = listOf(
        LevelRankingFragment(), //Pos = 0
        EveryFiveMinRankingFragment() //Pos = 1
    )

    // Returns the size of fragments
    override fun getItemCount(): Int {
        return fragments.size
    }

    // Creates and returns the fragment corresponding to position
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}