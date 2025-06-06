package com.example.myfinance.detailsFood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myfinance.AppDatabase
import com.example.myfinance.CATEGORYS
import com.example.myfinance.databinding.ActivityIncomeCategoryShowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IncomeCategoryActivityShow : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeCategoryShowBinding
    private var userId: Int = -1
    private var category: CATEGORYS? = null

    private val adapter = IncomeAdapter(this::deleteRecord)

    private fun deleteRecord(id: Int) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            db.userDao().deleteByIdIncome(id,userId)
            gettingLists(category ?: CATEGORYS.GIFT, db)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeCategoryShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            finish()
            return
        }

        binding.expRv.adapter = adapter
        val db = AppDatabase.getInstance(this)

        category = intent?.getSerializableExtra("name_category") as? CATEGORYS ?: CATEGORYS.GIFT

        lifecycleScope.launch {
            gettingLists(category ?: CATEGORYS.GIFT, db)
        }
    }

    private suspend fun gettingLists(category: CATEGORYS, db: AppDatabase) {
        when (category) {
            CATEGORYS.GIFT -> {
                val list = db.userDao().getGiftIncome(userId)
                val sum = db.userDao().getGiftIncomeSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryIncome.text = "Переводы"
                    binding.textMoneyIncome.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORYS.WORK -> {
                val list = db.userDao().getWorkIncome(userId)
                val sum = db.userDao().getWorkIncomeSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryIncome.text = "Работа"
                    binding.textMoneyIncome.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            CATEGORYS.WIN -> {
                val list = db.userDao().getWinIncome(userId)
                val sum = db.userDao().getWinIncomeSum(userId) ?: 0
                withContext(Dispatchers.Main) {
                    binding.textCategoryIncome.text = "Бизнес"
                    binding.textMoneyIncome.text = "$sum Р"
                    adapter.submitList(list)
                }
            }
            null -> {
                withContext(Dispatchers.Main) {
                    binding.textCategoryIncome.text = "Доходы"
                    binding.textMoneyIncome.text = "0 Р"
                    adapter.submitList(emptyList())
                }
            }
        }
    }
}