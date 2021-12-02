package com.example.hbennett.mlreceiptstorer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
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
        viewAdapter = BusinessRecyclerAdapter(this, businessNames)
        recyclerViewBusinessNames.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    fun onAddBusinessName(view: View) {
        val text = editTextBusinessName.text.toString().lowercase()
        if (text.isEmpty())
            return
        businessNames.add(text)
        viewAdapter.notifyDataSetChanged()
        editTextBusinessName.setText("")
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun onCancel(view: View) = finish()

    fun onSaveFolder(view: View) {
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