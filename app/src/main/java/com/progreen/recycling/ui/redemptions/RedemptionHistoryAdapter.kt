package com.progreen.recycling.ui.redemptions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.RedemptionHistoryItem
import com.progreen.recycling.databinding.ItemRedemptionHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RedemptionHistoryAdapter(
    private var items: List<RedemptionHistoryItem>
) : RecyclerView.Adapter<RedemptionHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRedemptionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val binding: ItemRedemptionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RedemptionHistoryItem) {
            binding.rewardTitle.text = item.rewardTitle
            binding.rewardMeta.text = buildString {
                append(item.providerName ?: "CycleMint")
                append(" | ")
                append(item.status)
                append(" | ")
                append(item.pointsSpent)
                append(" pts")
            }
            binding.rewardCodes.text = buildString {
                append("Claim token: ")
                append(item.claimToken ?: "-")
                if (!item.redeemCode.isNullOrBlank()) {
                    append("\nRedeem code: ")
                    append(item.redeemCode)
                }
            }
            binding.rewardTime.text = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(item.timestamp))
        }
    }
}
