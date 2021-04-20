package com.bestway.technologies.todolist.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    @Query("SELECT * FROM list_table ORDER BY created DESC")
    fun getAllListItems(): Flow<List<ListItem>>

    @Insert
    suspend fun insertList(list: ListItem)

    @Update
    suspend fun update(list: ListItem)

    @Delete
    suspend fun delete(list: ListItem)

    @Query("SELECT * FROM list_table ORDER BY created DESC LIMIT 1")
    suspend fun getTopListItem(): ListItem
}