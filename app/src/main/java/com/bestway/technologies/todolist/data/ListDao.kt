package com.bestway.technologies.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    fun getAllListItems(searchQuery: String, sortOrder: SortOrder): Flow<List<ListItem>> =
            when(sortOrder) {
                SortOrder.BY_DATE -> { getTasksSortedByDate(searchQuery) }
                SortOrder.BY_NAME -> { getTasksSortedByName(searchQuery) }
                SortOrder.BY_OLDEST -> {getOldestFirst(searchQuery)}
            }

    @Query("SELECT * FROM list_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name")
    fun getTasksSortedByName(searchQuery: String): Flow<List<ListItem>>

    @Query("SELECT * FROM list_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY created DESC")
    fun getTasksSortedByDate(searchQuery: String): Flow<List<ListItem>>

    @Query("SELECT * FROM list_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY created")
    fun getOldestFirst(searchQuery: String): Flow<List<ListItem>>

    @Insert
    suspend fun insertList(list: ListItem): Long

    @Update
    suspend fun update(list: ListItem)

    @Delete
    suspend fun delete(list: ListItem)

    @Query("SELECT * FROM list_table ORDER BY created DESC LIMIT 1")
    suspend fun getTopListItem(): ListItem

    @Query("SELECT * FROM list_table WHERE listId = :listId")
    suspend fun getListItem(listId: Int): ListItem
}