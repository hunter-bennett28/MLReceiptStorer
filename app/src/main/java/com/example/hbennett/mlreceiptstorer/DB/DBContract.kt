package com.example.hbennett.mlreceiptstorer.DB

import android.provider.BaseColumns


/** DBContract.kt
 * Connor Black, Hunter Bennett
 *
 * Implements a DB Contract that will be fulfilled by sqlite based on the documentation from:
 * https://developer.android.com/training/data-storage/sqlite#kotlin
 */

object DBContract {
    //Tables are grouped together in objects
    const val DB_NAME = "receiptStorer";
    const val DB_VERSION = 1;

    //BaseColumns provides an _ID field
    object Folder : BaseColumns {
        const val TABLE_NAME = "folder"
        const val COLUMN_NAME_ALIAS = "alias"
    }

    object Business : BaseColumns {
        const val TABLE_NAME = "business"
        const val COLUMN_NAME_FOLDER_ID = "folderId"
        const val COLUMN_NAME_NAME = "name"
    }

    object Receipt : BaseColumns {
        const val TABLE_NAME = "receipt"
        const val COLUMN_NAME_FOLDER_ID = "folderId"
        const val COLUMN_NAME_IMAGE = "image"
        const val COLUMN_NAME_TOTAL = "total"
    }
}