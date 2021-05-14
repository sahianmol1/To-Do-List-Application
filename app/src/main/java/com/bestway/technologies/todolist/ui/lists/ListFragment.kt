package com.bestway.technologies.todolist.ui.lists

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.data.SortOrder
import com.bestway.technologies.todolist.databinding.FragmentListBinding
import com.bestway.technologies.todolist.util.exhaustive
import com.bestway.technologies.todolist.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list), ListAdapter.OnListItemClickListener {

    private val viewModel: ListViewModel by viewModels()
    lateinit var searchView: SearchView
    lateinit var toBeDeletedListItem: ListItem
    private lateinit var binding: FragmentListBinding
    private lateinit var listAdapter: ListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)

        listAdapter = ListAdapter(this)

        binding.apply {
            recyclerViewLists.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewLists.adapter = listAdapter
            recyclerViewLists.setHasFixedSize(true)

            fabAddList.setOnClickListener {
                viewModel.onAddNewListClick()
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.list.observe(viewLifecycleOwner) {
                when (it.isEmpty()) {
                    true -> {
                        binding.apply {
                            textViewStartAddingList.visibility = View.VISIBLE
                            imageRightArrowList.visibility = View.VISIBLE
                        }
                    }
                    false -> {
                        binding.apply {
                            textViewStartAddingList.visibility = View.GONE
                            imageRightArrowList.visibility = View.GONE
                        }
                        listAdapter.submitList(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listEvent.collect { event ->
                when (event) {
                    is ListViewModel.ListEvent.NavigateToTaskFragmentScreen -> {
                        val action = ListFragmentDirections.actionListFragmentToTaskFragment(
                            event.list,
                            event.list.name
                        )
                        findNavController().navigate(action)
                    }
                    is ListViewModel.ListEvent.ShowDeleteAlertDialog -> {
                        val deleteAlertDialog = AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure? Deleting the list will delete all of its tasks.")
                            .setPositiveButton("DELETE") { _, _ ->
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
                    is ListViewModel.ListEvent.ShareTasksOfList -> {
                        viewModel.getAllTasks(event.list.listId).collect { tasks ->
                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_SEND
                            var allTask = ""
                            for (task in tasks) {
                                allTask += " - ${task.name} \n"
                            }
                            sendIntent.putExtra(Intent.EXTRA_TEXT, allTask)
                            sendIntent.type = "text/plain"
                            startActivity(sendIntent)
                        }
                    }
                }.exhaustive
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_list, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_oldest_first -> {
                viewModel.onSortOrderSelected(SortOrder.BY_OLDEST)
                true
            }

            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_help -> {

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onListItemClick(list: ListItem) {
        viewModel.onListItemClick(list)
    }

    override fun onCreateContextMenuListener(view: View, list: ListItem) {
        toBeDeletedListItem = list
        registerForContextMenu(view)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.menu_long_click_on_list, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_list -> {
                viewModel.onDeleteMenuClick(toBeDeletedListItem)
                true
            }
            R.id.action_share_list -> {
                viewModel.onShareClick(toBeDeletedListItem)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}