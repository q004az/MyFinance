package com.example.myfinance

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MakeIncomeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val incomeTitle: EditText = findViewById(R.id.income_name)
        val incomeAmount: EditText = findViewById(R.id.income_amount)
        val incomeDate: EditText = findViewById(R.id.income_date)

        // CheckBoxes for income categories
        val checkBoxGift: CheckBox = findViewById(R.id.checkBox3)
        val checkBoxWork: CheckBox = findViewById(R.id.checkBox2)
        val checkBoxWin: CheckBox = findViewById(R.id.checkBox)

        val buttonIncome: Button = findViewById(R.id.button_income)

        buttonIncome.setOnClickListener {
            val title = incomeTitle.text.toString().trim()
            val incomeSumStr = incomeAmount.text.toString().trim()
            val incomeDateStr = incomeDate.text.toString().trim()

            if (title.isEmpty() || incomeSumStr.isEmpty() || incomeDateStr.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check that exactly one category is selected
            val selectedCategory = when {
                checkBoxGift.isChecked -> "Гифт"
                checkBoxWork.isChecked -> "Работа"
                checkBoxWin.isChecked -> "Лотерея"
                else -> {
                    Toast.makeText(this, "Выберите категорию", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Try to convert to Int
            val incomeSum = incomeSumStr.toIntOrNull()

            if (incomeSum == null) {
                Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val income = Income(
                id = 0,
                category = selectedCategory,
                name = title,
                amount = incomeSum,
                date = incomeDateStr
            )

            val db = AppDatabase.getInstance(this)
            val incomeDao = db.userDao()
            lifecycleScope.launch {
                incomeDao.insert(income)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MakeIncomeActivity, "Доход добавлен", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }

            // Clear fields
            incomeTitle.text.clear()
            incomeAmount.text.clear()
            incomeDate.text.clear()

            // Reset CheckBoxes
            checkBoxGift.isChecked = false
            checkBoxWork.isChecked = false
            checkBoxWin.isChecked = false
        }
    }
}