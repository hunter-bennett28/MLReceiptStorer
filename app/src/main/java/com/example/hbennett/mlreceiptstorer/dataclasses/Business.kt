package com.example.hbennett.mlreceiptstorer.dataclasses

/**
 * Business.kt
 * Connor Black, Hunter Bennett
 *
 * Data class representing a business name connected to a folder
 */

data class Business(val id: Long?, val folderId: Long, val name: String)