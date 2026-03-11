package com.progreen.recycling.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.Submission
import com.progreen.recycling.databinding.ItemHistoryBinding
import com.progreen.recycling.util.DateFormatter

class HistoryAdapter(
    private val items: List<Submission>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Submission) {
            binding.historyTitle.text = "${item.categoryName} - ${item.weightKg}kg"
            binding.historyDate.text = DateFormatter.format(item.timestamp)
            binding.historyPoints.text = "+${item.pointsEarned} points"
        }
    }
}
