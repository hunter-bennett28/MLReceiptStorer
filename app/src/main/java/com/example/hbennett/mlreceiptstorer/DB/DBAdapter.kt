package com.example.hbennett.mlreceiptstorer.DB

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class DBAdapter {
    companion object {
        val SQL_CREATE_TABLES_SCRIPT = "CREATE TABLE ${DBContract.Folder.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${DBContract.Folder.COLUMN_NAME_ALIAS} TEXT UNIQUE);" +
                "" +
                "CREATE TABLE ${DBContract.BusinessPseudonym} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${DBContract.BusinessPseudonym.COLUMN_NAME_FOLDER_ID} INTEGER NOT NULL" + //FK
                "${DBContract.BusinessPseudonym.COLUMN_NAME_PSEUDONYM} TEXT" +
                "CONSTRAINT FK_FolderID FOREIGN KEY (${DBContract.BusinessPseudonym.COLUMN_NAME_FOLDER_ID})" +
                "REFERENCES ${DBContract.Folder.TABLE_NAME}(${BaseColumns._ID}));" +
                "" +
                "CREATE TABLE ${DBContract.Receipt} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${DBContract.Receipt.COLUMN_NAME_FOLDER_ID} INTEGER NOT NULL" + //FK
                "${DBContract.Receipt.COLUMN_NAME_IMAGE} TEXT" +
                "${DBContract.Receipt.COLUMN_NAME_TOTAL} DOUBLE" +
                "CONSTRAINT FK_FolderID FOREIGN KEY (${DBContract.Receipt.COLUMN_NAME_FOLDER_ID})" +
                "REFERENCES ${DBContract.Folder.TABLE_NAME}(${BaseColumns._ID})"
        val TAG = "DBAdapter"
    }

    var context: Context? = null
    var DBHelper: DatabaseHelper? = null
    var db: SQLiteDatabase? = null

    constructor(ctx: Context?) {
        context = ctx
        DBHelper = DatabaseHelper(context)
    }

    class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DBContract.DB_NAME, null, DBContract.DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            try {
                db.execSQL(SQL_CREATE_TABLES_SCRIPT)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                DBAdapter.TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
            )
            db.execSQL("DROP TABLE IF EXISTS contacts")
            onCreate(db)
        }
    }

    //---opens the database---
    @Throws(SQLException::class)
    fun open(): DBAdapter? {
        db = DBHelper!!.writableDatabase
        return this
    }

    //---closes the database---
    fun close() {
        DBHelper!!.close()
    }

    /**
     * insertFolder - Insert a folder and its pseudonyms into the DB
     */
    fun insertFolder(alias: String?, pseudonym: List<String>): Long {
        val initialValues = ContentValues()
        initialValues.put(DBContract.Folder.COLUMN_NAME_ALIAS, alias)
        var fid = db!!.insert(DBContract.Folder.TABLE_NAME, null, initialValues)

        //Add each pseudonym to the pseudonym table
        for(p in pseudonym){
            val initialValues = ContentValues()
            initialValues.put(DBContract.BusinessPseudonym.COLUMN_NAME_PSEUDONYM, p)
            initialValues.put(DBContract.BusinessPseudonym.COLUMN_NAME_FOLDER_ID, fid)
            db!!.insert(DBContract.BusinessPseudonym.TABLE_NAME, null, initialValues)
        }

        return fid;
    }

    /**
     * insertReceipt - Insert a Receipt with the folder id
     */
    fun insertReceipt(fid: Long, image: String?, total: Double?): Long {
        val initialValues = ContentValues()
        initialValues.put(DBContract.Receipt.COLUMN_NAME_FOLDER_ID, fid)
        initialValues.put(DBContract.Receipt.COLUMN_NAME_IMAGE, image)
        initialValues.put(DBContract.Receipt.COLUMN_NAME_TOTAL, total)

        return db!!.insert(DBContract.Receipt.TABLE_NAME, null, initialValues);
    }

    /**
     * deleteFolder - deletes a folder, its receipts, and its pseudonyms from the DB by a folder row id
     */
    fun deleteFolder(rowId: Long): Boolean {
        val res = db!!.delete(DBContract.Folder.TABLE_NAME, BaseColumns._ID + "=" + rowId, null) > 0

        if(res) { //A folder was deleted, we can delete each of its pseudonyms
            db!!.delete(
                DBContract.BusinessPseudonym.TABLE_NAME,
                DBContract.BusinessPseudonym.COLUMN_NAME_FOLDER_ID + "=" + rowId,
                null
            )
            db!!.delete(
                DBContract.Receipt.TABLE_NAME,
                DBContract.Receipt.COLUMN_NAME_FOLDER_ID + "=" + rowId,
                null
            )
        }
        return res;
    }

    /**
     * deleteReceipt - deletes a receipts by its ID
     */
    fun deleteReceipt(rowId: Long): Boolean {
        return db!!.delete(DBContract.Receipt.TABLE_NAME, BaseColumns._ID + "=" + rowId, null) > 0
    }

    /**
     *  getAllFolders - returns a cursor to all of the current folders
     */
    fun getAllFolders(): Cursor? {
        return db!!.query(
            DBContract.Folder.TABLE_NAME, arrayOf(
                BaseColumns._ID, DBContract.Folder.COLUMN_NAME_ALIAS
            ), null, null, null, null, null
        )
    }

    /**
     *  getPseudonyms - Gets all pseudonyms for a specified folder id
     */
    fun getPseudonyms(rowId: Long): Cursor? {
        return db!!.rawQuery("SELECT * FROM ${DBContract.BusinessPseudonym.TABLE_NAME} WHERE ${DBContract.BusinessPseudonym.COLUMN_NAME_FOLDER_ID} = ?", Array(1){"$rowId"})
    }

    /**
     *  getReceipts - Gets all receipts for a specified folder id
     */
    fun getReceipts(rowId: Long): Cursor? {
        return db!!.rawQuery("SELECT * FROM ${DBContract.Receipt.TABLE_NAME} WHERE ${DBContract.Receipt.COLUMN_NAME_FOLDER_ID} = ?", Array(1){"$rowId"})
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

}