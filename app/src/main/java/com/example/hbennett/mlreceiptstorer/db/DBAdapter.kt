package com.example.hbennett.mlreceiptstorer.db

/**
 * DBAdapter.kt
 * Connor Black, Hunter Bennett
 *
 * DB utility class for database creation and management.
 */

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.hbennett.mlreceiptstorer.dataclasses.Business
import com.example.hbennett.mlreceiptstorer.dataclasses.Folder
import com.example.hbennett.mlreceiptstorer.dataclasses.Receipt
import java.io.*
import java.lang.Exception
import java.time.LocalDate
import kotlin.collections.ArrayList

class DBAdapter : Closeable {
    companion object {
        const val SQL_CREATE_TABLE_FOLDER: String = "CREATE TABLE IF NOT EXISTS ${DBContract.Folder.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DBContract.Folder.COLUMN_NAME_ALIAS} TEXT UNIQUE);"
        const val SQL_CREATE_TABLE_BUSINESS: String = "CREATE TABLE IF NOT EXISTS  ${DBContract.Business.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DBContract.Business.COLUMN_NAME_FOLDER_ID} INTEGER NOT NULL," + //FK
                    "${DBContract.Business.COLUMN_NAME_NAME} TEXT," +
                    "CONSTRAINT FK_FolderID FOREIGN KEY (${DBContract.Business.COLUMN_NAME_FOLDER_ID})" +
                    "REFERENCES ${DBContract.Folder.TABLE_NAME}(${BaseColumns._ID}));"
        const val SQL_CREATE_TABLE_RECEIPT: String = "CREATE TABLE IF NOT EXISTS ${DBContract.Receipt.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${DBContract.Receipt.COLUMN_NAME_FOLDER_ID} INTEGER NOT NULL," + //FK
                    "${DBContract.Receipt.COLUMN_NAME_IMAGE} TEXT," +
                    "${DBContract.Receipt.COLUMN_NAME_TOTAL} DOUBLE," +
                    "${DBContract.Receipt.COLUMN_NAME_DATE} TEXT," +
                    "CONSTRAINT FK_FolderID FOREIGN KEY (${DBContract.Receipt.COLUMN_NAME_FOLDER_ID}) " +
                    "REFERENCES ${DBContract.Folder.TABLE_NAME}(${BaseColumns._ID}));"
        const val TAG = "DBAdapter"
    }

    var context: Context? = null
    var DBHelper: DatabaseHelper? = null
    var db: SQLiteDatabase? = null
    val packageName = "com.example.hbennett.mlreceiptstorer"

    constructor(ctx: Context?, baseContext: Context) {
        context = ctx
        DBHelper = DatabaseHelper(context)
        //create a database if it doesnt exist already in the file path
        val destPath = "data/data/$packageName/databases"
        val f = File(destPath)
        if (!f.exists()) {
            f.mkdirs()
            f.createNewFile()
            //copy db from assets folder
            copyDB(
                baseContext.assets.open("mydb"),
                FileOutputStream("$destPath/MyDB")
            )
        }
        openDB()
    }

