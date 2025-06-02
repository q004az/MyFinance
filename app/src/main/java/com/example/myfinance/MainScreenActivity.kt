package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
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

class MainScreenActivity : AppCompatActivity() {
    private lateinit var pieChartBalance: PieChart
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

        pieChartBalance = findViewById(R.id.pieChartBalance)
        setupBalancePieChart()


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
                R.id.bottom_income -> {
                    startActivity(Intent(applicationContext, PageIncomeActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }


    }

    private fun setupBalancePieChart() {
        pieChartBalance.setUsePercentValues(true)
        pieChartBalance.description.isEnabled = false
        pieChartBalance.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChartBalance.dragDecelerationFrictionCoef = 0.95f
        pieChartBalance.isDrawHoleEnabled = true
        pieChartBalance.setHoleColor(Color.WHITE)
        pieChartBalance.setTransparentCircleColor(Color.WHITE)
        pieChartBalance.setTransparentCircleAlpha(110)
        pieChartBalance.holeRadius = 58f
        pieChartBalance.transparentCircleRadius = 61f
        pieChartBalance.setDrawCenterText(true)
        pieChartBalance.rotationAngle = 0f
        pieChartBalance.isRotationEnabled = true
        pieChartBalance.isHighlightPerTapEnabled = true
        pieChartBalance.animateY(1400, Easing.EaseInOutQuad)
        pieChartBalance.legend.isEnabled = false
        pieChartBalance.setEntryLabelColor(Color.WHITE)
        pieChartBalance.setEntryLabelTextSize(12f)
    }

    private fun loadBalanceData(totalIncome: Int, totalExpenses: Int) {
        val entries = ArrayList<PieEntry>()

        if (totalIncome > 0) entries.add(PieEntry(totalIncome.toFloat(), "Доходы"))
        if (totalExpenses > 0) entries.add(PieEntry(totalExpenses.toFloat(), "Расходы"))

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
        }

        val dataSet = PieDataSet(entries, "Баланс")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // Цвета для доходов и расходов
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(this, R.color.green))  // Цвет для доходов
        colors.add(ContextCompat.getColor(this, R.color.red))    // Цвет для расходов

        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartBalance))
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChartBalance.data = data
        pieChartBalance.highlightValues(null)
        pieChartBalance.invalidate()
    }


    override fun onResume() {
        super.onResume()
        loadGoal()
    }

    private fun loadGoal() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val lastGoal = dao.getLastGoal()
            val totalExpenses = withContext(Dispatchers.IO) {
                (dao.getFoodExpensesSum() ?: 0) +
                        (dao.getMedicineExpensesSum() ?: 0) +
                        (dao.getRelaxExpensesSum() ?: 0)
            }
            val totalIncome = withContext(Dispatchers.IO) {
                (dao.getGiftIncomeSum() ?: 0) +
                        (dao.getWorkIncomeSum() ?: 0) +
                        (dao.getWinIncomeSum() ?: 0)
            }

            withContext(Dispatchers.Main) {
                // Обновляем данные для круговой диаграммы
                loadBalanceData(totalIncome, totalExpenses)

                if (lastGoal != null) {
                    val currentBalance = lastGoal.initialCapital + totalIncome - totalExpenses
                    val remaining = lastGoal.targetAmount - currentBalance

                    findViewById<TextView>(R.id.goal_name).text = lastGoal.title
                    findViewById<TextView>(R.id.goal_now).text = currentBalance.toString()
                    findViewById<TextView>(R.id.goal_need).text = lastGoal.targetAmount.toString()
                    findViewById<TextView>(R.id.goal_between).text =
                        if (remaining > 0) remaining.toString() else "Цель достигнута!"
                } else {
                    findViewById<TextView>(R.id.goal_name).text = "Нет цели"
                    findViewById<TextView>(R.id.goal_now).text = "0"
                    findViewById<TextView>(R.id.goal_need).text = "0"
                    findViewById<TextView>(R.id.goal_between).text = "0"
                }
            }
        }
    }
}
