package com.bestway.technologies.todolist.ui.addlistitem

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.data.ListItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddListItemDialogFragment : DialogFragment() {
    private val viewModel: AddListItemViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customDialogView = layoutInflater.inflate(R.layout.layout_list_add_dialog, null)
        val editText = customDialogView.findViewById<TextInputEditText>(R.id.text_input_add_list)
        val editTextLabel = customDialogView.findViewById<TextInputLayout>(R.id.text_input_label)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New List")
            .setView(customDialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val listName: String = editText.text.toString()
            viewModel.onDialogAddButtonClick(listName)
        }

        this.lifecycleScope.launchWhenStarted {
            viewModel.addListItemEvent.collect { event ->
                when(event) {
                    is AddListItemViewModel.AddListItemEvent.AddListItemIntoDB -> {
                        viewModel.addNewList(ListItem(name = event.listName))

                        viewModel.listId.observe(this@AddListItemDialogFragment) {
                            if (it != null) {
                                lifecycleScope.launch {
                                    val listItem = viewModel.getListItem(it.toInt())
                                    Log.i("AddListFrag", listItem.name)
                                    val action = AddListItemDialogFragmentDirections.actionAddListItemDialogFragmentToTaskFragment(listItem = listItem, title = listItem.name)
                                    findNavController().navigate(action)
                                }
                            }
                        }
                    }
                    is AddListItemViewModel.AddListItemEvent.ShowInvalidInputMessage -> {
                        editTextLabel.error = event.message
                    }
                }
            }
        }

        return alertDialog

    }

}