package com.example.hbennett.mlreceiptstorer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

import android.content.DialogInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    // Constants
    val REQUEST_IMAGE_CAPTURE = 1;
    val PICK_IMAGE = 2;

    // Misc
    lateinit var currentPhotoPath: String;
    lateinit var photoURI: Uri;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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