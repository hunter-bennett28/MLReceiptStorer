package com.example.hbennett.mlreceiptstorer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.example.hbennett.mlreceiptstorer.DB.DBAdapter
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class AddReceiptActivity : AppCompatActivity() {
    // Views
    lateinit var imageViewReceiptAdd: ImageView;
    lateinit var spinnerFolderSelect: Spinner;
    lateinit var editTextReceiptTotal: EditText;
    lateinit var folders: ArrayList<Folder>

    // Misc
    lateinit var photoUriPath: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)
        imageViewReceiptAdd = findViewById(R.id.imageViewReceiptAdd)
        spinnerFolderSelect = findViewById(R.id.spinnerFolderSelect)
        editTextReceiptTotal = findViewById(R.id.editTextReceiptTotal)
        editTextReceiptTotal.setText("0.0")
        photoUriPath = intent.getStringExtra("photoURI")!!
        var imageUri: Uri = Uri.parse(photoUriPath);
        var imageBitMap: Bitmap? =
            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri);
        imageViewReceiptAdd.setImageBitmap(imageBitMap);

        setUpRecognizer(imageBitMap)

        // Get folders
        folders = MainActivity.db.getAllFolders()
        val folderNames: ArrayList<String> = ArrayList()
        for (f in folders)
            folderNames.add(f.alias)

        var adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, folderNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFolderSelect.adapter = adapter;
    }

    private fun setUpRecognizer(imageBitMap: Bitmap?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(imageBitMap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                getReceiptTotal(visionText)
                getFolderRecommendation(visionText)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, R.string.parseFailed, Toast.LENGTH_LONG).show()
            }
    }

    private fun getReceiptTotal(visionText: Text) {
        val moneyRegex: Regex = Regex(".*\\.[0-9]{2}\$")
        val totalRegex: Regex = Regex("^total.*", RegexOption.IGNORE_CASE)
        var totalFound: Boolean = false;
        var total: String = "0.0";

        // Get either the last money value or the one immediately after finding "total"
        for (block in visionText.textBlocks) {
            var text = block.text
            if (moneyRegex.matches(text)) {
                total = text
                if (totalFound) break
            } else if (totalRegex.matches((text)))
                totalFound = true
        }
        // remove $ if found
        editTextReceiptTotal.setText(total.replace("$", ""))
    }

    private fun getFolderRecommendation(visionText: Text) {
        val textBlockTitle: Text.TextBlock;

        if (visionText.textBlocks.size > 0)
            textBlockTitle = visionText.textBlocks[0];
        else {
            Toast.makeText(this, "Error reading the title of the receipt", Toast.LENGTH_LONG)
                .show();
            return;
        }

        //Run each through the levenshtein edit distance formula to detect which is the closest match
        var closestDistance: Int? = null;
        var closestFolderIndex: Int = 0;

        folders.forEachIndexed { index, f ->
            val businessNames = MainActivity.db.getBusinesses(f.id!!)

            //Get closest distance over all business names for the folder
            var distance: Int? = null;
            businessNames.forEach { b ->
                val bNDistance = editDistance(textBlockTitle.text, b.name)

                if (distance == null)
                    distance = bNDistance;
                else if (bNDistance < distance!!)
                    distance = bNDistance;
            }

            //check the folder's closes distance against the current closest
            if (closestDistance == null) {
                closestDistance = distance;
                closestFolderIndex = index;
            } else if (distance!! < closestDistance!!) {
                closestDistance = distance;
                closestFolderIndex = index;
            }
        }

        findViewById<Spinner>(R.id.spinnerFolderSelect).setSelection(closestFolderIndex)
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    fun editDistance(s1: String, s2: String): Int {
        var s1 = s1
        var s2 = s2
        s1 = s1.toLowerCase()
        s2 = s2.toLowerCase()
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) costs[j] = j else {
                    if (j > 0) {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) newValue = Math.min(
                            Math.min(newValue, lastValue),
                            costs[j]
                        ) + 1
                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }
            if (i > 0) costs[s2.length] = lastValue
        }
        return costs[s2.length]
    }

    fun onSaveReceipt(view: View) {
        // Parse values
        val selectedFolder: String = spinnerFolderSelect.selectedItem.toString()
        val fid: Long = folders.find { it.alias === selectedFolder }?.id as Long

        // Ensure value is only 2 decimal places
        var totalValue: Double =
            Math.round(editTextReceiptTotal.text.toString().toDouble() * 100.0) / 100.0

        // Add to db
        val result: Long = MainActivity.db.insertReceipt(fid, photoUriPath, totalValue)
        if (result < 1L)
            Toast.makeText(this, R.string.addReceiptFailed, Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(this, R.string.addReceiptSuccess, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    fun onAddFolder(view: View) {
        val intent: Intent = Intent(this, AddFolderActivity::class.java)
        startActivity(intent)
    }

}