package com.example.myfinance.detailsFood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myfinance.Expense
import com.example.myfinance.Income
import com.example.myfinance.databinding.ItemExpenseBinding
import com.example.myfinance.databinding.ItemIncomeBinding

class IncomeAdapter(private val deleteRecord: (id: Int)-> Unit ): ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {
    inner class IncomeViewHolder(private val binding: ItemIncomeBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Income) {
            binding.dateTv.text = item.date
            binding.amountTv.text = "${item.amount} Р"
            binding.nameTv.text = item.name

            binding.deleteBtn.setOnClickListener{
                deleteRecord(item.id)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ItemIncomeBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return IncomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

class IncomeDiffCallback: DiffUtil.ItemCallback<Income>(){
    override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean {
        return oldItem == newItem
    }

}