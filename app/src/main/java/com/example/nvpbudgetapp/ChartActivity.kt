package com.example.nvpbudgetapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChartActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        btnBack = findViewById(R.id.btnBackToDashboard)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("expenses")

        btnBack.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadExpensesForUser(userId)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExpensesForUser(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categorySums = mutableMapOf<String, Float>()
                var totalAmount = 0f

                for (item in snapshot.children) {
                    val expense = item.getValue(Expense::class.java)
                    if (expense != null) {
                        val category = expense.category ?: "Uncategorized"
                        val amount = expense.amount.toFloatOrNull() ?: continue

                        categorySums[category] = categorySums.getOrDefault(category, 0f) + amount
                        totalAmount += amount
                    }
                }

                if (totalAmount == 0f) {
                    Toast.makeText(this@ChartActivity, "No expenses to show", Toast.LENGTH_SHORT).show()
                    return
                }

                displayPieChart(categorySums, totalAmount)
                displayBarChart(categorySums)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChartActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPieChart(categorySums: Map<String, Float>, totalAmount: Float) {
        val entries = categorySums.map { (category, amount) ->
            PieEntry((amount / totalAmount) * 100f, category)
        }

        val dataSet = PieDataSet(entries, "Expense Categories").apply {
            colors = listOf(
                Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA,
                Color.CYAN, Color.YELLOW, Color.GRAY
            )
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val pieData = PieData(dataSet)
        pieChart.apply {
            data = pieData
            description = Description().apply { text = "Percentage of Expenses" }
            centerText = "Expenses %"
            setUsePercentValues(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun displayBarChart(categorySums: Map<String, Float>) {
        val entries = categorySums.entries.mapIndexed { index, (category, amount) ->
            BarEntry(index.toFloat(), amount)
        }

        val dataSet = BarDataSet(entries, "Total per Category").apply {
            color = Color.parseColor("#FFA726")
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet)
        val xAxisLabels = categorySums.keys.toList()

        barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = -45f
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            description = Description().apply { text = "Total Amount per Category" }
            animateY(1000)
            invalidate()
        }
    }
}