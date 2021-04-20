package com.bestway.technologies.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY important DESC")
    fun getTasks(searchQuery: String, sortOrder: SortOrder, hideCompleted: Boolean, listId: Int): Flow<List<Task>> =
            when(sortOrder) {
                SortOrder.BY_DATE -> { getTasksSortedByDate(searchQuery, hideCompleted, listId) }
                SortOrder.BY_NAME -> { getTasksSortedByName(searchQuery, hideCompleted, listId) }
            }

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' AND listId = :listId ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean, listId: Int): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' AND listId = :listId ORDER BY important DESC, created DESC")
    fun getTasksSortedByDate(searchQuery: String, hideCompleted: Boolean, listId: Int): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteAllCompleted()
}