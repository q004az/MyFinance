package com.example.myfinance

import android.os.Bundle
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

class DepositMakeActivity : AppCompatActivity() {
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_deposit_make)
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

        findViewById<Button>(R.id.button_income).setOnClickListener {
            createDeposit()
        }
    }

    private fun createDeposit() {
        val name = findViewById<EditText>(R.id.deposit_name).text.toString().trim()
        val amountStr = findViewById<EditText>(R.id.deposit_amount).text.toString().trim()

        if (name.isEmpty()) {
            findViewById<EditText>(R.id.deposit_name).error = "Введите название вклада"
            return
        }

        if (amountStr.isEmpty()) {
            findViewById<EditText>(R.id.deposit_amount).error = "Введите сумму вклада"
            return
        }

        val amount = amountStr.toIntOrNull() ?: 0
        if (amount <= 0) {
            findViewById<EditText>(R.id.deposit_amount).error = "Сумма должна быть больше 0"
            return
        }

        val deposit = Deposit(userId = userId, name = name, amount = amount)

        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.userDao().insert(deposit)
                }
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@DepositMakeActivity, "Ошибка при создании вклада", Toast.LENGTH_SHORT).show()
            }
        }
    }
}