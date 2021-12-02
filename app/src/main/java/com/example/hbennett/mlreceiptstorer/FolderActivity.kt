package com.example.hbennett.mlreceiptstorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class FolderActivity : AppCompatActivity() {
    // Bundled vars
    private var id: Long? = null; // Default since kotlin cannot lateinit primitives
    private var folderName: String? =
        null; // Store this in case we decide that we can edit folder names in the activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        // Will pass through the Long and String (id, folderName)
        id = intent.getLongExtra("id", -1)
        folderName = intent.getStringExtra("folderName")
            .toString() //Requires a toString (Kotlin is weird)

        findViewById<TextView>(R.id.textViewFolderName).text = folderName

        calcRunningTotal();
    }

    // Get each receipt on the current folder and sum up the totals
    fun calcRunningTotal() {
        //TODO
        val total: Double=0.0;

        // Set text to {string resource}: $#.##
        findViewById<TextView>(R.id.textViewRunningTotalValue).text= "$"+total.toString();
    }
}