package com.example.hbennett.mlreceiptstorer

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import com.example.hbennett.mlreceiptstorer.DB.DBAdapter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AddReceiptActivity : AppCompatActivity() {
    // Views
    lateinit var imageViewReceiptAdd: ImageView;
    lateinit var spinnerFolderSelect: Spinner;
    lateinit var editTextReceiptTotal: EditText;
    lateinit var db: DBAdapter;
    lateinit var folders: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)
        imageViewReceiptAdd = findViewById(R.id.imageViewReceiptAdd)
        spinnerFolderSelect = findViewById(R.id.spinnerFolderSelect)
        editTextReceiptTotal = findViewById(R.id.editTextReceiptTotal)
        editTextReceiptTotal.setText("0.0")
        val photoUriPath = intent.getStringExtra("photoURI")!!
        var imageUri: Uri = Uri.parse(photoUriPath);
        var imageBitMap: Bitmap? =
            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri);
        imageViewReceiptAdd.setImageBitmap(imageBitMap);

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(imageBitMap, 0)
        val moneyRegex: Regex = Regex(".*\\.[0-9]{2}\$")
        val totalRegex: Regex = Regex("^total.*", RegexOption.IGNORE_CASE)
        var totalFound: Boolean = false;
        var total: String = "0.0";
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    var text = block.text
                    if (moneyRegex.matches(text)) {
                        total = text
                        if (totalFound) break
                    } else if (totalRegex.matches((text)))
                        totalFound = true
                }
                editTextReceiptTotal.setText(total)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }

        folders = mutableListOf();

        //Set the spinner to a default folder for now
        db = DBAdapter(this)
        db.open()

        var c = db.getAllFolders();
        if (c!!.moveToFirst()) {
            do {
                folders.add(c.getString(1));
            } while (c.moveToNext())
        }

        db.close()
        var adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, folders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFolderSelect.adapter=adapter;
    }


}