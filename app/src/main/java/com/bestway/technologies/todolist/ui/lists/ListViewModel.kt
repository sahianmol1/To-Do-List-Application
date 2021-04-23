package com.bestway.technologies.todolist.ui.lists

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.data.ListPreferencesManager
import com.bestway.technologies.todolist.data.SortOrder
import com.bestway.technologies.todolist.repositorry.TodoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ListViewModel @ViewModelInject constructor(
        private val repository: TodoRepository,
        private val preferencesManager: ListPreferencesManager,
        @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")
    val preferencesFlow = preferencesManager.preferencesFlow

    private val listEventChannel = Channel<ListEvent>()
    val listEvent = listEventChannel.receiveAsFlow()

    private val listFlow = combine(
            searchQuery.asFlow(),
            preferencesFlow
    ) {searchQuery, sortOrder ->
        Pair(searchQuery, sortOrder)
    }.flatMapLatest { (searchQuery, sortOrder) ->
        repository.getAllListItems(searchQuery, sortOrder)
    }

    val list = listFlow.asLiveData()

    fun onListItemClick(list: ListItem) = viewModelScope.launch {
        listEventChannel.send(ListEvent.NavigateToTaskFragmentScreen(list))
    }

    sealed class ListEvent {
        data class NavigateToTaskFragmentScreen(val list: ListItem) : ListEvent()
        data class ShowDeleteAlertDialog(val list: ListItem) : ListEvent()
        object OpenAddListItemDialog : ListEvent()
    }

    fun onDeleteButtonClick(list: ListItem) = viewModelScope.launch {
        repository.deleteList(list)
    }

    fun onAddNewListClick() = viewModelScope.launch {
        listEventChannel.send(ListEvent.OpenAddListItemDialog)
    }

    fun onDeleteMenuClick(list: ListItem) = viewModelScope.launch {
        listEventChannel.send(ListEvent.ShowDeleteAlertDialog(list))
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }
}