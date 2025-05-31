package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreenActivity : AppCompatActivity() {
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
                withContext(Dispatchers.Main) {
                    if (lastGoal != null) {
                        goalName.text = lastGoal.title
                        goalNow.text = lastGoal.initialCapital.toString()
                        goalNeed.text = lastGoal.targetAmount.toString()
                        goalBetween.text = (lastGoal.targetAmount - lastGoal.initialCapital).toString()
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
