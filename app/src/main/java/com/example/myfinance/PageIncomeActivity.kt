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
    private var userId: Int = -1
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
        userId = intent.getIntExtra("user_id", -1)
        if(userId == -1) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        pieChart = findViewById(R.id.pieChart)
        setupPieChart()

        initListener()
        val buttonMakeIncome: Button = findViewById(R.id.button_make_income)

        buttonMakeIncome.setOnClickListener {
            val intent = Intent(this, MakeIncomeActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_income

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

    private fun loadDataToPieChart(giftSum: Int, workSum: Int, winSum: Int) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (giftSum > 0) entries.add(PieEntry(giftSum.toFloat(), "Переводы"))
        if (workSum > 0) entries.add(PieEntry(workSum.toFloat(), "Работа"))
        if (winSum > 0) entries.add(PieEntry(winSum.toFloat(), "Бизнес"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
            colors.add(Color.GRAY) // Серый цвет для "нет данных"
        } else {
            colors.add(ContextCompat.getColor(this, R.color.pink))
            colors.add(ContextCompat.getColor(this, R.color.colorMedicine))
            colors.add(ContextCompat.getColor(this, R.color.colorRelax))
        }

        val dataSet = PieDataSet(entries, "Доходы по категориям")
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
        val buttonGiftContainer: ConstraintLayout = findViewById(R.id.constraintLayout_gift)
        val buttonWorkContainer: ConstraintLayout = findViewById(R.id.constraintLayout_work)
        val buttonWinContainer: ConstraintLayout = findViewById(R.id.constraintLayout_win)

        buttonGiftContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.GIFT)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }

        buttonWorkContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.WORK)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }

        buttonWinContainer.setOnClickListener {
            val intent = Intent(this, IncomeCategoryActivityShow::class.java)
            intent.putExtra("name_category", CATEGORYS.WIN)
            intent.putExtra("user_id", userId)  // Добавьте эту строку
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val db = AppDatabase.getInstance(this)
        val incomeDao = db.userDao()
        lifecycleScope.launch {
            Log.e("Доходы", incomeDao.getAllIncome(userId).toString())
        }

        loadIncomes()
    }

    private fun loadIncomes() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val giftSum = dao.getGiftIncomeSum(userId) ?: 0
            val workSum = dao.getWorkIncomeSum(userId) ?: 0
            val winSum = dao.getWinIncomeSum(userId) ?: 0
            val lastGoal = dao.getLastGoal(userId)

            // Calculate total income and current balance
            val totalIncome = giftSum + workSum + winSum
            val totalExpenses = withContext(Dispatchers.IO) {
                (dao.getFoodExpensesSum(userId) ?: 0) +
                        (dao.getMedicineExpensesSum(userId) ?: 0) +
                        (dao.getRelaxExpensesSum(userId) ?: 0)
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