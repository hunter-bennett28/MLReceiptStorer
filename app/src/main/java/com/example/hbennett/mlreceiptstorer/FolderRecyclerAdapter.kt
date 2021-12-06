package com.example.hbennett.mlreceiptstorer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder

class FolderRecyclerAdapter(
    private val context: Context,
    private val folders: ArrayList<Folder>
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
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.findViewById<TextView>(R.id.textViewRecyclerViewFolderTitle).text =
            folders[position].alias //Set it to use the name of the folder

        //Set on click for when the user interacts with a folder
        holder.view.findViewById<TextView>(R.id.textViewRecyclerViewFolderTitle)
            .setOnClickListener() {
                val intent: Intent = Intent(context, FolderActivity::class.java)
                intent.putExtra("id", folders[position].id)
                intent.putExtra("folderName", folders[position].alias)
                context.startActivity(intent)
            }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = folders.size


}