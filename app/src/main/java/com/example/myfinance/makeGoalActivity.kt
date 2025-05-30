package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class makeGoalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_make_goal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userGoaltitle: EditText = findViewById(R.id.goal_name)
        val userStartAmount: EditText = findViewById(R.id.goal_start_amount)
        val userEndAmount: EditText = findViewById(R.id.goal_end_amount)

        val buttonGoal: Button = findViewById(R.id.button_goal)

        buttonGoal.setOnClickListener{
            val title = userGoaltitle.text.toString().trim()
            val startAmount = userStartAmount.text.toString().trim()
            val endAmount = userEndAmount.text.toString().trim()

            if (title == "" || startAmount == "" || endAmount == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            else{
                val goal = Goal(0,title,startAmount,endAmount)
                val db = AppDatabase.getInstance(this)
                val goalDao = db.userDao()
                lifecycleScope.launch {
                    goalDao.insert(goal)
                }
                Toast.makeText(this, "Пользователь $login добавлен", Toast.LENGTH_LONG).show()
                title.text.clear()
                startAmount.text.clear()
                endAmount.text.clear()
            }
        }
    }
}