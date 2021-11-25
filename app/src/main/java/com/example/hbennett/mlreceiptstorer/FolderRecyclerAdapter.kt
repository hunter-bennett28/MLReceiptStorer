package com.example.hbennett.mlreceiptstorer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderRecyclerAdapter(
    private val context: Context,
    private val folders: MutableList<Pair<Long, String>>
) :
    RecyclerView.Adapter<FolderRecyclerAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FolderRecyclerAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_folder_layout, parent, false) as View
        // set the view's size, margins, paddings and layout parameters
        val lp = view.layoutParams
        lp.height = parent.measuredHeight / 10 //display 10 per screen
        view.layoutParams = lp
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.textViewRecyclerViewFolderTitle).text =
            folders[position].second //Set it to use the name of the folder

        //Set on click for when the user interacts with a folder
        holder.view.findViewById<TextView>(R.id.textViewRecyclerViewFolderTitle)
            .setOnClickListener() {
                val intent: Intent = Intent(context, FolderActivity::class.java);
                intent.putExtra("id", folders[position].first);
                intent.putExtra("folderName", folders[position].second);
                context.startActivity(intent)
            }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = folders.size


}