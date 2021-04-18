package com.bestway.technologies.todolist.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bestway.technologies.todolist.data.PreferencesManager
import com.bestway.technologies.todolist.data.SortOrder
import com.bestway.technologies.todolist.data.Task
import com.bestway.technologies.todolist.repositorry.TodoRepository
import com.bestway.technologies.todolist.ui.ADD_TASK_RESULT_OK
import com.bestway.technologies.todolist.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class TaskViewModel @ViewModelInject constructor(
    private val repository: TodoRepository,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle

) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")
    val preferencesFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { searchQuery, filterPreferences ->
        Pair(searchQuery, filterPreferences)
    }.flatMapLatest { (searchQuery, filterPreferences) ->
        repository.getAllTasks(
            searchQuery,
            filterPreferences.sortOrder,
            filterPreferences.hideCompleted
        )
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onCheckBoxClicked(task: Task, isChecked: Boolean) = viewModelScope.launch {
        repository.updateTask(task, isChecked)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        taskEventChannel.send(TaskEvent.SowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onAddEditResult(result:Int)  = viewModelScope.launch {
        when(result) {
            ADD_TASK_RESULT_OK -> {
                taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage("Task added"))
            }

            EDIT_TASK_RESULT_OK -> {
                taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage("Task updated"))
            }
        }
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedClick)
    }

    sealed class TaskEvent {
        data class SowUndoDeleteTaskMessage(val task: Task): TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String): TaskEvent()
        object NavigateToAddTaskScreen: TaskEvent()
        object NavigateToDeleteAllCompletedClick: TaskEvent()
    }
}