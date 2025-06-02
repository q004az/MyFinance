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
    private lateinit var binding: ActivityFoodCategoryShowBinding

    private var category: CATEGORY? = null
    private val adapter = ExpensesAdapter(this::deleteRecord)

    private fun deleteRecord(id: Int) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            db.userDao().deleteById(id)
            gettingLists(category ?: CATEGORY.FOOD, db)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodCategoryShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.expRv.adapter = adapter
        val db = AppDatabase.getInstance(this)

        category = intent?.getSerializableExtra("name_category") as? CATEGORY ?: CATEGORY.FOOD

        lifecycleScope.launch {
            gettingLists(category ?: CATEGORY.FOOD, db)
        }
    }

    private suspend fun gettingLists(category: CATEGORY, db: AppDatabase) {
        when (category) {
            CATEGORY.FOOD -> {
                val list = db.userDao().getFoodExpenses()
                val sum = db.userDao().getFoodExpensesSum() ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Еда"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.MEDICINE -> {
                val list = db.userDao().getMedicineExpenses()
                val sum = db.userDao().getMedicineExpensesSum() ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Медицина"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.RELAX -> {
                val list = db.userDao().getRelaxExpenses()
                val sum = db.userDao().getRelaxExpensesSum() ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Отдых"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            null -> {
                // Обработка случая, когда категория не передана
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Категория"
                    binding.textMoneyFood.text = "0 Р"
                    adapter.submitList(emptyList())
                }
            }
        }
    }
}