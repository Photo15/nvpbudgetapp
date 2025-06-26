package com.example.nvpbudgetapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class       DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var etDescription: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAdd: Button
    private lateinit var btnViewChart: Button
    private lateinit var btnGoals: Button
    private lateinit var btnInvest: Button
    private lateinit var btnLogout: Button
    private lateinit var listView: ListView

    private lateinit var expenseList: MutableList<Expense>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Firebase reference under user ID
        database = FirebaseDatabase.getInstance().getReference("expenses").child(userId)

        // Bind UI components
        etDescription = findViewById(R.id.etDescription)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnAdd = findViewById(R.id.btnAdd)
        btnViewChart = findViewById(R.id.btnViewChart)
        btnGoals = findViewById(R.id.btnGoals)
        btnInvest = findViewById(R.id.btnInvest)
        btnLogout = findViewById(R.id.btnLogout)
        listView = findViewById(R.id.listExpenses)

        expenseList = mutableListOf()

        // Setup category spinner
        val categories = listOf("Food", "Transport", "Rent", "Entertainment", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = spinnerAdapter

        // Add Expense
        btnAdd.setOnClickListener {
            val description = etDescription.text.toString().trim()
            val amount = etAmount.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            if (description.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amountValue = amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = database.push().key ?: return@setOnClickListener
            val timestamp = System.currentTimeMillis()
            val expense = Expense(id, description, amount, timestamp, category)

            database.child(id).setValue(expense)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    etDescription.text.clear()
                    etAmount.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Load expenses
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseList.clear()
                for (expenseSnapshot in snapshot.children) {
                    val expense = expenseSnapshot.getValue(Expense::class.java)
                    expense?.let { expenseList.add(it) }
                }

                val displayList = expenseList.map {
                    "${it.description} - R${it.amount} [${it.category}]\nDate: ${
                        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                    }"
                }

                adapter = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_list_item_1, displayList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Edit/Delete Expense
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedExpense = expenseList[position]

            val dialogBuilder = AlertDialog.Builder(this)
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val inputDesc = EditText(this).apply {
                setText(selectedExpense.description)
                hint = "Description"
            }

            val inputAmount = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(selectedExpense.amount)
                hint = "Amount"
            }

            layout.addView(inputDesc)
            layout.addView(inputAmount)

            dialogBuilder.setTitle("Edit/Delete Expense")
            dialogBuilder.setView(layout)

            dialogBuilder.setPositiveButton("Update") { _, _ ->
                val newDesc = inputDesc.text.toString().trim()
                val newAmount = inputAmount.text.toString().trim()

                val validAmount = newAmount.toDoubleOrNull()
                if (newDesc.isEmpty() || validAmount == null || validAmount <= 0) {
                    Toast.makeText(this, "Invalid fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedExpense = Expense(
                    selectedExpense.id,
                    newDesc,
                    newAmount,
                    System.currentTimeMillis(),
                    selectedExpense.category
                )

                database.child(selectedExpense.id).setValue(updatedExpense)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            dialogBuilder.setNegativeButton("Delete") { _, _ ->
                database.child(selectedExpense.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            dialogBuilder.setNeutralButton("Cancel", null)
            dialogBuilder.show()
        }

        // Navigation
        btnViewChart.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        btnGoals.setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }

        btnInvest.setOnClickListener {
            startActivity(Intent(this, InvestmentActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }
}