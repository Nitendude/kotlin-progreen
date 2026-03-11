package com.progreen.recycleapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycleapp.R
import com.progreen.recycleapp.databinding.ItemDashboardCardBinding
import com.progreen.recycleapp.model.DashboardAction

class DashboardAdapter(
    private val items: List<DashboardAction>,
    private val onClick: (DashboardAction) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding = ItemDashboardCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DashboardViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class DashboardViewHolder(private val binding: ItemDashboardCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardAction) = with(binding) {
            ivIcon.setImageResource(item.iconRes)
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            root.setOnClickListener { onClick(item) }

            val animation = AnimationUtils.loadAnimation(root.context, R.anim.card_pop_in)
            root.startAnimation(animation)
        }
    }
}
