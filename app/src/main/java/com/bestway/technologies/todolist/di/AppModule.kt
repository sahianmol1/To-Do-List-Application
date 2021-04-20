package com.bestway.technologies.todolist.di

import android.content.Context
import androidx.room.Room
import com.bestway.technologies.todolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(
        @ApplicationContext context: Context,
        callbacks: TaskDatabase.Callback
    ) =
        Room.databaseBuilder(context, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callbacks)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(taskDatabase: TaskDatabase) = taskDatabase.taskDao()

    @Provides
    @Singleton
    fun provideListDao(db: TaskDatabase) = db.listDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope