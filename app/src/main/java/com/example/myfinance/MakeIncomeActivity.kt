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
import com.example.myfinance.MakeExpenceActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class MakeIncomeActivity : AppCompatActivity() {
    private var userId: Int = -1

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

        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
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
                    Toast.makeText(this@MakeIncomeActivity, "Сначала создайте цель", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        val incomeTitle: EditText = findViewById(R.id.income_name)
        val incomeAmount: EditText = findViewById(R.id.income_amount)
        val incomeDate: EditText = findViewById(R.id.income_date)

        // Устанавливаем фильтр для суммы (только цифры)
        incomeAmount.filters = arrayOf(
            InputFilter.LengthFilter(10), // Максимум 10 цифр
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source.isNotEmpty() && !source.toString().matches(Regex("^\\d+\$"))) {
                    ""
                } else {
                    null
                }
            }
        )

        // CheckBoxes for income categories
        val checkBoxGift: CheckBox = findViewById(R.id.checkBox3)
        val checkBoxWork: CheckBox = findViewById(R.id.checkBox2)
        val checkBoxWin: CheckBox = findViewById(R.id.checkBox)

        // Настройка поведения CheckBox (только один выбран)
        val checkBoxes = listOf(checkBoxGift, checkBoxWork, checkBoxWin)
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
        incomeDate.setText(currentDate)

        val buttonIncome: Button = findViewById(R.id.button_income)

        buttonIncome.setOnClickListener {
            val title = incomeTitle.text.toString().trim()
            val incomeSumStr = incomeAmount.text.toString().trim()
            val incomeDateStr = incomeDate.text.toString().trim()

            // Валидация названия дохода
            if (title.isEmpty()) {
                incomeTitle.error = "Введите название дохода"
                incomeTitle.requestFocus()
                return@setOnClickListener
            }

            if (!isValidTitle(title)) {
                incomeTitle.error = "Название содержит недопустимые символы"
                incomeTitle.requestFocus()
                return@setOnClickListener
            }

            if (title.length > 20) {
                incomeTitle.error = "Название слишком длинное (макс. 20 символов)"
                incomeTitle.requestFocus()
                return@setOnClickListener
            }

            // Валидация суммы
            if (incomeSumStr.isEmpty()) {
                incomeAmount.error = "Введите сумму дохода"
                incomeAmount.requestFocus()
                return@setOnClickListener
            }

            val incomeSum = incomeSumStr.toIntOrNull()
            when {
                incomeSum == null -> {
                    incomeAmount.error = "Введите корректную сумму"
                    incomeAmount.requestFocus()
                    return@setOnClickListener
                }
                incomeSum <= 0 -> {
                    incomeAmount.error = "Сумма должна быть больше 0"
                    incomeAmount.requestFocus()
                    return@setOnClickListener
                }
                incomeSum > 1_000_000 -> {
                    incomeAmount.error = "Сумма слишком большая (макс. 1 млрд)"
                    incomeAmount.requestFocus()
                    return@setOnClickListener
                }
            }

            // Валидация даты
            if (incomeDateStr.isEmpty()) {
                incomeDate.error = "Введите дату"
                incomeDate.requestFocus()
                return@setOnClickListener
            }

            if (!isValidDate(incomeDateStr)) {
                incomeDate.error = "Неверный формат даты (используйте ДД.ММ.ГГГГ) или год должен быть между 2020-2030"
                incomeDate.requestFocus()
                return@setOnClickListener
            }

            // Проверка категории
            val selectedCategory = when {
                checkBoxGift.isChecked -> "Гифт"
                checkBoxWork.isChecked -> "Работа"
                checkBoxWin.isChecked -> "Лотерея"
                else -> {
                    Toast.makeText(this, "Выберите категорию дохода", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val income = Income(0, userId, selectedCategory, title, incomeSum, incomeDateStr)

            val db = AppDatabase.getInstance(this)
            val incomeDao = db.userDao()
            lifecycleScope.launch {
                try {
                    incomeDao.insert(income)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MakeIncomeActivity,
                            "Доход добавлен",
                            Toast.LENGTH_LONG
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MakeIncomeActivity,
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