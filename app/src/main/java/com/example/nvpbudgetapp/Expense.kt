package com.example.nvpbudgetapp

data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: String = "",
    val timestamp: Long = 0L,
    val category: String = ""

)
