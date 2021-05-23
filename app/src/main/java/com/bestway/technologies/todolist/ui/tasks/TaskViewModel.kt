package com.bestway.technologies.todolist.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.data.PreferencesManager
import com.bestway.technologies.todolist.data.SortOrder
import com.bestway.technologies.todolist.data.Task
import com.bestway.technologies.todolist.repositorry.TodoRepository
import com.bestway.technologies.todolist.ui.ADD_TASK_RESULT_OK
import com.bestway.technologies.todolist.ui.EDIT_TASK_RESULT_OK
import com.bestway.technologies.todolist.ui.SET_REMINDER_RESULT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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

    val listItem = state.get<ListItem>("listItem")

    val listId = listItem?.listId

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
            filterPreferences.hideCompleted,
                listId ?: 1
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

    fun onSetTimerResult(result: Int) = viewModelScope.launch {
        when(result) {
            SET_REMINDER_RESULT -> {
                taskEventChannel.send(TaskEvent.ShowReminderSetMessage("Reminder is set"))
            }
        }
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedClick)
    }

    fun onClockIconClick(listItem: ListItem) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToTimePickerFragment(listItem))
    }

//    suspend fun getTopMostList(listId: Int) =
//        repository.getTopMostList(listId)

    suspend fun getTopmostListItem() = repository.getTopListItem()

    sealed class TaskEvent {
        data class SowUndoDeleteTaskMessage(val task: Task): TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String): TaskEvent()
        object NavigateToAddTaskScreen: TaskEvent()
        object NavigateToDeleteAllCompletedClick: TaskEvent()
        data class NavigateToTimePickerFragment(val listItem: ListItem): TaskEvent()
        data class ShowReminderSetMessage(val msg: String): TaskEvent()
    }
}