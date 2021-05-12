package com.bestway.technologies.todolist.ui.addedittask

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.util.exhaustive
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskDialogFragment: DialogFragment() {
    private val viewModel: AddEditTaskViewModel by viewModels()
    private val args: AddEditTaskDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val taskDialog = layoutInflater.inflate(R.layout.layout_add_edit_task_dialog, null)
        val editTextTaskName = taskDialog.findViewById<TextInputEditText>(R.id.edit_text_task_name)
        val editTextLayout = taskDialog.findViewById<TextInputLayout>(R.id.text_layout_tasks)
        val checkBoxImportant = taskDialog.findViewById<CheckBox>(R.id.check_box_important)
        val textViewDateCreated = taskDialog.findViewById<TextView>(R.id.text_view_date_created)

        textViewDateCreated.isVisible = viewModel.task != null
        textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"
        editTextTaskName.setText(viewModel.taskName)
        checkBoxImportant.isChecked = viewModel.taskImportance
        checkBoxImportant.jumpDrawablesToCurrentState()

        editTextTaskName.addTextChangedListener {
            viewModel.taskName = it.toString()
        }

        checkBoxImportant.setOnCheckedChangeListener{_, isChecked ->
            viewModel.taskImportance = isChecked
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(args.title)
            .setView(taskDialog)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            viewModel.onSaveClick()
        }

        this.lifecycleScope.launchWhenStarted {
            viewModel.addEditEvent.collect { event ->
                when(event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        editTextLayout.error = event.msg
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }

        return alertDialog
    }
}