package com.example.myfinance.detailsFood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myfinance.AppDatabase
import com.example.myfinance.CATEGORY
import com.example.myfinance.databinding.ActivityFoodCategoryShowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodCategoryActivityShow : AppCompatActivity() {
    private var _binding: ActivityFoodCategoryShowBinding? = null
    private val binding get() = requireNotNull(_binding)


    private var category: CATEGORY? = null

    private val adapter = ExpensesAdapter(this::deleteRecord)

    private fun deleteRecord(id: Int) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            db.userDao().deleteById(id)
            gettingLists(category!!, db)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFoodCategoryShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.expRv.adapter = adapter
        val db = AppDatabase.getInstance(this)

        category = intent?.getSerializableExtra("name_category") as CATEGORY

        lifecycleScope.launch {
            gettingLists(category!!, db)
        }
    }

    private suspend fun gettingLists(category: CATEGORY, db: AppDatabase) {
        when (category) {
            CATEGORY.FOOD -> {
                val list = db.userDao().getFoodExpenses()
                val sum = db.userDao().getFoodExpensesSum() ?: 0
                withContext(Dispatchers.IO) {
                    adapter.submitList(list)
                    binding.textMoneyFood.text = "$sum Р"
                }
            }

            CATEGORY.MEDICINE -> {
                val list = db.userDao().getMedicineExpenses()
                val sum = db.userDao().getFoodExpensesSum() ?: 0
                withContext(Dispatchers.IO) {
                    adapter.submitList(list)
                    binding.textMoneyFood.text = "$sum Р"
                }
            }

            CATEGORY.RELAX -> {
                val list = db.userDao().getRelaxExpenses()
                val sum = db.userDao().getFoodExpensesSum() ?: 0
                withContext(Dispatchers.IO) {
                    adapter.submitList(list)
                    binding.textMoneyFood.text = "$sum Р"
                }
            }
        }
    }
}