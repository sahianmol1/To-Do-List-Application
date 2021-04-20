package com.bestway.technologies.todolist.ui.lists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bestway.technologies.todolist.data.ListItem
import com.bestway.technologies.todolist.databinding.ItemListBinding

class ListAdapter(private val listener: OnListItemClickListener) : androidx.recyclerview.widget.ListAdapter<ListItem, ListAdapter.ListViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }


    inner class ListViewHolder(private val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val listItem = getItem(position)
                        listener.onListItemClick(listItem)
                    }
                }
            }
        }

        fun bind(list: ListItem) {
            binding.apply {
                textViewListName.text = list.name
            }
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
                oldItem.listId == newItem.listId

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
                oldItem == newItem
    }

    interface OnListItemClickListener {
        fun onListItemClick(list: ListItem)
    }

}