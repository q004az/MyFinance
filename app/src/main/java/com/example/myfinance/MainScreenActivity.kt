package com.example.myfinance

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
    private var userId: Int = -1  // ID текущего пользователя
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

        userId = intent.getIntExtra("user_id", -1)
        if(userId == -1) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pieChartBalance = findViewById(R.id.pieChartBalance)
        setupBalancePieChart()

        loadUserData()

        val buttonGoal: Button = findViewById(R.id.button_make_goal)
        buttonGoal.setOnClickListener {
            val intent = Intent(this, makeGoalActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        val buttonEndGoal: Button = findViewById(R.id.button_end_goal)
        buttonEndGoal.setOnClickListener {
            endGoalAndClearData()
        }

        // Кнопка перехода в раздел вкладов
        findViewById<Button>(R.id.button_vklad).setOnClickListener {
            val intent = Intent(this, DepositActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        // Загружаем цель и данные о расходах/доходах
        loadGoalAndFinanceData()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.bottom_profile

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

        val buttonExit : Button = findViewById(R.id.button_exit_profile)
        buttonExit.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()
        val nameTextView: TextView = findViewById(R.id.text_name)

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                dao.getUserById(userId)
            }

            withContext(Dispatchers.Main) {
                user?.let {
                    nameTextView.text = it.login
                } ?: run {
                    nameTextView.text = "Гость"
                }
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
        val colors = ArrayList<Int>()

        // Добавляем доходы первыми (они будут зелеными)
        if (totalIncome > 0) {
            entries.add(PieEntry(totalIncome.toFloat(), "Доходы"))
            colors.add(ContextCompat.getColor(this, R.color.green))
        }

        // Добавляем расходы вторыми (они будут красными)
        if (totalExpenses > 0) {
            entries.add(PieEntry(totalExpenses.toFloat(), "Расходы"))
            colors.add(ContextCompat.getColor(this, R.color.red))
        }

        // Если нет данных, показываем заглушку
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
            colors.add(ContextCompat.getColor(this, R.color.gray))
        }

        val dataSet = PieDataSet(entries, "Баланс").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = MPPointF(0f, 40f)
            selectionShift = 5f
            this.colors = colors
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChartBalance))
            setValueTextSize(15f)
            setValueTypeface(Typeface.DEFAULT_BOLD)
            setValueTextColor(Color.WHITE)
        }

        pieChartBalance.apply {
            this.data = data
            highlightValues(null)
            invalidate()
        }
    }


    override fun onResume() {
        super.onResume()
        loadGoalAndFinanceData()
    }

    private fun loadGoalAndFinanceData() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            val lastGoal = dao.getLastGoal(userId)
            val totalExpenses = withContext(Dispatchers.IO) {
                (dao.getFoodExpensesSum(userId) ?: 0) +
                        (dao.getMedicineExpensesSum(userId) ?: 0) +
                        (dao.getRelaxExpensesSum(userId) ?: 0) +
                        (dao.getJKHExpensesSum(userId) ?: 0) +
                        (dao.getTRANSPORTExpensesSum(userId) ?: 0) +
                        (dao.getARENDAExpensesSum(userId) ?: 0) +
                        (dao.getMOBILEExpensesSum(userId) ?: 0) +
                        (dao.getCREDITExpensesSum(userId) ?: 0) +
                        (dao.getCLOTHESExpensesSum(userId) ?: 0)
            }

            val totalIncome = withContext(Dispatchers.IO) {
                (dao.getGiftIncomeSum(userId) ?: 0) +
                        (dao.getWorkIncomeSum(userId) ?: 0) +
                        (dao.getWinIncomeSum(userId) ?: 0) + (dao.getDepositIncomeSum(userId) ?: 0)
            }

            val currentBalance = totalIncome - totalExpenses

            withContext(Dispatchers.Main) {
                // Обновляем основные поля всегда
                findViewById<TextView>(R.id.exp_now).text = totalExpenses.toString()
                findViewById<TextView>(R.id.income_now).text = totalIncome.toString()

                // Обновляем текущий баланс в разделе "Ваша цель"
                findViewById<TextView>(R.id.goal_now).text = currentBalance.toString()

                // Обновляем круговую диаграмму
                loadBalanceData(totalIncome, totalExpenses)

                // Обработка цели (если есть)
                val goalAchieveTextView = findViewById<TextView>(R.id.goal_achive)
                val goalBetweenTextView = findViewById<TextView>(R.id.goal_between)

                if (lastGoal != null) {
                    findViewById<TextView>(R.id.goal_name).text = lastGoal.title
                    findViewById<TextView>(R.id.goal_need).text = lastGoal.targetAmount.toString()

                    val remaining = maxOf(lastGoal.targetAmount - currentBalance, 0)
                    goalBetweenTextView.text = remaining.toString()

                    if (currentBalance >= lastGoal.targetAmount) {
                        goalAchieveTextView.text = "Да"
                        goalAchieveTextView.setTextColor(Color.GREEN)
                    } else {
                        goalAchieveTextView.text = "Нет"
                        goalAchieveTextView.setTextColor(Color.RED)
                    }
                } else {
                    findViewById<TextView>(R.id.goal_name).text = "Нет цели"
                    findViewById<TextView>(R.id.goal_need).text = "0"
                    goalBetweenTextView.text = "0"
                    goalAchieveTextView.text = "Нет"
                    goalAchieveTextView.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun endGoalAndClearData() {
        val db = AppDatabase.getInstance(this)
        val dao = db.userDao()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val lastGoal = dao.getLastGoal(userId)
                if (lastGoal != null) {
                    dao.deleteAllGoals(userId)
                    dao.deleteAllExpenses(userId)
                    dao.deleteAllIncomes(userId)
                }
            }

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.goal_name).text = "Нет цели"
                findViewById<TextView>(R.id.goal_now).text = "0"
                findViewById<TextView>(R.id.goal_need).text = "0"
                findViewById<TextView>(R.id.goal_between).text = "0"
                findViewById<TextView>(R.id.exp_now).text = "0"
                findViewById<TextView>(R.id.income_now).text = "0"

                loadBalanceData(0, 0)

                Toast.makeText(this@MainScreenActivity, "Цель завершена, данные очищены", Toast.LENGTH_SHORT).show()
            }
        }
    }


}