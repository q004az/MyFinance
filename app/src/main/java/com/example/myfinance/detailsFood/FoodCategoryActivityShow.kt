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
    private var userId: Int = -1
    private var category: CATEGORY? = null
    private val adapter = ExpensesAdapter(this::deleteRecord)

    private fun deleteRecord(id: Int) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            db.userDao().deleteById(id, userId)
            gettingLists(category ?: CATEGORY.FOOD, db)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodCategoryShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            finish()
            return
        }

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
                val list = db.userDao().getFoodExpenses(userId)
                val sum = db.userDao().getFoodExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Еда"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.MEDICINE -> {
                val list = db.userDao().getMedicineExpenses(userId)
                val sum = db.userDao().getMedicineExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Медицина"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.RELAX -> {
                val list = db.userDao().getRelaxExpenses(userId)
                val sum = db.userDao().getRelaxExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Развлечения"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.JKH -> {
                val list = db.userDao().getJKHExpenses(userId)
                val sum = db.userDao().getJKHExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "ЖКХ"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }

            CATEGORY.TRANSPORT -> {
                val list = db.userDao().getTRANSPORTExpenses(userId)
                val sum = db.userDao().getTRANSPORTExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Транспорт"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }

            CATEGORY.ARENDA -> {
                val list = db.userDao().getARENDAExpenses(userId)
                val sum = db.userDao().getARENDAExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Аренда"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORY.MOBILE -> {
                val list = db.userDao().getMOBILEExpenses(userId)
                val sum = db.userDao().getMOBILEExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Мобильная связь"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }

            CATEGORY.CREDIT -> {
                val list = db.userDao().getCREDITExpenses(userId)
                val sum = db.userDao().getCREDITExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Кредит"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }

            CATEGORY.CLOTHES -> {
                val list = db.userDao().getCLOTHESExpenses(userId)
                val sum = db.userDao().getCLOTHESExpensesSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Одежда"
                    binding.textMoneyFood.text = "$sum Р"
                    adapter.submitList(list)
                }
            }

            null -> {
                withContext(Dispatchers.Main) {
                    binding.textCategoryFood.text = "Категория"
                    binding.textMoneyFood.text = "0 Р"
                    adapter.submitList(emptyList())
                }
            }
        }
    }
}