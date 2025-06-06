package com.example.myfinance

import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
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
import java.util.regex.Pattern

class makeGoalActivity : AppCompatActivity() {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_goal)
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

        val userGoalTitle: EditText = findViewById(R.id.goal_name)
        val userEndAmount: EditText = findViewById(R.id.goal_end_amount)
        val buttonGoal: Button = findViewById(R.id.button_goal)

        // Устанавливаем фильтр для суммы (только цифры)
        userEndAmount.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.isNotEmpty() && !source.toString().matches(Regex("^\\d+\$"))) {
                ""
            } else {
                null
            }
        })

        buttonGoal.setOnClickListener {
            val title = userGoalTitle.text.toString().trim()
            val endAmountStr = userEndAmount.text.toString().trim()

            // Валидация названия цели
            if (title.isEmpty()) {
                userGoalTitle.error = "Введите название цели"
                userGoalTitle.requestFocus()
                return@setOnClickListener
            }

            if (!isValidTitle(title)) {
                userGoalTitle.error = "Название содержит недопустимые символы"
                userGoalTitle.requestFocus()
                return@setOnClickListener
            }

            if (title.length > 20) {
                userGoalTitle.error = "Название слишком длинное (макс. 20 символов)"
                userGoalTitle.requestFocus()
                return@setOnClickListener
            }

            // Валидация суммы
            if (endAmountStr.isEmpty()) {
                userEndAmount.error = "Введите сумму цели"
                userEndAmount.requestFocus()
                return@setOnClickListener
            }

            val endAmount = endAmountStr.toIntOrNull()
            if (endAmount == null || endAmount <= 0) {
                userEndAmount.error = "Введите корректную сумму (больше 0)"
                userEndAmount.requestFocus()
                return@setOnClickListener
            }

            if (endAmount > 1_000_000) {
                userEndAmount.error = "Сумма слишком большая"
                userEndAmount.requestFocus()
                return@setOnClickListener
            }

            // Устанавливаем начальный капитал всегда 0
            val goal = Goal(0, userId, title, 0, endAmount)

            val db = AppDatabase.getInstance(this)
            val goalDao = db.userDao()
            lifecycleScope.launch {
                try {
                    goalDao.insert(goal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@makeGoalActivity,
                            "Цель добавлена",
                            Toast.LENGTH_LONG
                        ).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@makeGoalActivity,
                            "Ошибка при сохранении цели: ${e.message}",
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
}