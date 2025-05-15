package com.cyberguard.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cyberguard.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val historyList: List<HistoryItem>,
    private val onDeleteClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem, position: Int) {
            binding.numText.text = (position + 1).toString() // auto incrementing rows numbers
            binding.toolName.text = item.tool
            binding.inputText.text = item.input
            binding.resultText.text = item.result
            binding.deleteButton.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyList[position], position)
    }

    override fun getItemCount(): Int = historyList.size
}
