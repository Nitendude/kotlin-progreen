package com.progreen.recycling.ui.lgu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.LguSite
import com.progreen.recycling.databinding.ItemLguSiteBinding
import com.progreen.recycling.util.toast

class LguSiteAdapter(
    private val items: List<LguSite>
) : RecyclerView.Adapter<LguSiteAdapter.LguSiteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LguSiteViewHolder {
        val binding = ItemLguSiteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LguSiteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LguSiteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class LguSiteViewHolder(
        private val binding: ItemLguSiteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LguSite) {
            binding.lguName.text = item.name
            binding.lguAddress.text = item.address
            binding.lguDistance.text = "${item.distanceKm} km away"

            binding.openMapButton.setOnClickListener {
                val uri = Uri.parse("geo:0,0?q=${Uri.encode(item.mapQuery)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                try {
                    binding.root.context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    binding.root.context.toast("No map app found on this device")
                }
            }
        }
    }
}
