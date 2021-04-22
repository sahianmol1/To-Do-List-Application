package com.bestway.technologies.todolist.repositorry

import com.bestway.technologies.todolist.data.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(private val taskDao: TaskDao, private val listDao: ListDao) {

    fun getAllTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
        listId: Int
    ): Flow<List<Task>> =
        taskDao.getTasks(searchQuery, sortOrder, hideCompleted, listId)

    suspend fun updateTask(task: Task, isChecked: Boolean) =
        taskDao.update(task.copy(completed = isChecked))

    suspend fun deleteTask(task: Task) = taskDao.delete(task)

    suspend fun insertTask(task: Task) = taskDao.insert(task)

    suspend fun updateEditTask(task: Task) = taskDao.update(task)

    suspend fun deleteAllCompleted() = taskDao.deleteAllCompleted()

    fun getAllListItems(searchQuery: String, sortOrder: SortOrder) = listDao.getAllListItems(searchQuery, sortOrder)

    suspend fun deleteList(list: ListItem) = listDao.delete(list)

    suspend fun insertList(list: ListItem) = listDao.insertList(list)

    suspend fun getTopListItem() = listDao.getTopListItem()
}