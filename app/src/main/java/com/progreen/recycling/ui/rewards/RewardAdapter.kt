package com.progreen.recycling.ui.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.util.Base64
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
            binding.rewardDescription.text = "$provider\nType: ${item.rewardType}\n$details"
            binding.rewardCost.text = "${item.costPoints} pts"

            if (item.redeemCode.isNullOrBlank()) {
                binding.rewardCode.visibility = View.GONE
            } else {
                binding.rewardCode.visibility = View.VISIBLE
                binding.rewardCode.text = "Code available after redeem"
            }

            val imageBase64 = item.imageBase64
            if (!imageBase64.isNullOrBlank()) {
                val bitmap = decodeBase64(imageBase64)
                if (bitmap != null) {
                    binding.rewardImage.setImageBitmap(bitmap)
                } else {
                    binding.rewardImage.setImageResource(R.drawable.ic_reward)
                }
            } else {
                binding.rewardImage.load(item.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_reward)
                    error(R.drawable.ic_reward)
                }
            }

            binding.redeemButton.isEnabled = pointsBalance >= item.costPoints
            binding.redeemButton.alpha = if (binding.redeemButton.isEnabled) 1f else 0.5f
            binding.redeemButton.setOnClickListener { onRedeemClick(item) }
        }

        private fun decodeBase64(raw: String) = try {
            val bytes = Base64.decode(raw, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}
