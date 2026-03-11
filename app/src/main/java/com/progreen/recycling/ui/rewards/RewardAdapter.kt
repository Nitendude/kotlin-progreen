package com.progreen.recycling.ui.rewards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.databinding.ItemRewardBinding

class RewardAdapter(
    private var pointsBalance: Int,
    private val items: List<RewardItem>,
    private val onRedeemClick: (RewardItem) -> Unit
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(items[position], pointsBalance, onRedeemClick)
    }

    override fun getItemCount(): Int = items.size

    fun updatePoints(newPoints: Int) {
        pointsBalance = newPoints
        notifyDataSetChanged()
    }

    class RewardViewHolder(
        private val binding: ItemRewardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RewardItem, pointsBalance: Int, onRedeemClick: (RewardItem) -> Unit) {
            binding.rewardName.text = item.title
            binding.rewardDescription.text = "Redeem eco-friendly perks"
            binding.rewardCost.text = "${item.costPoints} pts"
            binding.redeemButton.isEnabled = pointsBalance >= item.costPoints
            binding.redeemButton.alpha = if (binding.redeemButton.isEnabled) 1f else 0.5f
            binding.redeemButton.setOnClickListener { onRedeemClick(item) }
        }
    }
}
