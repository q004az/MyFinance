package com.example.myfinance.detailsFood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myfinance.Deposit
import com.example.myfinance.databinding.ItemDepositBinding

class DepositAdapter(
    private val onDeleteClick: (Int) -> Unit,
    private val onSpendClick: (Deposit) -> Unit
) : ListAdapter<Deposit, DepositAdapter.DepositViewHolder>(DepositDiffCallback()) {

    inner class DepositViewHolder(private val binding: ItemDepositBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(deposit: Deposit) {
            binding.nameTv.text = deposit.name
            binding.amountTv.text = "${deposit.amount} Р"

            binding.deleteBtn.setOnClickListener {
                onDeleteClick(deposit.id)
            }

            binding.spisatTv.setOnClickListener {
                onSpendClick(deposit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepositViewHolder {
        val binding = ItemDepositBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DepositViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DepositViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DepositDiffCallback : DiffUtil.ItemCallback<Deposit>() {
    override fun areItemsTheSame(oldItem: Deposit, newItem: Deposit): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Deposit, newItem: Deposit): Boolean {
        return oldItem == newItem
    }
}