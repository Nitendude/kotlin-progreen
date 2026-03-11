package com.progreen.recycleapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycleapp.databinding.ItemRewardBinding
import com.progreen.recycleapp.model.RewardItem

class RewardAdapter(
    private var currentPoints: Int,
    private val items: List<RewardItem>,
    private val onRedeem: (RewardItem) -> Unit
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val binding = ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RewardViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updatePoints(points: Int) {
        currentPoints = points
        notifyDataSetChanged()
    }

    inner class RewardViewHolder(private val binding: ItemRewardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RewardItem) = with(binding) {
            tvRewardName.text = item.name
            tvRewardDescription.text = item.description
            tvCost.text = "${item.costPoints} pts"
            btnRedeem.isEnabled = currentPoints >= item.costPoints
            btnRedeem.setOnClickListener { onRedeem(item) }
        }
    }
}
