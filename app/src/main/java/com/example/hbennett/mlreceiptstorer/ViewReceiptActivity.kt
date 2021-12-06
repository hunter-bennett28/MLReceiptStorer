package com.example.hbennett.mlreceiptstorer

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView

class ViewReceiptActivity : AppCompatActivity() {

    lateinit var imageViewSavedReceipt: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_receipt)
        imageViewSavedReceipt = findViewById(R.id.imageViewSavedReceipt)

        // Get URI and set image
        val photoUriPath: String = intent.getStringExtra("image")!!
        val imageUri: Uri = Uri.parse(photoUriPath)
        val imageBitMap: Bitmap? =
            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageViewSavedReceipt.setImageBitmap(imageBitMap)
    }
}