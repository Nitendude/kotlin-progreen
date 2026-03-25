package com.progreen.recycling.ui.company

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.CompanyRedemptionRecord
import com.progreen.recycling.databinding.ItemCompanyRedemptionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompanyRedemptionAdapter(
    private var items: List<CompanyRedemptionRecord>
) : RecyclerView.Adapter<CompanyRedemptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompanyRedemptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<CompanyRedemptionRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemCompanyRedemptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CompanyRedemptionRecord) {
            binding.redemptionUser.text = "${item.userName} (${item.userEmail})"
            binding.redemptionReward.text = item.rewardTitle
            binding.redemptionStatus.text = "${item.status} | ${item.claimToken}"
            binding.redemptionTime.text = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(item.timestamp))
        }
    }
}
