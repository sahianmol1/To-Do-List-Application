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
                listDao.insertList(ListItem("Groceries"))
                listDao.insertList(ListItem("Today's Tasks"))
                listDao.insertList(ListItem("Productivity"))
                listDao.insertList(ListItem("Good Things"))
                listDao.insertList(ListItem("Points to remember"))
                listDao.insertList(ListItem("Baby Names"))
                listDao.insertList(ListItem("Bucket List"))

                taskDao.insert(Task(listId = 1,"Wash the dishes"))
                taskDao.insert(Task(listId = 1,"Do the laundry"))
                taskDao.insert(Task(listId = 1,"Buy groceries", important = true))
                taskDao.insert(Task(listId = 1,"Prepare food", completed = true))
                taskDao.insert(Task(listId = 1,"Call mom"))
                taskDao.insert(Task(listId = 1,"Visit grandma", completed = true))
                taskDao.insert(Task(listId = 2,"Repair my bike"))
                taskDao.insert(Task(listId = 3,"Call Elon Musk"))
            }
        }
    }
}