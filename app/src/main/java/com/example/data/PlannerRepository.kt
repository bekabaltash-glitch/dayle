package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val dao: PlannerDao) {
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = dao.getTasksForDate(date)

    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getStarredTasks(): Flow<List<TaskEntity>> = dao.getStarredTasks()

    suspend fun addTask(task: TaskEntity): Long = dao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)

    suspend fun deleteTask(taskId: Long) = dao.deleteTaskById(taskId)

    fun getDailyNote(date: String): Flow<DailyNoteEntity?> = dao.getDailyNote(date)

    suspend fun saveDailyNote(note: DailyNoteEntity) = dao.insertOrUpdateDailyNote(note)
}
