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

    //BaseColumns provides an _ID field
    object Folder : BaseColumns {
        const val TABLE_NAME = "folder"
        const val COLUMN_NAME_ALIAS = "alias"
    }

    object BusinessPseudonym : BaseColumns {
        const val TABLE_NAME = "business_pseudonym"
        const val COLUMN_NAME_FOLDER_ID = "folder_id"
        const val COLUMN_NAME_PSEUDONYM = "pseudonym"
    }

    object Receipt : BaseColumns {
        const val TABLE_NAME = "receipt"
        const val COLUMN_NAME_FOLDER_ID = "folder_id"
        const val COLUMN_NAME_IMAGE = "image"
        const val COLUMN_NAME_TOTAL = "total"
    }
}