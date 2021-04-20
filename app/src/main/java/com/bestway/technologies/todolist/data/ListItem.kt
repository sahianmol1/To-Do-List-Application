package com.bestway.technologies.todolist.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "list_table")
data class ListItem(
        val name: String,
        val created: Long = System.currentTimeMillis(),
        @PrimaryKey (autoGenerate = true) val listId: Int = 0
): Parcelable
