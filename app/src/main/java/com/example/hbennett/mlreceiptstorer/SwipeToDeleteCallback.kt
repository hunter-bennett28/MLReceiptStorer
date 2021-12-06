package com.example.hbennett.mlreceiptstorer

/**
 * SwipeToDeleteCallback.kt
 * Connor Black, Hunter Bennett
 *
 * Utility class for implementing swiping a RecyclerView's item to delete it
 */

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDeleteCallback : ItemTouchHelper.Callback() {
    // Only allow LEFT swipes
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(0, ItemTouchHelper.LEFT)

    // To be overridden per creation
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false
}