package com.example.hbennett.mlreceiptstorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.dataclasses.Receipt
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class FolderActivity : AppCompatActivity() {
    // Bundled vars
    private var id: Long? = null; // Default since kotlin cannot lateinit primitives
    private var folderName: String? = null; // Store this in case we decide that we can edit folder names in the activity

    // Receipt RecyclerView
    private lateinit var recyclerViewReceipts: RecyclerView;
    private lateinit var viewManager : RecyclerView.LayoutManager;

    //Misc
    lateinit var receipts : ArrayList<Receipt>;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        // Will pass through the Long and String (id, folderName)
        id = intent.getLongExtra("id", -1)
        folderName = intent.getStringExtra("folderName")
            .toString() //Requires a toString (Kotlin is weird)

        findViewById<TextView>(R.id.textViewFolderName).text = folderName

        //Load a recyclerView with the item data
        receipts = MainActivity.db.getReceipts(id!!);
        recyclerViewReceipts = findViewById(R.id.recyclerViewReceipts);
        viewManager = LinearLayoutManager(this)
        MainActivity.viewAdapter = ReceiptRecyclerAdapter(this, receipts);

        recyclerViewReceipts.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = MainActivity.viewAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        //Calculate the current total of the receipts
        calcRunningTotal();
    }

    // Get each receipt on the current folder and sum up the totals
    fun calcRunningTotal() {
        //TODO
        var total: Double=0.0;

        for(r in receipts)
            total+=r.total;

        val numberFormatter: NumberFormat = NumberFormat.getCurrencyInstance()
        findViewById<TextView>(R.id.textViewRunningTotalValue).text= numberFormatter.format(total);
    }
}