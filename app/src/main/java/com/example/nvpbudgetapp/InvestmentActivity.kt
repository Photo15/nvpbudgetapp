package com.example.nvpbudgetapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class InvestmentActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment)

        val list = findViewById<ListView>(R.id.investmentList)
        val btnBack = findViewById<Button>(R.id.btnBackToDashboard)

        val data = listOf(
            "💼 Unit Trusts: Great for long-term goals.",
            "🏠 Real Estate: Reliable but long-term.",
            "📈 ETFs: Low-cost, diversified investment.",
            "📊 Fixed Deposits: Safe and interest-earning.",
            "🪙 Crypto (risky): For experienced investors only."
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = data[position]
            val title = selectedItem.split(":")[0].trim()
            val description = selectedItem.split(":")[1].trim()

            val intent = Intent(this, InvestmentDetailActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("description", description)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
