package com.example.hbennett.mlreceiptstorer.dataclasses

/**
 * Receipt.kt
 * Connor Black, Hunter Bennett
 *
 * Data class representing a receipt
 */

data class Receipt(val id: Long?, val folderId: Long, val image: String, val total: Double, val date: String)
