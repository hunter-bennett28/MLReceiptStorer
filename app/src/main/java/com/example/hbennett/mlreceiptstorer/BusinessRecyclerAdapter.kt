package com.example.hbennett.mlreceiptstorer

/**
 * BusinessRecyclerAdapter.kt
 * Connor Black, Hunter Bennett
 *
 * Adapter class for the business name RecyclerView
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BusinessRecyclerAdapter(
    private val businesses: ArrayList<String>
) :
    RecyclerView.Adapter<BusinessRecyclerAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusinessRecyclerAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_business_names_layout, parent, false) as View
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.textViewBusinessName).text =
            businesses[position] //Set it to use the name of the folder
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = businesses.size
}