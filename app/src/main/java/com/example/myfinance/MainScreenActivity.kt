package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreenActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val login = intent.getStringExtra("user_login")
        val name: TextView = findViewById(R.id.text_name)
        name.text = login ?: "Гость"

        val goalName: TextView = findViewById(R.id.goal_name)
        val goalNow: TextView = findViewById(R.id.goal_now)
        val goalNeed: TextView = findViewById(R.id.goal_need)
        val goalBetween: TextView = findViewById(R.id.goal_between)

        val buttonGoal: Button = findViewById(R.id.button_make_goal)
        buttonGoal.setOnClickListener {
            val intent = Intent(this, makeGoalActivity::class.java)
            startActivity(intent)
        }

        // Загружаем цель
        loadGoal()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_profile -> {
                    true // Остаемся на текущем экране
                }
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PageExpencesActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }


    }

    override fun onResume() {
        super.onResume()
        loadGoal()
    }

    private fun loadGoal() {
        val goalName: TextView = findViewById(R.id.goal_name)
        val goalNow: TextView = findViewById(R.id.goal_now)
        val goalNeed: TextView = findViewById(R.id.goal_need)
        val goalBetween: TextView = findViewById(R.id.goal_between)

        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val lastGoal = dao.getLastGoal()
            val totalExpenses = withContext(Dispatchers.IO) {
                (dao.getFoodExpensesSum() ?: 0) +
                        (dao.getMedicineExpensesSum() ?: 0) +
                        (dao.getRelaxExpensesSum() ?: 0)
            }

            withContext(Dispatchers.Main) {
                if (lastGoal != null) {
                    val currentBalance = lastGoal.initialCapital - totalExpenses
                    val remaining = lastGoal.targetAmount - currentBalance

                    goalName.text = lastGoal.title
                    goalNow.text = currentBalance.toString() // Текущий баланс
                    goalNeed.text = lastGoal.targetAmount.toString()
                    goalBetween.text = remaining.toString() // Остаток до цели
                } else {
                    goalName.text = "Нет цели"
                    goalNow.text = "0"
                    goalNeed.text = "0"
                    goalBetween.text = "0"
                }
            }
        }
    }
}
