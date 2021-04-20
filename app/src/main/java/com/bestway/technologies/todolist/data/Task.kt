package com.bestway.technologies.todolist.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Parcelize
@Entity(tableName = "task_table", foreignKeys = arrayOf(ForeignKey(entity = ListItem::class,
        parentColumns = arrayOf("listId"),
        childColumns = arrayOf("listId"),
        onDelete = ForeignKey.CASCADE)))
data class Task(
        @ColumnInfo(index = true)
        val listId: Int,
        val name: String,
        val important: Boolean = false,
        val completed: Boolean = false,
        val created: Long = System.currentTimeMillis(),
        @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}
