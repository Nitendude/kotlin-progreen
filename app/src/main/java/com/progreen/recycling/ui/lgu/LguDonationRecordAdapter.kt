package com.progreen.recycling.ui.lgu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.LguDonationRecord
import com.progreen.recycling.databinding.ItemLguDonationRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LguDonationRecordAdapter(
    private var items: List<LguDonationRecord>
) : RecyclerView.Adapter<LguDonationRecordAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLguDonationRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<LguDonationRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemLguDonationRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LguDonationRecord) {
            binding.recordUser.text = "${item.userName} (${item.userEmail})"
            binding.recordMeta.text = "${item.categoryName} - ${String.format(Locale.getDefault(), "%.2f", item.weightKg)} kg"
            binding.recordPoints.text = "+${item.pointsEarned} pts"
            binding.recordTime.text = formatTimestamp(item.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            if (timestamp <= 0L) return "-"
            return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
