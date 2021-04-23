package com.bestway.technologies.todolist.ui.timepicker

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.ui.SET_REMINDER_RESULT
import com.bestway.technologies.todolist.workmanager.NotificationWorker
import java.util.*
import java.util.concurrent.TimeUnit

class TimePickerFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val args: TimePickerFragmentArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val listItem = args.listItem
        if (listItem != null) {
            setReminder(listItem, hourOfDay, minute)
            setFragmentResult(
                "time_picker_request",
                bundleOf("time_picker_result" to SET_REMINDER_RESULT)
            )
            findNavController().popBackStack()
        }
    }

    private fun setReminder(listItem: ListItem, hour: Int, minute: Int) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(calculateDelay(hour, minute), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("title" to listItem.name))
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            listItem.listId.toString(),
            ExistingWorkPolicy.APPEND,
            workRequest
        )
    }

    private fun calculateDelay(hour: Int, min: Int): Long {
        val customCalendar = Calendar.getInstance()
        val currentYear = customCalendar.get(Calendar.YEAR)
        val currentMonth = customCalendar.get(Calendar.MONTH)
        val date = customCalendar.get(Calendar.DATE)
        customCalendar.set(currentYear, currentMonth, date, hour, min, 0)
        val customTime = customCalendar.timeInMillis
        return customTime - System.currentTimeMillis()
    }
}