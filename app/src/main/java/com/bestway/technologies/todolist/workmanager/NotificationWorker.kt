package com.bestway.technologies.todolist.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bestway.technologies.todolist.R

class NotificationWorker(val context: Context, val params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        val titleInput = inputData.getString("title") ?: ""
        val title = "Reminder: $titleInput"
        val message = "Don't forget to complete this task: $titleInput"
        createNotification(context, title, message)
        return Result.success()
    }

    private fun createNotification(context: Context, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(123, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            "channel_id",
            "channel_name",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "This is for Scheduling Lists"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }
}