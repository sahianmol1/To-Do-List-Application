package com.bestway.technologies.todolist.ui.deleteAllCompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.bestway.technologies.todolist.di.ApplicationScope
import com.bestway.technologies.todolist.repositorry.TodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val repository: TodoRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
): ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        repository.deleteAllCompleted()
    }
}