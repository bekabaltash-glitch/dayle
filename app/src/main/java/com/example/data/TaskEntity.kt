package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val label: String) {
    HIGH("Жоғары"),
    MEDIUM("Орташа"),
    LOW("Төмен")
}

enum class TaskCategory(val displayName: String, val iconName: String) {
    WORK("Жұмыс", "work"),
    PERSONAL("Жеке", "person"),
    STUDY("Оқу", "school"),
    HEALTH("Денсаулық", "fitness_center"),
    FINANCE("Қаржы", "payments"),
    OTHER("Басқа", "bookmark")
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // Format: YYYY-MM-DD
    val time: String = "", // e.g., "09:30"
    val category: String = TaskCategory.PERSONAL.name,
    val priority: String = TaskPriority.MEDIUM.name,
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
