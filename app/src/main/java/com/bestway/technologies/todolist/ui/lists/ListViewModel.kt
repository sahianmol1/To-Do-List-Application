package com.bestway.technologies.todolist.ui.lists

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.repositorry.TodoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ListViewModel @ViewModelInject constructor(private val repository: TodoRepository): ViewModel() {

    fun getAllListItems() = repository.getAllListItems().asLiveData()

    private val listEventChannel = Channel<ListEvent>()
    val listEvent = listEventChannel.receiveAsFlow()

    fun onListItemClick(list: ListItem) = viewModelScope.launch {
        listEventChannel.send(ListEvent.NavigateToTaskFragmentScreen(list))
    }

    sealed class ListEvent {
        data class NavigateToTaskFragmentScreen(val list: ListItem): ListEvent()
        data class ShowDeleteAlertDialog(val list: ListItem): ListEvent()
        object OpenAddListItemDialog: ListEvent()
    }

    fun onDeleteButtonClick(list: ListItem) = viewModelScope.launch {
        repository.deleteList(list)
    }

    fun onAddNewListClick() = viewModelScope.launch {
        listEventChannel.send(ListEvent.OpenAddListItemDialog)
    }

    fun onLongClickListener(list: ListItem) = viewModelScope.launch {
        listEventChannel.send(ListEvent.ShowDeleteAlertDialog(list))
    }
}