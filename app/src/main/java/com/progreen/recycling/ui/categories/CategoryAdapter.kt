package com.progreen.recycling.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.RecyclingCategory
import com.progreen.recycling.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val items: List<RecyclingCategory>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecyclingCategory) {
            binding.categoryName.text = item.name
            binding.categoryDescription.text = item.description
            binding.categoryRate.text = "${item.pointsPerKg} pts/kg"
        }
    }
}
