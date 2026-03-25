package com.progreen.recycling.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.PendingAccount
import com.progreen.recycling.databinding.ItemPendingAccountBinding

class PendingAccountAdapter(
    private var items: List<PendingAccount>,
    private val onApprove: (PendingAccount) -> Unit,
    private val onReject: (PendingAccount) -> Unit
) : RecyclerView.Adapter<PendingAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onApprove, onReject)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<PendingAccount>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemPendingAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PendingAccount, onApprove: (PendingAccount) -> Unit, onReject: (PendingAccount) -> Unit) {
            binding.accountName.text = item.name
            binding.accountMeta.text = "${item.email} | ${item.role} | Verified: ${item.isVerified}"
            binding.approveButton.setOnClickListener { onApprove(item) }
            binding.rejectButton.setOnClickListener { onReject(item) }
        }
    }
}
