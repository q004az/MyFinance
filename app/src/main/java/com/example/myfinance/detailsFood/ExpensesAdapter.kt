package com.example.myfinance.detailsFood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myfinance.Expense
import com.example.myfinance.databinding.ItemExpenseBinding

class ExpensesAdapter(private val deleteRecord: (id: Int)-> Unit ): ListAdapter<Expense, ExpensesAdapter.ExpenseViewHolder>(ExpensesDiffCallback()) {
    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Expense) {
            binding.dateTv.text = item.date
            binding.amountTv.text = "${item.amount} Р"
            binding.nameTv.text = item.name

            binding.deleteBtn.setOnClickListener{
                deleteRecord(item.id)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

class ExpensesDiffCallback: DiffUtil.ItemCallback<Expense>(){
    override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
        return oldItem == newItem
    }

}