package com.progreen.recycling.ui.lgu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.databinding.ItemLguManagedRewardBinding

class LguManagedRewardAdapter(
    private var items: List<RewardItem>
) : RecyclerView.Adapter<LguManagedRewardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLguManagedRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<RewardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemLguManagedRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RewardItem) {
            binding.lguRewardTitle.text = item.title
            binding.lguRewardCost.text = "${item.costPoints} pts"
        }
    }
}
