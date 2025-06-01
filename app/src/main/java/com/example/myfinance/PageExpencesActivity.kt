package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageExpencesActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_expences)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val buttonMakeExpense: Button = findViewById(R.id.button_make_expense)

        buttonMakeExpense.setOnClickListener {
            val intent = Intent(this, MakeExpenceActivity::class.java)
            startActivity(intent)
        }



        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, MainScreenActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_home -> {
                    true // Остаемся на текущем экране
                }
                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val db = AppDatabase.getInstance(this)
        val expenseDao = db.userDao()
        lifecycleScope.launch {
            Log.e("Расходы", expenseDao.getAllExpenses().toString())
        }

        loadExpenses()
    }

    private fun loadExpenses() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val foodSum = dao.getFoodExpensesSum() ?: 0
            val medicineSum = dao.getMedicineExpensesSum() ?: 0
            val relaxSum = dao.getRelaxExpensesSum() ?: 0

            withContext(Dispatchers.Main) {
                // Update UI with the sums
                findViewById<TextView>(R.id.text_money_food).text = "$foodSum ₽"
                findViewById<TextView>(R.id.text_money_medicine).text = "$medicineSum ₽"
                findViewById<TextView>(R.id.text_money_relax).text = "$relaxSum ₽"

                // You can also calculate and display percentages if needed
                val total = foodSum + medicineSum + relaxSum
                if (total > 0) {
                    findViewById<TextView>(R.id.text_percent_food).text = "${(foodSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_medicine).text = "${(medicineSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_relax).text = "${(relaxSum * 100 / total)}%"
                }
            }
        }
    }

}
