package com.example.hbennett.mlreceiptstorer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.db.DBAdapter
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    // Constants
    private val requestImageCapture = 1
    private val pickImage = 2

    // Misc
    lateinit var currentPhotoPath: String
    lateinit var photoURI: Uri

    // Folder RecyclerView
    private lateinit var recyclerViewFolders: RecyclerView
    private lateinit var viewManager : RecyclerView.LayoutManager

    // Views
    private lateinit var textViewGetStarted: TextView

    companion object {
        lateinit var db: DBAdapter
        lateinit var viewAdapter : RecyclerView.Adapter<*>
        lateinit var folders: ArrayList<Folder>
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

        // Show get started message if no folders created
        textViewGetStarted = findViewById(R.id.textViewGetStarted)
        if (folders.size === 0)
            textViewGetStarted.visibility = View.VISIBLE

        //Load recycler view from the folders
        recyclerViewFolders = findViewById(R.id.recyclerViewFolders)
        viewManager = LinearLayoutManager(this)
        viewAdapter = FolderRecyclerAdapter(this, folders)

        recyclerViewFolders.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
    }

    // Reload folders data on resume because other activites add folders
    override fun onResume() {
        super.onResume()
        // If there are folders, hide the get started message
        if (folders.size > 0)
            textViewGetStarted.visibility = View.INVISIBLE
        recyclerViewFolders.adapter?.notifyDataSetChanged()
    }

    // Creates a dialog for the user to select image upload method
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

    // Creates a file to save the image in and launches the ACTION_IMAGE_CAPTURE intent for taking photos
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
                    startActivityForResult(takePictureIntent, requestImageCapture)
                }
            }
        }
    }

    // Handles processing results returned from the take photo or upload photo activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestImageCapture && resultCode == RESULT_OK) {
            launchReceiptActivity()
        } else if (requestCode == pickImage && resultCode == RESULT_OK) {
            // Extract the URI from the result
            photoURI = data!!.data!!

            // Save photo data to new file since API revokes access once app closed for returned URI
            try {
                savePhotoURI()
            } catch (ex: Exception) {
                Toast.makeText(this, R.string.savePhotoFailed, Toast.LENGTH_LONG).show()
                return
            }
            launchReceiptActivity()
        }
    }

    // Launches the AddReceiptActivity, passing the saved URI
    private fun launchReceiptActivity() {
        val intent: Intent = Intent(this, AddReceiptActivity::class.java)
        intent.putExtra("photoURI", photoURI.toString())
        startActivity(intent)
    }

    // Save the content of the current photoURI to a new local file for permanent access
    private fun savePhotoURI() {
        // Create a file to save the content in
        val saveFile: File = createImageFile()

        // Save the image Bitmap to the new file
        var fos: FileOutputStream = FileOutputStream(saveFile)
        val imageBitMap: Bitmap? =
            MediaStore.Images.Media.getBitmap(this.contentResolver, photoURI)
        imageBitMap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        // Update the photo's URI
        photoURI = saveFile.toUri()
    }

    // Creates a new image file to save the receipt image in for permanent local access
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("receipt_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Launches the ACTION_PICK intent for selecting a receipt from the camera roll
    private fun onSelectPhoto() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }

    // Opens the AddFolderActivity
    fun onAddFolder(view: View) {
        val intent: Intent = Intent(this, AddFolderActivity::class.java)
        startActivity(intent)
    }
}