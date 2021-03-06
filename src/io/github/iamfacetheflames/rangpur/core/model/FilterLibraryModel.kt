package io.github.iamfacetheflames.rangpur.core.model

import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import io.github.iamfacetheflames.rangpur.core.data.Directory

class FilterLibraryModel(private val database: Database) {
    fun getDateList(): List<String> = database.calendar.getDateList()
    fun getYears(): List<String> = database.calendar.getYears()
    fun getMonths(year: String): List<String> = database.calendar.getMonths(year)
    fun getDirectories(): List<Directory> = database.directories.getOnlyRoot()
    fun getDirectories(root: Directory): List<Directory> = database.directories.getFrom(root)
}