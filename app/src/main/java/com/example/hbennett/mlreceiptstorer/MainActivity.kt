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
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.hbennett.mlreceiptstorer.DB.DBAdapter
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    // Constants
    val REQUEST_IMAGE_CAPTURE = 1;
    val PICK_IMAGE = 2;

    // Misc
    lateinit var currentPhotoPath: String;
    lateinit var photoURI: Uri;
    lateinit var recyclerViewFolder: RecyclerView;

    //Database Related
    lateinit var folders: MutableList<Pair<Long, String>>; //Store the id and the folder name
    lateinit var db: DBAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Load the folders from the DB
        db = DBAdapter(this)
        //Initialize empty list
        folders = mutableListOf<Pair<Long, String>>();

        // get the existing database file or from the assets folder if doesn't exist
        try {
            //create a database if it doesnt exist already in the file path
            val destPath = "data/data/$packageName/databases"
            val f = File(destPath)
            if (!f.exists()) {
                f.mkdirs()
                f.createNewFile()
                //copy db from assets folder
                CopyDB(
                    baseContext.assets.open("mydb"),
                    FileOutputStream("$destPath/MyDB")
                )
            }

            //Load the current folders
            db.open()
            var c : Cursor? = db.getAllFolders();

            if (c!!.moveToFirst()) {
                do {
                    folders.add(Pair<Long, String> (c.getLong(0), c.getString(1)));
                } while (c.moveToNext())
            }

            db.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // copyDB to copy assets to phone
    @Throws(IOException::class)
    fun CopyDB(inputStream: InputStream, outputStream: OutputStream) {
        //Copy one byte at a time
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        inputStream.close()
        outputStream.close()
    }

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
                val photoFile: File? = try { createImageFile() } catch (ex: IOException) { null }
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
        }
        else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
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
}