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
import com.example.myfinance.detailsFood.IncomeCategoryActivityShow

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

class PageIncomeActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        pieChart = findViewById(R.id.pieChart)
        setupPieChart()

        initListener()
        val buttonMakeIncome: Button = findViewById(R.id.button_make_income)

        buttonMakeIncome.setOnClickListener {
            val intent = Intent(this, MakeIncomeActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_income

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, MainScreenActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_home -> {
                    startActivity(Intent(applicationContext, PageExpencesActivity::class.java))
                    finish()
                    true
                }
                R.id.bottom_income -> {
                    true // Stay on current screen
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

    private fun loadDataToPieChart(giftSum: Int, workSum: Int, winSum: Int) {
        val entries = ArrayList<PieEntry>()

        if (giftSum > 0) entries.add(PieEntry(giftSum.toFloat(), "Подарки"))
        if (workSum > 0) entries.add(PieEntry(workSum.toFloat(), "Работа"))
        if (winSum > 0) entries.add(PieEntry(winSum.toFloat(), "Лотерея"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
        }

        val dataSet = PieDataSet(entries, "Доходы по категориям")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

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
        val buttonGiftContainer: ConstraintLayout = findViewById(R.id.constraintLayout_gift)
        val buttonWorkContainer: ConstraintLayout = findViewById(R.id.constraintLayout_work)
        val buttonWinContainer: ConstraintLayout = findViewById(R.id.constraintLayout_win)

        buttonGiftContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.GIFT)
            startActivity(intent)
        }

        buttonWorkContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.WORK)
            startActivity(intent)
        }

        buttonWinContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.WIN)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val db = AppDatabase.getInstance(this)
        val incomeDao = db.userDao()
        lifecycleScope.launch {
            Log.e("Доходы", incomeDao.getAllIncome().toString())
        }

        loadIncomes()
    }

    private fun loadIncomes() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val giftSum = dao.getGiftIncomeSum() ?: 0
            val workSum = dao.getWorkIncomeSum() ?: 0
            val winSum = dao.getWinIncomeSum() ?: 0
            val lastGoal = dao.getLastGoal()

            // Calculate total income and current balance
            val totalIncome = giftSum + workSum + winSum
            val totalExpenses = withContext(Dispatchers.IO) {
                (dao.getFoodExpensesSum() ?: 0) +
                        (dao.getMedicineExpensesSum() ?: 0) +
                        (dao.getRelaxExpensesSum() ?: 0)
            }
            val currentBalance = (lastGoal?.initialCapital ?: 0) + totalIncome - totalExpenses

            withContext(Dispatchers.Main) {
                // Show current balance, not total income
                findViewById<TextView>(R.id.text_money_now).text = "$currentBalance ₽"

                // Update UI with the sums
                findViewById<TextView>(R.id.text_money_gift).text = "$giftSum ₽"
                findViewById<TextView>(R.id.text_money_work).text = "$workSum ₽"
                findViewById<TextView>(R.id.text_money_win).text = "$winSum ₽"

                val total = giftSum + workSum + winSum
                if (total > 0) {
                    findViewById<TextView>(R.id.text_percent_gift).text = "${(giftSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_work).text = "${(workSum * 100 / total)}%"
                    findViewById<TextView>(R.id.text_percent_win).text = "${(winSum * 100 / total)}%"

                    loadDataToPieChart(giftSum, workSum, winSum)
                } else {
                    loadDataToPieChart(0, 0, 0)
                }
            }
        }
    }
}