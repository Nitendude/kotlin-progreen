package com.progreen.recyclingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recyclingapp.databinding.ItemRewardBinding
import com.progreen.recyclingapp.model.Reward

class RewardAdapter(
    private val onRedeemClick: (Reward) -> Unit
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    private val items = mutableListOf<Reward>()

    fun submitList(newItems: List<Reward>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(items[position], onRedeemClick)
    }

    override fun getItemCount(): Int = items.size

    class RewardViewHolder(private val binding: ItemRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Reward, onRedeemClick: (Reward) -> Unit) {
            binding.rewardName.text = item.name
            binding.rewardDescription.text = item.description
            binding.rewardCost.text = "${item.cost} points"
            binding.redeemButton.setOnClickListener { onRedeemClick(item) }
        }
    }
}
