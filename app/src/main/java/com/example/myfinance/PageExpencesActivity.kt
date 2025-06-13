package com.example.myfinance
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry
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

        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null && e is PieEntry) {
                    when (e.label) {
                        "Еда" -> openCategoryActivity(CATEGORY.FOOD)
                        "Медицина" -> openCategoryActivity(CATEGORY.MEDICINE)
                        "Развлечения" -> openCategoryActivity(CATEGORY.RELAX)
                        "ЖКХ" -> openCategoryActivity(CATEGORY.JKH)
                        "Транспорт" -> openCategoryActivity(CATEGORY.TRANSPORT)
                        "Аренда" -> openCategoryActivity(CATEGORY.ARENDA)
                        "Мобильная" -> openCategoryActivity(CATEGORY.MOBILE)
                        "Кредит" -> openCategoryActivity(CATEGORY.CREDIT)
                        "Одежда" -> openCategoryActivity(CATEGORY.CLOTHES)
                    }
                }
            }

            override fun onNothingSelected() {
                // Ничего не выбрано
            }
        })
    }

    private fun openCategoryActivity(category: CATEGORY) {
        val intent = Intent(this, FoodCategoryActivityShow::class.java)
        intent.putExtra("name_category", category)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun loadDataToPieChart(foodSum: Int, medicineSum: Int, relaxSum: Int,
                                   jkhSum: Int, transportSum: Int, arendaSum: Int,
                                   mobileSum: Int, creditSum: Int, clothesSum: Int) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // Создаем карту категорий и их значений
        val categories = mapOf(
            "Еда" to foodSum,
            "Медицина" to medicineSum,
            "Развлечения" to relaxSum,
            "ЖКХ" to jkhSum,
            "Транспорт" to transportSum,
            "Аренда" to arendaSum,
            "Мобильная" to mobileSum,
            "Кредит" to creditSum,
            "Одежда" to clothesSum
        )

        // Добавляем только категории с положительными значениями
        categories.forEach { (label, value) ->
            if (value > 0) {
                entries.add(PieEntry(value.toFloat(), label))
            }
        }

        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "Нет данных"))
            colors.add(Color.GRAY)
        } else {
            // Назначаем цвета в фиксированном порядке для каждой категории
            entries.forEach { entry ->
                colors.add(getColorForCategory(entry.label))
            }
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

    // Функция для получения фиксированного цвета для каждой категории
    private fun getColorForCategory(category: String): Int {
        return when (category) {
            "Еда" -> ContextCompat.getColor(this, R.color.salad)
            "Медицина" -> ContextCompat.getColor(this, R.color.yellow)
            "Развлечения" -> ContextCompat.getColor(this, R.color.red)
            "ЖКХ" -> ContextCompat.getColor(this, R.color.blue)
            "Транспорт" -> ContextCompat.getColor(this, R.color.purple)
            "Аренда" -> ContextCompat.getColor(this, R.color.orange)
            "Мобильная" -> ContextCompat.getColor(this, R.color.teal)
            "Кредит" -> ContextCompat.getColor(this, R.color.pink)
            "Одежда" -> ContextCompat.getColor(this, R.color.brown)
            else -> Color.GRAY
        }
    }

    private fun initListener() {
        val buttonFoodContainer: ConstraintLayout = findViewById(R.id.constraintLayout_food)
        val buttonMedicineContainer: ConstraintLayout = findViewById(R.id.constraintLayout_medicine)
        val buttonRelaxContainer: ConstraintLayout = findViewById(R.id.constraintLayout_relax)
        val buttonJKHContainer: ConstraintLayout = findViewById(R.id.constraintLayout_JKH)
        val buttonTRANSPORTContainer: ConstraintLayout = findViewById(R.id.constraintLayout_Transport)
        val buttonARENDAContainer: ConstraintLayout = findViewById(R.id.constraintLayout_Arenda)
        val buttonMOBILEContainer: ConstraintLayout = findViewById(R.id.constraintLayout_mobile)
        val buttonCREDITContainer: ConstraintLayout = findViewById(R.id.constraintLayout_credit)
        val buttonCLOTHESContainer: ConstraintLayout = findViewById(R.id.constraintLayout_clothes)

        buttonFoodContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.FOOD)
        }

        buttonMedicineContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.MEDICINE)
        }

        buttonRelaxContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.RELAX)
        }

        buttonJKHContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.JKH)
        }

        buttonTRANSPORTContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.TRANSPORT)
        }

        buttonARENDAContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.ARENDA)
        }

        buttonMOBILEContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.MOBILE)
        }

        buttonCREDITContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.CREDIT)
        }

        buttonCLOTHESContainer.setOnClickListener {
            openCategoryActivity(CATEGORY.CLOTHES)
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
            val JKHSum = dao.getJKHExpensesSum(userId) ?: 0
            val TRANSPORTSum = dao.getTRANSPORTExpensesSum(userId) ?: 0
            val ARENDASum = dao.getARENDAExpensesSum(userId) ?: 0
            val MOBILESum = dao.getMOBILEExpensesSum(userId) ?: 0
            val CREDITSum = dao.getCREDITExpensesSum(userId) ?: 0
            val CLOTHESSum = dao.getCLOTHESExpensesSum(userId) ?: 0

            val lastGoal = dao.getLastGoal(userId)
            val totalIncome = withContext(Dispatchers.IO) {
                (dao.getGiftIncomeSum(userId) ?: 0) +
                        (dao.getWorkIncomeSum(userId) ?: 0) +
                        (dao.getWinIncomeSum(userId) ?: 0) + (dao.getDepositIncomeSum(userId) ?:0)
            }

            val totalExpenses = foodSum + medicineSum + relaxSum + JKHSum +
                    TRANSPORTSum + ARENDASum + MOBILESum +
                    CREDITSum + CLOTHESSum
            val currentBalance = totalIncome - totalExpenses

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.text_money_now).text = "$currentBalance ₽"

                // Update money values
                findViewById<TextView>(R.id.text_money_food).text = "$foodSum ₽"
                findViewById<TextView>(R.id.text_money_medicine).text = "$medicineSum ₽"
                findViewById<TextView>(R.id.text_money_relax).text = "$relaxSum ₽"
                findViewById<TextView>(R.id.text_money_JKH).text = "$JKHSum ₽"
                findViewById<TextView>(R.id.text_money_Transport).text = "$TRANSPORTSum ₽"
                findViewById<TextView>(R.id.text_money_Arenda).text = "$ARENDASum ₽"
                findViewById<TextView>(R.id.text_money_mobile).text = "$MOBILESum ₽"
                findViewById<TextView>(R.id.text_money_credit).text = "$CREDITSum ₽"
                findViewById<TextView>(R.id.text_money_clothes).text = "$CLOTHESSum ₽"

                // Update current balance
                findViewById<TextView>(R.id.text_money_now).text = "$currentBalance ₽"

                // Calculate and display percentages
                if (totalExpenses > 0) {
                    findViewById<TextView>(R.id.text_percent_food).text =
                        "${(foodSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_medicine).text =
                        "${(medicineSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_relax).text =
                        "${(relaxSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_JKH).text =
                        "${(JKHSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_Transport).text =
                        "${(TRANSPORTSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_Arenda).text =
                        "${(ARENDASum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_mobile).text =
                        "${(MOBILESum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_credit).text =
                        "${(CREDITSum * 100 / totalExpenses)}%"
                    findViewById<TextView>(R.id.text_percent_clothes).text =
                        "${(CLOTHESSum * 100 / totalExpenses)}%"

                    loadDataToPieChart(foodSum, medicineSum, relaxSum, JKHSum,
                        TRANSPORTSum, ARENDASum, MOBILESum,
                        CREDITSum, CLOTHESSum)
                } else {
                    // If no expenses, show empty chart
                    loadDataToPieChart(0, 0, 0, 0, 0, 0, 0, 0, 0)

                    // Reset percentages
                    findViewById<TextView>(R.id.text_percent_food).text = "0%"
                    findViewById<TextView>(R.id.text_percent_medicine).text = "0%"
                    findViewById<TextView>(R.id.text_percent_relax).text = "0%"
                    findViewById<TextView>(R.id.text_percent_JKH).text = "0%"
                    findViewById<TextView>(R.id.text_percent_Transport).text = "0%"
                    findViewById<TextView>(R.id.text_percent_Arenda).text = "0%"
                    findViewById<TextView>(R.id.text_percent_mobile).text = "0%"
                    findViewById<TextView>(R.id.text_percent_credit).text = "0%"
                    findViewById<TextView>(R.id.text_percent_clothes).text = "0%"
                }
            }
        }
    }
}