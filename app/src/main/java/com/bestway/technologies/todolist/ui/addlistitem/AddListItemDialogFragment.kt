package com.bestway.technologies.todolist.ui.addlistitem

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.data.ListItem
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddListItemDialogFragment: DialogFragment() {
    private val viewModel: AddListItemViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog  {


        val customDialogView = layoutInflater.inflate(R.layout.layout_list_add_dialog, null)
        val editText = customDialogView.findViewById<TextInputEditText>(R.id.text_input_add_list)

        return AlertDialog.Builder(requireContext())
                 .setTitle("Add New List")
                 .setView(customDialogView)
                 .setPositiveButton("Add") { _, _ ->
                     val listName: String = editText.text.toString()
                     viewModel.addNewList(ListItem(name = listName))
                     lifecycleScope.launch {
                         val listItem = viewModel.getTopListItem()
                         val action = AddListItemDialogFragmentDirections.actionAddListItemDialogFragmentToTaskFragment(listItem = listItem, title = listItem.name)
                         findNavController().navigate(action)
                     }
                 }
                 .setNegativeButton("Cancel", null)
                 .create()
    }

}