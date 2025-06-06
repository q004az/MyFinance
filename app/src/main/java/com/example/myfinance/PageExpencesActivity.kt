package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myfinance.detailsFood.FoodCategoryActivityShow
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageExpencesActivity : AppCompatActivity() {
    private var userId: Int = -1
    private lateinit var pieChart: PieChart

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

        userId = intent.getIntExtra("user_id", -1)
        if(userId == -1) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        pieChart = findViewById(R.id.pieChart)
        setupPieChart()

        initListener()
        val buttonMakeExpense: Button = findViewById(R.id.button_make_expense)

        buttonMakeExpense.setOnClickListener {
            val intent = Intent(this, MakeExpenceActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_home

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_profile -> {
                    val intent = Intent(this, MainScreenActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.bottom_home -> {
                    val intent = Intent(this, PageExpencesActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.bottom_income -> {
                    val intent = Intent(this, PageIncomeActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
    }

    private fun loadDataToPieChart(foodSum: Int, medicineSum: Int, relaxSum: Int) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (foodSum > 0) entries.add(PieEntry(foodSum.toFloat(), "Еда"))
        if (medicineSum > 0) entries.add(PieEntry(medicineSum.toFloat(), "Медицина"))
        if (relaxSum > 0) entries.add(PieEntry(relaxSum.toFloat(), "Развлечения"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
            colors.add(Color.GRAY) // Серый цвет для "нет данных"
        } else {
            colors.add(ContextCompat.getColor(this, R.color.salad))
            colors.add(ContextCompat.getColor(this, R.color.yellow))
            colors.add(ContextCompat.getColor(this, R.color.red))
        }

        val dataSet = PieDataSet(entries, "Расходы по категориям")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun initListener() {
        val buttonFoodContainer: ConstraintLayout = findViewById(R.id.constraintLayout_food)
        val buttonMedicineContainer: ConstraintLayout = findViewById(R.id.constraintLayout_medicine)
        val buttonRelaxContainer: ConstraintLayout = findViewById(R.id.constraintLayout_relax)

        buttonFoodContainer.setOnClickListener {
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.FOOD)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }

        buttonMedicineContainer.setOnClickListener {
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.MEDICINE)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }

        buttonRelaxContainer.setOnClickListener {
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.RELAX)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val db = AppDatabase.getInstance(this)
        val expenseDao = db.userDao()
        lifecycleScope.launch {
            Log.e("Расходы", expenseDao.getAllExpenses(userId).toString())
        }

        loadExpenses()
    }

    private fun loadExpenses() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val foodSum = dao.getFoodExpensesSum(userId) ?: 0
            val medicineSum = dao.getMedicineExpensesSum(userId) ?: 0
            val relaxSum = dao.getRelaxExpensesSum(userId) ?: 0
            val lastGoal = dao.getLastGoal(userId)

            val totalIncome = withContext(Dispatchers.IO) {
                (dao.getGiftIncomeSum(userId) ?: 0) +
                        (dao.getWorkIncomeSum(userId) ?: 0) +
                        (dao.getWinIncomeSum(userId) ?: 0)
            }

            val totalExpenses = foodSum + medicineSum + relaxSum
            val currentBalance = (lastGoal?.initialCapital ?: 0) + totalIncome - totalExpenses

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.text_money_now).text = "$currentBalance ₽"
                // Update UI with the sums
                findViewById<TextView>(R.id.text_money_food).text = "$foodSum ₽"
                findViewById<TextView>(R.id.text_money_medicine).text = "$medicineSum ₽"
                findViewById<TextView>(R.id.text_money_relax).text = "$relaxSum ₽"

                // Update current balance
                findViewById<TextView>(R.id.text_money_now).text = "$currentBalance ₽"

                // You can also calculate and display percentages if needed
                val total = foodSum + medicineSum + relaxSum
                if (total > 0) {
                    findViewById<TextView>(R.id.text_percent_food).text =
                        "${(foodSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_medicine).text =
                        "${(medicineSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_relax).text =
                        "${(relaxSum * 100 / total)}%"

                    loadDataToPieChart(foodSum, medicineSum, relaxSum)
                } else {
                    // If no expenses, show empty chart
                    loadDataToPieChart(0, 0, 0)
                }
            }
        }
    }
}