    // Helper class for performing DB management
    class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DBContract.DB_NAME, null, DBContract.DB_VERSION) {
        // Creates the required DB tables
        override fun onCreate(db: SQLiteDatabase) {
            try {
                db.execSQL(SQL_CREATE_TABLE_FOLDER)
                db.execSQL(SQL_CREATE_TABLE_BUSINESS)
                db.execSQL(SQL_CREATE_TABLE_RECEIPT)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        // Upgrades the DB if a new version is found
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                DBAdapter.TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
            )
            db.execSQL(
                "DROP TABLE IF EXISTS ${DBContract.Receipt.TABLE_NAME}" +
                        "DROP TABLE IF EXISTS ${DBContract.Business.TABLE_NAME}" +
                        "DROP TABLE IF EXISTS ${DBContract.Folder.TABLE_NAME}"
            )
            onCreate(db)
        }
    }

    // copyDB to copy assets to phone
    @Throws(IOException::class)
    fun copyDB(inputStream: InputStream, outputStream: OutputStream) {
        //Copy one byte at a time
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        inputStream.close()
        outputStream.close()
    }

    // opens the database
    @Throws(SQLException::class)
    private fun openDB(): DBAdapter? {
        db = DBHelper!!.writableDatabase
        return this
    }

    // closes the database
    private fun closeDB() {
        DBHelper!!.close()
    }

    /**
     * insertFolder - Insert a folder and its businesses into the DB
     */
    fun insertFolder(alias: String?, business: List<String>): Long {
        var initialValues = ContentValues()
        initialValues.put(DBContract.Folder.COLUMN_NAME_ALIAS, alias)
        var fid: Long = -1
        //Initialize transaction
        try {
            db!!.beginTransaction()

            fid = db!!.insert(DBContract.Folder.TABLE_NAME, null, initialValues)

            //Add each business to the business table
            for (b in business) {
                initialValues = ContentValues()
                initialValues.put(DBContract.Business.COLUMN_NAME_NAME, b)
                initialValues.put(DBContract.Business.COLUMN_NAME_FOLDER_ID, fid)
                db!!.insert(DBContract.Business.TABLE_NAME, null, initialValues)
            }

            db!!.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db!!.endTransaction()
            return fid
        }
    }

    /**
     * insertReceipt - Insert a Receipt with the folder id
     */
    fun insertReceipt(fid: Long, image: String?, total: Double?): Long {
        val initialValues = ContentValues()
        initialValues.put(DBContract.Receipt.COLUMN_NAME_FOLDER_ID, fid)
        initialValues.put(DBContract.Receipt.COLUMN_NAME_IMAGE, image)
        initialValues.put(DBContract.Receipt.COLUMN_NAME_TOTAL, total)
        initialValues.put(DBContract.Receipt.COLUMN_NAME_DATE, LocalDate.now().toString())

        return db!!.insert(DBContract.Receipt.TABLE_NAME, null, initialValues)
    }

    /**
     * deleteFolder - deletes a folder, its receipts, and its businesses from the DB by a folder row id
     */
    fun deleteFolder(rowId: Long): Boolean {
        var res: Boolean = false

        try {
            db!!.beginTransaction()

            res = db!!.delete(DBContract.Folder.TABLE_NAME, BaseColumns._ID + "=" + rowId, null) > 0

            if (res) { //A folder was deleted, we can delete each of its businesses
                db!!.delete(
                    DBContract.Business.TABLE_NAME,
                    DBContract.Business.COLUMN_NAME_FOLDER_ID + "=" + rowId,
                    null
                )
                db!!.delete(
                    DBContract.Receipt.TABLE_NAME,
                    DBContract.Receipt.COLUMN_NAME_FOLDER_ID + "=" + rowId,
                    null
                )
            }

            db!!.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db!!.endTransaction()
            return res
        }
    }

    /**
     * deleteReceipt - deletes a receipts by its ID
     */
    fun deleteReceipt(rowId: Long): Boolean {
        return db!!.delete(DBContract.Receipt.TABLE_NAME, BaseColumns._ID + "=" + rowId, null) > 0
    }

    /**
     *  getAllFolders - returns a list of the current folders
     */
    fun getAllFolders(): ArrayList<Folder> {
        var folders: ArrayList<Folder> = ArrayList()
        val cursor: Cursor = db!!.query(
            DBContract.Folder.TABLE_NAME, arrayOf(
                BaseColumns._ID, DBContract.Folder.COLUMN_NAME_ALIAS
            ), null, null, null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                folders.add(Folder(cursor.getLong(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        return folders
    }

    /**
     *  getBusinesses - Gets all businesses for a specified folder id
     */
    fun getBusinesses(folderId: Long): ArrayList<Business> {
        var businesses: ArrayList<Business> = ArrayList()
        val cursor: Cursor = db!!.rawQuery(
            "SELECT * FROM ${DBContract.Business.TABLE_NAME} WHERE ${DBContract.Business.COLUMN_NAME_FOLDER_ID} = ?",
            Array(1) { "$folderId" })

        if (cursor.moveToFirst()) {
            do {
                businesses.add(Business(cursor.getLong(0), cursor.getLong(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }
        return businesses
    }

    /**
     *  getReceipts - Gets all receipts for a specified folder id
     */
    fun getReceipts(folderId: Long): ArrayList<Receipt> {
        var receipts: ArrayList<Receipt> = ArrayList()
        val cursor: Cursor = db!!.rawQuery(
            "SELECT * FROM ${DBContract.Receipt.TABLE_NAME} WHERE ${DBContract.Receipt.COLUMN_NAME_FOLDER_ID} = ?",
            Array(1) { "$folderId" })

        if (cursor.moveToFirst()) {
            do {
                receipts.add(Receipt(
                    cursor.getLong(0), cursor.getLong(1), cursor.getString(2), cursor.getDouble(3), cursor.getString(4)
                ))
            } while (cursor.moveToNext())
        }
        return receipts
    }

    /**
     * updateFolder - updates the name of a folder based on the ID
     */
    fun updateFolder(rowId: Long, alias: String?): Boolean {
        val args = ContentValues()
        args.put(DBContract.Folder.COLUMN_NAME_ALIAS, alias)

        return db!!.update(
            DBContract.Folder.TABLE_NAME,
            args,
            BaseColumns._ID + "=" + rowId,
            null
        ) > 0
    }

    /**
     * close - destructor method for ensuring DB is closed
     */
    override fun close() {
        closeDB()
    }

}