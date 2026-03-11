package com.progreen.recycleapp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycleapp.databinding.ItemHistoryBinding
import com.progreen.recycleapp.model.HistoryItem
import com.progreen.recycleapp.utils.toReadableDate

class HistoryAdapter(
    private val items: List<HistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) = with(binding) {
            tvTitle.text = item.title
            tvMeta.text = "${item.meta} - ${item.timestamp.toReadableDate()}"

            val prefix = if (item.pointsDelta >= 0) "+" else ""
            tvDelta.text = "$prefix${item.pointsDelta} pts"
            tvDelta.setTextColor(if (item.pointsDelta >= 0) Color.parseColor("#1F7A4A") else Color.parseColor("#B23A48"))
        }
    }
}
