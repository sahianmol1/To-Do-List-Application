package com.bestway.technologies.todolist.ui.lists

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.databinding.FragmentListBinding
import com.bestway.technologies.todolist.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment: Fragment(R.layout.fragment_list), ListAdapter.OnListItemClickListener {

    private val viewModel: ListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentListBinding.bind(view)

        val listAdapter = ListAdapter(this)

        binding.apply {
            recyclerViewLists.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewLists.adapter = listAdapter
            recyclerViewLists.setHasFixedSize(true)

            fabAddList.setOnClickListener {
                viewModel.onAddNewListClick()
            }
        }

        lifecycleScope.launch {
            viewModel.getAllListItems().observe(viewLifecycleOwner) {
                listAdapter.submitList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listEvent.collect { event ->
                when(event) {
                    is ListViewModel.ListEvent.NavigateToTaskFragmentScreen -> {
                        val action = ListFragmentDirections.actionListFragmentToTaskFragment(event.list, event.list.name)
                        findNavController().navigate(action)
                    }
                    is ListViewModel.ListEvent.ShowDeleteAlertDialog -> {
                        val deleteAlertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Confirm Delete")
                                .setMessage("Are you sure? Deleting the list will delete all of its tasks.")
                                .setPositiveButton("DELETE") {_,_ ->
                                    viewModel.onDeleteButtonClick(event.list)
                                }
                                .setNegativeButton("CANCEL", null)
                                .create()
                        deleteAlertDialog.show()
                    }
                    is ListViewModel.ListEvent.OpenAddListItemDialog -> {
                        val action = ListFragmentDirections.actionGlobalAddListItemDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    override fun onListItemClick(list: ListItem) {
        viewModel.onListItemClick(list)
    }

    override fun onLongClickListener(list: ListItem) {
        viewModel.onLongClickListener(list)
    }
}