package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyNoteEntity
import com.example.data.PlannerRepository
import com.example.data.TaskCategory
import com.example.data.TaskEntity
import com.example.data.TaskPriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlannerRepository

    val todayDateString: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _filterStatus = MutableStateFlow("ALL") // "ALL", "PENDING", "COMPLETED"
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL") // "ALL" or TaskCategory name
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    private val _showDailyNoteDialog = MutableStateFlow(false)
    val showDailyNoteDialog: StateFlow<Boolean> = _showDailyNoteDialog.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PlannerRepository(db.plannerDao())

        // Seed initial sample data if app is opened for the first time
        viewModelScope.launch {
            repository.getTasksForDate(todayDateString).collect { list ->
                if (list.isEmpty()) {
                    seedSampleTasks()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<List<TaskEntity>> = combine(
        _selectedDate,
        _filterStatus,
        _selectedCategory,
        _searchQuery
    ) { date, filter, category, query ->
        Quadruple(date, filter, category, query)
    }.flatMapLatest { (date, filter, category, query) ->
        repository.getTasksForDate(date).combine(MutableStateFlow(Unit)) { taskList, _ ->
            taskList.filter { task ->
                val matchesFilter = when (filter) {
                    "PENDING" -> !task.isCompleted
                    "COMPLETED" -> task.isCompleted
                    else -> true
                }
                val matchesCategory = if (category == "ALL") true else task.category == category
                val matchesQuery = if (query.isBlank()) true else {
                    task.title.contains(query, ignoreCase = true) ||
                            task.description.contains(query, ignoreCase = true)
                }
                matchesFilter && matchesCategory && matchesQuery
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDailyNote: StateFlow<DailyNoteEntity?> = _selectedDate.flatMapLatest { date ->
        repository.getDailyNote(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowAddTaskDialog(show: Boolean) {
        _showAddTaskDialog.value = show
    }

    fun setShowDailyNoteDialog(show: Boolean) {
        _showDailyNoteDialog.value = show
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun toggleTaskStar(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isStarred = !task.isStarred))
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addNewTask(
        title: String,
        description: String,
        date: String,
        time: String,
        category: String,
        priority: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                date = date,
                time = time.trim(),
                category = category,
                priority = priority,
                isCompleted = false,
                isStarred = false
            )
            repository.addTask(newTask)
        }
    }

    fun saveDailyNote(date: String, noteText: String, mainGoal: String) {
        viewModelScope.launch {
            repository.saveDailyNote(
                DailyNoteEntity(
                    date = date,
                    noteText = noteText.trim(),
                    mainGoal = mainGoal.trim()
                )
            )
        }
    }

    private suspend fun seedSampleTasks() {
        val sample1 = TaskEntity(
            title = "Таңғы жаттығу мен медитация",
            description = "15 минут жеңіл дене шынықтыру және тыныс алу жаттығулары",
            date = todayDateString,
            time = "07:30",
            category = TaskCategory.HEALTH.name,
            priority = TaskPriority.HIGH.name,
            isCompleted = true
        )
        val sample2 = TaskEntity(
            title = "Бүгінгі басты мақсаттарды белгілеу",
            description = "Күнделікті жоспарды қарап шығып, маңызды міндеттерді реттеу",
            date = todayDateString,
            time = "08:30",
            category = TaskCategory.PERSONAL.name,
            priority = TaskPriority.HIGH.name,
            isCompleted = false,
            isStarred = true
        )
        val sample3 = TaskEntity(
            title = "Жұмыс немесе оқу тапсырмаларын орындау",
            description = "Басымдылығы жоғары жобаларды аяқтау",
            date = todayDateString,
            time = "10:00",
            category = TaskCategory.WORK.name,
            priority = TaskPriority.MEDIUM.name,
            isCompleted = false
        )
        val sample4 = TaskEntity(
            title = "Кітап оқу және күнделікті қорытынды жасау",
            description = "Пайдалы ақпарат оқып, күнді сараптау",
            date = todayDateString,
            time = "20:00",
            category = TaskCategory.STUDY.name,
            priority = TaskPriority.LOW.name,
            isCompleted = false
        )

        repository.addTask(sample1)
        repository.addTask(sample2)
        repository.addTask(sample3)
        repository.addTask(sample4)

        repository.saveDailyNote(
            DailyNoteEntity(
                date = todayDateString,
                noteText = "Бүгін өнімді күн болсын! Әрбір тапсырманы ықыласпен орындаймын.",
                mainGoal = "Барлық жоспарланған тапсырмалардың кем дегенде 80%-ын орындау"
            )
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
