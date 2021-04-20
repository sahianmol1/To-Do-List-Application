package com.bestway.technologies.todolist.ui.addlistitem

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.repositorry.TodoRepository
import kotlinx.coroutines.launch

class AddListItemViewModel @ViewModelInject constructor(private val repository: TodoRepository): ViewModel() {

    fun addNewList(list: ListItem) = viewModelScope.launch {
        repository.insertList(list)
    }

    suspend fun getTopListItem() = repository.getTopListItem()
}