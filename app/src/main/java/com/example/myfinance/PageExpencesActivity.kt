package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
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
        pieChart = findViewById(R.id.pieChart)
        setupPieChart()

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

        if (foodSum > 0) entries.add(PieEntry(foodSum.toFloat(), "Еда"))
        if (medicineSum > 0) entries.add(PieEntry(medicineSum.toFloat(), "Мед"))
        if (relaxSum > 0) entries.add(PieEntry(relaxSum.toFloat(), "Отдых"))

        if (entries.isEmpty()) {
            // Show some default data if no expenses
            entries.add(PieEntry(1f, "Нет данных"))
        }

        val dataSet = PieDataSet(entries, "Расходы по категориям")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // Add colors
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(this, R.color.purple_200)) // Food color
        colors.add(ContextCompat.getColor(this, R.color.yellow))     // Medicine color
        colors.add(ContextCompat.getColor(this, R.color.red))        // Relax color

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
            startActivity(intent)
        }

        buttonMedicineContainer.setOnClickListener {
            val intent = Intent(this, FoodCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORY.MEDICINE)
            startActivity(intent)
        }

        buttonRelaxContainer.setOnClickListener {
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
            val lastGoal = dao.getLastGoal()

            // Calculate current balance
            val totalExpenses = foodSum + medicineSum + relaxSum
            val currentBalance = (lastGoal?.initialCapital ?: 0) - totalExpenses

            withContext(Dispatchers.Main) {
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
