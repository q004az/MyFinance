package com.example.myfinance

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class MakeExpenceActivity : AppCompatActivity() {
    private var userId: Int = -1

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


        userId = intent.getIntExtra("user_id", -1)
        if(userId == -1) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()
        lifecycleScope.launch {
            val goal = dao.getLastGoal(userId)
            if(goal == null){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MakeExpenceActivity, "Сначала создайте цель", Toast.LENGTH_LONG).show()
                    finish()
                }
                }
        }



        val expanseTitle: EditText = findViewById(R.id.expense_name)
        val expenseAmount: EditText = findViewById(R.id.expense_amount)
        val expenseDate: EditText = findViewById(R.id.expanse_date)

        // Устанавливаем фильтр для суммы (только цифры)
        expenseAmount.filters = arrayOf(
            InputFilter.LengthFilter(10), // Максимум 10 цифр
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source.isNotEmpty() && !source.toString().matches(Regex("^\\d+\$"))) {
                    ""
                } else {
                    null
                }
            }
        )

        // CheckBoxes для категорий расходов
        val checkBoxFood: CheckBox = findViewById(R.id.checkBox3)
        val checkBoxMedicine: CheckBox = findViewById(R.id.checkBox2)
        val checkBoxRelax: CheckBox = findViewById(R.id.checkBox)

        // Настройка поведения CheckBox (только один выбран)
        val checkBoxes = listOf(checkBoxFood, checkBoxMedicine, checkBoxRelax)
        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Снимаем выделение с других CheckBox
                    checkBoxes.filter { it != buttonView }.forEach { it.isChecked = false }
                }
            }
        }

        // Установка текущей даты по умолчанию
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        expenseDate.setText(currentDate)

        val buttonGoal: Button = findViewById(R.id.button_goal)

        buttonGoal.setOnClickListener {
            val title = expanseTitle.text.toString().trim()
            val expenseSumStr = expenseAmount.text.toString().trim()
            val expenseData = expenseDate.text.toString().trim()

            // Валидация названия расхода
            if (title.isEmpty()) {
                expanseTitle.error = "Введите название расхода"
                expanseTitle.requestFocus()
                return@setOnClickListener
            }

            if (!isValidTitle(title)) {
                expanseTitle.error = "Название содержит недопустимые символы"
                expanseTitle.requestFocus()
                return@setOnClickListener
            }

            if (title.length > 20) {
                expanseTitle.error = "Название слишком длинное (макс. 20 символов)"
                expanseTitle.requestFocus()
                return@setOnClickListener
            }

            // Валидация суммы
            if (expenseSumStr.isEmpty()) {
                expenseAmount.error = "Введите сумму расхода"
                expenseAmount.requestFocus()
                return@setOnClickListener
            }

            val expenseSum = expenseSumStr.toIntOrNull()
            when {
                expenseSum == null -> {
                    expenseAmount.error = "Введите корректную сумму"
                    expenseAmount.requestFocus()
                    return@setOnClickListener
                }
                expenseSum <= 0 -> {
                    expenseAmount.error = "Сумма должна быть больше 0"
                    expenseAmount.requestFocus()
                    return@setOnClickListener
                }
                expenseSum > 1_000_000 -> {
                    expenseAmount.error = "Сумма слишком большая (макс. 1 млрд)"
                    expenseAmount.requestFocus()
                    return@setOnClickListener
                }
            }

            // Валидация даты
            if (expenseData.isEmpty()) {
                expenseDate.error = "Введите дату"
                expenseDate.requestFocus()
                return@setOnClickListener
            }

            if (!isValidDate(expenseData)) {
                expenseDate.error = "Неверный формат даты (используйте ДД.ММ.ГГГГ) или год должен быть между 2020-2030"
                expenseDate.requestFocus()
                return@setOnClickListener
            }

            // Проверка категории
            val selectedCategory = when {
                checkBoxFood.isChecked -> "Еда"
                checkBoxMedicine.isChecked -> "Мед"
                checkBoxRelax.isChecked -> "Отдых"
                else -> {
                    Toast.makeText(this, "Выберите категорию расхода", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val expense = Expense(0, userId, selectedCategory, title, expenseSum, expenseData)

            val db = AppDatabase.getInstance(this)
            val expenseDao = db.userDao()
            lifecycleScope.launch {
                try {
                    expenseDao.insert(expense)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MakeExpenceActivity,
                            "Расход добавлен",
                            Toast.LENGTH_LONG
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MakeExpenceActivity,
                            "Ошибка при сохранении: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Проверка названия на допустимые символы
    private fun isValidTitle(title: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Zа-яА-Я0-9 \\-.,!?()\"']+\$")
        return pattern.matcher(title).matches()
    }

    // Проверка формата даты (ДД.ММ.ГГГГ) и что год между 2020-2030
    private fun isValidDate(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            val date = dateFormat.parse(dateStr)

            val calendar = Calendar.getInstance()
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)

            date != null && year in 2020..2030
        } catch (e: Exception) {
            false
        }
    }
}