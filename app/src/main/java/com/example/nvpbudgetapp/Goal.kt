package com.example.nvpbudgetapp

data class Goal(
    var id: String? = null,
    var title: String = "",
    var amount: Double = 0.0,
    var dueDate: Long = 0L,
    var achieved: Boolean = false
)
