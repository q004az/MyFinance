package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myfinance.detailsFood.DepositAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DepositActivity : AppCompatActivity() {
    private lateinit var adapter: DepositAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_deposit)
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

        setupRecyclerView()
        loadDeposits()

        findViewById<Button>(R.id.button_make_deposit).setOnClickListener {
            navigateToDepositMakeActivity()
        }
    }

    private fun setupRecyclerView() {
        adapter = DepositAdapter(
            onDeleteClick = { depositId -> deleteDeposit(depositId) },
            onSpendClick = { deposit -> spendDeposit(deposit) }
        )

        findViewById<RecyclerView>(R.id.exp_rv).adapter = adapter
    }

    private fun loadDeposits() {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            try {
                val deposits = withContext(Dispatchers.IO) {
                    db.userDao().getAllDeposits(userId)
                }

                val totalAmount = withContext(Dispatchers.IO) {
                    db.userDao().getTotalDepositsAmount(userId) ?: 0
                }

                withContext(Dispatchers.Main) {
                    adapter.submitList(deposits)
                    updateTotalAmount(totalAmount)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DepositActivity,
                        "Ошибка при загрузке вкладов",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateTotalAmount(amount: Int) {
        findViewById<TextView>(R.id.text_money_now2).text = "$amount Р"
    }

    private fun deleteDeposit(depositId: Int) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.userDao().deleteDepositById(depositId, userId)
                }
                loadDeposits()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DepositActivity,
                        "Ошибка при удалении вклада",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun spendDeposit(deposit: Deposit) {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            try {
                // 1. Удаляем вклад
                withContext(Dispatchers.IO) {
                    db.userDao().deleteDepositById(deposit.id, userId)
                }

                // 2. Добавляем доход с категорией "Вклад"
                val income = Income(
                    id = 0,
                    userId = userId,
                    category = "Вклад",
                    name = "${deposit.name}",
                    amount = deposit.amount,
                    date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                )

                withContext(Dispatchers.IO) {
                    db.userDao().insert(income)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DepositActivity,
                        "Вклад на сумму ${deposit.amount} Р добавлен к балансу",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Обновляем список вкладов
                    loadDeposits()

                    // Отправляем интент для обновления данных в других активностях
                    sendBroadcast(Intent("UPDATE_DATA").apply {
                        putExtra("user_id", userId)
                    })
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DepositActivity,
                        "Ошибка при списании вклада: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToDepositMakeActivity() {
        val intent = Intent(this, DepositMakeActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivityForResult(intent, REQUEST_CODE_MAKE_DEPOSIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAKE_DEPOSIT && resultCode == RESULT_OK) {
            loadDeposits()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDeposits()
    }

    companion object {
        private const val REQUEST_CODE_MAKE_DEPOSIT = 1
    }
}