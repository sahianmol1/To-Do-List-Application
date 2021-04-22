package com.bestway.technologies.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bestway.technologies.todolist.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class, ListItem::class], version = 1)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun listDao(): ListDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val taskDao = database.get().taskDao()
            val listDao = database.get().listDao()

            applicationScope.launch {

            }
        }
    }
}