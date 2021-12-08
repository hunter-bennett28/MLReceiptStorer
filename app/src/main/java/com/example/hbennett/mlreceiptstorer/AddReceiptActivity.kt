package com.example.hbennett.mlreceiptstorer

/**
 * AddReceiptActivity.kt
 * Connor Black, Hunter Bennett
 *
 * Activity for adding receipts to folders
 */

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Integer.min
import kotlin.math.round

class AddReceiptActivity : AppCompatActivity() {
    // Views
    lateinit var imageViewReceiptAdd: ImageView
    lateinit var spinnerFolderSelect: Spinner
    lateinit var editTextReceiptTotal: EditText
    lateinit var folders: ArrayList<Folder>

    // Misc
    lateinit var photoUriPath: String
    lateinit var folderNames: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)
        imageViewReceiptAdd = findViewById(R.id.imageViewReceiptAdd)
        spinnerFolderSelect = findViewById(R.id.spinnerFolderSelect)
        editTextReceiptTotal = findViewById(R.id.editTextReceiptTotal)
        editTextReceiptTotal.setText("0.0")
        photoUriPath = intent.getStringExtra("photoURI")!!
        var imageUri: Uri = Uri.parse(photoUriPath)
        var imageBitMap: Bitmap? =
            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageViewReceiptAdd.setImageBitmap(imageBitMap)

        processImageText(imageBitMap)

        // Get folders
        folderNames = ArrayList<String>()
        getFolderList()
        spinnerFolderSelect.setSelection(0)
    }

    // Gets the list of folders from the DB and extracts their names for use in the Spinner
    private fun getFolderList() : ArrayList<String> {
        // Get folders and names
        folders = MainActivity.db.getAllFolders()
        folderNames.clear()
        folderNames.add(getString(R.string.selectFolder))
        for (f in folders)
            folderNames.add(f.alias)

        // Set up adapter
        var adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, folderNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFolderSelect.adapter = adapter

        return folderNames
    }

    // Updates data on resuming, setting folder selection to newly added Folder if coming from AddFolderActivity
    override fun onResume() {
        super.onResume()
        val totalFolders: Int = folderNames.size
        getFolderList()
        if (folderNames.size > totalFolders)
            spinnerFolderSelect.setSelection(folderNames.size - 1)
    }

    // Sets up OCR recognizer and extracts text
    private fun processImageText(imageBitMap: Bitmap?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(imageBitMap!!, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                getReceiptTotal(visionText)
                getFolderRecommendation(visionText)
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this, R.string.parseFailed, Toast.LENGTH_LONG).show()
            }
    }

    // Attempts to get the total from the receipt, taking the last monetary value or the next after
    // finding the word "total"
    private fun getReceiptTotal(visionText: Text) {
        val moneyRegex: Regex = Regex(".*\\.[0-9]{2}\$")
        val totalRegex: Regex = Regex("^total[:]", RegexOption.IGNORE_CASE)
        var totalFound: Boolean = false
        var total: String = "0.0"

        // Get either tfhe last money value or the one immediately after finding "total"
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

    // Uses the Levenshtein distance formula for trying to get the correct business name from saved ones
    private fun getFolderRecommendation(visionText: Text) {
        val textBlockTitle: Text.TextBlock

        if (visionText.textBlocks.size > 0)
            textBlockTitle = visionText.textBlocks[0]
        else {
            Toast.makeText(this, "Error reading the title of the receipt", Toast.LENGTH_LONG)
                .show()
            return
        }

        //Run each through the levenshtein edit distance formula to detect which is the closest match
        var closestDistance: Int? = null
        var closestFolderIndex: Int = 0

        folders.forEachIndexed { index, f ->
            val businessNames = MainActivity.db.getBusinesses(f.id!!)

            //Get closest distance over all business names for the folder
            var distance: Int? = null
            businessNames.forEach { b ->
                val bNDistance = editDistance(textBlockTitle.text, b.name)

                if (distance == null)
                    distance = bNDistance
                else if (bNDistance < distance!!)
                    distance = bNDistance
            }

            //check the folder's closes distance against the current closest
            if (closestDistance == null) {
                closestDistance = distance
                closestFolderIndex = index
            } else if (distance!! < closestDistance!!) {
                closestDistance = distance
                closestFolderIndex = index
            }
        }

        spinnerFolderSelect.setSelection(min(closestFolderIndex + 1, folderNames.size - 1))
    }

    // Implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private fun editDistance(str1: String, str2: String): Int {
        var s1 = str1.lowercase()
        var s2 = str2.lowercase()
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

    // Saves the receipt to the DB
    fun onSaveReceipt(view: View) {
        // Ensure folder selected
        if (spinnerFolderSelect.selectedItemPosition == 0) {
            Toast.makeText(this, R.string.selectFolderError, Toast.LENGTH_LONG).show()
            return
        }

        //Check that a total is greater than 0
        if(editTextReceiptTotal.text.toString().toDouble() < 0.0){
            Toast.makeText(this, R.string.receiptTotalError, Toast.LENGTH_LONG).show()
            return
        }

        // Parse values
        val selectedFolder: String = spinnerFolderSelect.selectedItem.toString()
        val fid: Long = folders.find { it.alias === selectedFolder }?.id as Long

        // Ensure value is only 2 decimal places
        var totalValue: Double =
            round(editTextReceiptTotal.text.toString().toDouble() * 100.0) / 100.0

        // Add to db
        val result: Long = MainActivity.db.insertReceipt(fid, photoUriPath, totalValue)
        if (result < 1L)
            Toast.makeText(this, R.string.addReceiptFailed, Toast.LENGTH_LONG).show()
        else {
            Toast.makeText(this, R.string.addReceiptSuccess, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Launches the AddFolder activity
    fun onAddFolder(view: View) {
        val intent: Intent = Intent(this, AddFolderActivity::class.java)
        startActivity(intent)
    }

    //Opens the receipt image in a new activity for viewing
    fun onViewReceipt(view: View) {
        val intent: Intent = Intent(this, ViewReceiptActivity::class.java)
        intent.putExtra("image", photoUriPath)
        startActivity(intent)
    }
}