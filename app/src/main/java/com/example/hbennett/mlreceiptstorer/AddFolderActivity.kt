package com.example.hbennett.mlreceiptstorer

/**
 * AddFolderActivity.kt
 * Connor Black, Hunter Bennett
 *
 * Activity for creating a new folder for storing receipts
 */

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder

class AddFolderActivity : AppCompatActivity() {

    // Business Name RecyclerView
    private lateinit var recyclerViewBusinessNames: RecyclerView
    private lateinit var viewAdapter : RecyclerView.Adapter<*>
    private lateinit var viewManager : RecyclerView.LayoutManager

    // Misc
    private lateinit var businessNames: ArrayList<String>

    // Views
    private lateinit var editTextBusinessName: EditText
    private lateinit var editTextFolderName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_folder)

        // Initialize
        businessNames = ArrayList()
        editTextBusinessName = findViewById(R.id.editTextBusinessName)
        editTextFolderName = findViewById(R.id.editTextFolderName)

        // Set up recycler view parts
        recyclerViewBusinessNames = findViewById(R.id.recyclerViewBusinessNames)
        viewManager = LinearLayoutManager(this)
        viewAdapter = BusinessRecyclerAdapter(businessNames)
        recyclerViewBusinessNames.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        // Add swipe to delete to recycler
        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                businessNames.removeAt(pos)
                viewAdapter.notifyItemRemoved(pos)
            }
        }
        val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerViewBusinessNames)
    }

    // Add the input Business name to the list and RecyclerView
    fun onAddBusinessName(view: View) {
        val text = editTextBusinessName.text.toString().lowercase()
        if (text.isEmpty())
            return
        businessNames.add(text)
        viewAdapter.notifyDataSetChanged()
        editTextBusinessName.setText("")
        this.currentFocus?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    // Return to MainActivity
    fun onCancel(view: View) = finish()

    // Save the folder and its associated businesses the the DB and notify the MainActivity recycler
    fun onSaveFolder(view: View) {
        if(businessNames.isEmpty() && editTextFolderName.text.isEmpty()){
            Toast.makeText(this,R.string.folderAddFail, Toast.LENGTH_LONG).show()
            return
        }

        val folderName: String = editTextFolderName.text.toString()
        val result: Long = MainActivity.db.insertFolder(folderName, businessNames)
        if (result < 1L)
            Toast.makeText(this, R.string.addFolderFailed, Toast.LENGTH_LONG).show()
        else {
            MainActivity.addFolder(Folder(result, folderName))
            Toast.makeText(this, R.string.addFolderSuccess, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}