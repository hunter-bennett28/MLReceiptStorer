package com.example.hbennett.mlreceiptstorer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.DB.DBAdapter
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    // Constants
    val REQUEST_IMAGE_CAPTURE = 1;
    val PICK_IMAGE = 2;

    // Misc
    lateinit var currentPhotoPath: String;
    lateinit var photoURI: Uri;

    // Folder RecyclerView
    private lateinit var recyclerViewFolders: RecyclerView;
    private lateinit var viewManager : RecyclerView.LayoutManager;

    // Views
    private lateinit var textViewGetStarted: TextView;

    companion object {
        lateinit var db: DBAdapter;
        lateinit var viewAdapter : RecyclerView.Adapter<*>;
        lateinit var folders: ArrayList<Folder>;
        fun addFolder (folder: Folder) {
            folders.add(folder)
            viewAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Load the folders from the DB
        db = DBAdapter(this, baseContext)
        folders = db.getAllFolders()

        // get the existing database file or from the assets folder if doesn't exist
//        try {
//
//
//            //DEBUG DATA TO LOAD IN
//            // Dont worry about multiple entries every time you load, it will fail the
//            // insert if it already exists since folder name is a unique field
//            db.open()
//            db.insertFolder("folder name", listOf<String>())
//            db.insertFolder("Another Demo Folder", listOf<String>())
//            db.insertFolder("This folder doesnt smell like ham...", listOf<String>())
//            db.insertFolder("Dennis the Dennist", listOf<String>())
//        } finally {
//            db.close()
//        }

        // Show get started message if no folders created
        textViewGetStarted = findViewById(R.id.textViewGetStarted)
        if (folders.size === 0)
            textViewGetStarted.visibility = View.VISIBLE;

        //Load recycler view from the folders
        recyclerViewFolders = findViewById(R.id.recyclerViewFolders);
        viewManager = LinearLayoutManager(this)
        viewAdapter = FolderRecyclerAdapter(this, folders);

        recyclerViewFolders.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onResume() {
        super.onResume()
        if (folders.size > 0)
            textViewGetStarted.visibility = View.INVISIBLE
        recyclerViewFolders.adapter?.notifyDataSetChanged()
    }

// ****  Receipt Selection  ****

    fun onAddReceipt(view: View) {
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.addReceipt)
            .setMessage(R.string.selectReceiptPrompt)
            .setPositiveButton(R.string.takePhoto, DialogInterface.OnClickListener { dialog, id ->
                onTakePhoto()
            })
            // use neutral as cancel because it is placed far left
            .setNeutralButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })
            // use negative as affirmative option because it is placed center
            .setNegativeButton(R.string.selectPhoto, DialogInterface.OnClickListener { dialog, id ->
                onSelectPhoto()
            })

        builder.show()
    }

    private fun onTakePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Check there's a camera activity to handle intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            launchReceiptActivity()
        } else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            photoURI = data!!.data!!;
            launchReceiptActivity()
        }
    }

    private fun launchReceiptActivity() {
        val intent: Intent = Intent(this, AddReceiptActivity::class.java)
        intent.putExtra("photoURI", photoURI.toString())
        startActivity(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("receipt_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun onSelectPhoto() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    fun onAddFolder(view: View) {
        val intent: Intent = Intent(this, AddFolderActivity::class.java)
        startActivity(intent)
    }
}