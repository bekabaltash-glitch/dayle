package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_notes")
data class DailyNoteEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val noteText: String = "",
    val mainGoal: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
