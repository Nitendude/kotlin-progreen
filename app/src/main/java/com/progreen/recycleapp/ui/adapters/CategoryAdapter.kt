package com.progreen.recycleapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycleapp.databinding.ItemCategoryBinding
import com.progreen.recycleapp.model.RecyclingCategory

class CategoryAdapter(
    private val items: List<RecyclingCategory>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecyclingCategory) = with(binding) {
            tvName.text = item.name
            tvPointsRate.text = "${item.pointsPerKg} pts per kg"
            tvTag.text = item.tag
        }
    }
}
