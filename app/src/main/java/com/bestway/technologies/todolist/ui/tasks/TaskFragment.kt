package com.bestway.technologies.todolist.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bestway.technologies.todolist.R
import com.bestway.technologies.todolist.data.SortOrder
import com.bestway.technologies.todolist.data.Task
import com.bestway.technologies.todolist.databinding.FragmentTasksBinding
import com.bestway.technologies.todolist.util.exhaustive
import com.bestway.technologies.todolist.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    private val viewModel: TaskViewModel by viewModels()
    lateinit var searchView: SearchView
    private val args: TaskFragmentArgs by navArgs()
    private lateinit var listOfTasks: List<Task>
    var listId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)

        listId = args.listItem.listId

        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = taskAdapter
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        setFragmentResultListener("time_picker_request") {_, bundle ->
            val result = bundle.getInt("time_picker_result")
            viewModel.onSetTimerResult(result)
        }

        lifecycleScope.launch {
            viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
                listOfTasks = tasks
                when (tasks.isEmpty()) {
                    true -> {
                        binding.apply {
                            textViewStartAddingTasks.visibility = View.VISIBLE
                            imageRightArrow.visibility = View.VISIBLE
                        }
                    }
                    false -> {
                        binding.apply {
                            textViewStartAddingTasks.visibility = View.GONE
                            imageRightArrow.visibility = View.GONE
                        }
                        taskAdapter.submitList(tasks)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TaskViewModel.TaskEvent.SowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG).setAction("UNDO") {
                            viewModel.onUndoDeleteClick(event.task)
                        }.show()
                    }

                    is TaskViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskDialogFragment(null, "Add Task", listId = listId)
                        findNavController().navigate(action)
                    }

                    is TaskViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskDialogFragment(event.task, "Edit Task", listId = listId)
                        findNavController().navigate(action)
                    }

                    is TaskViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {

                    }

                    is TaskViewModel.TaskEvent.NavigateToDeleteAllCompletedClick -> {
                        val action = TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.NavigateToTimePickerFragment -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToTimePickerFragment(event.listItem)
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.ShowReminderSetMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }


        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

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

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                    viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_sort_by_oldest_first -> {
                viewModel.onSortOrderSelected(SortOrder.BY_OLDEST)
                true
            }

            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }

            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }

            R.id.action_alarm -> {
                viewModel.onClockIconClick(args.listItem)
                true
            }

            R.id.action_share -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    var allTask = ""
                    for (task in listOfTasks) {
                        allTask += " - ${task.name} \n"
                    }
                    sendIntent.putExtra(Intent.EXTRA_TEXT, allTask)
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onCheckBoxClicked(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}