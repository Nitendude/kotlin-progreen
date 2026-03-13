package com.progreen.recycling.ui.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.progreen.recycling.R
import com.progreen.recycling.data.model.RewardItem
import com.progreen.recycling.databinding.ItemRewardBinding

class RewardAdapter(
    private var pointsBalance: Int,
    private var items: List<RewardItem>,
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

    fun updateData(newPoints: Int, newItems: List<RewardItem>) {
        pointsBalance = newPoints
        items = newItems
        notifyDataSetChanged()
    }

    class RewardViewHolder(
        private val binding: ItemRewardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RewardItem, pointsBalance: Int, onRedeemClick: (RewardItem) -> Unit) {
            binding.rewardName.text = item.title
            val provider = item.provider?.let { "Claim at: $it" } ?: "Redeem eco-friendly perks"
            val details = item.description?.takeIf { it.isNotBlank() } ?: "No description provided"
            binding.rewardDescription.text = "$provider\n$details"
            binding.rewardCost.text = "${item.costPoints} pts"

            if (item.redeemCode.isNullOrBlank()) {
                binding.rewardCode.visibility = View.GONE
            } else {
                binding.rewardCode.visibility = View.VISIBLE
                binding.rewardCode.text = "Code available after redeem"
            }

            binding.rewardImage.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_reward)
                error(R.drawable.ic_reward)
            }

            binding.redeemButton.isEnabled = pointsBalance >= item.costPoints
            binding.redeemButton.alpha = if (binding.redeemButton.isEnabled) 1f else 0.5f
            binding.redeemButton.setOnClickListener { onRedeemClick(item) }
        }
    }
}
