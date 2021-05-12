package com.bestway.technologies.todolist.ui.addlistitem

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.repositorry.TodoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddListItemViewModel @ViewModelInject constructor(private val repository: TodoRepository): ViewModel() {

    val listId = MutableLiveData<Long>()
    fun addNewList(list: ListItem) = viewModelScope.launch {
        listId.value = repository.insertList(list)
    }

    private val addListItemChannel = Channel<AddListItemEvent>()
    val addListItemEvent = addListItemChannel.receiveAsFlow()

    suspend fun getListItem(listId: Int) = repository.getListItem(listId)

    fun onDialogAddButtonClick(listName: String) {
        when(listName.isBlank()) {
            true -> {
                showInvalidInputMessage()
            }
            false -> {
                addListItemAndNavigate(listName)
            }
        }
    }

    private fun showInvalidInputMessage() = viewModelScope.launch {
        addListItemChannel.send(AddListItemEvent.ShowInvalidInputMessage("Name cannot be empty"))
    }

    private fun addListItemAndNavigate(listName: String) = viewModelScope.launch {
        addListItemChannel.send(AddListItemEvent.AddListItemIntoDB(listName))
    }

    sealed class AddListItemEvent {
        data class AddListItemIntoDB(val listName: String): AddListItemEvent()
        data class ShowInvalidInputMessage(val message: String): AddListItemEvent()
    }
}