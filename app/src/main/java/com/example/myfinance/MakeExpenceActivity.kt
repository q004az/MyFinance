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
import com.example.myfinance.makeGoalActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MakeExpenceActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_expence)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val expanseTitle: EditText = findViewById(R.id.expense_name)
        val expenseAmount: EditText = findViewById(R.id.expense_amount)
        val expenseDate: EditText = findViewById(R.id.expanse_date)

        // Добавляем CheckBox
        val checkBoxFood: CheckBox = findViewById(R.id.checkBox3)
        val checkBoxMedicine: CheckBox = findViewById(R.id.checkBox2)
        val checkBoxRelax: CheckBox = findViewById(R.id.checkBox)

        val buttonGoal: Button = findViewById(R.id.button_goal)

        buttonGoal.setOnClickListener {
            val title = expanseTitle.text.toString().trim()
            val expenseSumStr = expenseAmount.text.toString().trim()
            val expenseData = expenseDate.text.toString().trim()

            if (title.isEmpty() || expenseSumStr.isEmpty() || expenseData.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Проверяем, что выбрана ровно одна категория
            val selectedCategory = when {
                checkBoxFood.isChecked -> "Еда"
                checkBoxMedicine.isChecked -> "Мед"
                checkBoxRelax.isChecked -> "Отдых"
                else -> {
                    Toast.makeText(this, "Выберите категорию", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            // Попробуем преобразовать в Int
            val expenseSum = expenseSumStr.toIntOrNull()

            if (expenseSum == null) {
                Toast.makeText(this, "Введите корректные числовые значения", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val expense = Expense(0, selectedCategory, title, expenseSum, expenseData)

            val db = AppDatabase.getInstance(this)
            val expenseDao = db.userDao()
            lifecycleScope.launch {
                expenseDao.insert(expense)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MakeExpenceActivity, "Расход добавлен", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }

            expanseTitle.text.clear()
            expenseAmount.text.clear()
            expenseDate.text.clear()

            // Сбрасываем CheckBox
            checkBoxFood.isChecked = false
            checkBoxMedicine.isChecked = false
            checkBoxRelax.isChecked = false
        }


    }
}
