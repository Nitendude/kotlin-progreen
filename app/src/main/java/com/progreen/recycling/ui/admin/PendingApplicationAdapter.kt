package com.progreen.recycling.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progreen.recycling.data.model.PendingApplication
import com.progreen.recycling.databinding.ItemPendingApplicationBinding

class PendingApplicationAdapter(
    private var items: List<PendingApplication>,
    private val onApprove: (PendingApplication) -> Unit,
    private val onReject: (PendingApplication) -> Unit
) : RecyclerView.Adapter<PendingApplicationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingApplicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onApprove, onReject)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<PendingApplication>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemPendingApplicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: PendingApplication,
            onApprove: (PendingApplication) -> Unit,
            onReject: (PendingApplication) -> Unit
        ) {
            binding.applicationTitle.text = "${item.applicationType} Application"
            binding.applicationOrg.text = item.organizationName
            binding.applicationMeta.text =
                "${item.applicantName} | ${item.contactEmail} | Verified: ${item.isVerified}"
            binding.applicationAddress.text = item.officeAddress
            binding.applicationDocument.text = item.documentName ?: "No document uploaded"
            binding.approveButton.setOnClickListener { onApprove(item) }
            binding.rejectButton.setOnClickListener { onReject(item) }
        }
    }
}
