package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myfinance.detailsFood.FoodCategoryActivityShow
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

        initListener()
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

    private fun initListener() {
        val buttonFoodContainer: ConstraintLayout = findViewById(R.id.constraintLayout_food)
        val buttonMedicineContainer: ConstraintLayout = findViewById(R.id.constraintLayout_medicine)
        val buttonRelaxContainer: ConstraintLayout = findViewById(R.id.constraintLayout_relax)

        buttonFoodContainer.setOnClickListener{
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.FOOD)
            startActivity(intent)
        }

        buttonMedicineContainer.setOnClickListener{
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.MEDICINE)
            startActivity(intent)
        }

        buttonRelaxContainer.setOnClickListener{
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.RELAX)
            startActivity(intent)
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
