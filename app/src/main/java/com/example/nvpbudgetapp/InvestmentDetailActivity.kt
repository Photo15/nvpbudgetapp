package com.example.nvpbudgetapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class InvestmentDetailActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment_detail)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        val titleView = findViewById<TextView>(R.id.investmentTitle)
        val descriptionView = findViewById<TextView>(R.id.investmentDescription)
        val btnInvest = findViewById<Button>(R.id.btnInvestNow)
        val btnLearn = findViewById<Button>(R.id.btnLearnMore)

        titleView.text = title
        descriptionView.text = description

        btnInvest.setOnClickListener {
            Toast.makeText(this, "Starting investment in $title...", Toast.LENGTH_SHORT).show()
        }

        btnLearn.setOnClickListener {
            Toast.makeText(this, "More info about $title will be available soon.", Toast.LENGTH_SHORT).show()
        }
    }
}
